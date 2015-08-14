package hms.sentence.triplization;

import java.util.List;

public class MergedSentenceTriple {

	private Argument predicate;
	private List<List<Argument>> subject;
	private List<List<Argument>> object;
	private boolean containsSBAR ;
	
	
	
	public boolean isContainsSBAR() {
		return containsSBAR;
	}
	public void setContainsSBAR(boolean containsSBAR) {
		this.containsSBAR = containsSBAR;
	}
	public Argument getPredicate() {
		return predicate;
	}
	public void setPredicate(Argument predicate) {
		this.predicate = predicate;
	}
	public List<List<Argument>> getSubject() {
		return subject;
	}
	public void setSubject(List<List<Argument>> subject) {
		this.subject = subject;
	}
	public List<List<Argument>> getObject() {
		return object;
	}
	public void setObject(List<List<Argument>> object) {
		this.object = object;
	}
	

	
	
	
}
