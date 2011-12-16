package rulebender.simulate.parameterscan;

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

public class ParameterScanJob extends Job 
{

	private String m_filePath;
	private String m_bngPath;
	private String m_scriptFullPath;
	private ParameterScanData m_data;
	
	public ParameterScanJob(String name, 
						    String filePath,
						    String bngPath, 
						    String scriptFullPath, 
						    ParameterScanData data)
	{
		super(name);
		
		setFilePath(filePath);
		setBNGPath(bngPath);
		setScriptFullPath(scriptFullPath);
		setData(data);
		
		setProperty(IProgressConstants.KEEP_PROPERTY, true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		//DEBUG
		//Console.displayOutput("\tfilePath: " + filePath + "\n\tbng path: " + bngPath + "\n\tscriptFullPath:" + scriptFullPath);
		
		// Tell the monitor
		monitor.beginTask("Validation Files...", 4);
		
		if(!validateBNGLFile(m_filePath) || !validateBNGPath(m_bngPath) || !validateScriptPath(m_scriptFullPath))
		{
			Console.displayOutput("Parameter Scan: " + m_filePath, "Error in file path, script path, or bng path.");
			return Status.CANCEL_STATUS;
		}
		
		String timeStamp = getCurrentDateAndTime();
		
		//MONITOR
		monitor.setTaskName("Setting up the results directory");
		monitor.worked(1);
		
		// Set up the results directory
		String resultsDir = m_filePath.substring(0,m_filePath.indexOf(".bngl")) + 
				System.getProperty("file.separator") +
				"results/parascan-" + timeStamp + System.getProperty("file.separator");
		
		// Make the directory if necessary
		(new File(resultsDir)).mkdirs();
		
		//MONITOR
		monitor.setTaskName("Generating Commands...");
		monitor.worked(1);
				
		// Get a parameterscan command
		ParameterScanCommand command = new ParameterScanCommand(m_filePath, 
																m_bngPath,
																m_scriptFullPath,
																resultsDir,
																m_data);
		//DEBUG
		//Console.displayOutput("Command created.");
		
		//MONITOR
		monitor.setTaskName("Running Parameter Scan...");
		monitor.worked(1);
				
		// Run it in the commandRunner
		CommandRunner<ParameterScanCommand> runner = new CommandRunner<ParameterScanCommand>(command, new File(resultsDir), "Parameter Scan: " + m_filePath);
		
		runner.run();
		
		//MONITOR
		monitor.setTaskName("Done.");
		monitor.worked(1);
				
		return Status.OK_STATUS;
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
	
	private static boolean validateBNGLFile(String path)
	{
		if ((new File(path)).exists())
			return true;
		
		return false;
	}
	
	private static boolean validateBNGPath(String path)
	{
		if ((new File(path+"BNG2.pl")).exists())
			return true;
		
		return false;
	}
	
	private static boolean validateScriptPath(String path)
	{
		if ((new File(path)).exists())
			return true;
		
		return false;
	}
	
	public void setFilePath(String path)
	{
		m_filePath = path;
	}
	
	public void setBNGPath(String path)
	{
		m_bngPath = path;
	}
	
	public void setScriptFullPath(String path)
	{
		m_scriptFullPath = path;
	}
	
	public void setData(ParameterScanData data)
	{
		m_data = data;
	}
}
