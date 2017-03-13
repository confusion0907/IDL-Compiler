package GUI;

import java.util.HashSet;
import java.util.Set;

public class KeyWords {
	private Set<String> keywords;
	private Set<String> labelwords;
	private Set<String> functionwords;
	
	public KeyWords()
	{
		initKeywords();
		initLabelwords();
		initFuncionwords();
	}
	
	private void initFuncionwords()
	{
		functionwords = new HashSet<String>();
		functionwords.add("HASH");
		functionwords.add("IncNum");
	}

	private void initLabelwords() 
	{
		keywords = new HashSet<String>();
		keywords.add("<fileName>");
		keywords.add("<interfaceName>");
		keywords.add("<operationName>");
		keywords.add("<paramName>");
		keywords.add("<paramType>");
		keywords.add("<paramList>");
		keywords.add("<paramNameList>");
		keywords.add("<paramTypeList>");
		keywords.add("<returnType>");
		keywords.add("<structName>");
		keywords.add("<memberName>");
		keywords.add("<memberType>");
		keywords.add("<typedefName>");
		keywords.add("<typedefType>");
		keywords.add("<constantsName>");
		keywords.add("<constantsType>");
		keywords.add("<constantsValue>");
		keywords.add("<sequenceLength>");
		keywords.add("<enumName>");
		keywords.add("<enumLabel>");
		keywords.add("<exceptionName>");
		keywords.add("<unionName>");
		keywords.add("<switchType>");
		keywords.add("<caseValue>");
	}

	private void initKeywords()
	{
		labelwords = new HashSet<String>();
		labelwords.add("%newfile");
		labelwords.add("%typedef:sequence");
		labelwords.add("%length");
		labelwords.add("%module");
		labelwords.add("%enum");
		labelwords.add("%label");
		labelwords.add("%exception");
		labelwords.add("%member");
		labelwords.add("%union");
		labelwords.add("%case");
		labelwords.add("%default");
		labelwords.add("%interface");
		labelwords.add("%struct");
		labelwords.add("%operation");
		labelwords.add("%param:IN");
		labelwords.add("%param:OUT");
		labelwords.add("%param:INOUT");
		labelwords.add("%return");
		labelwords.add("%%");
	}

	public Set<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}

	public Set<String> getLabelwords() {
		return labelwords;
	}

	public void setLabelwords(Set<String> labelwords) {
		this.labelwords = labelwords;
	}

	public Set<String> getFunctionwords() {
		return functionwords;
	}

	public void setFunctionwords(Set<String> functionwords) {
		this.functionwords = functionwords;
	}
}
