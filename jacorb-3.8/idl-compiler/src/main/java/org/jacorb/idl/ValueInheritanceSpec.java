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

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Gerald Brose
 */

public class ValueInheritanceSpec
    extends SymbolList
{
    /** the value types (both abstract and stateful) inherited by this
     value type */
    //Vector v;

    /** the IDL interfaces inherited ("supported") by this value type */
    Vector supports;

    /** if the value type this spec belongs to is truncatable to the
     single stateful ancestor value type */
    Truncatable truncatable = null;

    public ValueInheritanceSpec( int num )
    {
        super( num );
        //v = new Vector();
        supports = new Vector();
    }

    public String getTruncatableId()
    {
        if( truncatable == null )
        {
            return null;
        }
        return truncatable.getId();
    }

    public boolean isEmpty()
    {
        return ( v.size() == 0 && truncatable == null );
    }

    public Enumeration getValueTypes()
    {
        return v.elements();
    }

    public Enumeration getSupportedInterfaces()
    {
        return supports.elements();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        if( truncatable != null )
            truncatable.scopedName.setPackage( s );

        for( Enumeration e = v.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setPackage( s );

        for( Enumeration e = supports.elements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setPackage( s );
    }

    public void parse()
    {
        if( truncatable != null )
        {
            ScopedName s = truncatable.scopedName;
            Value v = (Value)((ConstrTypeSpec)s.resolvedTypeSpec()).c_type_spec;
            if( v instanceof ValueAbsDecl )
            {
                parser.error( "truncatable base value " +
                              s.toString() + " must not be abstract", token );
            }
        }
        Enumeration e = v.elements();
        for( ; e.hasMoreElements(); )
        {
            ( (IdlSymbol)e.nextElement() ).parse();
        }

    }

    public void print( PrintWriter ps , Vector<String> template )
    {
    	int i = 1;
    	if(template.get(0).startsWith("%truncatable"))
    	{
    		while(i < template.size())
    		{
    			if(template.get(i).startsWith("%abstract"))
    			{
    				Vector<String> _template = new Vector<String>();
            		while(!template.get(i).equals("%%"))
            		{
    					_template.add(template.get(i));
    					i = i+1;
            		}
            		Enumeration e = v.elements();
            		for( ; e.hasMoreElements(); )
                    {
            			ScopedName scopedName = (ScopedName)e.nextElement();
                    	ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                        for(int j = 0 ; j < _template.size() ; j++)
                        {
                        	if(ts.c_type_spec instanceof ValueAbsDecl)
                        		ps.println(_template.get(j).replaceAll("<truncatableName>", ts.toString()));
                        }
                    }
            		i = i + 1;
    			}
    			else if(template.get(i).startsWith("%stateful"))
    			{
    				Vector<String> _template = new Vector<String>();
            		while(!template.get(i).equals("%%"))
            		{
    					_template.add(template.get(i));
    					i = i+1;
            		}
            		for( Enumeration e = v.elements() ; e.hasMoreElements(); )
                    {
            			ScopedName scopedName = (ScopedName)e.nextElement();
                    	ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                        for(int j = 0 ; j < _template.size() ; j++)
                        {
                        	if(!(ts.c_type_spec instanceof ValueAbsDecl))
                        		ps.println(_template.get(j).replaceAll("<truncatableName>", ts.toString()));
                        }
                    }
            		i = i + 1;
            		if(truncatable == null)
            			continue;
            		for(int j = 0 ; j < _template.size() ; j++)
                    {
                    	ScopedName scopedName = (ScopedName)truncatable.scopedName;
                    	ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                    	ps.println(_template.get(j).replaceAll("<truncatableName>", ts.toString()));
                    }
    			}
    			else
    			{
    				Vector<String> _template = new Vector<String>();
            		while(i < template.size() && !template.get(i).equals("%%"))
            		{
    					_template.add(template.get(i));
    					i = i+1;
            		}
            		for( Enumeration e = v.elements() ; e.hasMoreElements(); )
                    {
            			ScopedName scopedName = (ScopedName)e.nextElement();
                    	ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                        for(int j = 0 ; j < _template.size() ; j++)
                        	ps.println(_template.get(j).replaceAll("<truncatableName>", ts.toString()));
                    }
            		i = i + 1;
            		if(truncatable == null)
            			continue;
            		for(int j = 0 ; i < _template.size() ; j++)
                    {
                    	ScopedName scopedName = (ScopedName)truncatable.scopedName;
                    	ConstrTypeSpec ts = (ConstrTypeSpec)scopedName.resolvedTypeSpec().typeSpec();
                    	ps.println(_template.get(j).replaceAll("<truncatableName>", ts.toString()));
                    }
    			}
    		}
    	}
    	else if(template.get(0).startsWith("%supports"))
    	{
    		while(i < template.size())
    		{
    			if(template.get(i).startsWith("%abstract"))
    			{
    				Vector<String> _template = new Vector<String>();
            		while(!template.get(i).equals("%%"))
            		{
    					_template.add(template.get(i));
    					i = i+1;
            		}
            		for( Enumeration e = getSupportedInterfaces() ; e.hasMoreElements(); )
                    {
            			ScopedName sne = (ScopedName)e.nextElement();
                        for(int j = 0 ; j < _template.size() ; j++)
                        {
                        	if (!(Interface.abstractInterfaces == null) && Interface.abstractInterfaces.contains (sne.toString()))
                        		ps.println(_template.get(j).replaceAll("<supportsName>", sne.toString()));
                        }
                    }
            		i = i + 1;
    			}
    			else if(template.get(i).startsWith("%stateful"))
    			{
    				Vector<String> _template = new Vector<String>();
    				i = i + 1;
            		while(!template.get(i).equals("%%"))
            		{
    					_template.add(template.get(i));
    					i = i+1;
            		}
            		for( Enumeration e = getSupportedInterfaces() ; e.hasMoreElements();)
                    {
            			ScopedName sne = (ScopedName)e.nextElement();
                        for(int j = 0 ; j < _template.size() ; j++)
                        {
                        	if (Interface.abstractInterfaces == null || !Interface.abstractInterfaces.contains (sne.toString()))
                        		ps.println(_template.get(j).replaceAll("<supportsName>", sne.toString()));
                        }
                    }
            		i = i + 1;
    			}
    			else
    			{
    				Vector<String> _template = new Vector<String>();
            		while(i < template.size() && !(template.get(i).equals("%%")))
            		{
    					_template.add(template.get(i));
    					i = i+1;
            		}
            		for( Enumeration e = getSupportedInterfaces() ; e.hasMoreElements();)
                    {
            			ScopedName sne = (ScopedName)e.nextElement();
                        for(int j = 0 ; j < _template.size() ; j++)
                        	ps.println(_template.get(j).replaceAll("<supportsName>", sne.toString()));
                    }
            		i = i + 1;
    			}
    		}
    	}
    }

    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        if( truncatable != null )
            sb.append( truncatable.toString() + " " );

        Enumeration e = v.elements();

        if( e.hasMoreElements() )
            sb.append( e.nextElement() + " " );

        for( ; e.hasMoreElements(); )
        {
            sb.append( "," + e.nextElement() + " " );
        }

        Enumeration s = supports.elements();
        if( s.hasMoreElements() )
        {
            sb.append( "supports " );
            ( (IdlSymbol)s.nextElement() ).toString();
        }

        for( ; s.hasMoreElements(); )
        {
            sb.append(',');
            ( (IdlSymbol)s.nextElement() ).toString();
        }

        return sb.toString();
    }
}



