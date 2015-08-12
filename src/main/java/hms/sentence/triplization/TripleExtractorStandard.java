package hms.sentence.triplization;


import java.util.List;
import java.util.Map;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

/**
 * Extract triple from sentences that have NP VP (...) as children of the root component
 * Acceptable patterns:
 * S: NP VP; V = VB, VBD, VBG, VBP, VBZ
 * S: NP VP (PP); V = VBN
 * S: NP VP (PP); V = VBN
 * S: NP AJDP (PP) 	e.g. fields related to this item
 * 
 * @author mousselly
 *
 */
public class TripleExtractorStandard extends TripleExtractor {
	
	
		
	/**
	 * Extract the triple of (subject,predicate,object) from the sytanctic tree of a sentence
	 * The syntactic tree must have the following pattern: ROOT (NP VP (...))
	 * The implementation of this method is based on the algorithm described in 
	 * Rusu, Delia, et al. "Triplet extraction from sentences." Proceedings of the 10th International Multiconference" Information Society-IS. 2007.
	 * First, we extract the subject from the NP, then the verb is extracted from the deepest verb in the VP and the object is extracted as 
	 * the first noun of the siblings of VP
	 * 
	 */
	public SentenceTriple extractTriple() {
		
		//Get the root of the syntaxt tree.
		Tree root = this.syntaxTree.firstChild();
		
		Tree[] children = root.children();
		
		SentenceTriple triple = new SentenceTriple();
		
		boolean isVP = false;
		
		//For the case of NP_obj NP_sub VP ...
		boolean isDirectObjectSubject = false;
		SentenceTriple objectT = null;
		SentenceTriple subjectT = null;
		
		
		for (Tree child: children) {
		
			//The subject is extracted from NP
			String childLabel = child.label().value();
			if(childLabel.equals(PhraseTypes.NOUN_PHRASE)){
				
				//If the direct sibling of the NP another NP like: country a person represents -> person represents a country.
				//In this case a special handling is needed. The first NP is a source for an object while the second contains the subject
				
				TregexPattern tPattern = TregexPattern.compile("NP $. NP");
				TregexMatcher tMatcher = tPattern.matcher(child);

				if (tMatcher.find()) {
					
					Tree[] grandChildren = child.children();
					
					if(grandChildren.length == 2){
						
						objectT = new SentenceTriple();
						handleNP(objectT, grandChildren[0]);
						
						
						subjectT = new SentenceTriple();
						handleNP(subjectT, grandChildren[1]);
						
						isDirectObjectSubject = true;
					}
				}
				else{
					handleNP(triple, child);
				}
				
			}
			//The predicate and object are extracted from VP
			else if(childLabel.equals(PhraseTypes.VERB_PHRASE)){
				isVP= true;
				handleVP(triple, child);
			}
			else if(childLabel.equals(PhraseTypes.ADJECTIVE_PHRASE) || childLabel.equals(PhraseTypes.SENTENCE)){

				handleVP(triple, child);

			}
			//Reverse the subject/object roles in case of passive construction (VBN)
			
			
		}
		if(isDirectObjectSubject){
			
			triple.setSubject(subjectT.getSubject());
			triple.setObject(objectT.getSubject());
			
		}
		if(isVP && triple.getPredicate()!=null && triple.getPredicate().contains("VBN")){
			String temp = triple.getSubject();
			triple.setSubject(triple.getObject());
			triple.setObject(temp);
		}
		return triple;
	}

	

}
