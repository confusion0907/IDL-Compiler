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

import java.io.PrintWriter;
import java.util.Vector;
import java.util.logging.Level;


public class Definition
    extends IdlSymbol
{
    private Declaration declaration;

    public Definition( int num )
    {
        super( num );
        pack_name = "";
    }

    public Definition (Declaration d)
    {
        super (new_num());
        pack_name = "";
        this.declaration = d;
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        super.setPackage( s );
        declaration.setPackage( s );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
        {
            parser.logger.log(Level.SEVERE, "was " + enclosing_symbol.getClass().getName() + " now: " + s.getClass().getName());
            throw new RuntimeException( "Compiler Error: trying to reassign container for " + name );
        }
        enclosing_symbol = s;
        declaration.setEnclosingSymbol( s );
    }

    public Declaration get_declaration()
    {
        return declaration;
    }

    public void set_declaration( Declaration d )
    {
        declaration = d;
    }

    public void set_included( boolean i )
    {
        included = i;
        declaration.set_included( i );
    }

    public void print( PrintWriter ps , Vector<String> template , String type )
    {
    	if(type.equals("interface") && declaration instanceof Interface)
    		declaration.print( ps , template );
    	else if(type.equals("struct") && declaration instanceof TypeDeclaration)
    		declaration.print( ps , template , "struct" );
    	else if(type.equals("typedef") && declaration instanceof TypeDeclaration)
    		declaration.print(ps , template , "typedef");
    	else if(type.equals("exception") && declaration instanceof TypeDeclaration)
    		declaration.print(ps , template , "exception");
    	else if(type.equals("union") && declaration instanceof TypeDeclaration)
    		declaration.print(ps , template , "union");
    	else if(type.equals("enum") && declaration instanceof TypeDeclaration)
    		declaration.print(ps , template , "enum");
    	else if(type.equals("module") && declaration instanceof Declaration && !(declaration instanceof TypeDeclaration))
    		declaration.print(ps , template);
    	else if(type.equals("valuetype") && declaration instanceof Value)
    		declaration.print(ps , template);
    }

    public void parse()
    {
        declaration.parse();
    }

    /**
     */

    public void accept( IDLTreeVisitor visitor )
    {
        visitor.visitDefinition( this );
    }


}
