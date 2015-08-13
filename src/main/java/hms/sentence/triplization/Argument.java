package hms.sentence.triplization;

public class Argument {

	private String lemma;
	private  String partOfSpeech;
	public String getLemma() {
		return lemma;
	}
	public void setLemma(String lemma) {
		this.lemma = lemma;
	}
	public String getPartOfSpeech() {
		return partOfSpeech;
	}
	public void setPartOfSpeech(String partOfSpeech) {
		this.partOfSpeech = partOfSpeech;
	}
	@Override
	public String toString() {
		String res = lemma + "/" + partOfSpeech;
		return res;
	}
	
}
