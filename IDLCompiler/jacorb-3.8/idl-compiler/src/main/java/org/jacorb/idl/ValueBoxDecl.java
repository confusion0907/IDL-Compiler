package org.jacorb.idl;

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


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author Gerald Brose
 */

public class ValueBoxDecl
    extends Value
{
    private boolean written = false;
    private boolean parsed = false;

    TypeSpec typeSpec;

    public ValueBoxDecl(int num)
    {
        super(num);
        pack_name = "";
    }

    public Object clone()
    {
        return null;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public String typeName()
    {
        if (typeName == null)
        {
            setPrintPhaseNames();
        }

        if (typeSpec.typeSpec() instanceof BaseType)
        {
            return typeName;
        }
        else
        {
            return unwindTypedefs(typeSpec).typeName();
        }
    }

    public String boxTypeName()
    {
        if (typeName == null)
            setPrintPhaseNames();
        return typeName;
    }

    public boolean basic()
    {
        return false;
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        typeSpec.setPackage(s);
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
        {
            parser.logger.log(Level.SEVERE, "was " + enclosing_symbol.getClass().getName() +
            " now: " + s.getClass().getName());
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        }
        enclosing_symbol = s;
    }

    public String toString()
    {
        return typeName();
    }

    public void parse()
    {
        if (parsed)
            throw new RuntimeException("Compiler error: Value box already parsed!");

        escapeName();

        typeSpec.parse();

        try
        {
            ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
            ctspec.c_type_spec = this;

            NameTable.define(full_name(), IDLTypes.TYPE);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            parser.error("Value box " + typeName() + " already defined", token);
        }

        parsed = true;
    }

    public String className()
    {
        String fullName = typeName();
        if (fullName.indexOf('.') > 0)
        {
            return fullName.substring(fullName.lastIndexOf('.') + 1);
        }
        else
        {
            return fullName;
        }
    }

    public String printReadExpression(String streamname)
    {
        return "(" + typeName() + ")((org.omg.CORBA_2_3.portable.InputStream)" + streamname + ").read_value (new " + helperName() + "())";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ").write_value (" + var_name + ", new " + helperName() + "());";
    }

    public String holderName()
    {
        return boxTypeName() + "Holder";
    }

    public String helperName()
    {
        return boxTypeName() + "Helper";
    }

    /**
     * @return a string for an expression of type TypeCode that
     * describes this type
     *
     */

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }
        else
        {
            knownTypes.add(this);
            StringBuffer sb = new StringBuffer();
            String className = boxTypeName();
            if (className.indexOf('.') > 0)
                className = className.substring(className.lastIndexOf('.') + 1);
            sb.append("org.omg.CORBA.ORB.init().create_value_box_tc(" +
                       helperName() + ".id(),\"" + className + "\"," +
                       typeSpec.typeSpec().getTypeCodeExpression(knownTypes) + ")");

            return sb.toString();
        }
    }

    @SuppressWarnings("rawtypes")
	public String getTypeCodeExpression()
    {
        return this.getTypeCodeExpression(new HashSet());
    }

    /** generate required classes */

    public void print(PrintWriter ps,Vector<String> template)
    {
    	//FIXME
    	if(!template.get(0).equals("box"))
    		return;
    	
        setPrintPhaseNames();

        // no code generation for included definitions
        if (included && !generateIncluded())
            return;

        // only write once

        if (!written)
        {
        	String className = boxTypeName();
        	int i = 1;
            boolean judge = false;
            while(i < template.size())
            {
            	if(template.get(i).startsWith("%newfile"))
            	{
            		judge = true;
            		String tmp = template.get(i).replaceAll("<valuetypeName>", className);
            		tmp = tmp.replaceAll("<valuetypeType>", typeSpec.toString());
            		PrintWriter _ps = openOutput(tmp.substring(9));
            		
            		if(_ps == null)
            		{
            			System.out.println("文件"+tmp.substring(9)+"已存在，代码生成失败");
            			return;
            		}
            		else if(ps != null)
            		{
            			ps.close();
            			ps = _ps;
            		}
            		else
            			ps = _ps;
            		
            		i = i+1;
            	}
            	else if(ps == null)
					throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
            	else
            	{
            		String tmp = template.get(i).replaceAll("<valuetypeName>", className);
            		tmp = tmp.replaceAll("<valuetypeType>", typeSpec.toString());
            		ps.println(tmp);
            		i = i+1;
            	}
            }
        	written = true;
        	
        	if(ps != null && judge)
            	ps.close();
        }
    }
    
    protected PrintWriter openOutput(String typeName)
    {
        try
        {
            final File f = new File(typeName);
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

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        ps.println("\t\t" + anyname + ".type(" + getTypeCodeExpression() +");");
        ps.println("\t\t" + helperName() + ".write(" + anyname + ".create_output_stream()," + varname + ");");



    }


    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        ps.println("\t\t" + resultname + " = (" + resulttype + ")" + anyname + ".extract_Value();");
    }


    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitValue(this);
    }

    private TypeSpec unwindTypedefs(TypeSpec typedef)
    {
        TypeSpec spec = typedef.typeSpec();
        TypeSpec typeSpec2 = spec.typeSpec();

        if (typeSpec2 instanceof ScopedName)
        {
            ScopedName scopedName = (ScopedName) typeSpec2;

            TypeSpec resolvedTSpec = scopedName.resolvedTypeSpec();
            //unwind any typedefs
            while (resolvedTSpec instanceof AliasTypeSpec )
            {
                resolvedTSpec =
                    ((AliasTypeSpec)resolvedTSpec).originalType();
            }

            return resolvedTSpec;
        }

        return typedef;
    }
}
