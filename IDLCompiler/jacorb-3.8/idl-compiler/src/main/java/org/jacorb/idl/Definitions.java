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
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author Gerald Brose
 */

public class Definitions
    extends SymbolList
{

    public Definitions( int num )
    {
        super( num );
        v = new Vector();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        Enumeration e = getElements();
        for( ; e.hasMoreElements(); )
        {
            IdlSymbol i = (IdlSymbol)e.nextElement();
            i.setPackage( s );
        }
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
        {
            parser.logger.log(Level.SEVERE, "was " + enclosing_symbol.getClass().getName() +
            " now: " + s.getClass().getName());
            throw new RuntimeException( "Compiler Error: trying to reassign container for " +
                                        name );
        }
        enclosing_symbol = s;
        for( Enumeration e = getElements(); e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).setEnclosingSymbol( s );
    }

    public void set_included( boolean i )
    {
        included = i;
        Enumeration e = getElements();
        for( ; e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).set_included( i );
    }

    public Enumeration getElements()
    {
        return v.elements();
    }

	public void print( PrintWriter ps , Vector<String> template )
    {
    	int i = 0;
    	String str = "";
    	boolean judge = false;
    	while (i < template.size()) 
		{
    		str = template.get(i);
    		if(template.get(i).startsWith("%newfile"))
        	{
        		judge = true;
        		String tmp = template.get(i).replaceAll("<interfaceName>", name);
        		PrintWriter _ps = openOutput(tmp.substring(9));
        		
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
			else if(str.startsWith("%interface"))
			{
				int index = 1;
				Vector<String> _template = new Vector<String>();
				while(!(str.equals("%%") && index == 0))
				{
					i = i+1;
					str = template.get(i);
					str = str.replaceAll("<fileName>", parser.file_Name);
					_template.add(str);
					if(str.startsWith("%") && !str.equals("%%") && !str.contains("%newfile"))
						index = index+1;
					else if(str.equals("%%"))
						index = index-1;
				}
				_template.remove(_template.size()-1);
				
				Enumeration e = getElements();;
		        while( e.hasMoreElements() )
		            ( (IdlSymbol)e.nextElement() ).print( ps , _template , "interface" );
			}
			else if(str.startsWith("%struct"))
			{
				int index = 1;
				Vector<String> _template = new Vector<String>();
				while(!(str.equals("%%") && index == 0))
				{
					i = i+1;
					str = template.get(i);
					str = str.replaceAll("<fileName>", parser.file_Name);
					_template.add(str);
					if(str.startsWith("%") && !str.equals("%%") && !str.contains("%newfile"))
						index = index+1;
					else if(str.equals("%%"))
						index = index-1;
				}
				template.remove(_template.size()-1);
				
				Enumeration e = getElements();
		        while( e.hasMoreElements() )
		            ( (IdlSymbol)e.nextElement() ).print( ps , _template , "struct" );
			}
			else if(str.startsWith("%typedef"))
			{
				int index = 1;
				Vector<String> _template = new Vector<String>();
				template.add(str);
				while(!(str.equals("%%") && index == 0))
				{
					i = i+1;
					str = template.get(i);
					str = str.replaceAll("<fileName>", parser.file_Name);
					template.add(str);
					if(str.startsWith("%") && !str.equals("%%") && !str.contains("%newfile"))
						index = index+1;
					else if(str.equals("%%"))
						index = index-1;
				}
				_template.remove(_template.size()-1);
				
				Enumeration e = getElements();
		        while( e.hasMoreElements() )
		            ( (IdlSymbol)e.nextElement() ).print( ps , _template , "typedef" );
			}
			else if(str.startsWith("%exception"))
        	{
        		int index = 1;
        		Vector<String> _template = new Vector<String>();
        		while(!(str.equals("%%") && index == 0))
        		{
        			i = i+1;
					str = template.get(i);
					str = str.replaceAll("<fileName>", parser.file_Name);
					_template.add(str);
					if(str.startsWith("%") && !str.equals("%%"))
						index = index+1;
					else if(str.equals("%%"))
						index = index-1;
        		}
        		_template.remove(_template.size()-1);
        		
        		Enumeration e = getElements();
        		while( e.hasMoreElements() )
		            ( (IdlSymbol)e.nextElement() ).print( ps , _template , "exception" );
        	}
        	else if(str.startsWith("%union"))
        	{
        		int index = 1;
        		Vector<String> _template = new Vector<String>();
        		while(!(str.equals("%%") && index == 0))
        		{
        			i = i+1;
					str = template.get(i);
					str = str.replaceAll("<fileName>", parser.file_Name);
					_template.add(str);
					if(str.startsWith("%") && !str.equals("%%"))
						index = index+1;
					else if(str.equals("%%"))
						index = index-1;
        		}
        		_template.remove(_template.size()-1);
        		
        		Enumeration e = getElements();
        		while( e.hasMoreElements() )
		            ( (IdlSymbol)e.nextElement() ).print( ps , _template , "union" );
        	}
        	else if(str.startsWith("%enum"))
        	{
        		int index = 1;
        		Vector<String> _template = new Vector<String>();
        		while(!(str.equals("%%") && index == 0))
        		{
        			i = i+1;
					str = template.get(i);
					str = str.replaceAll("<fileName>", parser.file_Name);
					_template.add(str);
					if(str.startsWith("%") && !str.equals("%%"))
						index = index+1;
					else if(str.equals("%%"))
						index = index-1;
        		}
        		_template.remove(_template.size()-1);
        		
        		Enumeration e = getElements();
        		while( e.hasMoreElements() )
		            ( (IdlSymbol)e.nextElement() ).print( ps , _template , "enum" );
        	}
        	else if(str.startsWith("%module"))
        	{
        		int index = 1;
        		Vector<String> _template = new Vector<String>();
        		while(!(str.equals("%%") && index == 0))
        		{
        			i = i+1;
					str = template.get(i);
					str = str.replaceAll("<fileName>", parser.file_Name);
					_template.add(str);
					if(str.startsWith("%") && !str.equals("%%"))
						index = index+1;
					else if(str.equals("%%"))
						index = index-1;
        		}
        		_template.remove(_template.size()-1);
        		
        		Enumeration e = getElements();
        		while( e.hasMoreElements() )
		            ( (IdlSymbol)e.nextElement() ).print( ps , _template , "module" );
        	}
			else
			{
				ps.println(str);
			}
    		i = i+1;
		}
    	if(ps != null && judge)
        	ps.close();
    }

    private PrintWriter openOutput(String tmp) {
    	try
        {
            final File f = new File(parser.out_dir+"\\"+tmp);
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
                                        + tmp + " (" + e + ")");
        }
	}

	/**
     */

    public void accept( IDLTreeVisitor visitor )
    {
        visitor.visitDefinitions( this );
    }
}
