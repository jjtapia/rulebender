package org.bng.simulate;

public interface CommandInterface 
{
	/**
	 * @return the string array that should be executed as a system call.
	 */
	public String[] getCommand();
	
	/**
	 * 
	 * @return the string that represents the full path to bionetgen.  This is required by
	 * some of the perl scripts written in bionetgen (specifically scan_var.pl at the moment). 
	 */
	public String getBNGFullPath();
	
}
