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
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author Gerald Brose
 */

@SuppressWarnings({ "unchecked", "rawtypes" })
public class StructType
    extends TypeDeclaration
    implements Scope
{
    private boolean written = false;
    public boolean exc;
    public MemberList memberlist = null;
    private boolean parsed = false;
    private ScopeData scopeData;
	private static final HashSet<String> systemExceptionNames;

    static
    {
        systemExceptionNames = new HashSet();

        systemExceptionNames.add( "UNKNOWN" ) ;
        systemExceptionNames.add( "BAD_PARAM" ) ;
        systemExceptionNames.add( "NO_MEMORY" ) ;
        systemExceptionNames.add( "IMP_LIMIT" ) ;
        systemExceptionNames.add( "COMM_FAILURE" ) ;
        systemExceptionNames.add( "INV_OBJREF" ) ;
        systemExceptionNames.add( "NO_PERMISSION" ) ;
        systemExceptionNames.add( "INTERNAL" ) ;
        systemExceptionNames.add( "MARSHAL" ) ;
        systemExceptionNames.add( "INITIALIZE" ) ;
        systemExceptionNames.add( "NO_IMPLEMENT" ) ;
        systemExceptionNames.add( "BAD_TYPECODE" ) ;
        systemExceptionNames.add( "BAD_OPERATION" ) ;
        systemExceptionNames.add( "NO_RESOURCES" ) ;
        systemExceptionNames.add( "NO_RESPONSE" ) ;
        systemExceptionNames.add( "PERSIST_STORE" ) ;
        systemExceptionNames.add( "BAD_INV_ORDER" ) ;
        systemExceptionNames.add( "TRANSIENT" ) ;
        systemExceptionNames.add( "FREE_MEM" ) ;
        systemExceptionNames.add( "INV_IDENT" ) ;
        systemExceptionNames.add( "INV_FLAG" ) ;
        systemExceptionNames.add( "INTF_REPOS" ) ;
        systemExceptionNames.add( "BAD_CONTEXT" ) ;
        systemExceptionNames.add( "OBJ_ADAPTER" ) ;
        systemExceptionNames.add( "DATA_CONVERSION" ) ;
        systemExceptionNames.add( "OBJECT_NOT_EXIST" ) ;
        systemExceptionNames.add( "TRANSACTION_REQUIRED" ) ;
        systemExceptionNames.add( "TRANSACTION_ROLLEDBACK" ) ;
        systemExceptionNames.add( "INVALID_TRANSACTION" ) ;
        systemExceptionNames.add( "INV_POLICY" ) ;
        systemExceptionNames.add( "CODESET_INCOMPATIBLE" ) ;
        systemExceptionNames.add( "REBIND" ) ;
        systemExceptionNames.add( "TIMEOUT" ) ;
        systemExceptionNames.add( "TRANSACTION_UNAVAILABLE" ) ;
        systemExceptionNames.add( "TRANSACTION_MODE" ) ;
        systemExceptionNames.add( "BAD_QOS" ) ;
        systemExceptionNames.add( "INVALID_ACTIVITY" ) ;
        systemExceptionNames.add( "ACTIVITY_COMPLETED" ) ;
        systemExceptionNames.add( "ACTIVITY_REQUIRED" ) ;
    }

    public StructType(int num)
    {
        super(num);
        pack_name = "";
    }


    public void setScopeData(ScopeData data)
    {
        scopeData = data;
    }

    public ScopeData getScopeData()
    {
        return scopeData;
    }

    /**
     * @return true if this struct represents an IDL exception
     */

    public boolean isException()
    {
        return exc;
    }

    public Object clone()
    {
        StructType st = new StructType(new_num());
        st.pack_name = this.pack_name;
        st.name = this.name;
        st.memberlist = this.memberlist;
        st.included = this.included;
        st.token = this.token;
        st.exc = this.exc;
        st.scopeData = this.scopeData;
        st.enclosing_symbol = this.enclosing_symbol;
        return st;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public String typeName()
    {
        if (typeName == null)
            setPrintPhaseNames();
        return typeName;
    }

    public int getTCKind()
    {
    	return org.omg.CORBA.TCKind._tk_struct;
    }

    public boolean basic()
    {
        return false;
    }

    public void set_memberlist(MemberList m)
    {
        m.setContainingType(this);
        memberlist = m;
        memberlist.setPackage(name);
        if (memberlist != null)
            memberlist.setEnclosingSymbol(this);
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

        if (memberlist != null)
            memberlist.setPackage(s);
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
        if (memberlist != null)
            memberlist.setEnclosingSymbol(this);
    }

    public String toString()
    {
        return typeName();
    }

    public void parse()
    {
        boolean justAnotherOne = false;

        if (parsed)
        {
            // there are occasions where the compiler may try to parse
            // a struct type spec for a second time, viz if the struct is
            // referred to through a scoped name in another struct member.
            // that's not a problem, but we have to skip parsing again!
            // (Gerald: introduced together with the fix for bug #84).
            return;
        }

        if (parser.logger.isLoggable(Level.ALL))
            parser.logger.log(Level.ALL, "Parsing Struct " + name);

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
        try
        {
            // important: typeName must be set _before_ a new scope is introduced,
            // otherwise the typeName for this struct class will be the same
            // as the package name for the new pseudo scope!

            ScopedName.definePseudoScope(full_name());

            ctspec.c_type_spec = this;

            NameTable.define(full_name(), IDLTypes.TYPE_STRUCT);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            if (exc)
            {
                parser.error("Struct " + typeName() + " already defined", token);
            }
            else
            {
                Object forwardDeclaration = parser.get_pending (full_name());

                if (forwardDeclaration != null)
                {
                    if (! (forwardDeclaration instanceof StructType))
                    {
                        parser.error("Forward declaration types mismatch for "
                                     + full_name()
                                     + ": name already defined with another type" , token);
                    }

                    if (memberlist != null)
                    {
                        justAnotherOne = true;
                    }

                    if (!full_name().equals("org.omg.CORBA.TypeCode") && memberlist != null)
                    {
                        TypeMap.replaceForwardDeclaration(full_name(), ctspec);
                    }
                }
                else
                {
                    parser.error("Struct " + typeName() + " already defined", token);
                }
            }
        }
        if (memberlist != null)
        {
            ScopedName.addRecursionScope(typeName());
            memberlist.parse();
            ScopedName.removeRecursionScope(typeName());

            if (exc == false)
            {
                NameTable.parsed_interfaces.put(full_name(), "");
                parser.remove_pending(full_name());
            }
        }
        else if (!justAnotherOne && exc == false)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
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

        return fullName;
    }

    public String printReadExpression(String Streamname)
    {
        return toString() + "Helper.read(" + Streamname + ")";
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

    /**
     * @return a string for an expression of type TypeCode that
     * describes this type
     */

    public String getTypeCodeExpression()
    {
        return getTypeCodeExpression(new HashSet());
    }

	public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }

        knownTypes.add(this);

        StringBuffer sb = new StringBuffer();
        sb.append("org.omg.CORBA.ORB.init().create_" +
                (exc ? "exception" : "struct") + "_tc(" +
                typeName() + "Helper.id(),\"" + className() + "\",");

        if (memberlist != null)
        {
            sb.append("new org.omg.CORBA.StructMember[]{");
            for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            {
                Set knownTypesLocal = new HashSet(knownTypes);
                Member m = (Member)e.nextElement();
                Declarator d = m.declarator;
                sb.append("new org.omg.CORBA.StructMember(\"" + d.name() + "\", ");
                sb.append(m.type_spec.typeSpec().getTypeCodeExpression(knownTypesLocal));
                sb.append(", null)");
                if (e.hasMoreElements())
                    sb.append(",");
            }
            sb.append("}");
        }
        else
        {
            sb.append("new org.omg.CORBA.StructMember[0]");
        }
        sb.append(")");

        return sb.toString();
    }
    
    /**
     * Generates code from this AST class
     *
     * @param ps not used, the necessary output streams to classes
     * that receive code (e.g., helper and holder classes for the
     * IDL/Java mapping, are created inside this method.
     */

    public void print(PrintWriter ps,Vector<String> template)
    {
    	//FIXME
        setPrintPhaseNames();

        if (!parsed)
        {
            lexer.restorePosition(myPosition);
            parser.fatal_error ("Unparsed Struct!", token);
        }

        // no code generation for included definitions
        if (included && !generateIncluded())
        {
            return;
        }

        // only generate code once

        if (!written)
        {
            // guard against recursive entries, which can happen due to
            // containments, e.g., an alias within an interface that refers
            // back to the interface
            written = true;

            String className = className();
            
            int i = 0;
            boolean judge = false;
            while(i < template.size())
            {
            	if(template.get(i).startsWith("%newfile"))
            	{
            		judge = true;
            		String tmp = template.get(i).replaceAll("<structName>", name);
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
            	else if(template.get(i).startsWith("%member"))
            	{
            		Vector<String> _template = new Vector<String>();
            		while(!template.get(i).equals("%%"))
            		{
            			_template.add(template.get(i).replaceAll("<structName>", className));
            			i = i+1;
            		}
            		if ( ! isSystemException( className ) )
                    {
            			for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            			{
            				((Member)e.nextElement()).member_print(ps, _template);
            			}
            		}
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%exception"))
            	{
            		Vector<String> _template = new Vector<String>();
            		while(!template.get(i).equals("%%"))
            		{
            			_template.add(template.get(i).replaceAll("<exceptionName>", className));
            			i = i+1;
            		}
            		if ( ! isSystemException( className ) )
                    {
            			for (Enumeration e = memberlist.v.elements(); e.hasMoreElements();)
            			{
            				((Member)e.nextElement()).member_print(ps, _template);
            			}
            		}
            		i = i+1;
            	}
            	else
            	{
            		String tmp = template.get(i).replaceAll("<structName>", className);
            		tmp = tmp.replaceAll("<exceptionName>", className);
            		ps.println(tmp);
            		i = i+1;
            	}
            }
            
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
        ps.println("\t\t" + pack_name + "." + className() + "Helper.insert(" + anyname + ", " + varname + ");");
    }



    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        ps.println("\t\t" + resultname + " = " + pack_name + "." + className() + "Helper.extract(" + anyname + ");");
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitStruct(this);
    }

    /**
     * Decides if a class name is a CORBA System Exception name,
     * ignoring case.
     *
     * @param className a string containing the name to test
     *
     * @return true if the name is a system exception, false if not.
     */
    private boolean isSystemException( String className )
    {
        String ucClassName = className.toUpperCase();

        return systemExceptionNames.contains(ucClassName);
    }
}
