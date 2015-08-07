package hms.sentence.triplization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class SentenceTriplizer {

	
	private static Tree currentPredicate = null;
	private static Tree currentSubject = null;
	private static Tree currentAdj = null;
	
	private static Set<String> nounTagSet;
	private static Set<String> verbTagSet;
	private static Set<String> adjTagSet;
	private static Set<String> nounModifiersSet;
	
	private static StanfordCoreNLP pipeline ; 
	
	
	
	static{
		
		nounTagSet = new HashSet<String>();
		nounTagSet.add("NN");
		nounTagSet.add("NNP");
		nounTagSet.add("NNPS");
		nounTagSet.add("NNS");
		nounTagSet.add("PRP");
		
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
		
		init();
	}

	private static void init(){
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, parse");
		pipeline= new StanfordCoreNLP(props, false);
		
	}
	  
		/**
		 * Extract a triple of subject, predicate and object from a given sentence
		 * The implementation follow the algorithm described in:
		 * Rusu, Delia, et al. "Triplet extraction from sentences." Proceedings of the 10th International Multiconference" Information Society-IS. 2007.
		 * @param text
		 */
		public static Map<Integer,SentenceTriple> extractTriples(String text) {
			

			int sentenceId = 0;
			
			Map<Integer,SentenceTriple> tripleList = new HashMap<Integer,SentenceTriple>();
			
			Annotation document = pipeline.process(text);

			
			for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
				
				SentenceTriple triple = extractTriplesForOneSentence(sentence);
				tripleList.put(sentenceId, triple);
				sentenceId++;
			}
			

			return tripleList;
		}

		
		public static SentenceTriple extractTriplesForOneSentence(CoreMap sentence) {
			
			SentenceTriple triple = new SentenceTriple();
			Tree tree = sentence.get(TreeAnnotation.class);
			if(PRINT_TREE){
				tree.pennPrint();
			}
			Tree root = tree.firstChild();
			
			if(root.label().value().equals("NP")){ //e.g. female name, null(female name, null)
												   //e.g. name of person null(person, name)
			
				triple = extractTripleFromNPVP(root.firstChild().children());
				
				if(triple.getPredicate()==null){
					
					triple =  extractTriplesFromNPSBAR(root.firstChild().children());


				}
				if(triple.getPredicate() == null){
					triple = extractSubObjNoPred(tree, root);
				}
				
				
			}
			boolean containsSNP = false;
			boolean containsSVP = false;
			
			if(root.label().value().equals("S")||root.label().value().equals("SINV")||root.label().value().equals("UCP")  ){
				for(Tree child : root.children()) {
					if(child.label().value().equals("NP")){
						containsSNP = true;
					}
					if(child.label().value().equals("VP")){
						containsSVP = true;
					}
				}
				if(containsSNP && containsSVP){
					
					triple = extractTripleFromNPVP(root.children());
				
				}
				else if(containsSNP && !containsSVP){
					//Get the NP child
					
					for(Tree child : root.children()) {
						
						if(child.label().value().equals("NP")){
						
							triple = extractTripleFromNPVP(child.children());
					
							if(triple.getPredicate()==null){
								triple = extractSubObjNoPred(tree, child);
							}
							break;
						}
					}
				}

					
			}
			
			
			else if(root.label().value().equals("FRAG")){
				boolean containsNP= false;
				boolean containsSBAR= false;
				boolean containsPP= false;
				
				for(Tree child : root.children()) {
					if(child.label().value().equals("NP")){
						containsNP = true;
					}
					if(child.label().value().equals("SBAR")){
						containsSBAR = true;
					}
					
					if(child.label().value().equals("PP")){
						containsPP = true;

					}
				}
				if(containsNP && containsSBAR){  //e.g. Jack who killed Mary killed(Jack, Mary)
					triple =  extractTriplesFromNPSBAR(root.children());
				}
				else if(containsNP && containsPP){//e.g. Company where Jack works  works(Jack,Company)
					triple =  extractTriplesFromFRAGNPPP(root.children());
				}
				else if(containsNP && !containsPP && !containsSBAR){//e.g. Company where Jack works  works(Jack,Company)
					
					//Check if the NP in turn contains SBAR and NP
					for(Tree child : root.firstChild().children()) {
						
						if(child.label().value().equals("NP")){
							containsNP = true;
						}
						if(child.label().value().equals("SBAR")){
							containsSBAR = true;
						}
						
						if(child.label().value().equals("PP")){
							containsPP = true;

						}
					}
					if(containsNP && containsSBAR){  //e.g. Jack who killed Mary killed(Jack, Mary)
						triple =  extractTriplesFromNPSBAR(root.firstChild().children());
					}
					else if(containsNP && containsPP){//e.g. Company where Jack works  works(Jack,Company)
						triple =  extractTriplesFromFRAGNPPP(root.firstChild().children());
					}
					
					else if(containsNP && !containsPP && !containsSBAR){
						
						triple = extractTripleFromNPVP(root.firstChild().children());
						
						if(triple.getPredicate()==null){
							
							triple =  extractTriplesFromNPSBAR(root.firstChild().children());


						}
						if(triple.getPredicate() == null){
							triple = extractSubObjNoPred(tree, root);
						}
						

					}
				}
			}
			
			
			return triple;
			
			
		}


		private static SentenceTriple extractTriplesFromFRAGNPPP(Tree[] children) {

			SentenceTriple triple = new SentenceTriple();
			
			for (Tree child: children) {
			
				if(child.label().value().equals("NP")){
					
					Tree subject = extractSubjectRest(child);
					
					if(subject!=null){
						
						String attr = extractAttributesForNouns(subject.siblings(child));
						
						triple.setObject(attr + prettyPrint(subject));

					}
				}
				if(child.label().value().equals("PP")){
					
					for(Tree childOfChild : child.children()){
					
						if(childOfChild.label().value().equals("SBAR")){
							
							Tree[] sbarChildren = childOfChild.children();
							
							for(Tree sbarChild : sbarChildren){
								
								if(sbarChild.label().value().equals("S")){
									
									SentenceTriple triple2 = extractTripleFromNPVP(sbarChild.children());
									triple.setSubject(triple2.getSubject());
									triple.setPredicate(triple2.getPredicate());
								}
								
							}
						}
					}
				}
			}
			
			return triple;
		}

		/**
		 * Extract the components of PP 
		 * @param tree
		 * @param child
		 * @return
		 */
		private static SentenceTriple extractSubObjNoPred(Tree tree, Tree child) {
			
			SentenceTriple triple = new SentenceTriple();
			
			Tree sub = extractSubjectRest(child);

			if(sub!=null){
				
				String attr = extractAttributesForNouns(sub.siblings(child));
				
				triple.setSubject(attr + prettyPrint(sub));
			}
			//if the sibling is an PP you can extract the noun in that PP as object 
//			List<Tree> siblings = sub.siblings(tree);
			for(Tree sibling : child.children()){
			
				if(sibling.label().value().equals("PP")){
					Tree obj = extractSubjectRest(sibling);
					if(obj != null){
						String attr = extractAttributesForNouns(obj.siblings(sibling));
						triple.setObject(attr + prettyPrint(obj));

					}
				}
			}
			return triple;
		}

		private static SentenceTriple extractTriplesFromNPSBAR(Tree[] children) {
			
			SentenceTriple triple = new SentenceTriple();
			
			for (Tree child: children) {
			
				if(child.label().value().equals("NP")){
					
					Tree subject = extractSubjectRest(child);
					
					if(subject!=null){
						
						String attr = extractAttributesForNouns(subject.siblings(child));
						
						triple.setSubject(attr + prettyPrint(subject));

					}
				}
				
				if(child.label().value().equals("SBAR")){
					
					Tree[] sbarChildren = child.children();
					
					for(Tree sbarChild : sbarChildren){
						
						if(sbarChild.label().value().equals("S")){
					
								SentenceTriple triple2 = extractTripleFromNPVP(sbarChild.children());
								if(triple2.getSubject() !=null){
									triple.setObject(triple.getSubject());
									triple.setSubject(triple2.getSubject());

								}
								else{
									triple.setObject(triple2.getObject());

								}
								
								triple.setPredicate(triple2.getPredicate());
						}
						
					}
				}
				
			}
			return triple;
		}

		private static SentenceTriple extractTripleFromNPVP(Tree[] children) {
			
			SentenceTriple triple = new SentenceTriple();
			
			Tree npSubtree;
			Tree vpSubtree;
			for (Tree child: children) {
			
				//The subject is extracted from NP
				if(child.label().value().equals("NP")){
					npSubtree = child;
					
					currentSubject=null; //reset
					
					Tree sub = extractSubjectRest(npSubtree);
				
					if(sub!=null){
					
						String subStr = prettyPrint(sub);
						
						String attr = "" ;
						
						if(sub.siblings(npSubtree)!=null){
							 attr = extractAttributesForNouns(sub.siblings(npSubtree));
						}
						triple.setSubject(attr +subStr);
					}
				}
				//The predicate and object are extracted from VP
				else if(child.label().value().equals("VP")){
				
					vpSubtree = child;
					Tree pred = extractPredicateRest(vpSubtree);
					
					if(pred!=null){
						
						String predStr = prettyPrint(pred);
						
						triple.setPredicate(predStr);
					
					
					
						Tree obj = extractObject(pred.siblings(vpSubtree));
						
						if(obj!=null){
							String objStr = prettyPrint(obj);
							
							String attr ="";
							if(obj.siblings(vpSubtree) != null){
								
								attr = extractAttributesForNouns(obj.siblings(vpSubtree));
							}
							
							triple.setObject(attr + objStr);
						}
					}
				}
			}
			return triple;
		}

		private static String prettyPrint(Tree pred) {
			return pred.firstChild().label().value() +"/" + pred.label().value();
		}
		
		/**
		 * Extract the noun from NP branch of the sentence
		 * The extracted noun is the first noun in the tree
		 * @param npSubtree
		 * @return
		 */
		public static Tree extractSubject(Tree npSubtree) {
			
			Tree[] iter = npSubtree.children();
			
			for(Tree ch : iter){
				
				if(nounTagSet.contains(ch.label().value())){
					
					currentSubject = ch;
					return currentSubject;
						
				}
				else if (currentSubject == null){
					extractSubject(ch);
				}
					
			}
			return currentSubject;
		}
		
		public static Tree extractSubjectRest(Tree npSubtree) {
			currentSubject = null;
			return extractSubject(npSubtree);
		}
		
		
		/**
		 * Extract the verb from the VP branch of the sentence. 
		 * The extracted verb corresponds to the deepest verb descendant of the VP
		 * @param vpSubtree
		 * @return
		 */
		private static Tree extractPredicate(Tree vpSubtree) {
			
			
			if(vpSubtree.isLeaf())
				return currentPredicate;
			List<Tree> subTrees = vpSubtree.getChildrenAsList();

			for(Tree ch : subTrees){
			
				if(verbTagSet.contains(ch.label().value())){
					currentPredicate = ch;
				}
				
				extractPredicate(ch);
			}
			return currentPredicate;
			
		}
		
		private static Tree extractPredicateRest(Tree vpSubtree) {
			currentPredicate = null;
			return extractPredicate(vpSubtree);
			
		}
		
		/**
		 * Extract the object from sibling subtrees of the VP containing the predicate
		 * @param vpSiblings
		 * @return
		 */
		private static Tree extractObject(List<Tree> vpSiblings) {
			
			if(vpSiblings == null)
				return null;
			
			Tree object = null;
			for(Tree ch : vpSiblings){
				
				if(ch.label().value().equals("PP") || ch.label().value().equals("NP") ){
					
					object = extractSubjectRest(ch);
					if(object !=null)
						return object;
				}
				else if(ch.label().value().equals("ADJP")){
					object = extractAdjective(ch);
					
					if(object !=null)
					
						return object;
				}
			}
			return object;
			
		}

		
		public static Tree extractAdjective(Tree npSubtree) {
			
			Tree[] iter = npSubtree.children();
			
			for(Tree ch : iter){
				
				
				if(adjTagSet.contains(ch.label().value())){
					
					currentAdj = ch;
						
				}
				else{
					extractSubjectRest(ch);
				}
					
			}
			return currentAdj;
		}
		
		public static Tree extractAdjectiveRest(Tree npSubtree) {
			
			currentAdj = null;
			return extractAdjective(npSubtree);
					
		}
		
		
		/**
		 * Extract attributes (modifier) for nouns. Those are normally adjectives.
		 * @param npTree
		 * @return
		 */
		public static String extractAttributesForNouns(List<Tree> npTree){
			
			if(npTree == null)
				return "";
			
			StringBuffer res = new StringBuffer();
			for(Tree e : npTree){
				
				if(nounModifiersSet.contains(e.label().value())){
					
					res.append(prettyPrint(e)).append(" ");
				}
				
			}
			
			return res.toString();
		}

