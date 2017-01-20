package org.jacorb.idl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
import java.io.RandomAccessFile;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Gerald Brose
 */


public class Spec
    extends IdlSymbol
{
    public Vector definitions;

    public Spec( int num )
    {
        super( num );
        definitions = new Vector();
    }

    public void parse()
    {
        Enumeration e = definitions.elements();
        for( ; e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).parse();
    }

    public void setPackage( String s )
    {
        s = parser.pack_replace( s );
        Enumeration e = definitions.elements();
        while( e.hasMoreElements() )
        {
            IdlSymbol i = (IdlSymbol)e.nextElement();
            i.setPackage( s );
        }
    }

	public void print( PrintWriter ps )
    {
    	File f = new File("template.txt");
    	try {
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			
			long ptr = 0;
			String str = "";
			
			PrintWriter _ps = null;
			while (ptr < f.length()) 
			{
				str = raf.readLine();
				ptr = raf.getFilePointer();
				
				str = str.replaceAll("<fileName>", parser.file_Name);
				
				if(str.startsWith("%newfile"))
				{
					if(_ps != null)
						_ps.close();
					else
					{
						String tmp = str.substring(9);
						_ps = openOutput(tmp);
						if(_ps == null)
						{
							System.out.println("文件"+tmp+"已存在,代码生成失败");
							return;
						}
					}
				}
				else if(str.startsWith("%interface"))
				{
					int index = 1;
					Vector<String> template = new Vector<String>();
					while(!(str.equals("%%") && index == 0))
					{
						str = raf.readLine();
						ptr = raf.getFilePointer();
						str = str.replaceAll("<fileName>", parser.file_Name);
						template.add(str);
						if(str.startsWith("%") && !str.equals("%%") && !str.contains("%newfile"))
							index = index+1;
						else if(str.equals("%%"))
							index = index-1;
					}
					template.remove(template.size()-1);
					
					Enumeration e = definitions.elements();
			        while( e.hasMoreElements() )
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "interface" );
				}
				else if(str.startsWith("%struct"))
				{
					int index = 1;
					Vector<String> template = new Vector<String>();
					while(!(str.equals("%%") && index == 0))
					{
						str = raf.readLine();
						ptr = raf.getFilePointer();
						str = str.replaceAll("<fileName>", parser.file_Name);
						template.add(str);
						if(str.startsWith("%") && !str.equals("%%") && !str.contains("%newfile"))
							index = index+1;
						else if(str.equals("%%"))
							index = index-1;
					}
					template.remove(template.size()-1);
					
					Enumeration e = definitions.elements();
			        while( e.hasMoreElements() )
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "struct" );
				}
				else if(str.startsWith("%typedef"))
				{
					int index = 1;
					Vector<String> template = new Vector<String>();
					template.add(str);
					while(!(str.equals("%%") && index == 0))
					{
						str = raf.readLine();
						ptr = raf.getFilePointer();
						str = str.replaceAll("<fileName>", parser.file_Name);
						template.add(str);
						if(str.startsWith("%") && !str.equals("%%") && !str.contains("%newfile"))
							index = index+1;
						else if(str.equals("%%"))
							index = index-1;
					}
					template.remove(template.size()-1);
					
					Enumeration e = definitions.elements();
			        while( e.hasMoreElements() )
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "typedef" );
				}
				else if(str.startsWith("%exception"))
            	{
            		int index = 1;
            		Vector<String> template = new Vector<String>();
            		while(!(str.equals("%%") && index == 0))
            		{
            			str = raf.readLine();
						ptr = raf.getFilePointer();
						str = str.replaceAll("<fileName>", parser.file_Name);
						template.add(str);
						if(str.startsWith("%") && !str.equals("%%"))
							index = index+1;
						else if(str.equals("%%"))
							index = index-1;
            		}
            		template.remove(template.size()-1);
            		
            		Enumeration e = definitions.elements();
            		while( e.hasMoreElements() )
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "exception" );
            	}
            	else if(str.startsWith("%union"))
            	{
            		int index = 1;
            		Vector<String> template = new Vector<String>();
            		while(!(str.equals("%%") && index == 0))
            		{
            			str = raf.readLine();
						ptr = raf.getFilePointer();
						str = str.replaceAll("<fileName>", parser.file_Name);
						template.add(str);
						if(str.startsWith("%") && !str.equals("%%"))
							index = index+1;
						else if(str.equals("%%"))
							index = index-1;
            		}
            		template.remove(template.size()-1);
            		
            		Enumeration e = definitions.elements();
            		while( e.hasMoreElements() )
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "union" );
            	}
            	else if(str.startsWith("%enum"))
            	{
            		int index = 1;
            		Vector<String> template = new Vector<String>();
            		while(!(str.equals("%%") && index == 0))
            		{
            			str = raf.readLine();
						ptr = raf.getFilePointer();
						str = str.replaceAll("<fileName>", parser.file_Name);
						template.add(str);
						if(str.startsWith("%") && !str.equals("%%"))
							index = index+1;
						else if(str.equals("%%"))
							index = index-1;
            		}
            		template.remove(template.size()-1);
            		
            		Enumeration e = definitions.elements();
            		while( e.hasMoreElements() )
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "enum" );
            	}
				else
				{
					_ps.println(str);
				}
			}
			if(_ps != null)
				_ps.close();
			raf.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	
    	/*
        Enumeration e = definitions.elements();
        while( e.hasMoreElements() )
            ( (IdlSymbol)e.nextElement() ).print( ps );
            */
    }

    /**
     */ 

    public void accept( IDLTreeVisitor visitor )
    {
        visitor.visitSpec( this );
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
}
























