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
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author Andre Spiegel
 */

public class ValueDecl
    extends Value
{
    private MemberList stateMembers;
    @SuppressWarnings("rawtypes")
	private List operations;
    @SuppressWarnings("rawtypes")
	private List exports;
    @SuppressWarnings("rawtypes")
	private List factories;
    private ValueInheritanceSpec inheritanceSpec;

    // some flags...
    private boolean isCustomMarshalled = false;
    private boolean hasStatefulBases = false;
    private boolean hasBody = false;

    /** public c'tor, called by parser */

    @SuppressWarnings("rawtypes")
	public ValueDecl(int num)
    {
        super(num);
        stateMembers = new MemberList(new_num());
        operations = new ArrayList();
        exports = new ArrayList();
        factories = new ArrayList();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void setValueElements(Definitions d)
    {
        hasBody = true;

        for(Iterator i = d.v.iterator(); i.hasNext();)
        {
            Declaration dec = ((Definition)(i.next())).get_declaration();
            dec.setPackage(name);
            if (dec instanceof StateMember)
                stateMembers.v.add(dec);
            else if (dec instanceof OpDecl)
                operations.add(dec);
            else if (dec instanceof InitDecl)
                factories.add(dec);
            else
                exports.add(dec);
        }
        stateMembers.setContainingType(this);
        stateMembers.setPackage(name);
        stateMembers.setEnclosingSymbol(this);

        for(Iterator i = operations.iterator(); i.hasNext();)
            ((OpDecl)i.next()).setEnclosingSymbol(this);

        for(Iterator i = exports.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setEnclosingSymbol(this);

        for(Iterator i = factories.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setEnclosingSymbol(this);
    }

    public void setInheritanceSpec(ValueInheritanceSpec spec)
    {
        inheritanceSpec = spec;
    }

    public ValueInheritanceSpec getInheritanceSpec()
    {
        return inheritanceSpec;
    }

    public void isCustomMarshalled(boolean flag)
    {
        this.isCustomMarshalled = flag;
    }

    public boolean isCustomMarshalled()
    {
        return this.isCustomMarshalled;
    }

    @SuppressWarnings("rawtypes")
	public void setPackage(String s)
    {
        s = parser.pack_replace(s);
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        stateMembers.setPackage(s);

        if (inheritanceSpec != null)
            inheritanceSpec.setPackage(s);

        for(Iterator i = operations.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage(s);

        for(Iterator i = exports.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage(s);

        for(Iterator i = factories.iterator(); i.hasNext();)
            ((IdlSymbol)i.next()).setPackage(s);
    }

    public TypeDeclaration declaration()
    {
        return this;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void parse()
    {
        if (inheritanceSpec != null)
        {
            inheritanceSpec.parse();
        }

        boolean justAnotherOne = false;

        if (isCustomMarshalled() &&
            inheritanceSpec != null &&
            inheritanceSpec.truncatable != null)
        {
            parser.error("Valuetype " + typeName() +
                         " may no be BOTH custom AND truncatable", token);
        }

        ConstrTypeSpec ctspec = new ConstrTypeSpec(new_num());

        try
        {
            escapeName();
            ScopedName.definePseudoScope(full_name());

            ctspec.c_type_spec = this;

            NameTable.define(full_name(), IDLTypes.TYPE);
            TypeMap.typedef(full_name(), ctspec);
        }
        catch (NameAlreadyDefined nad)
        {
            Object forwardDeclaration = parser.get_pending (full_name ());

            if (forwardDeclaration != null)
            {
                if (! (forwardDeclaration instanceof ValueDecl))
                {
                    parser.error("Forward declaration types mismatch for "
                            + full_name()
                            + ": name already defined with another type" , token);
                }

                if (stateMembers.size () != 0)
                {
                    justAnotherOne = true;
                }
                if (! full_name().equals("org.omg.CORBA.TypeCode") &&
                    stateMembers.size () != 0)
                {
                    TypeMap.replaceForwardDeclaration(full_name(), ctspec);
                }
            }
            else
            {
                parser.error("Valuetype " + typeName() + " already defined", token);
            }
        }

        if (hasBody)
        {
            parser.logger.log(Level.WARNING, "valueDecl.parse(): exports (but not attributes)");

            // parse exports
            Iterator iter = exports.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();

                if (! ( idlSymbol instanceof AttrDecl ) )
                {
                    idlSymbol.parse();
                }
            }

            parser.logger.log(Level.WARNING, "valueDecl.parse(): members");

            ScopedName.addRecursionScope(typeName());
            stateMembers.parse();
            ScopedName.removeRecursionScope(typeName());


            parser.logger.log(Level.WARNING, "valueDecl.parse(): operations");

            // parse operations
            iter = operations.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();
                idlSymbol.parse();
            }


            parser.logger.log(Level.WARNING, "valueDecl.parse(): exports(attributes)");

            // parser exports
            iter = exports.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();

                if (idlSymbol instanceof AttrDecl)
                {
                    idlSymbol.parse();
                    Enumeration e = ((AttrDecl)idlSymbol).getOperations();
                    while(e.hasMoreElements())
                    {
                        operations.add(e.nextElement());
                    }
                }
            }

            parser.logger.log(Level.WARNING, "valueDecl.parse(): factories");

            // parse factories
            iter = factories.iterator();
            while(iter.hasNext())
            {
                IdlSymbol idlSymbol = (IdlSymbol)iter.next();
                idlSymbol.parse();
            }

            // check inheritance rules
            parser.logger.log(Level.WARNING, "valueDecl.parse(): check inheritance");

            if (inheritanceSpec != null)
            {
                HashSet h = new HashSet();
                for(Enumeration e = inheritanceSpec.getValueTypes();
                    e.hasMoreElements();)
                {
                    ScopedName scopedName = (ScopedName)e.nextElement();

                    ConstrTypeSpec ts = unwindTypedefs(scopedName);

                    if (ts.declaration() instanceof Value)
                    {
                        if (h.contains(ts.full_name()))
                        {
                            parser.fatal_error("Illegal inheritance spec: " +
                                               inheritanceSpec  +
                                               " (repeated inheritance not allowed).",
                                               token);
                        }
                        // else:
                        h.add(ts.full_name());
                        continue;
                    }
                    parser.logger.log(Level.SEVERE, " Declaration is " + ts.declaration().getClass());
                    parser.fatal_error("Non-value type in inheritance spec: " + Environment.NL
                                       + "\t" + inheritanceSpec, token);
                }

                for(Enumeration e = inheritanceSpec.getSupportedInterfaces();
                    e.hasMoreElements();)
                {
                    ScopedName scopedName = (ScopedName)e.nextElement();
                    ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                    if (ts.declaration() instanceof Interface)
                    {
                        continue;
                    }
                    parser.fatal_error("Non-interface type in supported interfaces list: " + Environment.NL
                                       + "\t" + inheritanceSpec, token);
                }
            }
            NameTable.parsed_interfaces.put(full_name(), "");
            parser.remove_pending(full_name());
        }
        else if (! justAnotherOne)
        {
            // i am forward declared, must set myself as
            // pending further parsing
            parser.set_pending(full_name(), this);
        }

    }

    private ConstrTypeSpec unwindTypedefs(ScopedName scopedName)
    {
        TypeSpec resolvedTSpec = scopedName.resolvedTypeSpec();
        //unwind any typedefs
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
                 resolvedTSpec.getClass() + ", name " + scopedName);
            }
            parser.fatal_error("Illegal inheritance spec (not a constr. type): " +
                               inheritanceSpec, token);
        }

        return (ConstrTypeSpec) resolvedTSpec;
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
        {
            parser.logger.log(Level.SEVERE, "was " + enclosing_symbol.getClass().getName() +
               " now: " + s.getClass().getName());
            throw new RuntimeException("Compiler Error: trying to reassign container for " +
                                       name);
        }

        enclosing_symbol = s;
    }

    public void set_included(boolean i)
    {
        included = i;
    }

    public boolean basic()
    {
        return true;
    }

    public String toString()
    {
        return full_name();
    }

    public String holderName()
    {
        return javaName() + "Holder";
    }

    public String helperName() {
        return javaName() + "Helper";
    }

    public String typeName()
    {
        return full_name();
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

        String baseType = "null";

        // Only add e.g. FooHelper.type() for those inherited non-abstract ValueTypes.
        if (hasStatefulBases &&
            inheritanceSpec != null && inheritanceSpec.v.size() > 0)
        {
            baseType = ((ScopedName)inheritanceSpec.v.get(0)).resolvedName() + "Helper.type()";
        }

        StringBuffer result = new StringBuffer
        ("org.omg.CORBA.ORB.init().create_value_tc (" +
                // id, name
                "\"" + id() + "\", " + "\"" + name + "\", " +
                // type modifier
                "(short)" +
                (this.isCustomMarshalled()
                        ? org.omg.CORBA.VM_CUSTOM.value
                        : org.omg.CORBA.VM_NONE.value
                ) + ", " +
                // concrete base type
                baseType + ", " +
                // value members
        "new org.omg.CORBA.ValueMember[] {");
        knownTypes.add(this);
        for(Iterator i = stateMembers.v.iterator(); i.hasNext();)
        {
            Set knownTypesLocal = new HashSet(knownTypes);
        	StateMember m = (StateMember)i.next();
            result.append(getValueMemberExpression(m, knownTypesLocal));
            if (i.hasNext()) result.append(", ");
        }
        knownTypes.remove(this);
        result.append("})");
        return result.toString();
    }

    @SuppressWarnings("rawtypes")
	private String getValueMemberExpression(StateMember m, Set knownTypes)
    {
        TypeSpec typeSpec = m.typeSpec();
        //if the type is not a basic type and is in the typeMap
        //use the typeSpec saved within the TypeMap
        if (typeSpec.full_name() != null &&
        		!typeSpec.full_name().equals("IDL:*primitive*:1.0")
        		&&
        		TypeMap.typemap.containsKey(typeSpec.full_name()))
        	typeSpec =  TypeMap.map(typeSpec.full_name());

        String memberTypeExpression = typeSpec.getTypeCodeExpression(knownTypes);
        short access = m.isPublic
            ? org.omg.CORBA.PUBLIC_MEMBER.value
            : org.omg.CORBA.PRIVATE_MEMBER.value;

        return "new org.omg.CORBA.ValueMember (" +
            "\"" + m.name + "\", \"" + typeSpec.id() +
            "\", \"" + name + "\", \"1.0\", " +
            memberTypeExpression + ", null, " +
            "(short)" + access + ")";
    }

    @SuppressWarnings("rawtypes")
	public void print(PrintWriter ps , Vector<String> template)
    {
    	//FIXME
    	if(!template.get(0).equals("normal") && !template.get(0).equals("custom") && !template.get(0).equals("nocustom") && !template.get(0).equals("all"))
    		return;
    	else if(template.get(0).equals("custom") && !isCustomMarshalled)
    		return;
    	else if(template.get(0).equals("nocustom") && isCustomMarshalled)
    		return;
    	
        // no code generation for included definitions
        if (included && !generateIncluded())
        {
            return;
        }

        //no code generation for forward declarations (bug #539)
        if (!hasBody)
        {
            return;
        }
        
        boolean judge = false;
    	int i = 1;
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
        		for (Iterator m = operations.iterator(); m.hasNext();)
        		{
        			Operation o = ((Operation)m.next());
        			if(o instanceof OpDecl)
        				o.printSignature(ps,_template);
        		}
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
        		for (Iterator m = exports.iterator(); m.hasNext();)
        		{
        			IdlSymbol s = (IdlSymbol)m.next();
        			if(s instanceof TypeDeclaration)
        				s.print(ps,_template,"typedef");
        		}
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
        		for (Iterator m = exports.iterator(); m.hasNext();)
        		{
        			IdlSymbol s = (IdlSymbol)m.next();
        			if(s instanceof ConstDecl)
        				s.print(ps,_template);
        		}
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
        		for (Iterator m = exports.iterator(); m.hasNext();)
        		{
        			IdlSymbol s = (IdlSymbol)m.next();
        			if(s instanceof TypeDeclaration)
        				s.print(ps,_template,"struct");
        		}
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
        		for (Iterator m = exports.iterator(); m.hasNext();)
        		{
        			IdlSymbol s = (IdlSymbol)m.next();
        			if(s instanceof TypeDeclaration)
        				s.print(ps,_template,"exception");
        		}
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
        		for (Iterator m = exports.iterator(); m.hasNext();)
        		{
        			IdlSymbol s = (IdlSymbol)m.next();
        			if(s instanceof TypeDeclaration)
        				s.print(ps,_template,"union");
        		}
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
        		for (Iterator m = exports.iterator(); m.hasNext();)
        		{
        			IdlSymbol s = (IdlSymbol)m.next();
        			if(s instanceof TypeDeclaration)
        				s.print(ps,_template,"enum");
        		}
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
        		for (Iterator m = exports.iterator(); m.hasNext();)
        		{
        			IdlSymbol s = (IdlSymbol)m.next();
        			if(s instanceof AttrDecl)
        				s.print(ps,_template);
        		}
        		i = i+1;
        	}
        	else if(template.get(i).startsWith("%statemember"))
        	{
        		boolean _public = false;
        		if(template.get(i).contains(":public"))
        			_public = true;
        		else if(template.get(i).contains(":private"))
        			_public = false;
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
        		for(Iterator t = stateMembers.v.iterator(); t.hasNext();)
	            {
	                ((StateMember)t.next()).print(ps,_template,_public);
	            }
        		i = i+1;
        	}
        	else if(template.get(i).startsWith("%factory"))
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
        		printFactory(ps,_template);
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
        		tmp = tmp.replaceAll("<truncatableList>", getTruncatableList());
    			tmp = tmp.replaceAll("<abstractTruncatableList>", getAbstractTruncatableList());
    			tmp = tmp.replaceAll("<statefulTruncatableList>", getStatefulTruncatableList());
    			tmp = tmp.replaceAll("<supportsList>", getTruncatableList());
    			tmp = tmp.replaceAll("<abstractSupportsList>", getAbstractSupportsList());
    			tmp = tmp.replaceAll("<statefulSupportsList>", getStatefulSupportsList());
        		ps.println(tmp);
        		i = i+1;
        	}
        }
    	
    	if(ps != null && judge)
        	ps.close();
    }
    
    @SuppressWarnings("rawtypes")
	private void printFactory(PrintWriter ps, Vector<String> template) 
    {
    	if (factories.size() == 0)
        {
            return;
        }
    	
    	if (hasBody)
        {
    		for(Iterator t = factories.iterator(); t.hasNext();)
            {
    			((InitDecl)t.next()).print(ps,template);
            }
        }
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

    public String printWriteStatement(String var_name, String streamname)
    {
        // pass in null repository id to prevent CDROutputStream
        // to resolve the RMI repository ID

        return "((org.omg.CORBA_2_3.portable.OutputStream)" + streamname + ")"
            + ".write_value (" + var_name + ", (String)null);";
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

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname) {
        ps.println( "\t\t" + anyname + ".insert_Value(" + varname + ", "+ varname +"._type());");
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
