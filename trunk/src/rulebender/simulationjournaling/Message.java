package rulebender.simulationjournaling;

public class Message {
	private String m_messageType;
	private String m_messageDetails;
	
	public Message() {
		setType(null);
		setDetails(null);
	} //Message (constructor)
	
	public Message(String type, String details) {
		setType(type);
		setDetails(details);
	} //Message (constructor)
	
	public void setType(String type) {
		m_messageType = type;
	} //setType
	
	public String getType() {
		return m_messageType;
	} //getType
	
	public void setDetails(String details) {
		m_messageDetails = details;
	} //setDetails
	
	public String getDetails() {
		return m_messageDetails;
	} //getDetails
	
} //Message (class)
