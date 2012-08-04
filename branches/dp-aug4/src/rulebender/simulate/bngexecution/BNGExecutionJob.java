package rulebender.simulate.bngexecution;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressConstants;

import rulebender.core.utility.Console;
import rulebender.core.utility.FileInputUtility;
import rulebender.simulate.CommandRunner;
import rulebender.simulate.SimulationErrorException;

import org.eclipse.ui.*;

public class BNGExecutionJob extends Job 
{
	private String m_relativeFilePath;
	private String m_absoluteFilePath;
	private String m_bngFullPath;
	private String m_resultsPath;
	private IFile  m_iFile;				// store to get path info
	
	public BNGExecutionJob(String name, IFile ifile, String bngFullPath, String resultsPath) 
	{
		super(name);
		setAbsoluteFilePath(ifile.getLocation().makeAbsolute().toOSString());
		setRelativeFilePath(ifile.getFullPath().toOSString());
		setBNGFullPath(bngFullPath);
		setResultsPath(resultsPath);
		setFile(ifile);
		
		setProperty(IProgressConstants.KEEP_PROPERTY, true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) 
	{
		System.out.println("Starting the run");
		
		
		// Tell the monitor
		monitor.beginTask("Validation Files...", 4);
		
		if(!validateBNGLFile(m_absoluteFilePath))
		{
			Console.displayOutput(m_absoluteFilePath, "Error in file path.");
			return Status.CANCEL_STATUS;
		}
		if(!validateBNGPath(m_bngFullPath))
		{
			Console.displayOutput(m_bngFullPath, "Error in bng path.");
			return Status.CANCEL_STATUS;
		}
		
		//MONITOR
		monitor.setTaskName("Setting up the results directory");
		monitor.worked(1);
		
		// Create the directory if necessary.
	    (new File(m_resultsPath)).mkdirs();
	    	       
		//MONITOR
		monitor.setTaskName("Generating Scripts...");
		monitor.worked(1);
		
		SimulateCommand simCommand =
		    new SimulateCommand(m_absoluteFilePath, 
		                        m_bngFullPath, 
		                        m_resultsPath, 
		                        true);
		
		//MONITOR
		monitor.setTaskName("Running Simulation...");
		monitor.worked(1);
				
		// Run it in the commandRunner
		CommandRunner<SimulateCommand> runner = 
		    new CommandRunner<SimulateCommand>(simCommand, 
		                                       new File(m_resultsPath), 
		                                       m_relativeFilePath, 
		                                       monitor);
		
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
		showResults();
		return Status.OK_STATUS;
	}
	
	/*
	 * Display the generated gdat file after simulation
	 */
	private void showResults() {
		
		// Get the full absolute path to the gdat file
		String baseFileName = new File(m_relativeFilePath).getName();
		//System.out.println("baseFileName = "+ baseFileName);
		String updated = baseFileName.replaceAll(".bngl", ".gdat"); 
		final String gdatFileToOpen = m_resultsPath + updated;
		//System.out.println("gdatFileToOpen = "+ gdatFileToOpen);
		
		// Calculate the path of the project folder for RuleBender
		String workspacePath = Platform.getInstanceLocation().getURL().getPath().toString();
		//System.out.println("workspacePath: " + workspacePath);
		String projectPath = m_iFile.getProject().getLocation().toFile().getName();
		//System.out.println("projectPath: " + projectPath);
		String completeProjectPath = new String(workspacePath + projectPath);
		//System.out.println("completeProjectPath = " + completeProjectPath);
		
		// Turn absolute gdat path into a path relative to RB project folder
		final String relGdatFileToOpen = gdatFileToOpen.replaceAll(completeProjectPath, "");
		//System.out.println("relGdatFileToOpen: " + relGdatFileToOpen);

		// Prepare to pass off file to an editor
		IPath path = new Path(relGdatFileToOpen);
		IFile file = m_iFile.getProject().getFile(path);
		final IEditorInput editorInput = new FileEditorInput(file);

		// Figure out what page is open right now. There must be a better way to do this...
		IWorkbenchPage page_last = null;
		//IWorkbenchWindow w = null;
		for (IWorkbenchWindow window : PlatformUI.getWorkbench().getWorkbenchWindows()) {
		    //w = window;
			for (IWorkbenchPage page : window.getPages()) {
		    	page_last = page;
		        //for (IEditorReference editor : page.getEditorReferences()) {
		            //System.out.println(page.getLabel() + " -> " + editor.getName());
		        //}
		    }
		}
		final IWorkbenchPage p = page_last;
		//final IWorkbenchWindow w2open = w;
		
		// In the UI thread open the gdat editor
		Display.getDefault().asyncExec(new Runnable() {
		 
			public void run() {
				try {
					p.openEditor(editorInput, FileInputUtility.getEditorId(new File(gdatFileToOpen)));
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			}
		});
		
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
		Console.displayOutput(m_relativeFilePath, "Simulation Cancelled!\n\n");
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
		if ((new File(path)).exists())
			return true;
		
		return false;
	}

	public void setAbsoluteFilePath(String path)
	{
		m_absoluteFilePath = path;
	}
	
	public void setRelativeFilePath(String path)
	{
		m_relativeFilePath = path;
	}
	
	public void setBNGFullPath(String path)
	{
		m_bngFullPath = path;
	}
	
	public void setResultsPath(String path)
	{
		m_resultsPath = path;
	}
	
	public void setFile(IFile ifile)
	{
		m_iFile = ifile;
	}
	
}
