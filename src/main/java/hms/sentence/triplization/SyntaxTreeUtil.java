package hms.sentence.triplization;

import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.Tree;

public class SyntaxTreeUtil {

	
	public static void test(Tree syntaxTree){
		
		
		Tree[] children = syntaxTree.skipRoot().children();
		System.out.println(children);
		for(Tree child : children){
			System.out.println(child.labels());
		}
		
	}
	
	
	/**
	 * Check if the first child of the syntax tree contains one of the words given in the parameter
	 * @param words
	 * @param syntaxTree
	 * @return
	 */
	public static boolean isFirstChildStartingWith(Collection<String> words,MergedSentenceTriple triple){
		
//		int sentenceLength = triple.getCompleteSyntaxTree().getLeaves().size();
		
		if(triple.getCompleteSyntaxTree() != null && triple.getCompleteSyntaxTree().skipRoot()!=null){
			
			Tree[] children = triple.getCompleteSyntaxTree().skipRoot().children();
			
			Tree child = children[0];
			
			Collection<Label> labels = child.labels();
			
			
			for(Label label : labels){
				
				if(label.toString().contains("-") && words.contains(label.value())){
				
					int positionInSentence = Integer.valueOf(label.toString().substring(label.toString().lastIndexOf("-")+1, label.toString().length()));
					
					if(positionInSentence <=2)
						return true;
				}
					
			}
		}
		
		return false;
	}
}