//		public static String extractAttributesForVerb(List<Tree> npTree){
//			StringBuffer res = new StringBuffer();
//			for(Tree e : npTree){
//				if(e.label().value().equals("DT")){
//					res.append(e.firstChild()).append(" ");
//				}
//				if(e.label().value().equals("PRP")){
//					res.append(e.firstChild()).append(" ");
//				}
//				if(e.label().value().equals("POS")){
//					res.append(e.firstChild()).append(" ");
//				}
//				if(e.label().value().equals("JJ")){
//					res.append(e.firstChild()).append(" ");
//				}
//				if(e.label().value().equals("CD")){
//					res.append(e.firstChild()).append(" ");
//				}
//				if(e.label().value().equals("ADJP")){
//					res.append(e.firstChild()).append(" ");
//				}
//				if(e.label().value().equals("QP")){
//					res.append(e.firstChild()).append(" ");
//				}
//			}
//			return res.toString();
//		}
		
		static boolean PRINT_TREE = false;
		
		public static void main(String[] args) {
//			circumstances of a person's death
//			language that a person learned natively
//			method (or type) of distribution for the subject
//			country of origin of the creative work or subject item
//			Map<Integer, SentenceTriple> results = extractTriples("person or institution organizing an event");
			
			Map<Integer, SentenceTriple> results = extractTriples("subject has brother");

			
			for(Entry<Integer, SentenceTriple> res: results.entrySet()){
				SentenceTriple triple = res.getValue();
				System.out.println(res.getKey());
//				System.out.println(triple.getSubject() + "\t" + triple.getPredicate() + "\t" + triple.getObject());
				System.out.println( triple.getPredicate() + "(" + triple.getSubject() + "," +  triple.getObject() + ")");
				System.out.println("...");
			}
		}
}
