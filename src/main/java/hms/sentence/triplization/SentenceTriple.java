package hms.sentence.triplization;

public class SentenceTriple {

	private String subject;
	private String predicate;
	private String object;
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getPredicate() {
		return predicate;
	}
	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}
	public String getObject() {
		return object;
	}
	public void setObject(String object) {
		this.object = object;
	}
	
	@Override
	public String toString() {
		StringBuffer res = new StringBuffer();
		res.append("Sub:").append(subject).append(" , Pred:").append(predicate).append(" , Obj:").append(object);
		return res.toString();
	}
	
}
