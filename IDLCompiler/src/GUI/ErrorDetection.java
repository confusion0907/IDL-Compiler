package GUI;

import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import javax.swing.JTextPane;

public class ErrorDetection {
	private static TreeNode errorTree;
	private static ErrorDetection errorDetection = null;
	private static Set<String> labelwords;
	private static Set<String> keywords;
	
	private ErrorDetection()
	{
		errorTreeInit();
	}
	
	private void errorTreeInit()
	{
		errorTree = new TreeNode("%spec");
		errorTree.addKeywords("<fileName>");
		TreeNode interface_all = new TreeNode("%interface");
		interface_all.addKeywords("<interfaceName>");
		interface_all.addKeywords("<scopeList>");
		TreeNode interface_abstract = new TreeNode("%interface:abstract");
		interface_abstract.addKeywords("<interfaceName>");
		interface_abstract.addKeywords("<scopeList>");
		TreeNode interface_pseudo = new TreeNode("%interface:pseudo");
		interface_pseudo.addKeywords("<interfaceName>");
		interface_pseudo.addKeywords("<scopeList>");
		TreeNode interface_local = new TreeNode("%interface:local");
		interface_local.addKeywords("<interfaceName>");
		interface_local.addKeywords("<scopeList>");
		TreeNode interface_normal = new TreeNode("%interface:normal");
		interface_normal.addKeywords("<interfaceName>");
		interface_normal.addKeywords("<scopeList>");
		errorTree.addChildren(interface_all);
		errorTree.addChildren(interface_abstract);
		errorTree.addChildren(interface_pseudo);
		errorTree.addChildren(interface_local);
		errorTree.addChildren(interface_normal);
		TreeNode struct = new TreeNode("%struct");
		struct.addKeywords("<structName>");
		TreeNode member = new TreeNode("%member");
		member.addKeywords("<memberType>");
		member.addKeywords("<memberName>");
		struct.addChildren(member);
		TreeNode member_string = new TreeNode("%member:string");
		member_string.addKeywords("<memberType>");
		member_string.addKeywords("<memberName>");
		struct.addChildren(member_string);
		errorTree.addChildren(struct);
		TreeNode typedef_all = new TreeNode("%typedef");
		typedef_all.addKeywords("<typedefType>");
		typedef_all.addKeywords("<typedefName>");
		TreeNode typedef_local = new TreeNode("%typedef:local");
		typedef_local.addKeywords("<typedefType>");
		typedef_local.addKeywords("<typedefName>");
		TreeNode typedef_sequence = new TreeNode("%typedef:sequence");
		typedef_sequence.addKeywords("<typedefType>");
		typedef_sequence.addKeywords("<typedefName>");
		typedef_sequence.addKeywords("<sequenceLength>");
		TreeNode length = new TreeNode("%length");
		typedef_sequence.addChildren(length);
		TreeNode typedef_fixed = new TreeNode("%typedef:fixed");
		typedef_fixed.addKeywords("<typedefType>");
		typedef_fixed.addKeywords("<typedefName>");
		typedef_fixed.addKeywords("<digitsNumber>");
		typedef_fixed.addKeywords("<scaleNumber>");
		TreeNode typedef_array = new TreeNode("%typedef:array");
		typedef_array.addKeywords("<typedefType>");
		typedef_array.addKeywords("<typedefName>");
		errorTree.addChildren(typedef_all);
		errorTree.addChildren(typedef_local);
		errorTree.addChildren(typedef_sequence);
		errorTree.addChildren(typedef_fixed);
		errorTree.addChildren(typedef_array);
		TreeNode exception = new TreeNode("%exception");
		exception.addKeywords("<exceptionName>");
		exception.addChildren(member);
		exception.addChildren(member_string);
		errorTree.addChildren(exception);
		TreeNode union = new TreeNode("%union");
		union.addKeywords("<unionName>");
		union.addKeywords("<switchType>");
		TreeNode case_ = new TreeNode("%case");
		case_.addKeywords("<memberType>");
		case_.addKeywords("<memberName>");
		case_.addKeywords("<caseValue>");
		union.addChildren(case_);
		TreeNode default_ = new TreeNode("%default");
		default_.addKeywords("<memberType>");
		default_.addKeywords("<memberName>");
		union.addChildren(default_);
		errorTree.addChildren(union);
		TreeNode enum_ = new TreeNode("%enum");
		enum_.addKeywords("<enumName>");
		TreeNode label = new TreeNode("%label");
		label.addKeywords("<enumLabel>");
		enum_.addChildren(label);
		errorTree.addChildren(enum_);
		TreeNode typeprefix = new TreeNode("%typeprefix");
		typeprefix.addKeywords("<scopeName>");
		typeprefix.addKeywords("<prefixString>");
		errorTree.addChildren(typeprefix);
		TreeNode operation_all = new TreeNode("%operation");
		operation_all.addKeywords("<operationName>");
		operation_all.addKeywords("<returnType>");
		operation_all.addKeywords("<paramList>");
		operation_all.addKeywords("<paramTypeList>");
		operation_all.addKeywords("<paramNameList>");
		operation_all.addKeywords("<raisesList>");
		TreeNode operation_normal = new TreeNode("%operation:normal");
		operation_normal.addKeywords("<operationName>");
		operation_normal.addKeywords("<returnType>");
		operation_normal.addKeywords("<paramList>");
		operation_normal.addKeywords("<paramTypeList>");
		operation_normal.addKeywords("<paramNameList>");
		operation_normal.addKeywords("<raisesList>");
		TreeNode operation_oneway = new TreeNode("%operation:oneway");
		operation_oneway.addKeywords("<operationName>");
		operation_oneway.addKeywords("<returnType>");
		operation_oneway.addKeywords("<paramList>");
		operation_oneway.addKeywords("<paramTypeList>");
		operation_oneway.addKeywords("<paramNameList>");
		operation_oneway.addKeywords("<raisesList>");
		TreeNode operation_noraises = new TreeNode("%operation:noraises");
		operation_noraises.addKeywords("<operationName>");
		operation_noraises.addKeywords("<returnType>");
		operation_noraises.addKeywords("<paramList>");
		operation_noraises.addKeywords("<paramTypeList>");
		operation_noraises.addKeywords("<paramNameList>");
		operation_noraises.addKeywords("<raisesList>");
		TreeNode operation_raises = new TreeNode("%operation:raises");
		operation_raises.addKeywords("<operationName>");
		operation_raises.addKeywords("<returnType>");
		operation_raises.addKeywords("<paramList>");
		operation_raises.addKeywords("<paramTypeList>");
		operation_raises.addKeywords("<paramNameList>");
		operation_raises.addKeywords("<raisesList>");
		interface_all.addChildren(operation_all);
		interface_all.addChildren(operation_normal);
		interface_all.addChildren(operation_oneway);
		interface_all.addChildren(operation_noraises);
		interface_all.addChildren(operation_raises);
		interface_abstract.addChildren(operation_all);
		interface_abstract.addChildren(operation_normal);
		interface_abstract.addChildren(operation_oneway);
		interface_abstract.addChildren(operation_noraises);
		interface_abstract.addChildren(operation_raises);
		interface_local.addChildren(operation_all);
		interface_local.addChildren(operation_normal);
		interface_local.addChildren(operation_oneway);
		interface_local.addChildren(operation_noraises);
		interface_local.addChildren(operation_raises);
		interface_pseudo.addChildren(operation_all);
		interface_pseudo.addChildren(operation_normal);
		interface_pseudo.addChildren(operation_oneway);
		interface_pseudo.addChildren(operation_noraises);
		interface_pseudo.addChildren(operation_raises);
		interface_normal.addChildren(operation_all);
		interface_normal.addChildren(operation_normal);
		interface_normal.addChildren(operation_oneway);
		interface_normal.addChildren(operation_noraises);
		interface_normal.addChildren(operation_raises);
		TreeNode raises = new TreeNode("%raises");
		raises.addKeywords("<scopeName>");
		TreeNode param_all = new TreeNode("%param");
		param_all.addKeywords("<paramType>");
		param_all.addKeywords("<paramName>");
		TreeNode param_INOUT = new TreeNode("%param:INOUT");
		param_INOUT.addKeywords("<paramType>");
		param_INOUT.addKeywords("<paramName>");
		TreeNode param_IN = new TreeNode("%param:IN");
		param_IN.addKeywords("<paramType>");
		param_IN.addKeywords("<paramName>");
		TreeNode param_OUT = new TreeNode("%param:OUT");
		param_OUT.addKeywords("<paramType>");
		param_OUT.addKeywords("<paramName>");
		TreeNode return_all = new TreeNode("%return");
		TreeNode return_void = new TreeNode("%return:void");
		operation_all.addChildren(raises);
		operation_all.addChildren(param_all);
		operation_all.addChildren(param_INOUT);
		operation_all.addChildren(param_IN);
		operation_all.addChildren(param_OUT);
		operation_all.addChildren(return_all);
		operation_all.addChildren(return_void);
		operation_normal.addChildren(raises);
		operation_normal.addChildren(param_all);
		operation_normal.addChildren(param_INOUT);
		operation_normal.addChildren(param_IN);
		operation_normal.addChildren(param_OUT);
		operation_normal.addChildren(return_all);
		operation_normal.addChildren(return_void);
		operation_oneway.addChildren(raises);
		operation_oneway.addChildren(param_all);
		operation_oneway.addChildren(param_INOUT);
		operation_oneway.addChildren(param_IN);
		operation_oneway.addChildren(param_OUT);
		operation_oneway.addChildren(return_all);
		operation_oneway.addChildren(return_void);
		operation_noraises.addChildren(raises);
		operation_noraises.addChildren(param_all);
		operation_noraises.addChildren(param_INOUT);
		operation_noraises.addChildren(param_IN);
		operation_noraises.addChildren(param_OUT);
		operation_noraises.addChildren(return_all);
		operation_noraises.addChildren(return_void);
		operation_raises.addChildren(raises);
		operation_raises.addChildren(param_all);
		operation_raises.addChildren(param_INOUT);
		operation_raises.addChildren(param_IN);
		operation_raises.addChildren(param_OUT);
		operation_raises.addChildren(return_all);
		operation_raises.addChildren(return_void);
		interface_all.addChildren(typedef_all);
		interface_all.addChildren(typedef_local);
		interface_all.addChildren(typedef_sequence);
		interface_all.addChildren(typedef_fixed);
		interface_all.addChildren(typedef_array);
		interface_normal.addChildren(typedef_all);
		interface_normal.addChildren(typedef_local);
		interface_normal.addChildren(typedef_sequence);
		interface_normal.addChildren(typedef_fixed);
		interface_normal.addChildren(typedef_array);
		interface_abstract.addChildren(typedef_all);
		interface_abstract.addChildren(typedef_local);
		interface_abstract.addChildren(typedef_sequence);
		interface_abstract.addChildren(typedef_fixed);
		interface_abstract.addChildren(typedef_array);
		interface_pseudo.addChildren(typedef_all);
		interface_pseudo.addChildren(typedef_local);
		interface_pseudo.addChildren(typedef_sequence);
		interface_pseudo.addChildren(typedef_fixed);
		interface_pseudo.addChildren(typedef_array);
		interface_local.addChildren(typedef_all);
		interface_local.addChildren(typedef_local);
		interface_local.addChildren(typedef_sequence);
		interface_local.addChildren(typedef_fixed);
		interface_local.addChildren(typedef_array);
		TreeNode constants = new TreeNode("%constants");
		constants.addKeywords("<constantsType>");
		constants.addKeywords("<constantsName>");
		constants.addKeywords("<constantsValue>");
		interface_all.addChildren(constants);
		interface_normal.addChildren(constants);
		interface_abstract.addChildren(constants);
		interface_pseudo.addChildren(constants);
		interface_local.addChildren(constants);
		interface_all.addChildren(struct);
		interface_normal.addChildren(struct);
		interface_abstract.addChildren(struct);
		interface_pseudo.addChildren(struct);
		interface_local.addChildren(struct);
		interface_all.addChildren(exception);
		interface_normal.addChildren(exception);
		interface_abstract.addChildren(exception);
		interface_pseudo.addChildren(exception);
		interface_local.addChildren(exception);
		interface_all.addChildren(union);
		interface_normal.addChildren(union);
		interface_abstract.addChildren(union);
		interface_pseudo.addChildren(union);
		interface_local.addChildren(union);
		interface_all.addChildren(enum_);
		interface_normal.addChildren(enum_);
		interface_abstract.addChildren(enum_);
		interface_pseudo.addChildren(enum_);
		interface_local.addChildren(enum_);
		TreeNode inheritance = new TreeNode("%inheritance");
		inheritance.addKeywords("<scopeName>");
		interface_all.addChildren(inheritance);
		interface_normal.addChildren(inheritance);
		interface_abstract.addChildren(inheritance);
		interface_pseudo.addChildren(inheritance);
		interface_local.addChildren(inheritance);
		TreeNode attribute_all = new TreeNode("%attribute");
		attribute_all.addKeywords("<attributeType>");
		attribute_all.addKeywords("<attributeName>");
		TreeNode getraises = new TreeNode("%getraises");
		getraises.addKeywords("<scopeList>");
		getraises.addKeywords("<scopeName>");
		attribute_all.addChildren(getraises);
		TreeNode setraises = new TreeNode("%setraises");
		setraises.addKeywords("<scopeList>");
		setraises.addKeywords("<scopeName>");
		attribute_all.addChildren(setraises);
		TreeNode attribute_normal = new TreeNode("%attribute:normal");
		attribute_normal.addKeywords("<attributeType>");
		attribute_normal.addKeywords("<attributeName>");
		attribute_normal.addChildren(getraises);
		attribute_normal.addChildren(setraises);
		TreeNode attribute_readonly = new TreeNode("%attribute:readonly");
		attribute_readonly.addKeywords("<attributeType>");
		attribute_readonly.addKeywords("<attributeName>");
		interface_all.addChildren(attribute_all);
		interface_all.addChildren(attribute_normal);
		interface_all.addChildren(attribute_readonly);
		interface_normal.addChildren(attribute_all);
		interface_normal.addChildren(attribute_normal);
		interface_normal.addChildren(attribute_readonly);
		interface_abstract.addChildren(attribute_all);
		interface_abstract.addChildren(attribute_normal);
		interface_abstract.addChildren(attribute_readonly);
		interface_pseudo.addChildren(attribute_all);
		interface_pseudo.addChildren(attribute_normal);
		interface_pseudo.addChildren(attribute_readonly);
		interface_local.addChildren(attribute_all);
		interface_local.addChildren(attribute_normal);
		interface_local.addChildren(attribute_readonly);
		TreeNode native_ = new TreeNode("%native");
		native_.addKeywords("<nativeName>");
		interface_all.addChildren(native_);
		interface_normal.addChildren(native_);
		interface_abstract.addChildren(native_);
		interface_pseudo.addChildren(native_);
		interface_local.addChildren(native_);
		TreeNode module = new TreeNode("%module");
		TreeNode valuetype_box = new TreeNode("%valuetype:box");
		valuetype_box.addKeywords("<valuetypeType>");
		valuetype_box.addKeywords("<valuetypeName>");
		TreeNode valuetype_abstract = new TreeNode("%valuetype:abstract");
		valuetype_abstract.addKeywords("<valuetypeName>");
		valuetype_abstract.addKeywords("<truncatableList>");
		valuetype_abstract.addKeywords("<abstractTruncatableList>");
		valuetype_abstract.addKeywords("<statefulTruncatableList>");
		valuetype_abstract.addKeywords("<supportsList>");
		valuetype_abstract.addKeywords("<abstractSupportsList>");
		valuetype_abstract.addKeywords("<statefulSupportsList>");
		valuetype_abstract.addChildren(operation_all);
		valuetype_abstract.addChildren(operation_normal);
		valuetype_abstract.addChildren(operation_oneway);
		valuetype_abstract.addChildren(operation_raises);
		valuetype_abstract.addChildren(operation_noraises);
		valuetype_abstract.addChildren(typedef_all);
		valuetype_abstract.addChildren(typedef_local);
		valuetype_abstract.addChildren(typedef_sequence);
		valuetype_abstract.addChildren(typedef_fixed);
		valuetype_abstract.addChildren(typedef_array);
		valuetype_abstract.addChildren(constants);
		valuetype_abstract.addChildren(struct);
		valuetype_abstract.addChildren(exception);
		valuetype_abstract.addChildren(union);
		valuetype_abstract.addChildren(enum_);
		valuetype_abstract.addChildren(attribute_all);
		valuetype_abstract.addChildren(attribute_normal);
		valuetype_abstract.addChildren(attribute_readonly);
		TreeNode truncatable = new TreeNode("%truncatable");
		truncatable.addKeywords("<truncatableName>");
		TreeNode abstract_ = new TreeNode("%abstract");
		TreeNode stateful_ = new TreeNode("%stateful");
		truncatable.addChildren(abstract_);
		truncatable.addChildren(stateful_);
		valuetype_abstract.addChildren(truncatable);
		TreeNode supports = new TreeNode("%supports");
		supports.addKeywords("<supportsName>");
		supports.addChildren(abstract_);
		supports.addChildren(stateful_);
		valuetype_abstract.addChildren(supports);
		TreeNode valuetype_normal = new TreeNode("%valuetype:normal");
		valuetype_normal.addKeywords("<valuetypeName>");
		valuetype_normal.addKeywords("<truncatableList>");
		valuetype_normal.addKeywords("<abstractTruncatableList>");
		valuetype_normal.addKeywords("<statefulTruncatableList>");
		valuetype_normal.addKeywords("<supportsList>");
		valuetype_normal.addKeywords("<abstractSupportsList>");
		valuetype_normal.addKeywords("<statefulSupportsList>");
		valuetype_normal.addChildren(operation_all);
		valuetype_normal.addChildren(operation_normal);
		valuetype_normal.addChildren(operation_oneway);
		valuetype_normal.addChildren(operation_noraises);
		valuetype_normal.addChildren(operation_raises);
		valuetype_normal.addChildren(constants);
		valuetype_normal.addChildren(typedef_all);
		valuetype_normal.addChildren(typedef_local);
		valuetype_normal.addChildren(typedef_sequence);
		valuetype_normal.addChildren(typedef_fixed);
		valuetype_normal.addChildren(typedef_array);
		valuetype_normal.addChildren(struct);
		valuetype_normal.addChildren(union);
		valuetype_normal.addChildren(enum_);
		valuetype_normal.addChildren(exception);
		valuetype_normal.addChildren(attribute_all);
		valuetype_normal.addChildren(attribute_normal);
		valuetype_normal.addChildren(attribute_readonly);
		TreeNode statemember_public = new TreeNode("%statemember:public");
		statemember_public.addChildren(member);
		statemember_public.addChildren(member_string);
		TreeNode statemember_private = new TreeNode("%statemember:private");
		statemember_private.addChildren(member);
		statemember_private.addChildren(member_string);
		valuetype_normal.addChildren(statemember_public);
		valuetype_normal.addChildren(statemember_private);
		TreeNode factory = new TreeNode("%factory");
		factory.addKeywords("<factoryName>");
		factory.addKeywords("<paramList>");
		factory.addKeywords("<paramTypeList>");
		factory.addKeywords("<paramNameList>");
		factory.addKeywords("<raisesList>");
		factory.addChildren(param_all);
		factory.addChildren(raises);
		valuetype_normal.addChildren(factory);
		valuetype_normal.addChildren(truncatable);
		valuetype_normal.addChildren(supports);
		TreeNode valuetype_nocustom = new TreeNode("%valuetype:nocustom");
		valuetype_nocustom.addKeywords("<valuetypeName>");
		valuetype_nocustom.addKeywords("<truncatableList>");
		valuetype_nocustom.addKeywords("<abstractTruncatableList>");
		valuetype_nocustom.addKeywords("<statefulTruncatableList>");
		valuetype_nocustom.addKeywords("<supportsList>");
		valuetype_nocustom.addKeywords("<abstractSupportsList>");
		valuetype_nocustom.addKeywords("<statefulSupportsList>");
		valuetype_nocustom.addChildren(operation_all);
		valuetype_nocustom.addChildren(operation_normal);
		valuetype_nocustom.addChildren(operation_oneway);
		valuetype_nocustom.addChildren(operation_noraises);
		valuetype_nocustom.addChildren(operation_raises);
		valuetype_nocustom.addChildren(typedef_all);
		valuetype_nocustom.addChildren(typedef_local);
		valuetype_nocustom.addChildren(typedef_sequence);
		valuetype_nocustom.addChildren(typedef_fixed);
		valuetype_nocustom.addChildren(typedef_array);
		valuetype_nocustom.addChildren(constants);
		valuetype_nocustom.addChildren(union);
		valuetype_nocustom.addChildren(enum_);
		valuetype_nocustom.addChildren(struct);
		valuetype_nocustom.addChildren(exception);
		valuetype_nocustom.addChildren(attribute_all);
		valuetype_nocustom.addChildren(attribute_normal);
		valuetype_nocustom.addChildren(attribute_readonly);
		valuetype_nocustom.addChildren(statemember_public);
		valuetype_nocustom.addChildren(statemember_private);
		valuetype_nocustom.addChildren(factory);
		valuetype_nocustom.addChildren(truncatable);
		valuetype_nocustom.addChildren(supports);
		TreeNode valuetype_custom = new TreeNode("%valuetype:custom");
		valuetype_custom.addKeywords("<valuetypeName>");
		valuetype_custom.addKeywords("<truncatableList>");
		valuetype_custom.addKeywords("<abstractTruncatableList>");
		valuetype_custom.addKeywords("<statefulTruncatableList>");
		valuetype_custom.addKeywords("<supportsList>");
		valuetype_custom.addKeywords("<abstractSupportsList>");
		valuetype_custom.addKeywords("<statefulSupportsList>");
		valuetype_custom.addChildren(operation_all);
		valuetype_custom.addChildren(operation_normal);
		valuetype_custom.addChildren(operation_oneway);
		valuetype_custom.addChildren(operation_noraises);
		valuetype_custom.addChildren(operation_raises);
		valuetype_custom.addChildren(typedef_all);
		valuetype_custom.addChildren(typedef_local);
		valuetype_custom.addChildren(typedef_sequence);
		valuetype_custom.addChildren(typedef_fixed);
		valuetype_custom.addChildren(typedef_array);
		valuetype_custom.addChildren(constants);
		valuetype_custom.addChildren(union);
		valuetype_custom.addChildren(enum_);
		valuetype_custom.addChildren(struct);
		valuetype_custom.addChildren(exception);
		valuetype_custom.addChildren(attribute_all);
		valuetype_custom.addChildren(attribute_normal);
		valuetype_custom.addChildren(attribute_readonly);
		valuetype_custom.addChildren(statemember_public);
		valuetype_custom.addChildren(statemember_private);
		valuetype_custom.addChildren(factory);
		valuetype_custom.addChildren(truncatable);
		valuetype_custom.addChildren(supports);
		module.addKeywords("<moduleName>");
		module.addChildren(interface_all);
		module.addChildren(interface_abstract);
		module.addChildren(interface_pseudo);
		module.addChildren(interface_local);
		module.addChildren(interface_normal);
		module.addChildren(struct);
		module.addChildren(typedef_all);
		module.addChildren(typedef_local);
		module.addChildren(typedef_sequence);
		module.addChildren(typedef_fixed);
		module.addChildren(typedef_array);
		module.addChildren(exception);
		module.addChildren(union);
		module.addChildren(enum_);
		module.addChildren(module);
		module.addChildren(valuetype_nocustom);
		module.addChildren(valuetype_custom);
		module.addChildren(valuetype_normal);
		module.addChildren(valuetype_abstract);
		module.addChildren(valuetype_box);
		module.addChildren(typeprefix);
		errorTree.addChildren(module);
		errorTree.addChildren(valuetype_box);
		errorTree.addChildren(valuetype_abstract);
		errorTree.addChildren(valuetype_normal);
		errorTree.addChildren(valuetype_nocustom);
		errorTree.addChildren(valuetype_custom);
	}
	
