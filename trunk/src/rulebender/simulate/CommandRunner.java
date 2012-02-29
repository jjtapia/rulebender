package rulebender.simulate;
import java.io.File;
import java.io.IOException;

/**
 * 
 * @author adammatthewsmith
 *
 * @param <T>
 */
public class CommandRunner<T extends CommandInterface> 
{
	private T m_command;
	private File m_workingDirectory;
	private String m_fullLog;
	private String m_errorLog;
	private String m_stdLog;
	private String m_name;
	
	private Process m_scanProc;
	
	/**
	 * @param command The CommandInterface object that should be executed.
	 * @param workingDirectory The working directory where it should be executed.
	 */
	public CommandRunner(T command, File workingDirectory, String name)
	{
		m_command = command;
		m_workingDirectory = workingDirectory;
		m_name = name;
		
	}

	public boolean run() 
	{
		m_fullLog = "";
		
		if(!m_workingDirectory.isDirectory())
		{
			m_workingDirectory.mkdirs();	
		}
		
		System.out.println("Running command in: " + m_workingDirectory);
		
		// Run the command
		try 
		{
			m_scanProc = Runtime.getRuntime().exec(m_command.getCommand(), null, m_workingDirectory);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		StreamDisplayThread stdOut = new StreamDisplayThread(m_name, m_scanProc.getInputStream(), false);
		StreamDisplayThread errOut = new StreamDisplayThread(m_name, m_scanProc.getErrorStream(), true);
		
		stdOut.start();
		errOut.start();
		
		// Before we do anything else, wait for the command to finish.
		// /*
		try 
		{
			m_scanProc.waitFor();
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
		
		return true;
				
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
	
	public void kill()
	{
		m_scanProc.destroy();
	}
}
