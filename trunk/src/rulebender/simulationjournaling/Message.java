package rulebender.simulationjournaling;

public class Message {
	private String m_messageType;
	private String m_messageDetails;
	
	/**
	 * Default constructor
	 */
	public Message() {
		setType(null);
		setDetails(null);
	} //Message (constructor)
	
	/**
	 * Constructor
	 * 
	 * @param type - message type
	 * @param details - message details
	 */
	public Message(String type, String details) {
		setType(type);
		setDetails(details);
	} //Message (constructor)
	
	/**
	 * Sets the message type
	 * 
	 * @param type - message type
	 */
	public void setType(String type) {
		m_messageType = type;
	} //setType
	
	/**
	 * Returns the message type
	 * 
	 * @return - message type
	 */
	public String getType() {
		return m_messageType;
	} //getType
	
	/**
	 * Adds details to the message
	 * 
	 * @param details - details
	 */
	public void setDetails(String details) {
		m_messageDetails = details;
	} //setDetails
	
	/**
	 * Returns the details stored in a message
	 * 
	 * @return - details
	 */
	public String getDetails() {
		return m_messageDetails;
	} //getDetails
	
} //Message (class)
