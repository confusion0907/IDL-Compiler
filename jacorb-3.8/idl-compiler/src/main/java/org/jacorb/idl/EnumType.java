/*
*        JacORB - a free Java ORB
*
*   Copyright (C) 1997-2014 Gerald Brose / The JacORB Team.
*
*   This library is free software; you can redistribute it and/or
*   modify it under the terms of the GNU Library General Public
*   License as published by the Free Software Foundation; either
*   version 2 of the License, or (at your option) any later version.
*
*   This library is distributed in the hope that it will be useful,
*   but WITHOUT ANY WARRANTY; without even the implied warranty of
*   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
*   Library General Public License for more details.
*
*   You should have received a copy of the GNU Library General Public
*   License along with this library; if not, write to the Free
*   Software Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

package org.jacorb.idl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * @author Gerald Brose
 */

public class EnumType
    extends TypeDeclaration
    implements SwitchTypeSpec
{
    public SymbolList enumlist;
    int const_counter = 0;
    private boolean parsed = false;

    public EnumType(int num)
    {
        super(num);
        pack_name = "";
    }

    public Object clone()
    {
        EnumType et = new EnumType(new_num());
        et.enumlist = this.enumlist;
        et.typeName = this.typeName;
        et.pack_name = this.pack_name;
        et.name = this.name;
        et.token = this.token;
        et.included = this.included;
        et.enclosing_symbol = this.enclosing_symbol;
        et.parsed = this.parsed;
        return et;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public int size()
    {
        return enumlist.v.size();
    }

    /**
     */

    public void set_included(boolean i)
    {
        included = i;
    }

    public String typeName()
    {
        if (typeName == null)
        {
            setPrintPhaseNames();
        }
        return typeName;
    }

    public boolean basic()
    {
        return true;
    }

    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        enclosing_symbol = s;
    }

    @SuppressWarnings("rawtypes")
	public void parse()
    {
        if (parsed)
        {
            return ;
        }
        parsed = true;

        escapeName();

        try
        {
            ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
            ctspec.c_type_spec = this;
            NameTable.define(full_name(), IDLTypes.TYPE);
            TypeMap.typedef(full_name(), ctspec);
            String enum_ident = null;

            // we have to get the scoping right: enums do not
            // define scopes, but their element identifiers are scoped.
            // for the Java mapping, we need to get the enum type name
            // back as it defines the class name where the constants
            // are defined. Therefore, an additional mapping in
            // ScopedName is required.

            String prefix = (pack_name.length() > 0 ?
                              full_name().substring(0, full_name().lastIndexOf('.') + 1) :
                              "");

            for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
            {
                enum_ident = (String) e.nextElement();
                try
                {
                    NameTable.define(prefix + enum_ident, IDLTypes.ENUM_LABEL);
                    ScopedName.enumMap(prefix + enum_ident, full_name() +
                                        "." + enum_ident);
                }
                catch (NameAlreadyDefined p)
                {
                    parser.error("Identifier " + enum_ident +
                                  " already defined in immediate scope", token);
                }
            }
        }
        catch (NameAlreadyDefined p)
        {
            parser.error("Enum " + full_name() + " already defined", token);
        }
    }

    public String className()
    {
        String fullName = typeName();
        if (fullName.indexOf('.') > 0)
        {
            return fullName.substring(fullName.lastIndexOf('.') + 1);
        }

        return fullName;
    }

    public String printReadExpression(String streamname)
    {
        return toString() + "Helper.read(" + streamname + ")";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        return toString() + "Helper.write(" + streamname + "," + var_name + ");";
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }

    public String helperName()
    {
        return typeName() + "Helper";
    }


    @SuppressWarnings("rawtypes")
	public String getTypeCodeExpression()
    {
        return getTypeCodeExpression(new HashSet());
    }

    @SuppressWarnings("rawtypes")
	public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }

        StringBuffer sb = new StringBuffer();
        sb.append("org.omg.CORBA.ORB.init().create_enum_tc(" +
        		typeName() + "Helper.id(),\"" + className() + "\",");

        sb.append("new String[]{");

        for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
        {
        	sb.append("\"" + (String) e.nextElement() + "\"");
        	if (e.hasMoreElements())
        		sb.append(",");
        }
        sb.append("})");

        return sb.toString();
    }

    @SuppressWarnings("rawtypes")
	public void print(PrintWriter ps,Vector<String> template)
    {
    	//FIXME
        setPrintPhaseNames();

        // no code generation for included definitions
        if (included && !generateIncluded())
        {
            return ;
        }

        // only write once
        boolean judge = false;
    	String className = className();
    	
    	int i = 0;
    	while(i < template.size())
    	{
    		if(template.get(i).startsWith("%newfile"))
        	{
        		judge = true;
        		String tmp = template.get(i).replaceAll("<enumName>", name);
        		PrintWriter _ps;
        		
        		try{
					_ps = openOutput(tmp.substring(9));
					if(_ps == null)
						throw new Exception();
				}catch(Exception e){
					throw new RuntimeException ("文件"+tmp+"已存在,代码生成失败");
				}
        		
        		if(ps != null)
        		{
        			ps.close();
        			ps = _ps;
        		}
        		else
        			ps = _ps;
        		
        		i = i+1;
        	}
        	else if(ps == null)
				throw new RuntimeException ("模板代码有误,文件已被关闭");
        	else if(template.get(i).startsWith("%label"))
    		{
    			i = i+1;
    			Vector<String> _template = new Vector<String>();
    			while(!template.get(i).equals("%%"))
    			{
    				_template.add(template.get(i).replaceAll("<enumName>", className));
    				i = i+1;
    			}
    			int basicNum = 0;
	            String IncNumSymbol = null;
	            for(int j = 0 ; j < _template.size() ; j++)
	            {
	            	String tmp = _template.get(j);
	            	if(tmp.contains("IncNum("))
	            	{
	            		int begin=-1,end=-1;
	            		for(int k = 0 ; k < _template.get(j).length() ; k++)
	            		{
	            			if(tmp.charAt(k)=='I' && tmp.charAt(k+1)=='n' && tmp.charAt(k+2)=='c' && tmp.charAt(k+3)=='N' && tmp.charAt(k+4)=='u' && tmp.charAt(k+5)=='m' && tmp.charAt(k+6)=='(')
	                		{
	                			begin = k;
	                			break;
	                		}
	            		}
	            		String IncNum = tmp.substring(begin);
	            		end = IncNum.indexOf(')');
	            		IncNum = IncNum.substring(0, end+1);
	            		basicNum = Integer.parseInt(IncNum.substring(7,IncNum.length()-1));
	            		IncNumSymbol = IncNum;
	            		break;
	            	}
	            }
	            int index = 0;
    			for (Enumeration e = enumlist.v.elements(); e.hasMoreElements();)
    	        {
    	            String label = (String) e.nextElement();
    	            for(int j = 0 ; j < _template.size() ; j++)
    	            {
    	            	String tmp = _template.get(j).replaceAll("<enumLabel>", label);
    	            	tmp = tmp.replace(IncNumSymbol, Integer.toString(basicNum+index));
    	            	ps.println(tmp);
    	            }
    	            index = index+1;
    	        }
    			i = i+1;
    		}
    		else
    		{
    			ps.println(template.get(i).replaceAll("<enumName>", className));
    			i = i+1;
    		}
    	}
    	
    	if(ps != null && judge)
        	ps.close();
    }
    
    protected PrintWriter openOutput(String typeName)
    {
        try
        {
            final File f = new File(parser.out_dir+"\\"+typeName);
            if (GlobalInputStream.isMoreRecentThan(f))
            {
                PrintWriter ps = new PrintWriter(new java.io.FileWriter(f));
                return ps;
            }

            // no need to open file for printing, existing file is more
            // recent than IDL file.

            return null;
        }
        catch (IOException e)
        {
            throw new RuntimeException ("Could not open output file for "
                                        + typeName + " (" + e + ")");
        }
    }

    public String toString()
    {
        return typeName();
    }

    public boolean isSwitchable()
    {
        return true;
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        ps.println("\t\t" + pack_name + "." + className() + "Helper.insert(" + anyname + ", " + varname + ");");
    }


    public void printExtractResult(PrintWriter ps,
                                    String resultname,
                                    String anyname,
                                    String resulttype)
    {
        ps.println("\t\t" + resultname + " = " + pack_name + "." + className() + "Helper.extract(" + anyname + ");");
    }


    /**
     */

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitEnum(this);
    }
}
