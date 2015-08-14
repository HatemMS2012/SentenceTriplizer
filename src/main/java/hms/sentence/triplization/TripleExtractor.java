package hms.sentence.triplization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Predicate;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public abstract class TripleExtractor {

	private static Tree currentPredicate = null;
	private static Tree currentSubject = null;
	private static Tree currentAdj = null;


	/**
	 * The input syntax tree
	 */
	protected Tree syntaxTree;
	protected Tree rootSyntaxTree;
	
	
	
	
	public Tree getRootSyntaxTree() {
		return rootSyntaxTree;
	}


	public void setRootSyntaxTree(Tree rootSyntaxTree) {
		this.rootSyntaxTree = rootSyntaxTree;
	}


	public Tree getSyntaxTree() {
		return syntaxTree;
	}


	public void setSyntaxTree(Tree syntaxTree) {
		this.syntaxTree = syntaxTree;
	}


	protected abstract SentenceTriple extractTriple();
	
		
	/**
	 * Extract attributes (modifier) for nouns. Those are normally adjectives.
	 * @param npTree
	 * @return
	 */
	public static List<String> extractAttributesForNouns(List<Tree> npTree){
		
		List<String> modifiers = new ArrayList<String>();
		int index = 0;
		
		if(npTree == null)
			return null;
			
		for(Tree e : npTree){
			
			if(TagSets.nounModifiersSet.contains(e.label().value())){
				
				modifiers.add(index++,e.toString());
			}
			
			
		}
		
		return modifiers;
	}
	
	

	/**
	 * Extract the noun from NP branch of the sentence
	 * The extracted noun is the first noun in the tree
	 * @param npSubtree
	 * @return
	 */
	private Tree extractSubjectPrivate(Tree npSubtree) {
		
		Tree[] iter = npSubtree.children();
		
		for(Tree ch : iter){
			
			if(TagSets.nounTagSet.contains(ch.label().value())){
				
				currentSubject = ch;
				
				
				return currentSubject;
					
			}
			else if (currentSubject == null){
				extractSubjectPrivate(ch);
			}
				
		}
		return currentSubject;
	}
	
	public Tree extractSubject(Tree npSubtree) {
		currentSubject = null;
		return extractSubjectPrivate(npSubtree);
	}
	
	/**
	 * Extract the verb from the VP branch of the sentence. 
	 * The extracted verb corresponds to the deepest verb descendant of the VP
	 * @param vpSubtree
	 * @return
	 */
	private Tree extractPredicatePrivate(Tree vpSubtree) {
		
		
		if(vpSubtree.isLeaf())
			return currentPredicate;
		List<Tree> subTrees = vpSubtree.getChildrenAsList();

		for(Tree ch : subTrees){
		
			if(TagSets.verbTagSet.contains(ch.label().value())){
				currentPredicate = ch;
			}
			
			extractPredicatePrivate(ch);
		}
		return currentPredicate;
		
	}
	
	private static Tree deepesNount = null;
	
	/**
	 * extract the deepest noun in syntax tree
	 * @param vpSubtree
	 * @return
	 */
	private Tree extractDeepestNounPrivate(Tree vpSubtree) {
		
		
//		if(vpSubtree.isLeaf())
//			return deepesNount;
		
		List<Tree> subTrees = vpSubtree.getChildrenAsList();

		for(Tree ch : subTrees){
		
			if(TagSets.nounTagSet.contains(ch.label().value())){
				deepesNount = ch;
			}
			
			extractDeepestNounPrivate(ch);
		}
		return deepesNount;
		
	}
	
	public Tree extractDeepestNoun(Tree vpSubtree) {
		deepesNount = null;
		return extractDeepestNounPrivate(vpSubtree);
	}
	
	
	public Tree extractPredicate(Tree vpSubtree) {
		currentPredicate = null;
		return extractPredicatePrivate(vpSubtree);
		
	}
	
	/**
	 * Extract the object from sibling subtrees of the VP containing the predicate
	 * @param vpSiblings
	 * @return
	 */
	public Tree extractObject(List<Tree> vpSiblings) {
		
		if(vpSiblings == null)
			return null;
		
		Tree object = null;
		for(Tree ch : vpSiblings){
			
			if(ch.label().value().equals("PP") || ch.label().value().equals("NP") ){
				
				object = extractSubject(ch);
				if(object !=null)
					return object;
			}
			else if(ch.label().value().equals("ADJP")){
				object = extractAdjectivePrivate(ch);
				
				if(object !=null)
				
					return object;
			}
		}
		return object;
		
	}
	
	public Tree extractAdjectivePrivate(Tree npSubtree) {
		
		Tree[] iter = npSubtree.children();
		
		for(Tree ch : iter){
			
			
			if(TagSets.adjTagSet.contains(ch.label().value())){
				
				currentAdj = ch;
					
			}
			else{
				extractSubject(ch);
			}
				
		}
		return currentAdj;
	}
	
	public Tree extractAdjective(Tree npSubtree) {
		
		currentAdj = null;
		return extractAdjectivePrivate(npSubtree);
				
	}
	
	

	
	public List<Tree> splitSyntaxTree(final String splitNodeName){
		
		List<Tree> result = new ArrayList<Tree>();
		

		TregexPattern tPattern = TregexPattern.compile(splitNodeName);

		
		TregexMatcher tMatcher = tPattern.matcher(syntaxTree);
		Tree sbarTree  = null;
		
		while (tMatcher.find()) {
			
			sbarTree = tMatcher.getMatch();
			break;
			
		}
			Predicate<Tree>  f = new  Predicate<Tree>() { 
		
			public boolean test(Tree t) {
				// TODO Auto-generated method stub
				return  ! t.label().value().equals(splitNodeName); 
			} 
		}; 
			 
		result.add(0,sbarTree);
		result.add(1,syntaxTree.prune(f));
		
	
		return result;
	}
	
	

	/**
	 * Extract the noun from NP subtree
	 * @param triple
	 * @param child
	 */
	protected void handleNP(SentenceTriple triple, Tree child) {
		
		Tree npSubtree;
		
		npSubtree = child;
	
		Tree sub = extractSubject(npSubtree);

		if(sub!=null){
		
			List<String> attr = enrichWithModifier(npSubtree, sub);
			List<String> furtherAtrr = enrichFromPP(npSubtree,syntaxTree);
			if(furtherAtrr.size() == 0){
				//TODO CHECK Try with first chiled
				furtherAtrr = enrichFromPP(npSubtree.firstChild(),syntaxTree);
			}
			attr.addAll(furtherAtrr);
			triple.setSubject(sub.toString());
			triple.setSubjectModifier(attr);
			
			
		}
	}

	/**
	 * Extends a noun with modifier extracted from PP tree branch
	 * @param npSubtree
	 * @param syntaxTree
	 * @return
	 */
	protected List<String> enrichFromPP(Tree npSubtree, Tree syntaxTree) {
	
		List<String> modifiers = new ArrayList<String>();
		
		List<Tree> children = npSubtree.siblings(syntaxTree);
		
			for(Tree child : children){
			
			String childLabel = child.label().value();
			
			
			if(childLabel.equals(PhraseTypes.PROPOSITION_PHRASE)){
				
				TregexPattern tPattern = TregexPattern.compile(PhraseTypes.NOUN_PHRASE);
				
				TregexMatcher tMatcher = tPattern.matcher(child);
				
				
				if (tMatcher.find()) {
					
					Tree modifierTree = tMatcher.getMatch().firstChild();
					if(modifierTree!=null){
						
						if(modifierTree.firstChild().isLeaf()){
								modifiers.add(modifierTree.toString());
							
						}
						 else if(modifierTree.firstChild().firstChild().isLeaf()){
							modifiers.add(modifierTree.firstChild().toString()); //TODO sometimes we should not take the first child
						}
						
					}
					
				}
				
			}
		}
		
		return modifiers;
	}

	protected List<String> enrichWithModifier(Tree npSubtree, Tree sub) {
	
		List<String> modifiers = new ArrayList<String>();
		
		
		List<Tree> siblings = sub.siblings(npSubtree);
		
		if(siblings!=null && siblings.size() > 0){
			
			modifiers.addAll( extractAttributesForNouns(siblings));
		}
		else {
			//TODO NOT YET CHECKED Get the father of the subject 
			Tree parent = sub.parent(npSubtree);
			if(parent!=null){
				List<String> res = extractAttributesForNouns(parent.siblings(npSubtree));
				if(res!=null)
					modifiers.addAll( res);
			}
		}
		return modifiers;
	}

	/**
	 * Extract the verb/adjective and object from VP/ADJP 
	 * @param triple
	 * @param child
	 */
	protected void handleVP(SentenceTriple triple, Tree child) {
		Tree vpSubtree;
		vpSubtree = child;
		
		Tree pred = extractPredicate(vpSubtree);
		
		if(pred!=null){
			
			triple.setPredicate(pred.toString());
			Tree obj = extractObject(pred.siblings(vpSubtree));
			
			if(obj!=null){
				
				List<String> attr = enrichWithModifier(vpSubtree, obj);
				
				triple.setObject(obj.toString());
				
				triple.setObjectModifier(attr);
				
			}
		}
	}
	
	/**
	 * Deal with the SBAR tree branch of the syntax tree
	 * @param child
	 * @return
	 */
	protected SentenceTriple handleSBAR(Tree child) {
		//Navigate the PP tree to rich the S node
		
		TregexPattern tPattern = TregexPattern.compile("S");
		TregexMatcher tMatcher = tPattern.matcher(child);
		SentenceTriple tt = new SentenceTriple();

		while (tMatcher.find()) {
			
			
			Tree sentenceRoot = tMatcher.getMatch();
		
			//The NP must belong to first level children of the syntax tree
			
			if(sentenceRoot.firstChild().label().value().equals(PhraseTypes.NOUN_PHRASE)){
				this.handleNP(tt, sentenceRoot);
			}
			this.handleVP(tt, sentenceRoot);
			
		}
		return tt ;
	}
	
	
	/**
	 * Deal with the case where the syntax tree consists only of nouns
	 * Conisder the first noun as subject and the deepest noun as object
	 * E.g.: Number of faces of a shape (sub:number, obj:shape)
	 * @param triple
	 * @param root
	 */
	protected void handlePureNP(SentenceTriple triple, Tree root) {
	
		
		Tree subject = extractSubject(root);
		
		if(subject!=null){
			
			List<String> attr = enrichWithModifier(root, subject);
			attr.addAll(enrichFromPP(root,syntaxTree));
			
			triple.setSubject(subject.toString());
			triple.setSubjectModifier(attr);

			
		}
		
		int countNP = getPhraseCounts(root, PhraseTypes.NOUN_PHRASE);
		
		Tree object = extractDeepestNoun(root);

		if(object!=null && !object.equals(subject) && countNP > 1) {
			
			
				List<String> attr = enrichWithModifier(root, object);
				attr.addAll(enrichFromPP(object,syntaxTree));
				
				triple.setObject(object.toString());
				triple.setObjectModifier(attr);
			
		}
		
	}
	

	/**
	 * Check if the syntax tree contains a VP
	 * @param syntaxTree
	 * @return
	 */
	protected boolean isVPFreeSyntaxTree() {
		
		
		TregexPattern tPattern = TregexPattern.compile("VP");
		TregexMatcher tMatcher = tPattern.matcher(rootSyntaxTree);

		if (tMatcher.find()) {
			return false;
		}
		return true;
	}

	
	/**
	 * Get the number of occurrences of a given phrase type in the syntax tree
	 * @param root
	 * @param phraseType
	 * @return
	 */
	public int getPhraseCounts(Tree root, String phraseType){
		
		int countP = 0;
		// If there are further NPs in the sentence

		TregexPattern tPattern = TregexPattern.compile(phraseType);
		TregexMatcher tMatcher = tPattern.matcher(root);

		while (tMatcher.find()) {

			countP++;
		}
		return countP;
	}
}
