/*
 *        JacORB  - a free Java ORB
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
import java.util.Vector;

/**
 * Declaration of typeprefix
 * 
 * @author Alexander Birchenko
 */

public class TypePrefixDecl extends Declaration
{
    public ScopedName scopedname;
    public String prefix;

    public TypePrefixDecl (int num)
    {
        super(num);
    }

    public void print( PrintWriter ps , Vector<String> template )
    {
    	boolean judge = false;
    	for(int i = 0 ; i < template.size() ; i++)
    	{
    		if(template.get(i).startsWith("%newfile"))
        	{
        		judge = true;
        		String tmp = template.get(i).replaceAll("<scopeName>", scopedname.name);
        		tmp = tmp.replaceAll("<prefixString>", prefix);
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
    		else
    		{
    			String tmp = template.get(i).replaceAll("<scopeName>", scopedname.name);
        		tmp = tmp.replaceAll("<prefixString>", prefix);
        		ps.println(tmp);
    		}
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

    public void parse() throws ParseException
    {
        String rname = this.scopedname.typeName;

        if(!NameTable.isDefined(rname, IDLTypes.MODULE))
        {
            //            Compose full module name if specified briefly.
            //            For example:
            //
            //            module A1 {
            //                module B {
            //                    module A2 {
            //                        module B {
            //                            typeprefix B "com.something";
            //                        }
            //                    }
            //                }
            //            }
            //
            //            "typeprefix B" will be "typeprefix A1.B"

            java.util.StringTokenizer strtok = new java.util.StringTokenizer( this.pack_name, "." );
            String nameScopes[] = new String[ strtok.countTokens() ];
            boolean isModuleFinded = false;

            int count = 0;
            for( ; strtok.hasMoreTokens(); count++ )
            {
                String name = strtok.nextToken();
                nameScopes[count] = name;

                if(name.equals(rname))
                {
                    isModuleFinded = true;
                    break;
                }
            }

            if(isModuleFinded)
            {
                StringBuffer fullName = new StringBuffer();
                for( int i = 0; i < count+1; ++i )
                {
                    fullName.append( nameScopes[ i ] );
                    fullName.append( "." );
                }

                rname = fullName.substring(0, fullName.length()-1);
            }
            else
            {
                parser.error("Module name " + rname + " undefined", scopedname.token);
            }
        }

        //overrides previously defined prefix for this module without check
        TypePrefixes.define(rname, this.prefix);
    }
}
