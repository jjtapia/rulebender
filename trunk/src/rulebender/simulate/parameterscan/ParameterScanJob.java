package rulebender.simulate.parameterscan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.progress.IProgressConstants;

import rulebender.core.utility.Console;
import rulebender.core.utility.FileInputUtility;
import rulebender.simulate.CommandRunner;
import rulebender.simulate.SimulationErrorException;

public class ParameterScanJob extends Job
{

  private String m_absoluteFilePath;
  private String m_relativeFilePath;
  private String m_bngPath;
  private String m_scriptFullPath;
  private ParameterScanData m_data;
  private String m_resultsPath;
  private IFile m_iFile;
  private String m_scanPath;


  public ParameterScanJob(String name, IFile iFile, String bngPath,
      String scriptFullPath, ParameterScanData data, String resultsPath)
  {
    super(name);

    setAbsoluteFilePath(iFile.getLocation().makeAbsolute().toOSString());
    setRelativeFilePath(iFile.getFullPath().makeRelative().toOSString());

    setBNGPath(bngPath);
    setScriptFullPath(scriptFullPath);
    setData(data);
    setResultsPath(resultsPath);
    setIfile(iFile);

    setProperty(IProgressConstants.KEEP_PROPERTY, true);
  }


  @Override
  protected IStatus run(IProgressMonitor monitor)
  {
    // Tell the monitor
    monitor.beginTask("Validation Files...", 4);

    if (!validateBNGLFile(m_absoluteFilePath))
    {
      Console.displayOutput(m_relativeFilePath, "Error in file path.");
      return Status.CANCEL_STATUS;
    }
    if (!validateBNGPath(m_bngPath))
    {
      Console.displayOutput(m_relativeFilePath, "Error in BNG path. Use the "
          + "Window->Preferences menu to set the correct path to BNG.");
      return Status.CANCEL_STATUS;
    }
    if (!validateScriptPath(m_scriptFullPath))
    {
      Console.displayOutput(m_relativeFilePath, "Error in script path.");
      return Status.CANCEL_STATUS;
    }

    // MONITOR
    monitor.setTaskName("Setting up the results directory");
    monitor.worked(1);

    // Set up the results directory

    // Make the directory if necessary
    (new File(m_resultsPath)).mkdirs();

    // MONITOR
    monitor.setTaskName("Generating Commands...");
    monitor.worked(1);

    // Get a parameterscan command
    ParameterScanCommand command = new ParameterScanCommand(m_absoluteFilePath,
        m_bngPath, m_scriptFullPath, m_resultsPath, m_data);

    String prefix = command.constructPrefix() + "_" + m_data.getName();
    setScanPath(m_resultsPath + prefix + ".scan");
    // System.out.println("PREFIX gleaned from command: " + m_scanPath);

    // MONITOR
    monitor.setTaskName("Running Parameter Scan...");
    monitor.worked(1);

    // Run it in the commandRunner
    CommandRunner<ParameterScanCommand> runner = new CommandRunner<ParameterScanCommand>(
        command, new File(m_resultsPath), m_relativeFilePath, monitor);

    try
    {
      runner.run();
    }
    catch (SimulationErrorException e)
    {
      updateTrees();
      return Status.CANCEL_STATUS;
    }

    if (monitor.isCanceled())
    {
      undoSimulation();
      updateTrees();
      return Status.CANCEL_STATUS;
    }
    else
    {
      // MONITOR
      monitor.setTaskName("Opening Results File(s)...");
      monitor.worked(1);
    }

    try
    {
      copyBNGLFileToResults();
    }
    catch (IOException e)
    {
      // TODO do something about it.  
      e.printStackTrace();
    }
    
    updateTrees();
    showResults();
    monitor.setTaskName("Done.");
    monitor.worked(1);
    
    return Status.OK_STATUS;
  }


  private void undoSimulation()
  {
    try
    {
      deleteRecursive(new File(m_resultsPath));
    }
    catch (FileNotFoundException e)
    {
      e.printStackTrace();
    }

    Console.displayOutput(m_relativeFilePath, "Parameter Scan Cancelled!\n\n");
  }


