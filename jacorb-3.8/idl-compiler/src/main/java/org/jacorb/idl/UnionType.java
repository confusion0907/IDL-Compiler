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
import java.util.Enumeration;
import java.util.Set;
import java.util.Vector;

/**
 * @author Gerald Brose
 */
public class UnionType
    extends TypeDeclaration
    implements Scope
{
    /** the union's discriminator's type spec */
    TypeSpec switch_type_spec;

    SwitchBody switch_body;
    private boolean written = false;

    private ScopeData scopeData;

    private boolean isParsed = false;
    public UnionType(int num)
    {
        super(num);
        pack_name = "";
    }

    public Object clone()
    {
        UnionType ut = new UnionType(new_num());
        ut.switch_type_spec = this.switch_type_spec;
        ut.switch_body = switch_body;
        ut.pack_name = this.pack_name;
        ut.name = this.name;
        ut.written = this.written;
        ut.scopeData = this.scopeData;
        ut.enclosing_symbol = this.enclosing_symbol;
        ut.token = this.token;
        return ut;
    }

    public void setScopeData(ScopeData data)
    {
        scopeData = data;
    }

    public ScopeData getScopeData()
    {
        return scopeData;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        enclosing_symbol = s;
        if (switch_body != null)
        {
            switch_body.setEnclosingSymbol(s);
        }
    }

    public String typeName()
    {
        if (typeName == null)
            setPrintPhaseNames();
        return typeName;
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

    public String printReadExpression(String Streamname)
    {
        return typeName() + "Helper.read(" + Streamname + ")";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        return typeName() + "Helper.write(" + streamname + "," + var_name + ");";
    }

    public String holderName()
    {
        return typeName() + "Holder";
    }

    public String helperName()
    {
        return typeName() + "Helper";
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public void setSwitchType(TypeSpec s)
    {
        switch_type_spec = s;
    }

    public void setSwitchBody(SwitchBody sb)
    {
        switch_body = sb;
    }

    public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
        {
            pack_name = s + "." + pack_name;
        }
        else
        {
            pack_name = s;
        }
        if (switch_type_spec != null)
        {
            switch_type_spec.setPackage (s);
        }
        if (switch_body != null)
        {
            switch_body.setPackage(s);
        }
    }

    public boolean basic()
    {
        return false;
    }

    @SuppressWarnings("unchecked")
	public void parse()
    {
        if (isParsed)
        {
            // there are occasions where the compiler may try to parse
            // a union type spec for a second time, viz if the union is
            // referred to through a scoped name in another struct member.
            // that's not a problem, but we have to skip parsing again!

            return;
        }

        isParsed = true;

        boolean justAnotherOne = false;

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
        try
        {
            ScopedName.definePseudoScope(full_name());
            ctspec.c_type_spec = this;
            NameTable.define(full_name(), IDLTypes.TYPE_UNION);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            Object forwardDeclaration = parser.get_pending (full_name ());
            if (forwardDeclaration != null)
            {
                if (! (forwardDeclaration instanceof UnionType))
                {
                    parser.error("Forward declaration types mismatch for "
                            + full_name()
                            + ": name already defined with another type" , token);
                }

                if (switch_type_spec == null)
                {
                    justAnotherOne = true;
                }
                // else actual definition

                if (!full_name().equals("org.omg.CORBA.TypeCode") && switch_type_spec != null)
                {
                    TypeMap.replaceForwardDeclaration(full_name(), ctspec);
                }
            }
            else
            {
                parser.error("Union " + full_name() + " already defined", token);
            }
        }

        if (switch_type_spec != null)
        {
            // Resolve scoped names and aliases
            TypeSpec ts;
            if (switch_type_spec.type_spec instanceof ScopedName)
            {
                ts = ((ScopedName)switch_type_spec.type_spec).resolvedTypeSpec();

                while(ts instanceof ScopedName || ts instanceof AliasTypeSpec)
                {
                    if (ts instanceof ScopedName)
                    {
                        ts = ((ScopedName)ts).resolvedTypeSpec();
                    }
                    else
                    {
                        ts = ((AliasTypeSpec)ts).originalType();
                    }
                }
                addImportedName(switch_type_spec.typeName());
            }
            else
            {
                ts = switch_type_spec.type_spec;
            }

            // Check if we have a valid discriminator type

            if
                (!(
                   ((ts instanceof SwitchTypeSpec) &&
                    (((SwitchTypeSpec)ts).isSwitchable()))
                   ||
                   ((ts instanceof BaseType) &&
                    (((BaseType)ts).isSwitchType()))
                   ||
                   ((ts instanceof ConstrTypeSpec) &&
                    (((ConstrTypeSpec)ts).c_type_spec instanceof EnumType))
                   ))
            {
                parser.error("Illegal Switch Type: " + ts.typeName(), token);
            }

            switch_type_spec.parse();
            switch_body.setTypeSpec(switch_type_spec);
            switch_body.setUnion(this);

            ScopedName.addRecursionScope(typeName());
            switch_body.parse();
            ScopedName.removeRecursionScope(typeName());

            NameTable.parsed_interfaces.put(full_name(), "");
            parser.remove_pending(full_name());
        }
        else if (!justAnotherOne)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
        }
    }

    /**
     * @return a string for an expression of type TypeCode that
     * describes this type
     */
    public String getTypeCodeExpression()
    {
        return typeName() + "Helper.type()";
    }

    @SuppressWarnings("rawtypes")
	public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }
        return this.getTypeCodeExpression();
    }

    public void printHolderClass(String className, PrintWriter ps)
    {
        if (!pack_name.equals(""))
        {
            ps.println("package " + pack_name + ";");
        }

        printClassComment("union", className, ps);

        ps.println("public" + parser.getFinalString() + " class " + className + "Holder");
        ps.println("\timplements org.omg.CORBA.portable.Streamable");
        ps.println("{");

        ps.println("\tpublic " + className + " value;" + Environment.NL);

        ps.println("\tpublic " + className + "Holder ()");
        ps.println("\t{");
        ps.println("\t}");

        ps.println("\tpublic " + className + "Holder (final " + className + " initial)");
        ps.println("\t{");
        ps.println("\t\tvalue = initial;");
        ps.println("\t}");

        ps.println("\tpublic org.omg.CORBA.TypeCode _type ()");
        ps.println("\t{");
        ps.println("\t\treturn " + className + "Helper.type ();");
        ps.println("\t}");

        ps.println("\tpublic void _read (final org.omg.CORBA.portable.InputStream in)");
        ps.println("\t{");
        ps.println("\t\tvalue = " + className + "Helper.read (in);");
        ps.println("\t}");

        ps.println("\tpublic void _write (final org.omg.CORBA.portable.OutputStream out)");
        ps.println("\t{");
        ps.println("\t\t" + className + "Helper.write (out, value);");
        ps.println("\t}");

        ps.println("}");
    }

    /** generate required classes */

    @SuppressWarnings("rawtypes")
	public void print(PrintWriter _ps,Vector<String> template)
    {
    	//FIXME
        setPrintPhaseNames();

        boolean judge = false;
        // no code generation for included definitions
        if (included && !generateIncluded())
            return;

        // only write once
        if (!written)
        {
            // Forward declaration
            if (switch_type_spec != null)
            {
                // JAC570: when the enum declaration is used in union declaration
				// the following code will generate implementation classes for enum
				//if (switch_type_spec.type_spec instanceof ConstrTypeSpec)
				//{
				    //switch_type_spec.print(_ps);
				//}

				//switch_body.print(_ps);

				String className = className();
				
				int i = 0;
				TypeSpec ts = switch_type_spec.typeSpec();
				while(i < template.size())
				{
					if(template.get(i).startsWith("%newfile"))
	            	{
	            		judge = true;
	            		String tmp = template.get(i).replaceAll("<unionName>", className);
	            		tmp = tmp.replaceAll("<switchType>", ts.toString());
	            		PrintWriter ps = openOutput(tmp.substring(9));
	            		
	            		if(ps == null)
	            		{
	            			System.out.println("文件"+tmp.substring(9)+"已存在，代码生成失败");
	            			return;
	            		}
	            		else if(_ps != null)
	            		{
	            			_ps.close();
	            			_ps = ps;
	            		}
	            		else
	            			_ps = ps;
	            		
	            		i = i+1;
	            	}
					else if(_ps == null)
						throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
					else if(template.get(i).startsWith("%case"))
					{
						i = i+1;
						Enumeration e = switch_body.caseListVector.elements();
						String type = "",name = "",value = "";
						while (e.hasMoreElements())
				        {
				            Case c = (Case)e.nextElement();
				            for (int k = 0; k < c.case_label_list.v.size(); k++)
				            {
				                Object ce = c.case_label_list.v.elementAt(k);
				                if (ce != null)
				                {
				                    name = c.element_spec.declarator.name();
				                    type = c.element_spec.typeSpec.toString();
				                    value = ((ConstExpr)ce).toString();
				                    int position = i;
				                    while(!template.get(i).equals("%%"))
				                    {
				                    	String tmp = template.get(i).replaceAll("<unionName>", className);
										tmp = tmp.replaceAll("<switchType>", ts.toString());
										tmp = tmp.replaceAll("<memberName>", name);
										tmp = tmp.replaceAll("<memberType>", type);
										tmp = tmp.replaceAll("<caseValue>", value);
										_ps.println(tmp);
										i = i+1;
				                    }
				                    i = position;
				                }
				            }
				        }
						while(!template.get(i).equals("%%"))
							i = i+1;
						i = i+1;
					}
					else if(template.get(i).startsWith("%default"))
					{
						i = i+1;
						int def = 0;
						Enumeration e = switch_body.caseListVector.elements();
						String type = "",name = "";
						while (e.hasMoreElements())
				        {
				            Case c = (Case)e.nextElement();
				            for (int k = 0; k < c.case_label_list.v.size(); k++)
				            {
				                Object ce = c.case_label_list.v.elementAt(k);
				                if (ce == null)
				                {
				                    def = 1;
				                    name = c.element_spec.declarator.name();
				                    type = c.element_spec.typeSpec.toString();
				                }
				            }
				        }
						while(!template.get(i).equals("%%"))
						{
							if(def == 1)
							{
								String tmp = template.get(i).replaceAll("<unionName>", className);
								tmp = tmp.replaceAll("<switchType>", ts.toString());
								tmp = tmp.replaceAll("<memberName>", name);
								tmp = tmp.replaceAll("<memberType>", type);
								_ps.println(tmp);
							}
							i = i+1;
						}
						i = i+1;
					}
					else
					{
						String tmp = template.get(i).replaceAll("<unionName>", className);
						tmp = tmp.replaceAll("<switchType>", ts.toString());
						_ps.println(tmp);
						i = i+1;
					}
				}
				
				if(_ps != null && judge)
		        	_ps.close();
				
				written = true;
            }
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
        ps.println("\t\t" + pack_name + "." + className() + "Helper.insert(" + anyname + ", " + varname + ");");
    }

    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        ps.println("\t\t" + resultname + " = " + className() + "Helper.extract(" + anyname + ");");
    }

    public String toString()
    {
        return typeName();
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitUnion(this);
    }

    public void set_name(String n)
    {
        super.set_name(n);

        boolean setpkg = (switch_type_spec != null && !(switch_type_spec.typeSpec() instanceof ScopedName));

        // Don't override the package if this is a scopedname.
        if (setpkg)
        {
            switch_type_spec.setPackage( n );
        }

        // As per above.
        if (switch_body != null && setpkg)
        {
            switch_body.setPackage( n );
        }
    }
}
