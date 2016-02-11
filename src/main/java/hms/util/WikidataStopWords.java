package hms.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class WikidataStopWords {

	private static Set<String> stopWordList = new HashSet<String>();
	
	static{
		stopWordList.add("wikidata");
		stopWordList.add("where");
		stopWordList.add("about");
		stopWordList.add("property");
		stopWordList.add("properties");
		stopWordList.add("item");
		stopWordList.add("subject");
		stopWordList.add("be");
		stopWordList.add("different");
		stopWordList.add("until");
		stopWordList.add("from");
		stopWordList.add("wikipedium");
		stopWordList.add("wikipedia");
		stopWordList.add("ii");
		stopWordList.add(".");
		stopWordList.add("iii");
		stopWordList.add("iv");
		stopWordList.add("v");
		stopWordList.add("vi");
		stopWordList.add("vii");
		stopWordList.add("viii");
		stopWordList.add("ix");
		stopWordList.add("x");
		stopWordList.add("xi");
		stopWordList.add("xii");
		stopWordList.add("xiii");
		stopWordList.add("des");
		stopWordList.add("fn");
		stopWordList.add("fe");
		stopWordList.add("wikimedia disambiguation page");
		stopWordList.add("item with given name property");
		stopWordList.add("wikimedia list article");
		stopWordList.add("à");
		stopWordList.add("page");
		stopWordList.add("et");
		stopWordList.add("van");
		stopWordList.add("a.");
		
		
		
		
		stopWordList.add("for");
		stopWordList.add("which");
		stopWordList.add("who");
		stopWordList.add("items");
		stopWordList.add("a");
		stopWordList.add("an");
		stopWordList.add("on");
		stopWordList.add("me");
		stopWordList.add("being");
		stopWordList.add("being");
		stopWordList.add("one");
		stopWordList.add("de");
		stopWordList.add("often");
		stopWordList.add("etc");
		stopWordList.add("wikimedium");
		stopWordList.add("disambiguation");
		stopWordList.add("certain");
		stopWordList.add("un");
		stopWordList.add("BitTorrent");
		stopWordList.add("Wikimedia");
		
		
		loadStopWords("stopwords_en.txt");
	}
	
	public static boolean isStopWord(String term){
		return stopWordList.contains(term);
	}
	
	private static void loadStopWords(String file){
		
		FileInputStream fstream;
		try {
			fstream = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

			String strLine;

			//Read File Line By Line
			while ((strLine = br.readLine()) != null)   {
			
				stopWordList.add(strLine);
			}

			//Close the input stream
			br.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
}