  /**
   * Display the generated scan file after simulation
   */
  private void showResults()
  {

    final String scanFileToOpen = m_scanPath; // provided by scan command

    // Calculate the path of the project folder for RuleBender
    String workspacePath = Platform.getInstanceLocation().getURL().getPath()
        .toString();
    // System.out.println("workspacePath: " + workspacePath);
    String projectPath = m_iFile.getProject().getLocation().toFile().getName();
    // System.out.println("projectPath: " + projectPath);
    String completeProjectPath = new String(workspacePath + projectPath);
    // System.out.println("completeProjectPath = " + completeProjectPath);

    // Turn absolute gdat path into a path relative to RB project folder
    final String relScanFileToOpen = scanFileToOpen.replaceAll(
        completeProjectPath, "");
    // System.out.println("relGdatFileToOpen: " + relGdatFileToOpen);

    // Prepare to pass off file to an editor
    IPath path = new Path(relScanFileToOpen);
    IFile file = m_iFile.getProject().getFile(path);

    if (!file.exists())
    {
      System.out.println("No results created.");
      return;
    }

    final IEditorInput editorInput = new FileEditorInput(file);

    // Figure out what page is open right now. There must be a better way to do
    // this...
    IWorkbenchPage page_last = null;
    // IWorkbenchWindow w = null;
    for (IWorkbenchWindow window : PlatformUI.getWorkbench()
        .getWorkbenchWindows())
    {
      // w = window;
      for (IWorkbenchPage page : window.getPages())
      {
        page_last = page;
      }
    }
    final IWorkbenchPage p = page_last;

    // In the UI thread open the scan editor
    Display.getDefault().asyncExec(new Runnable()
    {
      public void run()
      {
        try
        {
          p.openEditor(editorInput,
              FileInputUtility.getEditorId(new File(relScanFileToOpen)));
        }
        catch (PartInitException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });

  }

  private void copyBNGLFileToResults() throws IOException
  {

    File sourceFile = new File(m_absoluteFilePath);
    File destFile = new File(m_resultsPath
        + (new File(m_absoluteFilePath).getName()));

    // Don't copy BNGL if it already exists in the results directory, e.g., due
    // to call to writeModel()
    if (!destFile.exists())
    {
      try
      {
        destFile.createNewFile();
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      FileChannel source = null;
      FileChannel destination = null;

      try
      {
        source = new FileInputStream(sourceFile).getChannel();
        destination = new FileOutputStream(destFile).getChannel();
        destination.transferFrom(source, 0, source.size());
      }
      catch (FileNotFoundException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      finally
      {
        if (source != null)
        {
          try
          {
            source.close();
          }
          catch (IOException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
        if (destination != null)
        {
          try
          {
            destination.close();
          }
          catch (IOException e)
          {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
    }
  }

  private boolean deleteRecursive(File path) throws FileNotFoundException
  {
    if (!path.exists())
      throw new FileNotFoundException(path.getAbsolutePath());
    boolean ret = true;
    if (path.isDirectory())
    {
      for (File f : path.listFiles())
      {
        ret = ret && deleteRecursive(f);
      }
    }
    return ret && path.delete();
  }


  private void updateTrees()
  {
    // Update the resource tree.
    try
    {
      ResourcesPlugin.getWorkspace().getRoot()
          .refreshLocal(IResource.DEPTH_INFINITE, null);
    }
    catch (CoreException e)
    {
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
    if ((new File(path + "BNG2.pl")).exists())
      return true;

    return false;
  }


  private static boolean validateScriptPath(String path)
  {
    if ((new File(path)).exists())
      return true;

    return false;
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


  public void setAbsoluteFilePath(String path)
  {
    m_absoluteFilePath = path;
  }


  public void setRelativeFilePath(String path)
  {
    m_relativeFilePath = path;
  }


  public void setIfile(IFile ifile)
  {
    m_iFile = ifile;
  }


  public void setScanPath(String path)
  {
    m_scanPath = path;
  }

}
