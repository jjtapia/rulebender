package rulebender.simulate;
import java.io.File;
import java.io.IOException;


import org.eclipse.swt.widgets.Display;

public class CommandRunner<T extends CommandInterface> implements Runnable 
{
	private T m_command;
	private File m_workingDirectory;
	private String m_fullLog;
	private String m_errorLog;
	private String m_stdLog;
	
	/**
	 * @param command The CommandInterface object that should be executed.
	 * @param workingDirectory The working directory where it should be executed.
	 */
	public CommandRunner(T command, File workingDirectory)
	{
		m_command = command;
		m_workingDirectory = workingDirectory;
	}
	
	/**
	 * Runs the parameter scan
	 */
	public void run()
	{
		m_fullLog = "";
		Process scanProc = null;
		
		if(!m_workingDirectory.isDirectory())
		{
			m_workingDirectory.mkdirs();	
		}
		
		System.out.println("Running command in: " + m_workingDirectory);
		
		// Run the command
		try 
		{
			scanProc = Runtime.getRuntime().exec(m_command.getCommand(), null, m_workingDirectory);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		StreamDisplayThread stdOut = new StreamDisplayThread(scanProc.getInputStream(), false);
		StreamDisplayThread errOut = new StreamDisplayThread(scanProc.getErrorStream(), true);
		
		// Start the reading of the inputstream.  This is automatically printed to the console.
		Display.getDefault().syncExec(stdOut);
		
		// start the reading of the error stream.  This is saved to the m_log field.
		Display.getDefault().syncExec(errOut);
		
		// Before we do anything else, wait for the command to finish.
		// /*
		try 
		{
			scanProc.waitFor();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		}
		//*/
		
		m_stdLog = stdOut.getLog();
		m_errorLog = errOut.getLog();
		m_fullLog = m_stdLog + System.getProperty("line.separator") + System.getProperty("line.separator") + m_errorLog;
		
		// DEBUG
		System.out.println("Done running command.");
				
	}

	public String getErrorLog()
	{
		return m_errorLog;
	}
	
	public String getSTDLog()
	{
		return m_stdLog;
	}
	
	public String getFullLog()
	{
		return m_fullLog;
	}
}
