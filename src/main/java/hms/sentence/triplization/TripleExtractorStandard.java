package hms.sentence.triplization;


import java.util.Arrays;
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
		
		SentenceTriple triple = new SentenceTriple();
		
		triple.setSentenceParseTree(syntaxTree);
		
		//Get the root of the syntaxt tree.
		Tree root = this.syntaxTree.firstChild();
				
		//TODO if the sentence does not contain a verb
		if(isVPFreeSyntaxTree()){
			
			
			handlePureNP(triple, root);
			
			return triple;
		}
		
		
		Tree[] children = root.children();
		
		
		
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
					
					Tree tempTree = tMatcher.getMatch();

						objectT = new SentenceTriple();
						handleNP(objectT,tempTree);
						
						if(tempTree.siblings(root).size() == 1){
							subjectT = new SentenceTriple();
							handleNP(subjectT, tempTree.siblings(root).get(0));
						}
						
						isDirectObjectSubject = true;

				}
				//if the NP has a VP child
				
				else{
					handleNP(triple, child);
				}
				
				 
				//For cases like NP (NP (NP NP) VP) Mistake a person made
				tPattern = TregexPattern.compile("VP");
				tMatcher = tPattern.matcher(child);

				if (tMatcher.find()) {
						
					Tree vpTree = tMatcher.getMatch();
						
					handleVP(triple, vpTree);
					
					isVP= true;
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
		if(isVP && triple.getPredicate()!=null && triple.getPredicate().contains("VBN") && !isDirectObjectSubject){
			String temp = triple.getSubject();
			
			List<String> tempSubModifiers = triple.getSubjectModifier();
			List<String> tempObjModifiers = triple.getObjectModifier();
			
			triple.setSubject(triple.getObject());
			triple.setSubjectModifier(tempObjModifiers);
			
			
			triple.setObject(temp);
			triple.setObjectModifier(tempSubModifiers);
			
		}
		return triple;
	}



	

}
