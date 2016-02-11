package hms.util;

import hms.sentence.triplization.TagSets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.international.arabic.ArabicHeadFinder.TagSet;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.logging.RedwoodConfiguration;

public class NLPUtil {

	
	private static StanfordCoreNLP pipeline ;
	private static StanfordCoreNLP pipelineNoLemma ;
	
	public static final String customStopWordList = "``,a,an,and,are,is,to,for,from,as,at,be,but,by,for,from,if,in,into,is,it,no,not,of,on,or,such,that,the,their,then,there,these,they,this,through,to,was,will,with,-lrb-,-rrb-,-lsb-,-rsb-,during,'s";

	static{
		init();
	}
	
	private static void init(){
		if(pipeline ==null){
			RedwoodConfiguration.empty().capture(System.out).apply(); //Stop logging
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos, parse, lemma"); 
			pipeline= new StanfordCoreNLP(props, false);
	        RedwoodConfiguration.current().clear().apply(); //Reactivate logging
		}
		if(pipelineNoLemma ==null){
			Properties props = new Properties();
			props.put("annotators", "tokenize, ssplit, pos"); 
			pipelineNoLemma= new StanfordCoreNLP(props, false);
		}
		
	}
	
	/**
	 * Extract head word from a sentence
	 * @param text
	 * @return
	 */
	public static Collection<String> identifyHeadWord(String text) {
		
//		RedwoodConfiguration.empty().capture(System.out).apply();

		Annotation document = pipeline.process(text);
	
		Collection<String> headWordList = new HashSet<String>();

		for (CoreMap sentence : document.get(SentencesAnnotation.class)) {

			Tree tree = sentence.get(TreeAnnotation.class);

			HeadFinder headFinder = new PennTreebankLanguagePack().headFinder();

			TregexPattern tPattern = TregexPattern.compile("NP");
			TregexMatcher tMatcher = tPattern.matcher(tree);


			while (tMatcher.find()) {

				Tree nounPhrase = tMatcher.getMatch();
				Tree headConstituent = null;
				try{
					headConstituent = headFinder.determineHead(nounPhrase);
				}
				catch(IllegalArgumentException e){
					System.err.println("NullPointerException");
					System.out.println("Contnue.....");
					continue;
				}
				catch(Exception e){
					System.err.println("NullPointerException");
					System.out.println("Contnue.....");
					continue;
				}
				
				List<Tree> ff = headConstituent.getLeaves();

				for (Tree f : ff) {
					if(!isStopWord(f.toString().trim())){
						headWordList.add(f.toString().toLowerCase().trim());
					}

				}

			}

		}
//		RedwoodConfiguration.current().clear().apply();

		return headWordList;
	}

	
	public static List<String> tokenize(String text, String stopTag){
		List<String> tokenList = new ArrayList<String>();
		
		
		Annotation document = pipelineNoLemma.process(text);  
		for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
			
			for(CoreLabel token: sentence.get(TokensAnnotation.class)){
				if(!token.tag().equals(stopTag)){
					tokenList.add(token.value());
				}
			}
	    
		}
		return tokenList;
	}
	
	public static Map<String,String> tokenizeAndLemmatize(String text, String stopTag){
		Map<String,String> tokenList = new HashMap<String, String>();
		
		
		Annotation document = pipeline.process(text);  
		for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
			
			for(CoreLabel token: sentence.get(TokensAnnotation.class)){
				if(!token.tag().equals(stopTag)){
					tokenList.put(token.lemma(),token.tag());
				}
			}
	    
		}
		return tokenList;
	}
	
	public static int getFirstPropositionLocation(String text){
		
		int propositionIndex = 0;
		Annotation document = pipelineNoLemma.process(text);  
		for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
			
			for(CoreLabel token: sentence.get(TokensAnnotation.class)){
				if(token.tag().equals("IN")){
					return propositionIndex;
				}
				propositionIndex ++ ;

			}
	    
		}
		return -1;
	}
	
	
	public static boolean containVerb(String text){
		Annotation document = pipelineNoLemma.process(text);  
		for(CoreMap sentence: document.get(SentencesAnnotation.class)) {    
			
			for(CoreLabel token: sentence.get(TokensAnnotation.class)){
				if(TagSets.verbTagSet.contains(token.tag())){
					return true;
				}

			}
	    
		}
		return false;
	}
	
	/**
	 * Extract lemmas from a given text
	 * @param text
	 * @return
	 */
	public static List<String> lemmatizeAndRemoveStopWords(String text)
    {
 		RedwoodConfiguration.empty().capture(System.out).apply();
		
		List<String> lemmaList = new ArrayList<String>();
     
        if(text!=null){
        	
        	
        	text = text.toLowerCase();
        	text = text.replace("[", "").replace("]", "").replace("{", "").replace("}", "").replace("(", "").replace(")", "").replace(",", "");
        	
        	Annotation document = pipeline.process(text);  
        
	        for(CoreMap sentence: document.get(SentencesAnnotation.class))
	        {    
	        	if(sentence.get(TokensAnnotation.class).size() == 2){
 	               
	        		String finalLemma = "";
	        			        		
	        		List<CoreLabel> tokenList = sentence.get(TokensAnnotation.class);
	        		
	        		CoreLabel fristToken = tokenList.get(0);
	        		CoreLabel secondToken = tokenList.get(1);
	        		
	        		
	        		
	        		if((TagSets.adjTagSet.contains(fristToken.tag()) && TagSets.nounTagSet.contains(secondToken.tag()))){//||(TagSets.verbTagSet.contains(fristToken.tag()) &&  !TagSets.nounTagSet.contains(secondToken.tag()) && !TagSets.adjTagSet.contains(secondToken.tag()))){
	        			finalLemma += fristToken.lemma() + " " +  secondToken.lemma(); 
		        		lemmaList.add(finalLemma.toLowerCase().trim());

	        		}
//	        		else{
//		        		
//	        			for(CoreLabel token: sentence.get(TokensAnnotation.class))
//		 	            {       
//		 	            	//remove stop words
//		 	            	
//		 	            	if(token.lemma().equals("-lsb-") || token .lemma().equals("-rsb-") || customStopWordList.contains(token.lemma())||isStopWord(token.lemma())){
//		 	            		continue;
//		 	            	
//		 	            	}
//		 	            	String lemma = token.get(LemmaAnnotation.class); 
//		 	                lemmaList.add(lemma.toLowerCase().trim());
//		 	            }
//	        		}
	        		
	        	
	        		
	        	}
//	        	else{
	        		
	        		for(CoreLabel token: sentence.get(TokensAnnotation.class))
	 	            {       
	 	            	//remove stop words
	 	            	
	 	            	if(token.lemma().equals("-lsb-") || token .lemma().equals("-rsb-") || customStopWordList.contains(token.lemma())||isStopWord(token.lemma())){
	 	            		continue;
	 	            	
	 	            	}
	 	            	String lemma = token.get(LemmaAnnotation.class); 
	 	                lemmaList.add(lemma.toLowerCase().trim());
	 	            }
//	        	}
	           
	        }
        }
	     
        RedwoodConfiguration.current().clear().apply();

        return lemmaList;
    }
	
	
	
	public static List<String> lemmatizeAndRemoveStopWords(String text,Set<String> pos)
    {
		RedwoodConfiguration.empty().capture(System.out).apply();

		

        List<String> lemmaList = new ArrayList<String>();
     
        if(text!=null){
        	
        	
        	text = text.toLowerCase();
        	text = text.replace("[", "").replace("]", "").replace("{", "").replace("}", "").replace("(", "").replace(")", "").replace(",", "");
        	
        	Annotation document = pipeline.process(text);  
        
	        for(CoreMap sentence: document.get(SentencesAnnotation.class))
	        {    
	            for(CoreLabel token: sentence.get(TokensAnnotation.class))
	            {       
	            	//remove stop words
	            	if(token.lemma().equals("-lsb-") || token .lemma().equals("-rsb-") || customStopWordList.contains(token.lemma())||WikidataStopWords.isStopWord(token.lemma())){
	            		continue;
	            	
	            	}
	            	String lemma = token.get(LemmaAnnotation.class); 
	            	
	            	if(pos.contains(token.tag())){
	            		lemmaList.add(lemma);
	            	}
	            }
	        }
        }
	     
        RedwoodConfiguration.current().clear().apply();

        return lemmaList;
    }
	
	
	public static List<String> getWords(Collection<String> text,Set<String> pos)
    {
		List<String> lemmaList = new ArrayList<String>();
		 
		if(text != null && text.size() > 0){
		
		 
			 for(String t : text){
				 
				 lemmaList.addAll(getWords(t, pos));
			 }
		}
		 
		return lemmaList;
    }
	public static List<String> getWords(String text,Set<String> pos)
    {
		RedwoodConfiguration.empty().capture(System.out).apply();

        List<String> lemmaList = new ArrayList<String>();
     
        if(text!=null){
        
        	text = text.toLowerCase();
            Annotation document = pipeline.process(text);  

        
	        for(CoreMap sentence: document.get(SentencesAnnotation.class))
	        {    
	            for(CoreLabel token: sentence.get(TokensAnnotation.class))
	            {       
	            	
	            	if(pos.contains(token.tag())){
	            	
	            		String lemma = token.get(LemmaAnnotation.class); 
	            		if(!WikidataStopWords.isStopWord(lemma)){
	            			lemmaList.add(lemma);
	            		}
	            	}
	            }
	        }
        }
	     
        RedwoodConfiguration.current().clear().apply();

        return lemmaList;
    }
	
	
	public static List<String> getSentences(String text)
    {
		RedwoodConfiguration.empty().capture(System.out).apply();

		List<String> sentenceList = new ArrayList<String>();
		
        Annotation document = pipeline.process(text);  

        int index = 0;
        for(CoreMap sentence: document.get(SentencesAnnotation.class)){
        	
        	sentenceList.add(index, sentence.toString());
        	index++;
        }
        
        RedwoodConfiguration.current().clear().apply();

        return sentenceList;
    }
	
	
	public static boolean isStopWord(String word){
		
		return customStopWordList.contains(word)||WikidataStopWords.isStopWord(word);
		
	}
	public static void main(String[] args) {
//		System.out.println(identifyHeadWord("the nice big house is great"));
		
//		System.out.println(getNouns("subject has the object as their sister (female sibling)"));
		
//		System.out.println(lemmatizeAndRemoveStopWords("meidical condition", TagSets.nounTagSet));
		System.out.println(tokenize("educated at","IN"));
		System.out.println(tokenize("student of","IN"));
		System.out.println(getFirstPropositionLocation("educated at"));
		System.out.println(getFirstPropositionLocation("alma mater of"));
		
		System.out.println(containVerb("educated at"));
		System.out.println(containVerb("college attended"));
		System.out.println(containVerb("alma mater"));

	}
}
