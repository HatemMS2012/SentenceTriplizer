package hms.sentence.triplization;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;


public class SentenceTripleMerger {
	
	private static final String SUB_OBJ_SEP = ",";
	private static final String MODIFIER_SPE = ",";
	private SentenceTriple mainTriple;
	private SentenceTriple sbarTriple;
	
	public SentenceTripleMerger(SentenceTriple mainTriple, SentenceTriple sbarTriple) {
		
		
		this.mainTriple = mainTriple;
		this.sbarTriple = sbarTriple;
	
	}
	
	
	public SentenceTriple getMainTriple() {
		return mainTriple;
	}


	public void setMainTriple(SentenceTriple mainTriple) {
		this.mainTriple = mainTriple;
	}


	public SentenceTriple getSbarTriple() {
		return sbarTriple;
	}


	public void setSbarTriple(SentenceTriple sbarTriple) {
		this.sbarTriple = sbarTriple;
	}



	public MergedSentenceTriple merge11(){
		
		MergedSentenceTriple finalTriple = new MergedSentenceTriple();
		
		if(sbarTriple == null){
			
			finalTriple = handleNoSbarTriple(mainTriple);
			
			return finalTriple;
		
		}
		if(mainTriple == null && sbarTriple !=null){
			finalTriple = handleNoSbarTriple(sbarTriple);
			return finalTriple;
		}
		
		if(mainTriple !=null && sbarTriple != null){
			
			
		}
		return finalTriple;
	}

	
	/**
	 * Apply this procedure if the sbar triple is null
	 * @return
	 */
	private MergedSentenceTriple handleNoSbarTriple(SentenceTriple triple) {
		MergedSentenceTriple finalTriple = new MergedSentenceTriple();
		
		
		
		List<Argument> mainSubject = null;
		List<String> mainSubjectModifiers = triple.getSubjectModifier();
		
		if(triple.getSubject()!=null){
			mainSubject = mergeArgumentModifiers(triple.getSubject(), mainSubjectModifiers);
		}
				
		List<Argument> mainObject = null;
			
		List<String> mainObjectModifiers = triple.getObjectModifier();
			if(triple.getObject()!=null){
				mainObject = mergeArgumentModifiers(triple.getObject(), mainObjectModifiers);
		}
			
		Argument mainPred = null;
		if(triple.getPredicate() !=null){
			 mainPred = cleanArgument(triple.getPredicate())  ;
		}	
			
		List<List<Argument>> mainSubjectList = new ArrayList<List<Argument>>();
		mainSubjectList.add(mainSubject);
		finalTriple.setSubject(mainSubjectList);
		
		List<List<Argument>> mainObjectList = new ArrayList<List<Argument>>();
		mainObjectList.add(mainObject);
		finalTriple.setObject(mainObjectList);
		finalTriple.setPredicate(mainPred);
		
		return finalTriple;
	}
	
