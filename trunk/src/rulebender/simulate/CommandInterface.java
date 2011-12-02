package rulebender.simulate;

public interface CommandInterface 
{
	/**
	 * @return the string array that should be executed as a system call.
	 */
	public String[] getCommand();
	
}
