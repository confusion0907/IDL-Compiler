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

/**
 * @author Gerald Brose
 */


public class SimpleDeclarator
    extends Declarator
{
    public SimpleDeclarator( int num )
    {
        super( num );
    }

    public String name()
    {
        return name;
    }

    /**
     */

    public void escapeName()
    {
        if( !name.startsWith( "_" ) &&
                lexer.strictJavaEscapeCheck( name ) )
        {
            name = "_" + name;
        }
    }

    public void parse()
    {
        // add sloppy_declarator behavior here....
        try
        {
            NameTable.define( full_name(), IDLTypes.DECLARATOR );
        }
        catch( NameAlreadyDefined p )
        {
            parser.error( "Declarator name " + full_name() +
                    " already declared in this scope.", token );
        }
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        enclosing_symbol = s;
    }

    public IdlSymbol getEnclosingSymbol()
    {
        return enclosing_symbol;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
    }

    String full_name()
    {
        if( name.length() == 0 )
            return null;
        if( pack_name.length() > 0 )
            return pack_name + "." + name;
        else
            return name;
    }

    public String toString()
    {
        return name;
    }
}
