package old;

import hms.sentence.triplization.PhraseTypes;
import hms.sentence.triplization.SentenceTriple;
import hms.sentence.triplization.TripleExtractorStandard;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;


public class TripleExtratorForNounSBARSentence extends TripleExtractorStandard {
	
	@Override
	public SentenceTriple extractTriple() {
		
//		this.syntaxTree.pennPrint();
		
		//Get the root of the syntaxt tree.
			
		Tree root = this.syntaxTree.firstChild();
				
		Tree[] children = root.children();
				
		
		SentenceTriple triple = new SentenceTriple();
			
		for (Tree child: children) {
			
			String childLabel = child.label().value();
			
			if(childLabel.equals(PhraseTypes.NOUN_PHRASE)){
				
				this.handleNP(triple,child);
			
				TregexPattern tPattern = TregexPattern.compile("SBAR");
					
				TregexMatcher tMatcher = tPattern.matcher(root);

				while (tMatcher.find()) {
						
						
					Tree sentenceRoot = tMatcher.getMatch();
						
					SentenceTriple tt = handleSBAR(sentenceRoot);
						
					if(tt.getPredicate() !=null && tt.getObject() !=null){
						
						triple.setPredicate(tt.getPredicate());
						triple.setObject(tt.getObject());
						break;
					}
					
				}
				
			}

		}

		return triple;
	}



}
