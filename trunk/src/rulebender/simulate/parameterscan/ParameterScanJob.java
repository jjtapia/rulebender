package rulebender.simulate.parameterscan;


import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressConstants;

import rulebender.core.utility.Console;
import rulebender.simulate.CommandRunner;
import rulebender.simulate.SimulationErrorException;

public class ParameterScanJob extends Job 
{

	private String m_filePath;
	private String m_bngPath;
	private String m_scriptFullPath;
	private ParameterScanData m_data;
	private String m_resultsPath;
	
	public ParameterScanJob(String name, 
						    String filePath,
						    String bngPath, 
						    String scriptFullPath, 
						    ParameterScanData data,
						    String resultsPath)
	{
		super(name);
		
		setFilePath(filePath);
		setBNGPath(bngPath);
		setScriptFullPath(scriptFullPath);
		setData(data);
		setResultsPath(resultsPath);
		
		setProperty(IProgressConstants.KEEP_PROPERTY, true);
	}


	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		//DEBUG
		//Console.displayOutput("\tfilePath: " + filePath + "\n\tbng path: " + bngPath + "\n\tscriptFullPath:" + scriptFullPath);
		
		// Tell the monitor
		monitor.beginTask("Validation Files...", 4);
		
		if(!validateBNGLFile(m_filePath))
		{
			Console.displayOutput(m_filePath, "Error in file path.");
			return Status.CANCEL_STATUS;
		}
		if(!validateBNGPath(m_bngPath) )
		{
			Console.displayOutput(m_filePath, "Error bng path.");
			return Status.CANCEL_STATUS;
		}
		if(!validateScriptPath(m_scriptFullPath))
		{
			Console.displayOutput(m_filePath, "Error in script path.");
			return Status.CANCEL_STATUS;
		}
		
		//MONITOR
		monitor.setTaskName("Setting up the results directory");
		monitor.worked(1);
		
		// Set up the results directory
		
		// Make the directory if necessary
		(new File(m_resultsPath)).mkdirs();
		
		
		//MONITOR
		monitor.setTaskName("Generating Commands...");
		monitor.worked(1);
				
		// Get a parameterscan command
		ParameterScanCommand command = new ParameterScanCommand(m_filePath, 
																m_bngPath,
																m_scriptFullPath,
																m_resultsPath,
																m_data);
		//DEBUG
		//Console.displayOutput("Command created.");
		
		//MONITOR
		monitor.setTaskName("Running Parameter Scan...");
		monitor.worked(1);
				
		// Run it in the commandRunner
		CommandRunner<ParameterScanCommand> runner = new CommandRunner<ParameterScanCommand>(command, new File(m_resultsPath), "Parameter Scan: " + m_filePath, monitor);
		
		try {
			runner.run();
		} catch (SimulationErrorException e) {
			// TODO Auto-generated catch block
			return Status.CANCEL_STATUS;
		}
		
		if(monitor.isCanceled())
		{
			undoSimulation();
			updateTrees();
			return Status.CANCEL_STATUS;
		}
		else
		{
			//MONITOR
			monitor.setTaskName("Done.");
			monitor.worked(1);
		}
		
		updateTrees();   
		return Status.OK_STATUS;
 	}
	
	private void undoSimulation() 
	{
		try {
			deleteRecursive(new File(m_resultsPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Console.displayOutput(m_filePath, "Parameter Scan Cancelled!\n\n");
	}
	
	private boolean deleteRecursive(File path) throws FileNotFoundException{
        if (!path.exists()) throw new FileNotFoundException(path.getAbsolutePath());
        boolean ret = true;
        if (path.isDirectory()){
            for (File f : path.listFiles()){
                ret = ret && deleteRecursive(f);
            }
        }
        return ret && path.delete();
    }

	private void updateTrees()
	{	
		 // Update the resource tree.
	       try {
			ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
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

	private void setResultsPath(String resultsPath) 
	{
		m_resultsPath = resultsPath;
	}

}
