package rulebender.simulate.bngexecution;

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
import rulebender.navigator.views.ModelTreeView;
import rulebender.simulate.CommandRunner;
import rulebender.simulate.SimulationErrorException;

public class BNGExecutionJob extends Job 
{

	private String m_filePath;
	private String m_bngFullPath;
	private String m_resultsPath;
	
	public BNGExecutionJob(String name, String filePath, String bngFullPath, String resultsPath) 
	{
		super(name);
		setFilePath(filePath);
		setBNGFullPath(bngFullPath);
		setResultsPath(resultsPath);
		
		setProperty(IProgressConstants.KEEP_PROPERTY, true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) 
	{
		// Tell the monitor
		monitor.beginTask("Validation Files...", 4);
		
		if(!validateBNGLFile(m_filePath))
		{
			Console.displayOutput("Simulation: " + m_filePath, "Error in file path.");
			return Status.CANCEL_STATUS;
		}
		if(!validateBNGPath(m_bngFullPath))
		{
			Console.displayOutput("Simulation: " + m_filePath, "Error in bng path.");
			return Status.CANCEL_STATUS;
		}
		
		//MONITOR
		monitor.setTaskName("Setting up the results directory");
		monitor.worked(1);
		
		// Create the directory if necessary.
	    (new File(m_resultsPath)).mkdirs();
	    
	    //TODO start here by copying the file to be simulated, then use that file for the simulations.
	       
		//MONITOR
		monitor.setTaskName("Generating Scripts...");
		monitor.worked(1);
		
		SimulateCommand simCommand = new SimulateCommand(m_filePath, m_bngFullPath, m_resultsPath, true);
		
		//MONITOR
		monitor.setTaskName("Running Simulation...");
		monitor.worked(1);
				
		// Run it in the commandRunner
		CommandRunner<SimulateCommand> runner = new CommandRunner<SimulateCommand>(simCommand, new File(m_resultsPath), "Simulation: " + m_filePath, monitor);
		
		try 
		{
			runner.run();
		} catch (SimulationErrorException e) 
		{	
			updateTrees();
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

	private void undoSimulation() 
	{
		try {
			deleteRecursive(new File(m_resultsPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Deleting: " + m_resultsPath);
		Console.displayOutput("Simulation: " + m_filePath, "Simulation Cancelled!\n\n");
	}

	private void updateTrees()
	{
		//FIXME  This is "bad design" that tightly couples the simulation and 
		// tree view
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() 
			{
				IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						 .getActivePage().getActivePart().getSite().getPage()
						 .getViewReferences();
					
				for(IViewReference view : views)
				{
					if(view.getId().equals("rulebender.views.Navigator"))
					{
						((ModelTreeView) view.getPart(true)).rebuildWholeTree();
					}
				}
			}});
		
		
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
	
	public void setResultsPath(String path)
	{
		m_resultsPath = path;
	}
}
