package hms.sentence.triplization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class MultipleTripleExtractor {
	
	private final String MAIN_TIPRLE = "MAIN" ;
	private final String SBAR_TIPRLE = "SBAR" ;
	
	private static StanfordCoreNLP pipeline ;
	
	private TripleExtractor tripleExtractor;
	private Map<String,SentenceTriple> sentensTripleMap;

	
	public MultipleTripleExtractor(TripleExtractor tripleExtractor) {
		init();
		this.tripleExtractor = tripleExtractor;
		sentensTripleMap = new HashMap<String,SentenceTriple>();
	}
	
	
	private static void init(){
		if(pipeline ==null){
			RedwoodConfiguration.empty().capture(System.out).apply(); //Stop logging
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, parse");
			pipeline= new StanfordCoreNLP(props, false);
	        RedwoodConfiguration.current().clear().apply(); //Reactivate logging
		}
		
	}
		
	
	public TripleExtractor getTripleExtractor() {
		return tripleExtractor;
	}


	public void setTripleExtractor(TripleExtractor tripleExtractor) {
		this.tripleExtractor = tripleExtractor;
	}


	public Map<String, SentenceTriple> getSentensTripleMap() {
		return sentensTripleMap;
	}


	public void setSentensTripleMap(Map<String, SentenceTriple> sentensTripleMap) {
		this.sentensTripleMap = sentensTripleMap;
	}


	/**
	 * Extract triples (subject, predicate and object) from the sentences of a given text
	 * The implementation follow the algorithm described in:
	 * Rusu, Delia, et al. "Triplet extraction from sentences." Proceedings of the 10th International Multiconference" Information Society-IS. 2007.
	 * @param text
	 */
	public void extractTriples(String text) {
		

		int sentenceId = 0;
		
		Annotation document = pipeline.process(text);

		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			Tree tree = sentence.get(TreeAnnotation.class);
			tree.pennPrint();
			tripleExtractor.setSyntaxTree(tree);
		
			//Split the parse tree of the sentence to main part and SBAR (if any)
			List<Tree> res = tripleExtractor.splitSyntaxTree("SBAR");
			
			//Extract a triple from non SBAR sub-tree
			if(res.get(1) != null){
				tripleExtractor.setSyntaxTree(res.get(1));	
				SentenceTriple mainTriple = tripleExtractor.extractTriple();
				sentensTripleMap.put(sentenceId + "-" +MAIN_TIPRLE, mainTriple);
			}
			
			//Extract a triple from SBAR sub-tree 
			SentenceTriple sbarTriple =null;
			if(res.get(0) != null){
				tripleExtractor.setSyntaxTree(res.get(0));
				sbarTriple = tripleExtractor.handleSBAR(tripleExtractor.getSyntaxTree());
				sentensTripleMap.put(sentenceId + "-" +SBAR_TIPRLE , sbarTriple);
				
				
			}
			sentenceId ++ ;
		}

	}
	
	public static void main(String[] args) {
		
//		
//		String[] sentences = {"number of faces of a mathematical solid","entities owned by the subject", "artist whose work group are the likely creator of an artwork","street address that the subject is located at","educational institution attended by the subject","John killed Mary", "features depicted in a work", "Mary was killed by John", "fields related to this item",
//				"the political party of which this politician is a member", "people who speak a language",
//				"number of people who speak a language",
//				"the object is a country that recognizes the subject as its citizen","country a person represents when playing a sport",
//				"the institution holding the subject's archives","URL containing the full text for this item","pathogen of which this species is a long-term host",
//				"specify the work that an award was given to the creator for"};
		
		String[] sentences = {"female name of person of nice country of Japan"};
		
		TripleExtractor tet1 = new TripleExtractorStandard();

		for(String s: sentences){
		
			MultipleTripleExtractor  te = new MultipleTripleExtractor(tet1);
		
			te.extractTriples(s);
			
			Map<String, SentenceTriple> result = te.getSentensTripleMap();
			
		
			
			
			SentenceTriple mainTriple = result.get("0-" + te.MAIN_TIPRLE) ;
			SentenceTriple sbarTriple = result.get("0-"+te.SBAR_TIPRLE) ;
			
			SentenceTripleMerger m = new SentenceTripleMerger(mainTriple, sbarTriple);
			MergedSentenceTriple finalTriple = m.merge();
			
			System.out.println("Triple: MERGED: " + s );
			System.out.println("PRED: " + finalTriple.getPredicate());
			System.out.println("SUB: " + finalTriple.getSubject());
			System.out.println("OBJ: " + finalTriple.getObject());
			
//			System.out.println(" ..... Original Triples" );
//			for(Entry<String, SentenceTriple> e : result.entrySet()){
//				
//			
//				
//				System.out.println("Triple: " + e.getKey());
//				System.out.println("PRED: " + e.getValue().getPredicate());
//				System.out.println("SUB: " + e.getValue().getSubject() + " , MOD: " + e.getValue().getSubjectModifier());
//				System.out.println("OBJ: " + e.getValue().getObject() +  " , MOD: " + e.getValue().getObjectModifier());
//				
//			}

			System.out.println(" ..............");
	
		}
	
	}

}
