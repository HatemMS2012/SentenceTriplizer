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
	
	
	private static StanfordCoreNLP pipeline ;
	
	private TripleExtractor tripleExtractor;
	private Map<Integer,SentenceTriple> sentensTripleMap;

	
	public MultipleTripleExtractor(TripleExtractor tripleExtractor) {
		init();
		this.tripleExtractor = tripleExtractor;
		sentensTripleMap = new HashMap<Integer,SentenceTriple>();
	}
	
	
	private static void init(){
		if(pipeline ==null){
			RedwoodConfiguration.empty().capture(System.out).apply();
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, parse");
			pipeline= new StanfordCoreNLP(props, false);
	        RedwoodConfiguration.current().clear().apply();
		}
		
	}
	
	
	
	
	
	public TripleExtractor getTripleExtractor() {
		return tripleExtractor;
	}


	public void setTripleExtractor(TripleExtractor tripleExtractor) {
		this.tripleExtractor = tripleExtractor;
	}


	public Map<Integer, SentenceTriple> getSentensTripleMap() {
		return sentensTripleMap;
	}


	public void setSentensTripleMap(Map<Integer, SentenceTriple> sentensTripleMap) {
		this.sentensTripleMap = sentensTripleMap;
	}


	/**
	 * Extract a triple of subject, predicate and object from a given sentence
	 * The implementation follow the algorithm described in:
	 * Rusu, Delia, et al. "Triplet extraction from sentences." Proceedings of the 10th International Multiconference" Information Society-IS. 2007.
	 * @param text
	 */
	public void extractTriples(String text) {
		

		int sentenceId = 0;
		
		
		
		Annotation document = pipeline.process(text);

		
		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {
			Tree tree = sentence.get(TreeAnnotation.class);
//			tree.pennPrint();
			tripleExtractor.setSyntaxTree(tree);
			//parse the SBAR sub tree
			List<Tree> res = tripleExtractor.splitSyntaxTree("SBAR");
			
			//Parse the non SBAR sub-tree
			tripleExtractor.setSyntaxTree(res.get(1));	
			SentenceTriple t2 = tripleExtractor.extractTriple();
			sentensTripleMap.put(sentenceId++, t2);
			
			SentenceTriple t1 =null;
				
			if(res.get(0) != null){
				tripleExtractor.setSyntaxTree(res.get(0));
				t1 = tripleExtractor.handleSBAR(res.get(0));
				sentensTripleMap.put(sentenceId++, t1);
				
			}
	
		}

	}
	
	public static void main(String[] args) {
		
		
		String[] sentences = {"John killed Mary", "features depicted in a work", "Mary was killed by John", "fields related to this item",
				"the political party of which this politician is a member", "people who speak a language",
				"number of people who speak a language",
				"the object is a country that recognizes the subject as its citizen","country a person represents when playing a sport",
				"the institution holding the subject's archives","URL containing the full text for this item","pathogen of which this species is a long-term host",
				"specify the work that an award was given to the creator for"};
		
//		String[] sentences = {"the institution holding the subject's archives"};
		
		TripleExtractor tet1 = new TripleExtractorStandard();

		for(String s: sentences){
		
			MultipleTripleExtractor  te = new MultipleTripleExtractor(tet1);
		
			te.extractTriples(s);
			
			Map<Integer, SentenceTriple> result = te.getSentensTripleMap();
			
			System.out.println(s);
			
			for(Entry<Integer, SentenceTriple> e : result.entrySet()){
				System.out.println("Triple: " + e.getKey());
				System.out.println("PRED: " + e.getValue().getPredicate());
				System.out.println("SUB: " + e.getValue().getSubject() + " , MOD: " + e.getValue().getSubjectModifier());
				System.out.println("OBJ: " + e.getValue().getObject() +  " , MOD: " + e.getValue().getObjectModifier());
				
			}

			System.out.println(" ..............");
	
		}
	
	}

}
