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

import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author Gerald Brose
 */

public class InitDecl
    extends Declaration
{
    @SuppressWarnings("rawtypes")
	public Vector paramDecls;
    public IdlSymbol myValue;

    /** new in CORBA 3.0, factory methods may raise exceptions */
    public RaisesExpr raisesExpr;

    @SuppressWarnings("rawtypes")
	public InitDecl( int num )
    {
        super( num );
        paramDecls = new Vector();
    }

    @SuppressWarnings("rawtypes")
	public void setPackage( String s )
    {
        s = parser.pack_replace( s );

        if( pack_name.length() > 0 )
            pack_name = s + "." + pack_name;
        else
            pack_name = s;

        for( Enumeration e = paramDecls.elements();
             e.hasMoreElements();
             ( (ParamDecl)e.nextElement() ).setPackage( s )
                )
            ;
        raisesExpr.setPackage( s );
    }

    public void setEnclosingSymbol( IdlSymbol s )
    {
        if( enclosing_symbol != null && enclosing_symbol != s )
            throw new RuntimeException( "Compiler Error: trying to reassign container for "
                    + name );
        enclosing_symbol = s;
        raisesExpr.setEnclosingSymbol( s );
    }

    @SuppressWarnings("rawtypes")
	public void parse()
    {
        myValue = enclosing_symbol;

        try
        {
            NameTable.define( full_name(), IDLTypes.FACTORY );
        }
        catch( NameAlreadyDefined nad )
        {
            parser.error( "Factory " + full_name() + " already defined", token );
        }

        for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
        {
            ParamDecl param = (ParamDecl)e.nextElement();
            param.parse();
            try
            {
                NameTable.define( full_name() + "." +
                        param.simple_declarator.name(),
                        IDLTypes.ARGUMENT );
            }
            catch( NameAlreadyDefined nad )
            {
                parser.error( "Argument " + param.simple_declarator.name() +
                        " already defined in operation " + full_name(),
                        token );
            }
        }
        raisesExpr.parse();
    }

    /**
     * Prints the method's signature, for inclusion in the
     * factory interface.
     */
    @SuppressWarnings("rawtypes")
	public void print( PrintWriter ps, Vector<String> template )
    {
    	//FIXME
        int i = 0 ;
    	while(i < template.size())
    	{
    		if(template.get(i).startsWith("%raises"))
        	{
        		i = i+1;
        		Vector<String> _template = new Vector<String>();
        		while(!template.get(i).equals("%%"))
        		{
        			String tmp = template.get(i).replaceAll("<factoryName>", name);
            		tmp = tmp.replaceAll("<paramList>", paramList());
            		tmp = tmp.replaceAll("<paramTypeList>", paramTypeList());
            		tmp = tmp.replaceAll("<paramNameList>", paramNameList());
            		tmp = tmp.replaceAll("<raisesList>", raisesExpr.getRaisesList());
            		if(template.get(i).contains("HASH("))
                	{
                		int begin=-1,end=-1;
                		for(int j = 0 ; j < tmp.length() ; j++)
                		{
                			if(tmp.charAt(j)=='H' && tmp.charAt(j+1)=='A' && tmp.charAt(j+2)=='S' && tmp.charAt(j+3)=='H' && tmp.charAt(j+4)=='(')
                			{
                				begin = j;
                				break;
                			}
                		}
                		String hash = tmp.substring(begin);
                		end = hash.indexOf(')');
                		hash = hash.substring(0, end+1);
                		String arg = hash.substring(6,hash.length()-2);
                		String result = toMD5(arg);
                		if(result.equals(arg))
                			throw new RuntimeException ("加密"+arg+"出错");
                		tmp = tmp.replace(hash, result);
                	}
        			_template.add(tmp);
        			i = i+1;
        		}
        		raisesExpr.print(ps,_template);
        		i = i+1;
        	}
        	else if(template.get(i).startsWith("%param"))
        	{
        		i = i+1;
    			while(!template.get(i).equals("%%"))
    			{
    				for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
    				{
    					ParamDecl p = (ParamDecl)e.nextElement();
    					String tmp = template.get(i).replaceAll("<factoryName>", name);
    					tmp = tmp.replaceAll("<paramType>", p.paramTypeSpec.typeName());
    					tmp = tmp.replaceAll("<paramName>", p.simple_declarator.toString());
    					tmp = tmp.replaceAll("<paramList>", paramList());
    					tmp = tmp.replaceAll("<paramTypeList>", paramTypeList());
    					tmp = tmp.replaceAll("<paramNameList>", paramNameList());
    					tmp = tmp.replaceAll("<raisesList>", raisesExpr.getRaisesList());
    					if(template.get(i).contains("HASH("))
    					{
    						int begin=-1,end=-1;
    						for(int j = 0 ; j < tmp.length() ; j++)
    						{
    							if(tmp.charAt(j)=='H' && tmp.charAt(j+1)=='A' && tmp.charAt(j+2)=='S' && tmp.charAt(j+3)=='H' && tmp.charAt(j+4)=='(')
    							{
    								begin = j;
    								break;
    								}
    						}
    						String hash = tmp.substring(begin);
    						end = hash.indexOf(')');
    						hash = hash.substring(0, end+1);
    						String arg = hash.substring(6,hash.length()-2);
    						String result = toMD5(arg);
    						if(result.equals(arg))
    							throw new RuntimeException ("加密"+arg+"出错");
    						tmp = tmp.replace(hash, result);
    					}
    					ps.println(tmp);
    				}
    				i = i+1;
    			}
    			i = i+1;
        	}
        	else
        	{
        		String tmp = template.get(i).replaceAll("<factoryName>", name);
        		tmp = tmp.replaceAll("<paramList>", paramList());
        		tmp = tmp.replaceAll("<paramTypeList>", paramTypeList());
        		tmp = tmp.replaceAll("<paramNameList>", paramNameList());
        		tmp = tmp.replaceAll("<raisesList>", raisesExpr.getRaisesList());
        		if(template.get(i).contains("HASH("))
            	{
            		int begin=-1,end=-1;
            		for(int j = 0 ; j < tmp.length() ; j++)
            		{
            			if(tmp.charAt(j)=='H' && tmp.charAt(j+1)=='A' && tmp.charAt(j+2)=='S' && tmp.charAt(j+3)=='H' && tmp.charAt(j+4)=='(')
            			{
            				begin = j;
            				break;
            			}
            		}
            		String hash = tmp.substring(begin);
            		end = hash.indexOf(')');
            		hash = hash.substring(0, end+1);
            		String arg = hash.substring(6,hash.length()-2);
            		String result = toMD5(arg);
            		if(result.equals(arg))
            			throw new RuntimeException ("加密"+arg+"出错");
            		tmp = tmp.replace(hash, result);
            	}
        		ps.println(tmp);
        		i = i+1;
        	}
    	}
        //raisesExpr.print( ps );
    }

    public String name()
    {
        return name;
    }

    public String opName()
    {
        return name();
    }
    
    @SuppressWarnings("rawtypes")
	private String paramList()
    {
    	String result = "";
    	for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
    	{
    		ParamDecl p = (ParamDecl)e.nextElement();
    		result = result+","+p.paramTypeSpec.typeName()+" "+p.simple_declarator.toString();
    	}
    	if(!result.equals(""))
    		result = result.substring(1);
    	return result;
    }
    
    @SuppressWarnings("rawtypes")
	private String paramTypeList()
    {
    	String result = "";
    	for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
    	{
    		ParamDecl p = (ParamDecl)e.nextElement();
    		result = result+","+p.paramTypeSpec.typeName();
    	}
    	if(!result.equals(""))
    		result = result.substring(1);
    	return result;
    }
    
    @SuppressWarnings("rawtypes")
	private String paramNameList()
    {
    	String result = "";
    	for( Enumeration e = paramDecls.elements(); e.hasMoreElements(); )
    	{
    		ParamDecl p = (ParamDecl)e.nextElement();
    		result = result+","+p.simple_declarator.toString();
    	}
    	if(!result.equals(""))
    		result = result.substring(1);
    	return result;
    }
    
    public String toMD5(String plainText)
    {
    	try 
    	{
    		MessageDigest md = MessageDigest.getInstance("MD5"); 
    		md.update(plainText.getBytes());
    		byte b[] = md.digest();
    		int i;
    		StringBuffer buf = new StringBuffer("");
    		for (int offset = 0; offset < b.length; offset++) 
    		{
    			i = b[offset];
    			if (i < 0)
    				i += 256;
    			if (i < 16)
    				buf.append("0");
    			buf.append(Integer.toHexString(i));
    		}
    		return buf.toString();
    	} 
    	catch (Exception e) {
    		e.printStackTrace();
    	}
		return plainText;
    }
}
