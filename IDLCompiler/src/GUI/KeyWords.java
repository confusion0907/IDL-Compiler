package GUI;

import java.util.HashSet;
import java.util.Set;

public class KeyWords {
	private static KeyWords keyWords = null;
	private static Set<String> keywords;
	private static Set<String> labelwords;
	private static Set<String> functionwords;
	
	public static KeyWords getInstance()
	{
		if(keyWords == null)
			keyWords = new KeyWords();
		return keyWords;
	}
	
	private KeyWords()
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
		keywords.add("<digitsNumber>");
		keywords.add("<scaleNumber>");
		keywords.add("<enumName>");
		keywords.add("<enumLabel>");
		keywords.add("<exceptionName>");
		keywords.add("<unionName>");
		keywords.add("<moduleName>");
		keywords.add("<switchType>");
		keywords.add("<caseValue>");
		keywords.add("<valuetypeType>");
		keywords.add("<valuetypeName>");
		keywords.add("<sequenceLength>");
		keywords.add("<truncatableList>");
		keywords.add("<abstractTruncatableList>");
		keywords.add("<statefulTruncatableList>");
		keywords.add("<supportsList>");
		keywords.add("<abstractSupportsList>");
		keywords.add("<statefulSupportsList>");
		keywords.add("<factoryName>");
		keywords.add("<raisesList>");
		keywords.add("<scopeList>");
		keywords.add("<scopeName>");
		keywords.add("<prefixString>");
		keywords.add("<attributeName>");
		keywords.add("<attributeType>");
		keywords.add("<nativeName>");
	}

	private void initKeywords()
	{
		labelwords = new HashSet<String>();
		labelwords.add("%newfile");
		labelwords.add("%typedef");
		labelwords.add("%typedef:local");
		labelwords.add("%typedef:sequence");
		labelwords.add("%typedef:fixed");
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
		labelwords.add("%interface:abstract");
		labelwords.add("%interface:local");
		labelwords.add("%interface:pseudo");
		labelwords.add("%interface:normal");
		labelwords.add("%struct");
		labelwords.add("%operation");
		labelwords.add("%operation:normal");
		labelwords.add("%operation:oneway");
		labelwords.add("%operation:noraises");
		labelwords.add("%operation:raises");
		labelwords.add("%param");
		labelwords.add("%param:IN");
		labelwords.add("%param:OUT");
		labelwords.add("%param:INOUT");
		labelwords.add("%return");
		labelwords.add("%constants");
		labelwords.add("%attribute");
		labelwords.add("%attribute:readonly");
		labelwords.add("%attribute:normal");
		labelwords.add("%getraises");
		labelwords.add("%setraises");
		labelwords.add("%raises");
		labelwords.add("%valuetype:box");
		labelwords.add("%valuetype:abstract");
		labelwords.add("%valuetype:normal");
		labelwords.add("%truncatable");
		labelwords.add("%supports");
		labelwords.add("%factory");
		labelwords.add("%statemember:public");
		labelwords.add("%statemember:private");
		labelwords.add("%supports");
		labelwords.add("%inheritance");
		labelwords.add("%native");
		labelwords.add("%typeprefix");
		labelwords.add("%%");
	}

	public Set<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(Set<String> keywords) {
		KeyWords.keywords = keywords;
	}

	public Set<String> getLabelwords() {
		return labelwords;
	}

	public void setLabelwords(Set<String> labelwords) {
		KeyWords.labelwords = labelwords;
	}

	public Set<String> getFunctionwords() {
		return functionwords;
	}

	public void setFunctionwords(Set<String> functionwords) {
		KeyWords.functionwords = functionwords;
	}
}
