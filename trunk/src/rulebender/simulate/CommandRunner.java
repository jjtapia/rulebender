package rulebender.simulate;
import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;

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
	
	private IProgressMonitor m_monitor; 
	
	private Process m_scanProc;
	
	/**
	 * @param command The CommandInterface object that should be executed.
	 * @param workingDirectory The working directory where it should be executed.
	 * @param monitor 
	 */
	public CommandRunner(T command, File workingDirectory, String name, IProgressMonitor monitor)
	{
		m_command = command;
		m_workingDirectory = workingDirectory;
		m_name = name;
		
		m_monitor = monitor;
		
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
		
		boolean done = false;
		
		while(!done)
		{
			//Check to see if it has been cancelled.
			if(m_monitor.isCanceled())
			{
				cancelled(stdOut, errOut);
				return false;
			}
			
			// Try to read the exit value.  If the process is not done, then
			// an exception will be thrown. 
			try
			{
				m_scanProc.exitValue();
				done = true;
			}
			catch(IllegalThreadStateException e)
			{
				System.out.println("Process not done...");
			}
			
			try 
			{
				// Wait before you try again.
				System.out.println("\tSleeping...");
				Thread.sleep(1000);
			} 
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// Before we do anything else, wait for the command to finish.
	   /*  This is not necessary anymore due to the above block that looks for
	    *  cancelled state. 
	    
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

	private void cancelled(StreamDisplayThread stdOut, StreamDisplayThread errOut)
	{
		m_scanProc.destroy();
		m_stdLog = stdOut.getLog();
		m_errorLog = errOut.getLog();
		m_fullLog = m_stdLog + System.getProperty("line.separator") + System.getProperty("line.separator") + m_errorLog;
		
		System.out.println("Command cancelled.");
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
