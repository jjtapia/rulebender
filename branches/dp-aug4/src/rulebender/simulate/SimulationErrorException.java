package rulebender.simulate;

public class SimulationErrorException extends InterruptedException 
{
	private String m_errorMessage;

	public SimulationErrorException(String message)
	{
		setErrorMessage(message);
	}
	public String getErrorMessage() {
		return m_errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.m_errorMessage = errorMessage;
	}
	
	
}
