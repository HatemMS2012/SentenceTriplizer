package hms.sentence.triplization;

import java.util.HashSet;
import java.util.Set;

public class TagSets {

	public static Set<String> nounTagSet;
	public static Set<String> verbTagSet;
	public static Set<String> adjTagSet;
	public static Set<String> nounModifiersSet;
	
	
	
	static{
		
		nounTagSet = new HashSet<String>();
		nounTagSet.add("NN");
		nounTagSet.add("NNP");
		nounTagSet.add("NNPS");
		nounTagSet.add("NNS");
//		nounTagSet.add("PRP");
		
		verbTagSet = new HashSet<String>();
		verbTagSet.add("VB");
		verbTagSet.add("VBD");
		verbTagSet.add("VBG");
		verbTagSet.add("VBN");
		verbTagSet.add("VBP");
		verbTagSet.add("VBZ");
		
		adjTagSet = new HashSet<String>();
		adjTagSet.add("JJ");
		adjTagSet.add("JJR");
		adjTagSet.add("JJS");
		
		nounModifiersSet = new HashSet<String>();
		nounModifiersSet.add("DT");
		nounModifiersSet.add("PRP$");
		nounModifiersSet.add("PRP");
		
		nounModifiersSet.add("POS");
		nounModifiersSet.add("JJ");
		nounModifiersSet.add("CD");
		nounModifiersSet.add("ADJP");
		nounModifiersSet.add("RB");
		nounModifiersSet.add("QP");
		nounModifiersSet.addAll(nounTagSet);		
		
	}
}
