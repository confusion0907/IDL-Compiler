/*
 *        JacORB - a free Java ORB
 *
 *   Copyright (C) 1999-2014 Gerald Brose / The JacORB Team.
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
 *
 */
package org.jacorb.idl;

import java.io.PrintWriter;
import java.util.Vector;

public class StateMember 
    extends Member
{
    public boolean isPublic = false;

    public StateMember( int num )
    {
        super( num );
    }

    /**
     * Creates a new Member that is similar to this one,
     * but only for declarator d.
     */
    public Member extractMember( Declarator d )
    {
        StateMember result = new StateMember( new_num() );
        result.declarator = d;
        result.isPublic = this.isPublic;
        return result;
    }

    public void print( PrintWriter ps , Vector<String> template , boolean _public )
    {
        int i = 0;
        while(i < template.size())
        {
        	if(template.get(i).startsWith("%member"))
        	{
        		Vector<String> _template = new Vector<String>();
        		while(!template.get(i).equals("%%"))
        		{
        			_template.add(template.get(i));
        			i = i + 1;
        		}
        		if( this.isPublic && _public )
                    member_print( ps, _template );
                else if( !this.isPublic && !_public )
                    member_print( ps, _template );
        		i = i + 1;
        	}
        	else
        	{
        		if( this.isPublic && _public )
                    ps.println(template.get(i));
                else if( !this.isPublic && !_public )
                	ps.println(template.get(i));
        		i = i + 1;
        	}
        }
    }

    public String writeStatement( String outStreamName )
    {
        return type_spec.printWriteStatement( declarator.name(),
                outStreamName );
    }

    public String readStatement( String inStreamName )
    {
        return type_spec.printReadStatement( declarator.name(),
                inStreamName );
    }
}



