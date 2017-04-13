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
    @SuppressWarnings("rawtypes")
	public Vector definitions;
    public static int line = 0;

    @SuppressWarnings("rawtypes")
	public Spec( int num )
    {
        super( num );
        definitions = new Vector();
    }

    @SuppressWarnings("rawtypes")
	public void parse()
    {
        Enumeration e = definitions.elements();
        for( ; e.hasMoreElements(); )
            ( (IdlSymbol)e.nextElement() ).parse();
    }

    @SuppressWarnings("rawtypes")
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

	@SuppressWarnings("rawtypes")
	public void print( PrintWriter ps )
    {
		File file = new File("typeMapping.type");
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
			long ptr = 0;
			String str = "";
			Vector<String> type = new Vector<String>();
			int index = 0;
			while (ptr < file.length() && index < 19) 
			{
				str = raf.readLine();
				ptr = raf.getFilePointer();
				type.add(new String(str));
				++index;
			}
			ShortType.setTypeName(type.get(0));
			ShortType.setTypeName_unsigned(type.get(1));
			LongType.setTypeName(type.get(2));
			LongType.setTypeName_unsigned(type.get(3));
			LongLongType.setTypeName(type.get(4));
			LongLongType.setTypeName_unsigned(type.get(5));
			OctetType.setTypeName(type.get(6));
			FloatType.setTypeName(type.get(7));
			DoubleType.setTypeName(type.get(8));
			CharType.setTypeName(type.get(9));
			CharType.setTypeName_wide(type.get(10));
			StringType.setTypeName(type.get(11));
			StringType.setTypeName_wide(type.get(12));
			BooleanType.setTypeName(type.get(13));
			AnyType.setTypeName(type.get(14));
			ObjectTypeSpec.setTypeName(type.get(15));
			ValueBase.setTypeName(type.get(16));
			FixedPointType.setTypeName(type.get(17));
			VoidTypeSpec.setTypeName(type.get(18));
			raf.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    	File f = new File("template.txt");
    	try {
			@SuppressWarnings("resource")
			RandomAccessFile raf = new RandomAccessFile(f, "r");
			
			long ptr = 0;
			String str = "";
			
			PrintWriter _ps = null;
			while (ptr < f.length()) 
			{
				str = raf.readLine();
				ptr = raf.getFilePointer();
				line = line+1;
				
				str = str.replaceAll("<fileName>", parser.file_Name);
				
				if(str.startsWith("%newfile"))
				{
					if(_ps != null)
						_ps.close();
					else
					{
						String tmp = str.substring(9);
						try{
							_ps = openOutput(tmp);
							if(_ps == null)
								throw new Exception();
						}catch(Exception e){
							throw new RuntimeException ("文件"+tmp+"已存在,代码生成失败");
						}
					}
				}
				else if(str.startsWith("%interface"))
				{
					int index = 1;
					Vector<String> template = new Vector<String>();
					String temp = "";
					if(str.contains(":abstract"))
						temp = "abstract";
					else if(str.contains(":local"))
						temp = "local";
					else if(str.contains(":pseudo"))
						temp = "pseudo";
					else if(str.contains(":normal"))
						temp = "normal";
					else
						temp = "all";
					
					template.add(temp);
					
					while(!(str.equals("%%") && index == 0))
					{
						str = raf.readLine();
						ptr = raf.getFilePointer();
						line = line+1;
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
						line = line+1;
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
						line = line+1;
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
						line = line+1;
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
						line = line+1;
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
						line = line+1;
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
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "enum" );
            	}
            	else if(str.startsWith("%module"))
            	{
            		int index = 1;
            		Vector<String> template = new Vector<String>();
            		while(!(str.equals("%%") && index == 0))
            		{
            			str = raf.readLine();
						ptr = raf.getFilePointer();
						line = line+1;
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
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "module" );
            	}
            	else if(str.startsWith("%valuetype"))
            	{
            		String type = "";
            		if(str.contains(":box"))
            			type = "box";
            		else if(str.contains(":abstract"))
            			type = "abstract";
            		else if(str.contains(":normal"))
            			type = "normal";
            		else if(str.contains(":nocustom"))
            			type = "nocustom";
            		else if(str.contains(":custom"))
            			type = "custom";
            		else
            			type = "all";
            		
            		Vector<String> template = new Vector<String>();
            		template.add(type);
            		int index = 1;
            		while(!(str.equals("%%") && index == 0))
            		{
            			str = raf.readLine();
						ptr = raf.getFilePointer();
						line = line+1;
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
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "valuetype" );
            	}
            	else if(str.startsWith("%typeprefix"))
            	{
            		int index = 1;
            		Vector<String> template = new Vector<String>();
            		while(!(str.equals("%%") && index == 0))
            		{
            			str = raf.readLine();
						ptr = raf.getFilePointer();
						line = line+1;
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
			            ( (IdlSymbol)e.nextElement() ).print( _ps , template , "typeprefix" );
            	}
            	else if(_ps == null)
					throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+line+")");
				else
					_ps.println(str);
			}
			if(_ps != null)
				_ps.close();
			raf.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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
}