	public MergedSentenceTriple merge(){
		MergedSentenceTriple finalTriple = new MergedSentenceTriple();
		
		if(mainTriple == null && sbarTriple==null){
			
			return finalTriple;
		}
		if(mainTriple == null && sbarTriple!=null){
			mainTriple = sbarTriple;
			sbarTriple = null;
		}
		else if(mainTriple.getSubject() == null && mainTriple.getObject() == null && mainTriple.getPredicate() == null && sbarTriple!=null){
			if(sbarTriple.getSubject()!=null || sbarTriple.getObject() != null || sbarTriple.getPredicate() !=null){
				mainTriple = sbarTriple;
				sbarTriple = null;
			}
		}

		//Main
		List<Argument> mainSubject = null;
		List<String> mainSubjectModifiers = mainTriple.getSubjectModifier();
		if(mainTriple.getSubject()!=null){
			mainSubject = mergeArgumentModifiers(mainTriple.getSubject(), mainSubjectModifiers);
		}
				
		List<Argument> mainObject = null;
			List<String> mainObjectModifiers = mainTriple.getObjectModifier();
			if(mainTriple.getObject()!=null){
				mainObject = mergeArgumentModifiers(mainTriple.getObject(), mainObjectModifiers);
		}
		
		Argument mainPred = null;
		if(mainTriple.getPredicate() !=null){
			 mainPred = cleanArgument(mainTriple.getPredicate())  ;
		}
				
				
		if(sbarTriple == null){
		
			List<List<Argument>> mainSubjectList = new ArrayList<List<Argument>>();
			mainSubjectList.add(mainSubject);
			finalTriple.setSubject(mainSubjectList);
			
			List<List<Argument>> mainObjectList = new ArrayList<List<Argument>>();
			mainObjectList.add(mainObject);
			finalTriple.setObject(mainObjectList);
			finalTriple.setPredicate(mainPred);
			
			return finalTriple;
		
		}
		
		
		
		//SBAR
		finalTriple.setContainsSBAR(true);
		
		List<Argument> sbarSubject = null;
		List<String> sbarSubjectModifiers = sbarTriple.getSubjectModifier();
		if(sbarTriple.getSubject() != null){
			sbarSubject = mergeArgumentModifiers(sbarTriple.getSubject(), sbarSubjectModifiers);
		}
		
		List<Argument> sbarObject = null;
		List<String> sbarObjectModifiers = sbarTriple.getObjectModifier();
		if(sbarTriple.getObject() !=null){
			sbarObject = mergeArgumentModifiers(sbarTriple.getObject(), sbarObjectModifiers);
		}
		Argument sbarPred = null;
		if(sbarTriple.getPredicate()!=null){
			sbarPred = cleanArgument(sbarTriple.getPredicate());
		}
		
		
		
		//Case 1: MAIN is full sentence with subject, predicate and object
		if(mainTriple.getSubject()!=null && mainTriple.getObject() !=null & mainTriple.getPredicate() !=null){
			
			//The subject is a list
			List<List<Argument>> finalSubjectList = new ArrayList<List<Argument>>();
			finalSubjectList.add(mainSubject);
			finalSubjectList.add(mainObject);
			List<Argument> predicateList = new ArrayList<Argument>();
			predicateList.add(mainPred);
			finalSubjectList.add(predicateList);
			finalTriple.setSubject(finalSubjectList);
									
			List<List<Argument>> finalObjectList = new ArrayList<List<Argument>>();
			
			
			if(sbarSubject !=null){
				finalObjectList.add(sbarSubject);

				
			}
			
			if(sbarObject !=null){
				finalObjectList.add(sbarObject);
			
			}
			
			finalTriple.setObject(finalObjectList);
			
			
			finalTriple.setPredicate(sbarPred);
			
		}
		
		
		//Case 2: MAIN contains only subject and SBAR contains only object
		if(mainTriple.getSubject()!=null && mainTriple.getObject() ==null & mainTriple.getPredicate() ==null && sbarTriple.getSubject() ==null && sbarTriple.getPredicate() !=null){
			
	
			List<List<Argument>> finalSubjectList = new ArrayList<List<Argument>>();
			finalSubjectList.add(mainSubject);
			
			finalTriple.setSubject(finalSubjectList);
			
			
			if(sbarObject !=null){
				
				List<List<Argument>> finalObjectList = new ArrayList<List<Argument>>();
				finalObjectList.add(sbarObject);
				finalTriple.setObject(finalObjectList);
								
			}
		
			finalTriple.setPredicate(sbarPred);
		}

		//Case 3: MAIN contains only subject and SBAR is full
		
		if(mainTriple.getSubject()!=null && mainTriple.getObject() ==null & mainTriple.getPredicate() ==null && sbarTriple.getSubject() != null  && sbarTriple.getPredicate()  !=null &&  sbarTriple.getObject() !=null){
			
			List<List<Argument>> finalSubjectList = new ArrayList<List<Argument>>();
			finalSubjectList.add(mainSubject);
			finalTriple.setSubject(finalSubjectList);

			List<List<Argument>> finalObjectList = new ArrayList<List<Argument>>();
			finalObjectList.add(sbarSubject);
			finalObjectList.add(sbarObject);
			List<Argument> predicateList = new ArrayList<Argument>();
			predicateList.add(sbarPred);
			finalObjectList.add(predicateList);
			
			finalTriple.setObject(finalObjectList);
			

			
		}
		
		//Case 3: MAIN contains only subject and SBAR is full
		
		if(mainTriple.getSubject()!=null && mainTriple.getObject() ==null & mainTriple.getPredicate() ==null && sbarTriple.getSubject() != null  && sbarTriple.getPredicate()  !=null &&  sbarTriple.getObject() ==null){
					
					List<List<Argument>> finalSubjectList = new ArrayList<List<Argument>>();
					finalSubjectList.add(mainSubject);
					finalTriple.setObject(finalSubjectList);

					List<List<Argument>> finalObjectList = new ArrayList<List<Argument>>();
					finalObjectList.add(sbarSubject);
				
					finalTriple.setSubject(finalObjectList);
					
//					List<Argument> predicateList = new ArrayList<Argument>();
//					predicateList.add(sbarPred);
					finalTriple.setPredicate(sbarPred);

					
		}
				
		
		//Case 4: MAIN contains predicate and object and SBAR is full
		if(mainTriple.getSubject()==null && mainTriple.getObject() !=null & mainTriple.getPredicate() !=null && sbarTriple.getSubject() != null  && sbarTriple.getPredicate()  !=null &&  sbarTriple.getObject() !=null){
			
			
			List<List<Argument>> finalSubjectList = new ArrayList<List<Argument>>();
			finalSubjectList.add(mainObject);
			finalTriple.setSubject(finalSubjectList);

			List<List<Argument>> finalObjectList = new ArrayList<List<Argument>>();
			finalObjectList.add(sbarSubject);
			finalObjectList.add(sbarObject);
			List<Argument> predicateList = new ArrayList<Argument>();
			predicateList.add(sbarPred);
			finalObjectList.add(predicateList);
			finalTriple.setObject(finalObjectList);

			finalTriple.setPredicate(sbarPred);
		}
		
		
		
		return finalTriple;
	}
	
	public List<Argument> mergeArgumentModifiers(String argument, List<String> modifiers){
		
		
		List<Argument> result = new ArrayList<Argument>();
		int index = 0 ;
		Argument arg = cleanArgument(argument);
		
		result.add(index++, arg);
		
		if(modifiers.size() > 0){
			
			for(String modifier:modifiers){
				Argument arg2 = cleanArgument(modifier);
				
//				if(!arg2.getPartOfSpeech().equals("DT") && !arg2.getPartOfSpeech().equals("PRP$") ){
					result.add(index++,arg2);
//				}
			}
			
		}
		
		return result;
	}
	
	public Argument cleanArgument(String argument){
		
		argument = argument.replace("(", "").replace(")", "");
		String[] argumentElements = argument.split(" ");
		if(argumentElements.length == 2){
			String argumentValue = argumentElements[1] ;
			String argumentPOS = argumentElements[0] ;
			Argument arg = new Argument();
			arg.setLemma(argumentValue);
			arg.setPartOfSpeech(argumentPOS);
			return arg;
		}
		return null;
	}

}
