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
 * @author Andre Spiegel
 * @author Gerald Brose
 *
 * This class is basically the same as Interface.java, but we can't extend
 * that one because we have to extend Value, and delegating some parts and
 * not others is a nuisance...
 */
public class ValueAbsDecl
    extends Value
{
    ValueBody body = null;
    ValueInheritanceSpec inheritanceSpec;

    public ValueAbsDecl(int num)
    {
        super(num);
        pack_name = "";
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

        if (body != null) // could've been a forward declaration)
        {
            body.setPackage(s); // a new scope!
        }

        if (inheritanceSpec != null)
        {
            inheritanceSpec.setPackage(s);
        }
    }

    public void setInheritanceSpec(ValueInheritanceSpec spec)
    {
        inheritanceSpec = spec;
    }

    public ValueInheritanceSpec setInheritanceSpec()
    {
        return inheritanceSpec;
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    public String typeName()
    {
        return full_name();
    }

    public Object clone()
    {
        throw new RuntimeException("Don't clone me, i am an interface!");
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

    public boolean basic()
    {
        return true;
    }

    public String holderName()
    {
        return javaName() + "Holder";
    }

    public String helperName() throws NoHelperException {
        throw new NoHelperException();
    }

    public String toString()
    {
        return getFullName(typeName());
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void parse()
    {
        boolean justAnotherOne = false;

        escapeName();

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());
        try
        {
            ScopedName.definePseudoScope(full_name());
            ctspec.c_type_spec = this;

            NameTable.define(full_name(), IDLTypes.TYPE);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (IllegalRedefinition ill)
        {
            parser.fatal_error("Cannot redefine " + token.str_val +
                    " in nested scope as " + ill.newDef, token);
        }
        catch (NameAlreadyDefined nad)
        {
            // if we get here, there is already a type spec for this interface
            // in the global type table for a forward declaration of this
            // interface. We must replace that table entry with this type spec
            // if this is not yet another forwad declaration

            if (body != null)
            {
                justAnotherOne = true;
            }

            if (!full_name().equals("org.omg.CORBA.TypeCode") && body != null)
            {
                TypeMap.replaceForwardDeclaration(full_name(), ctspec);
            }
        }

        if (body != null)
        {
            if (inheritanceSpec != null && inheritanceSpec.v.size() > 0)
            {
                if (parser.logger.isLoggable(Level.ALL))
                    parser.logger.log(Level.ALL, "Checking inheritanceSpec of " + full_name());
                for (Enumeration e = inheritanceSpec.v.elements(); e.hasMoreElements();)
                {
                    ScopedName name = (ScopedName)e.nextElement();

                    TypeSpec resolvedTSpec = name.resolvedTypeSpec();

                    // struct checking can be turned off to tolerate strange
                    // typedefs generated by rmic
                    if (parser.strict_inheritance)
                    {
                        // unwind any typedef's interface names
                        while (resolvedTSpec instanceof AliasTypeSpec )
                        {
                            resolvedTSpec =
                                ((AliasTypeSpec)resolvedTSpec).originalType();
                        }

                        if (! (resolvedTSpec instanceof ConstrTypeSpec))
                        {
                            if (parser.logger.isLoggable(Level.ALL))
                            {
                                parser.logger.log(Level.ALL, "Illegal inheritance spec, not a constr. type but " +
                                 resolvedTSpec.getClass() + ", name " + name);
                            }
                            parser.fatal_error("Illegal inheritance spec (not a constr. type): " +
                                           inheritanceSpec, token);
                        }

                        ConstrTypeSpec ts = (ConstrTypeSpec)resolvedTSpec;
                        if (!(ts.declaration() instanceof Interface) &&
                            !(ts.declaration() instanceof ValueAbsDecl))
                        {
                            parser.fatal_error("Illegal inheritance spec (not an intf. or abs. value type): " +
                                               inheritanceSpec, token);
                        }
                    }
                }
                body.set_ancestors(inheritanceSpec);
            }
            body.parse();
            NameTable.parsed_interfaces.put(full_name(), "");
        }
        else if (!justAnotherOne)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
        }
    }

    ValueBody getBody()
    {
        if (parser.get_pending(full_name()) != null)
        {
            parser.fatal_error(full_name() + " is forward declared and still pending!", token);
        }
        else if (body == null)
        {
            if (((ValueAbsDecl)((ConstrTypeSpec)TypeMap.map(full_name())).c_type_spec) != this)
            {
                body = ((ValueAbsDecl)((ConstrTypeSpec)TypeMap.map(full_name())).c_type_spec).getBody();
            }
            if (body == null)
            {
                parser.fatal_error(full_name() + " still has an empty body!", token);
            }
        }
        return body;
    }

    @SuppressWarnings("rawtypes")
	public String getTypeCodeExpression()
    {
        return this.getTypeCodeExpression(new HashSet());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public String getTypeCodeExpression(Set knownTypes)
    {
        if (knownTypes.contains(this))
        {
            return this.getRecursiveTypeCodeExpression();
        }

        knownTypes.add(this);

        return "org.omg.CORBA.ORB.init().create_value_tc(\"" + id() +
            "\", \"" + name +
            "\", org.omg.CORBA.VM_ABSTRACT.value " +
            ", null, null)";
    }


    public String printReadExpression(String streamname)
    {
        return "(" + javaName() + ")"
            + "((org.omg.CORBA_2_3.portable.InputStream)" + streamname + ")"
            + ".read_value (\"" + id() + "\")";
    }

    public String printReadStatement(String var_name, String streamname)
    {
        return var_name + " = " + printReadExpression(streamname);
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ")"
            + ".write_value (" + var_name + ");";
    }


    /**
     * generate the mapped class that extends ValueBase and has the
     * operations and attributes
     */

    @SuppressWarnings("rawtypes")
	public void print(PrintWriter ps , Vector<String> template)
    {
    	//FIXME
    	if(!template.get(0).equals("abstract"))
    		return;
    	
        if (included && !generateIncluded())
        {
            return;
        }
        
        int i = 1;
        boolean judge = false;

        // divert output into class files
        if (body != null) // forward declaration
        {
            while(i < template.size())
            {
            	if(template.get(i).startsWith("%newfile"))
            	{
            		judge = true;
            		String tmp = template.get(i).replaceAll("<valuetypeName>", name);
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
            	else if(template.get(i).startsWith("%operation"))
            	{
            		String type = "";
            		if(template.get(i).contains(":normal"))
            			type = "normal";
            		else if(template.get(i).contains(":oneway"))
            			type = "oneway";
            		else if(template.get(i).contains("noraises"))
            			type = "noraises";
            		else if(template.get(i).contains("raises"))
            			type = "raises";
            		else
            			type = "all";
            		int index = 1;
            		Vector<String> _template = new Vector<String>();
            		_template.add(type);
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		body.printOperationSignatures(ps,_template,"operation");
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%typedef"))
            	{
            		int index = 1;
            		Vector<String> _template = new Vector<String>();
            		_template.add(template.get(i));
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		body.print(ps,_template,"typedef");
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%constants"))
            	{
            		i= i+1;
            		Vector<String> _template = new Vector<String>();
            		while(!template.get(i).equals("%%"))
            		{
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					i = i+1;
            		}
            		body.printConstants(ps,_template);
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%struct"))
            	{
            		int index = 1;
            		Vector<String> _template = new Vector<String>();
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		body.print(ps,_template,"struct");
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%exception"))
            	{
            		int index = 1;
            		Vector<String> _template = new Vector<String>();
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		body.print(ps,_template,"exception");
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%union"))
            	{
            		int index = 1;
            		Vector<String> _template = new Vector<String>();
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		body.print(ps,_template,"union");
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%enum"))
            	{
            		int index = 1;
            		Vector<String> _template = new Vector<String>();
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		body.print(ps,_template,"enum");
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%attribute"))
            	{
            		int index = 1;
            		Vector<String> _template = new Vector<String>();
            		_template.add(template.get(i));
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		body.printOperationSignatures(ps,_template,"attribute");
            		i = i+1;
            	}
            	else if(template.get(i).startsWith("%truncatable"))
            	{
            		Vector<String> _template = new Vector<String>();
            		_template.add(template.get(i));
            		int index = 1;
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		Enumeration e = inheritanceSpec.getValueTypes();
                	if (!e.hasMoreElements() && inheritanceSpec.truncatable == null)
                	{
                		i = i + 1;
                		continue;
                	}
            		if(_template.size() == 2 && !(_template.get(1).contains("<truncatableName>") || (_template.get(1).contains("<supportsName>"))))
            			ps.println(_template.get(1));
            		else
            			inheritanceSpec.print(ps,_template);
            		i = i + 1;
            	}
            	else if(template.get(i).startsWith("%supports"))
            	{
            		Vector<String> _template = new Vector<String>();
            		_template.add(template.get(i));
            		int index = 1;
            		while(!(template.get(i).equals("%%") && index == 0))
            		{
            			i = i + 1;
            			String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            			tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
            			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
            			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
            			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
            			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
    					_template.add(tmp);
    					if(template.get(i).startsWith("%") && !template.get(i).equals("%%"))
    						index = index+1;
    					else if(template.get(i).equals("%%"))
    						index = index-1;
            		}
            		_template.remove(_template.size()-1);
            		Enumeration enumeration = inheritanceSpec.getSupportedInterfaces();
            		if (!enumeration.hasMoreElements())
                	{
                		i = i + 1;
                		continue;
                	}
            		if(_template.size() == 2 && !(_template.get(1).contains("<truncatableName>") || (_template.get(1).contains("<supportsName>"))))
            			ps.println(_template.get(1));
            		else
            			inheritanceSpec.print(ps,_template);
            		i = i + 1;
            	}
            	else if(ps == null)
					throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
            	else
            	{
            		String tmp = template.get(i).replaceAll("<valuetypeName>", name);
            		ps.println(tmp);
            		i = i+1;
            	}
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
    
    @SuppressWarnings("rawtypes")
	public String getTruncatableList()
    {
    	String result = "";
    	Enumeration e = inheritanceSpec.getValueTypes();
    	if (e.hasMoreElements() || inheritanceSpec.truncatable != null)
        {
            for(; e.hasMoreElements();)
            {
                ScopedName scopedName = (ScopedName)e.nextElement();
                ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                result = result + "," + ts.toString();
            }
            if (inheritanceSpec.truncatable != null)
            	result = result + "," + inheritanceSpec.truncatable.scopedName;
            if(!result.equals(""))
            	result = result.substring(1);
        }
    	return result;
    }
	
	@SuppressWarnings("rawtypes")
	public String getAbstractTruncatableList()
    {
    	String result = "";
    	Enumeration e = inheritanceSpec.getValueTypes();
    	if (e.hasMoreElements() || inheritanceSpec.truncatable != null)
        {
            for(; e.hasMoreElements();)
            {
                ScopedName scopedName = (ScopedName)e.nextElement();
                ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                if (ts.c_type_spec instanceof ValueAbsDecl)
                	result = result + "," + ts.toString();
            }
            if(!result.equals(""))
            	result = result.substring(1);
        }
    	return result;
    }
	
	@SuppressWarnings("rawtypes")
	public String getStatefulTruncatableList()
    {
    	String result = "";
    	Enumeration e = inheritanceSpec.getValueTypes();
    	if (e.hasMoreElements() || inheritanceSpec.truncatable != null)
        {
            for(; e.hasMoreElements();)
            {
                ScopedName scopedName = (ScopedName)e.nextElement();
                ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                if (!(ts.c_type_spec instanceof ValueAbsDecl))
                	result = result + "," + ts.toString();
            }
            if (inheritanceSpec.truncatable != null)
            	result = result + "," + inheritanceSpec.truncatable.scopedName;
            if(!result.equals(""))
            	result = result.substring(1);
        }
    	return result;
    }
	
	@SuppressWarnings("rawtypes")
	public String getSupportsList()
    {
    	String result = "";
    	Enumeration enumeration = inheritanceSpec.getSupportedInterfaces();
        for(; enumeration.hasMoreElements();)
        {
        	ScopedName sne = (ScopedName)enumeration.nextElement();
        	result = result + ", " + sne;
        }
        if(!result.equals(""))
        	result = result.substring(1);
    	return result;
    }
	
	@SuppressWarnings("rawtypes")
	public String getStatefulSupportsList()
    {
    	String result = "";
    	Enumeration enumeration = inheritanceSpec.getSupportedInterfaces();
        for(; enumeration.hasMoreElements();)
        {
        	ScopedName sne = (ScopedName)enumeration.nextElement();
        	if (Interface.abstractInterfaces == null || !Interface.abstractInterfaces.contains (sne.toString()))
        		result = result + ", " + sne;
        }
        if(!result.equals(""))
        	result = result.substring(1);
    	return result;
    }
	
	@SuppressWarnings("rawtypes")
	public String getAbstractSupportsList()
    {
    	String result = "";
    	Enumeration enumeration = inheritanceSpec.getSupportedInterfaces();
        for(; enumeration.hasMoreElements();)
        {
        	ScopedName sne = (ScopedName)enumeration.nextElement();
        	if (!(Interface.abstractInterfaces == null) && Interface.abstractInterfaces.contains (sne.toString()))
        		result = result + ", " + sne;
        }
        if(!result.equals(""))
        	result = result.substring(1);
    	return result;
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        ps.println( "\t\t" + anyname + ".insert_Value(" + varname + ", " + varname + "._type());");
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
}