	public static ErrorDetection getInstance()
	{
		if(errorDetection == null)
		{
			labelwords = KeyWords.getInstance().getLabelwords();
			keywords = KeyWords.getInstance().getKeywords();
			errorDetection = new ErrorDetection();
		}
		return errorDetection;
	}
	
	public boolean errorFinding(String fileName, String text, JTextPane textPane)
	{
		textPane.setText("");
		int warning = 0;
		int error = 0;
		textPane.setText(textPane.getText()+fileName + "检错:");
		String []code = text.replaceAll("\r", "").split("\n");
		int labelMatch = 0;
		Stack<TreeNode> stack = new Stack<TreeNode>();
		Vector<String> labelword = new Vector<String>();
		stack.push(errorTree);
		labelword.addAll(errorTree.getKeywords());
		for(int i = 0 ; i < code.length ; i++)
		{
			if(code[i].equals("%%"))
			{
				if(labelMatch == 0)
				{
					textPane.setText(textPane.getText() + "\r\nerror:不合法的结束标签 line(" + i+1 + ")");
					++error;
				}
				else
				{
					TreeNode tmp = stack.pop();
					for(int k = 0 ; k < tmp.getKeywords().size() ; k++)
						labelword.remove(tmp.getKeywords().get(k));
					--labelMatch;
				}
			}
			else if(code[i].startsWith("%") && !code[i].startsWith("%newfile"))
			{
				TreeNode newtemp = stack.lastElement().findChild(code[i]);
				if(newtemp != null)
				{
					stack.push(newtemp);
					labelword.addAll(newtemp.getKeywords());
					++labelMatch;
				}
				else if(labelwords.contains(code[i]))
				{
					textPane.setText(textPane.getText() + "\r\nwarning:当前位置标签" + code[i] + "不可用 line(" + (i+1) + ")");
					++warning;
				}
				else
				{
					textPane.setText(textPane.getText() + "\r\nwarning:不可识别的标签" + code[i] + " line(" + (i+1) + ")");
					++warning;
				}
			}
			else
			{
				if(code[i].contains("<") && code[i].contains(">"))
				{
					String []tmp = code[i].split("<");
					for(int j = 1 ; j < tmp.length ; j++)
					{
						String word = tmp[j].split(">")[0];
						String newtemp = "<" + word + ">";
						if(labelword.contains(newtemp))
							continue;
						else if(keywords.contains(newtemp))
						{
							textPane.setText(textPane.getText() + "\r\nwarning:当前位置关键字" + newtemp + "不可用 line(" + (i+1) + ")");
							++warning;
						}
						else
						{
							if(!isNumeric(word))
							{
								textPane.setText(textPane.getText() + "\r\nwarning:不可识别的关键字" + newtemp + " line(" + (i+1) + ")");
								++warning;
							}
						}
					}
				}
			}
		}
		
		while(!stack.isEmpty())
			stack.pop();
		
		if(labelMatch != 0)
		{
			textPane.setText(textPane.getText() + "\r\nerror:标签匹配有误，请检查代码");
			++error;
		}
		
		textPane.setText(textPane.getText() + "\r\n模板文件" + fileName + "共有" + error + "个error," + warning + "个warning.");
		
		if(error > 0)
			return false;
		else if(error == 0)
			return true;
		
		return false;
	}
	
