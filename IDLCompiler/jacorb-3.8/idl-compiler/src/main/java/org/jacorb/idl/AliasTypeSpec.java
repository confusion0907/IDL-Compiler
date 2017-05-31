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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

/**
 * @author Gerald Brose
 */

public class AliasTypeSpec
    extends TypeSpec
{
    /** the type for which this is an alias */
    public TypeSpec originalType;
    private boolean written;
    private boolean originalTypeWasScopedName = false;

    /**
     * Class constructor,
     * @param ts - the TypeSpec for which to create a new alias
     */

    public AliasTypeSpec(TypeSpec ts )
    {
        super(IdlSymbol.new_num());
        originalType = ts;
    }

    public Object clone()
    {
        AliasTypeSpec alias =
            new AliasTypeSpec((TypeSpec)type_spec.clone());
        alias.name = name;
        alias.pack_name = pack_name;
        return alias;
    }

    public String full_name()
    {
        if (pack_name.length() > 0)
        {
            String fullName =
                ScopedName.unPseudoName(pack_name + "." + name);

            return getFullName(fullName);
        }
        else
        {
            return ScopedName.unPseudoName(name);
        }
    }


    /**
     * @return the type name of this alias, which is the name of the
     * original type
     */

    public String typeName()
    {
        return originalType.typeName();
    }

    public TypeSpec typeSpec()
    {
        return this;
    }

    /**
     * @return the original type for which this is an alias
     */

    public TypeSpec originalType()
    {
        if (originalType instanceof AliasTypeSpec)
        {
            return (((AliasTypeSpec)originalType).originalType ());
        }
        return originalType;
    }

    public void setPackage(String s)
    {
        if (pack_name.length() > 0)
            pack_name = s + "." + pack_name;
        else
            pack_name = s;
        pack_name = parser.pack_replace(pack_name);
    }

    public void setEnclosingSymbol(IdlSymbol s)
    {
        if (enclosing_symbol != null && enclosing_symbol != s)
            throw new RuntimeException("Compiler Error: trying to reassign container for " + name);
        enclosing_symbol = s;
    }

    /**
     * @return true if this is a basic type
     */

    public boolean basic()
    {
        return false;
    }

    /**
     * Perform the parsing phase, must be called before code
     * generation
     */

    public void parse()
    {
        if (originalType instanceof TemplateTypeSpec)
        {
            ((TemplateTypeSpec)originalType).markTypeDefd();
        }

        if (originalType instanceof ConstrTypeSpec ||
            originalType instanceof FixedPointType ||
            originalType instanceof SequenceType ||
            originalType instanceof ArrayTypeSpec)
        {
            originalType.parse();
            if (originalType.typeName().indexOf('.') < 0)
            {
                String tName = null;
                if (originalType instanceof VectorType)
                {
                    tName =
                        originalType.typeName().substring(0,
                              originalType.typeName().indexOf('['));
                }
                else
                {
                    tName = originalType.typeName();
                }

                addImportedName(tName);
            }
        }

        if (originalType instanceof ScopedName)
        {
            if (parser.logger.isLoggable(Level.ALL))
                parser.logger.log(Level.ALL, " Alias " + name +
                 " has scoped name orig Type : " +
                 ((ScopedName)originalType).toString());

            originalType = ((ScopedName)originalType).resolvedTypeSpec();
            originalTypeWasScopedName = true;

            if (originalType instanceof AliasTypeSpec)
                addImportedAlias(originalType.full_name());
            else
                addImportedName(originalType.typeName());
        }
    }



    public String toString()
    {
        return originalType.toString();
    }


    /**
     * @return a string for an expression of type TypeCode that describes this type
     * Note that this is the TypeSpec for the alias type and is not unwound to
     * the original type.
     */

    @SuppressWarnings("rawtypes")
	public String getTypeCodeExpression()
    {
        return getTypeCodeExpression(new HashSet());
    }

    public String getTypeCodeExpression(@SuppressWarnings("rawtypes") Set knownTypes)
    {
        return "org.omg.CORBA.ORB.init().create_alias_tc(" +
               full_name() + "Helper.id(), \"" + name + "\"," +
               originalType.typeSpec().getTypeCodeExpression(knownTypes) + ")";
    }

    public String className()
    {
        String fullName = full_name();
        String cName;
        if (fullName.indexOf('.') > 0)
        {
            pack_name = fullName.substring(0, fullName.lastIndexOf('.'));
            cName = fullName.substring(fullName.lastIndexOf('.') + 1);
        }
        else
        {
            pack_name = "";
            cName = fullName;
        }
        return cName;

    }

    /**
     * Code generation, generate holder and helper classes. Holder classes
     * are only generated for array and sequence types.
     */

    public void print(PrintWriter ps , Vector<String> template)
    {
    	//FIXME
        setPrintPhaseNames();
        boolean judge = false;

        // no code generation for included definitions
        if (included && !generateIncluded())
            return;

        if (!written)
        {
            // guard against recursive entries, which can happen due to
            // containments, e.g., an alias within an interface that refers
            // back to the interface
            //written = true;

            if (!(originalType instanceof FixedPointType) &&
			    !(originalType.typeSpec() instanceof ArrayTypeSpec) &&
			    !(originalType.typeSpec() instanceof StringType) &&
			    !(originalType.typeSpec() instanceof SequenceType) &&
			    ! originalTypeWasScopedName &&
			    !(originalType instanceof ConstrTypeSpec &&
			       ((ConstrTypeSpec)originalType).declaration() instanceof Interface )
			   )
			{
			    // only print local type definitions, not just
			    // scoped names (references to other defs), which would
			    // lead to loops!
			    //originalType.print(ps);
			}

			if(originalType.typeSpec() instanceof ArrayTypeSpec)
			{
			    //originalType.type_spec.print(ps);
			}

			String className = className();
			
			if(template.get(0).contains(":local"))
			{
				if (!(originalType instanceof FixedPointType) &&
						!(originalType.typeSpec() instanceof ArrayTypeSpec) &&
						!(originalType.typeSpec() instanceof StringType) &&
						!(originalType.typeSpec() instanceof SequenceType) &&
						! originalTypeWasScopedName &&
						!(originalType instanceof ConstrTypeSpec &&
								((ConstrTypeSpec)originalType).declaration() instanceof Interface )
						)
				{
					for(int i = 1 ; i < template.size() ; i++)
					{
						if(template.get(i).startsWith("%newfile"))
			        	{
			        		judge = true;
			        		String tmp = template.get(i).replaceAll("<typedefName>", className);
			        		PrintWriter _ps;
			        		
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
						else if(ps == null)
							throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
						else
						{
							String tmp = template.get(i).replaceAll("<typedefType>", originalType.typeName());
							tmp = tmp.replaceAll("<typedefName>", className);
							ps.println(tmp);
						}
					}
				}
			}
			else if(template.get(0).contains(":sequence"))
			{
				if(originalType instanceof SequenceType)
				{
					for(int i = 1 ; i < template.size() ; i++)
					{
						if(template.get(i).startsWith("%newfile"))
			        	{
			        		judge = true;
			        		String tmp = template.get(i).replaceAll("<typedefName>", className);
			        		PrintWriter _ps;
			        		
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
						else if(ps == null)
							throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
						else if(template.get(i).startsWith("%length"))
						{
							i = i+1;
							while(!template.get(i).equals("%%"))
							{
								if(originalType.getSequenceLength() != 0)
								{
									String tmp = template.get(i).replaceAll("<typedefType>", originalType.typeName());
									tmp = tmp.replaceAll("<typedefName>", className);
									tmp = tmp.replaceAll("<sequenceLength>", Integer.toString(originalType.getSequenceLength()));
									ps.println(tmp);
								}
								i = i+1;
							}
							i = i+1;
						}
						else
						{
							String tmp = template.get(i).replaceAll("<typedefType>", originalType.typeName());
							tmp = tmp.replaceAll("<typedefName>", className);
							tmp = tmp.replaceAll("<sequenceLength>", Integer.toString(originalType.getSequenceLength()));
							ps.println(tmp);
						}
					}
				}
			}
			else if(template.get(0).contains(":fixed"))
			{
				if(originalType instanceof FixedPointType)
				{
					for(int i = 1 ; i < template.size() ; i++)
					{
						if(template.get(i).startsWith("%newfile"))
			        	{
			        		judge = true;
			        		String tmp = template.get(i).replaceAll("<typedefName>", className);
			        		PrintWriter _ps;
			        		
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
						else if(ps == null)
							throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
						else
						{
							String tmp = template.get(i).replaceAll("<typedefType>", originalType.typeName());
							tmp = tmp.replaceAll("<typedefName>", className);
							tmp = tmp.replaceAll("<digitsNumber>", Integer.toString(originalType.typeSpec().getDigits()));
							tmp = tmp.replaceAll("<scaleNumber>", Integer.toString(originalType.typeSpec().getScale()));
							ps.println(tmp);
						}
					}
				}
			}
			else if(template.get(0).contains(":array"))
			{
				if(originalType.typeSpec() instanceof ArrayTypeSpec)
				{
					for(int i = 1 ; i < template.size() ; i++)
					{
						if(template.get(i).startsWith("%newfile"))
			        	{
			        		judge = true;
			        		String tmp = template.get(i).replaceAll("<typedefName>", className);
			        		PrintWriter _ps;
			        		
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
						/*
						else if(template.get(i).startsWith("%arraytypedef"))
						{
							i = i+1;
							Vector<String> _template = new Vector<String>();
							while(!template.get(i).equals("%%"))
							{
								String tmp = template.get(i).replaceAll("<typedefType>", originalType.typeName());
								tmp = tmp.replaceAll("<typedefName>", className);
								_template.add(tmp);
								i = i+1;
							}
							originalType.type_spec.print(ps, _template);
							i = i+1;
						}
						*/
						else if(ps == null)
							throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
						else
						{
							String tmp = template.get(i).replaceAll("<typedefType>", originalType.typeName());
							tmp = tmp.replaceAll("<typedefName>", className);
							ps.println(tmp);
						}
					}
				}
			}
			else
			{
				for(int i = 1 ; i < template.size() ; i++)
				{
					if(template.get(i).startsWith("%newfile"))
		        	{
		        		judge = true;
		        		String tmp = template.get(i).replaceAll("<typedefName>", className);
		        		PrintWriter _ps;
		        		
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
					else if(ps == null)
						throw new RuntimeException ("模板代码有误,文件已被关闭 line"+"("+(Spec.line-template.size()+i+1)+")");
					else
					{
						String tmp = template.get(i).replaceAll("<typedefType>", originalType.typeName());
						tmp = tmp.replaceAll("<typedefName>", className);
						if(originalType.typeSpec() instanceof SequenceType)
							tmp = tmp.replaceAll("<sequenceLength>", Integer.toString(originalType.getSequenceLength()));
						else
							tmp = tmp.replaceAll("<sequenceLength>", "");
						ps.println(tmp);
					}
				}
			}
		}
        
        if(ps != null && judge)
        	ps.close();
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

    public String printReadStatement(String varname, String streamname)
    {
        if (doUnwind())
        {
            return originalType.printReadStatement(varname, streamname);
        }

        return varname + " = " + full_name() + "Helper.read(" + streamname + ");";
    }

    public String printReadExpression(String streamname)
    {
        if (doUnwind())
        {
            return originalType.printReadExpression(streamname);
        }
        return full_name() + "Helper.read(" + streamname + ")";
    }

    public String printWriteStatement(String var_name, String streamname)
    {
        if (doUnwind())
        {
            return originalType.printWriteStatement(var_name, streamname);
        }
        return full_name() + "Helper.write(" + streamname + "," + var_name + ");";
    }


    /**
     * @return true iff the original type is such that the alias should
     * be unwound to it, either anothetr alias, a constructed type (e.g a struct),
     * an any, a basic type (long, short, etc.)
     */

    private boolean doUnwind()
    {
        return
            (
             originalType.basic() &&
             (
              !(originalType instanceof TemplateTypeSpec)
              || originalType instanceof StringType
             )
            )
        || originalType instanceof AliasTypeSpec
        || originalType instanceof ConstrTypeSpec
        || originalType instanceof AnyType
        ;
    }


    public String holderName()
    {
        if (doUnwind())
        {
            return originalType.holderName();
        }

        return full_name() + "Holder";
    }

    public void printInsertIntoAny(PrintWriter ps,
                                   String anyname,
                                   String varname)
    {
        String helpername = className() + "Helper";
        ps.println("\t\t" + pack_name + "." + helpername + ".insert(" + anyname + ", " + varname + " );");
    }

    public void printExtractResult(PrintWriter ps,
                                   String resultname,
                                   String anyname,
                                   String resulttype)
    {
        String helpername = className() + "Helper";
        ps.println("\t\t" + resultname + " = " + pack_name + "." + helpername + ".extract(" + anyname + ");");
    }

    public void accept(IDLTreeVisitor visitor)
    {
        visitor.visitAlias(this);
    }



}
