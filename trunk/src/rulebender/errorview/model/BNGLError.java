package rulebender.errorview.model;

public class BNGLError 
{
	private String m_filePath;
	private int m_lineNumber;
	private String m_message;
	
	public BNGLError(String filePath, int line, String message)
	{
		setFilePath(filePath);
		setLineNumber(line);
		setMessage(message);
	}

	public String getFilePath() {
		return m_filePath;
	}

	public void setFilePath(String m_filePath) {
		this.m_filePath = m_filePath;
	}

	public String getMessage() {
		return m_message;
	}

	public void setMessage(String m_message) {
		this.m_message = m_message;
	}

	public int getLineNumber() {
		return m_lineNumber;
	}

	public void setLineNumber(int m_lineNumber) {
		this.m_lineNumber = m_lineNumber;
	}
	
	public String toString()
	{
		return "Path: " + m_filePath + "\n" +
			   "Line: " + m_lineNumber + "\n" +
			   "Message: " + m_message;
	}
}
