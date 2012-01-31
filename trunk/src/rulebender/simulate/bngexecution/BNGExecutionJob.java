package rulebender.simulate.bngexecution;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.IProgressConstants;

import rulebender.core.utility.Console;
import rulebender.simulate.CommandRunner;

public class BNGExecutionJob extends Job 
{

	private String m_filePath;
	private String m_bngFullPath;
	
	public BNGExecutionJob(String name, String filePath, String bngFullPath) 
	{
		super(name);
		setFilePath(filePath);
		setBNGFullPath(bngFullPath);
		
		setProperty(IProgressConstants.KEEP_PROPERTY, true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) 
	{
		
		// Tell the monitor
		monitor.beginTask("Validation Files...", 4);
		
		if(!validateBNGLFile(m_filePath) || !validateBNGPath(m_bngFullPath))
		{
			Console.displayOutput("Simulation: " + m_filePath, "Error in file path, or bng path.");
			return Status.CANCEL_STATUS;
		}
		
		//MONITOR
		monitor.setTaskName("Setting up the results directory");
		monitor.worked(1);
		
		// Set up the results directory
		String timeStamp = getCurrentDateAndTime();
		String resultsDir = m_filePath.substring(0,m_filePath.indexOf(".bngl")) + 
				System.getProperty("file.separator") +
				"results/" + timeStamp + System.getProperty("file.separator");
		
		// Make the directory if necessary
		(new File(resultsDir)).mkdirs();
		
		//MONITOR
		monitor.setTaskName("Generating Scripts...");
		monitor.worked(1);
		
		SimulateCommand simCommand = new SimulateCommand(m_filePath, m_bngFullPath, resultsDir, true);
		
		//MONITOR
		monitor.setTaskName("Running Simulation...");
		monitor.worked(1);
				
		// Run it in the commandRunner
		CommandRunner<SimulateCommand> runner = new CommandRunner<SimulateCommand>(simCommand, new File(resultsDir), "Simulation: " + m_filePath);
		
		runner.run();
		
		//MONITOR
		monitor.setTaskName("Done.");
		monitor.worked(1);
				
		return Status.OK_STATUS;
	}
	
	private static boolean validateBNGLFile(String path)
	{
		if ((new File(path)).exists())
			return true;
		
		return false;
	}
	
	private static boolean validateBNGPath(String path)
	{
		if ((new File(path)).exists())
			return true;
		
		return false;
	}

	public void setFilePath(String path)
	{
		m_filePath = path;
	}
	
	public void setBNGFullPath(String path)
	{
		m_bngFullPath = path;
	}
	
	/**
	 * Used for time-stamping results files.  Returns the date in a
	 * dd-mm-yy_hh-mm-ss format.
	 * 
	 * @return
	 */
	private static String getCurrentDateAndTime() 
	{
		Date dateNow = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy_HH-mm-ss");
		String curTime = dateFormat.format(dateNow);
		return curTime;
	}
}