	private boolean isNumeric(String str)
	{
		for (int i = 0; i < str.length(); i++)
		{
			if (!Character.isDigit(str.charAt(i)))
				return false;
		}
		return true;
	}
}

class TreeNode
{
	private String label;
	private Vector<String> keywords;
	private Vector<TreeNode> children;
	
	public TreeNode(String label)
	{
		this.label = label;
		this.keywords = new Vector<String>();
		this.children = new Vector<TreeNode>();
	}
	
	public String getLabel() 
	{
		return label;
	}
	
	public Vector<String> getKeywords() 
	{
		return keywords;
	}

	public void setKeywords(Vector<String> keywords) 
	{
		this.keywords = keywords;
	}
	
	public void addChildren(TreeNode child)
	{
		children.add(child);
	}
	
	public void addKeywords(String keyword)
	{
		keywords.addElement(keyword);
	}
	
	public boolean findKeywords(String keyword)
	{
		for(int i = 0 ; i < keywords.size() ; i++)
		{
			if(keywords.get(i).equals(keyword))
				return true;
		}
		return false;
	}
	
	public TreeNode findChild(String labelword)
	{
		for(int i = 0 ; i < children.size() ; i++)
		{
			if(children.get(i).label.equals(labelword))
				return children.get(i);
		}
		return null;
	}
}