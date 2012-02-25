package rulebender.simulate.bngexecution;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.progress.IProgressConstants;

import rulebender.core.utility.Console;
import rulebender.navigator.views.ModelTreeView;
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
		final String resultsDir = m_filePath.substring(0,m_filePath.indexOf(".bngl")) + 
				System.getProperty("file.separator") +
				"results/" + timeStamp + System.getProperty("file.separator");
		
		// Create the directory if necessary.
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
