package old;

import hms.sentence.triplization.PhraseTypes;
import hms.sentence.triplization.SentenceTriple;
import hms.sentence.triplization.TripleExtractorStandard;

import java.util.Map;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


public class TripleExtratorForNounPropositalSentence extends TripleExtractorStandard {
	
	
	

	@Override
	public SentenceTriple extractTriple() {
		
		this.syntaxTree.pennPrint();
		
		//Get the root of the syntaxt tree.
			
		Tree root = this.syntaxTree.firstChild();
				
		Tree[] children = root.children();
				
		
		SentenceTriple triple = new SentenceTriple();
			
		for (Tree child: children) {
			
			String childLabel = child.label().value();
			
			if(childLabel.equals(PhraseTypes.NOUN_PHRASE)){
				
				this.handleNP(triple,child);
			}
		
			//PP SBAR
			else if(childLabel.equals(PhraseTypes.PROPOSITION_PHRASE) ){
				
				SentenceTriple tt = handleSBAR(child);
				triple.setObject(tt.toString());
				
			}
			else if(childLabel.equals(PhraseTypes.SBAR_PHRASE) ){
				SentenceTriple tt = handleSBAR(child);
				triple.setObject(tt.toString());
			}
			
			//SBAR
		}
		return triple;
	}

	
	

}
