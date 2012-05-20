package org.bng.simulate.parameterscan;


import java.io.File;

import org.bng.simulate.CommandRunner;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;


import editor.BNGEditor;

public class ParameterScanJob implements IRunnableWithProgress
{

	private String m_filePath;
	private String m_bngPath;
	private String m_scriptFullPath;
	private ParameterScanData m_data;
	private String m_resultsPath;
	
	public ParameterScanJob(String filePath,
						    String bngPath, 
						    String scriptFullPath, 
						    ParameterScanData data,
						    String resultsPath)
	{
		setFilePath(filePath);
		setBNGPath(bngPath);
		setScriptFullPath(scriptFullPath);
		setData(data);
		setResultsPath(resultsPath);
	}


	public void run(IProgressMonitor monitor) throws InterruptedException
	{
		
		//DEBUG
		//BNGEditor.displayOutput("\tfilePath: " + filePath + "\n\tbng path: " + bngPath + "\n\tscriptFullPath:" + scriptFullPath);
		
		// Tell the monitor
		monitor.beginTask("Validation Files...", 4);
		
		if(!validateBNGLFile(m_filePath))
		{
			BNGEditor.displayOutput("Error in file path.");
			return;
		}
		if(!validateBNGPath(m_bngPath) )
		{
			BNGEditor.displayOutput("Error bng path: " + m_bngPath);
			return;
		}
		if(!validateScriptPath(m_scriptFullPath))
		{
			BNGEditor.displayOutput("Error in script path.");
			return;
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
		//BNGEditor.displayOutput("Command created.");
		
		//MONITOR
		monitor.setTaskName("Running Parameter Scan...");
		monitor.worked(1);
				
		// Run it in the commandRunner
		CommandRunner<ParameterScanCommand> runner = new CommandRunner<ParameterScanCommand>(command, new File(m_resultsPath), "Parameter Scan: " + m_filePath, monitor);
		
		runner.run();
		
		if(monitor.isCanceled())
		{
			throw new InterruptedException();
		}
		else
		{
			//MONITOR
			monitor.setTaskName("Done.");
			monitor.worked(1);
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
