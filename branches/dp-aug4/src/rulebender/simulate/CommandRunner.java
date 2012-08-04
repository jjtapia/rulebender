package rulebender.simulate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.navigator.CommonNavigator;

import rulebender.preferences.OS;
import rulebender.preferences.PreferencesClerk;

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
   * @param command
   *          The CommandInterface object that should be executed.
   * @param workingDirectory
   *          The working directory where it should be executed.
   * @param name
   *          The relative path from the workspace to the file.
   * @param monitor
   */
  public CommandRunner(T command, File workingDirectory, String name,
      IProgressMonitor monitor)
  {
    m_command = command;
    m_workingDirectory = workingDirectory;
    m_name = name;

    m_monitor = monitor;

  }

  public boolean run() throws SimulationErrorException
  {
    m_fullLog = "";

    if (!m_workingDirectory.isDirectory())
    {
      m_workingDirectory.mkdirs();
    }

    System.out.println("Running command in: " + m_workingDirectory);

    ProcessBuilder pb = new ProcessBuilder(m_command.getCommand());
    Map<String, String> env = pb.environment();
    env.put("CYGWIN", "nodosfilewarning");
    env.put(
        "BNGPATH",
        m_command.getBNGFullPath().substring(
            0,
            m_command.getBNGFullPath().lastIndexOf(
                System.getProperty("file.separator"))));
    // env.remove("OTHERVAR");
    // env.put("VAR2", env.get("VAR1") + "suffix");
    pb.directory(m_workingDirectory);
    // Process p = pb.start();

    // Run the command
    try
    {
      m_scanProc = pb.start();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }

    StreamDisplayThread stdOut = new StreamDisplayThread(m_name,
        m_scanProc.getInputStream(), true);
    StreamDisplayThread errOut = new StreamDisplayThread(m_name,
        m_scanProc.getErrorStream(), true);

    stdOut.start();
    errOut.start();

    boolean done = false;

    while (!done)
    {
      // Check to see if it has been cancelled.
      if (m_monitor.isCanceled())
      {
        cancelled(stdOut, errOut);
        return false;
      }

      // Try to read the exit value. If the process is not done, then
      // an exception will be thrown.
      try
      {
        m_scanProc.exitValue();
        done = true;
      }
      catch (IllegalThreadStateException e)
      {
        System.out.println("Process not done...");
      }

      try
      {
        // Wait before you try again.
        System.out.println("\tSleeping...");
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    // Before we do anything else, wait for the command to finish.
    /*
     * This is not necessary anymore due to the above block that looks for
     * cancelled state.
     * 
     * try { m_scanProc.waitFor(); } catch (InterruptedException e) {
     * e.printStackTrace(); } //
     */

    m_stdLog = stdOut.getLog();
    m_errorLog = errOut.getLog();
    m_fullLog = m_stdLog + System.getProperty("line.separator")
        + System.getProperty("line.separator") + m_errorLog;

    writeLogToResults();

    // DEBUG
    System.out.println("Done running command.");
    System.out.println("Errors: \n" + m_errorLog);

    if (!m_errorLog.equals(""))
    {
      throw new SimulationErrorException(m_errorLog);
    }

    // exposeResults();
    return true;
  }

  // TODO This is the start of the method to show which files have been
  // created....
  // feel free to totally ignore or remove. I don't remember getting very far
  // with it.
  private void exposeResults()
  {
    Display.getDefault().syncExec(new Runnable()
    {

      @Override
      public void run()
      {
        IPath path = new Path(m_workingDirectory.getAbsolutePath());

        IWorkbenchPage page = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getActivePage();

        if (page != null)
        {

          IViewPart view = page.findView("rulebender.cnf.CommonNavigator");

          if (view != null && view instanceof CommonNavigator)
          {
            CommonNavigator cn = (CommonNavigator) view;
            Object[] objects = { path };
            // cn.getCommonViewer().getTree().get
            cn.getCommonViewer().setSelection(new StructuredSelection(objects),
                true);
          }
        }

      }
    });
  }

  private void writeLogToResults()
  {
    System.out.println("Writing log to " + m_workingDirectory);

    String logFileName = m_workingDirectory
        + System.getProperty("file.separator") + "sim_log.log";

    File logFile = new File(logFileName);
    logFile.deleteOnExit();

    PrintWriter pw = null;
    try
    {
      pw = new PrintWriter(logFile);
    }
    catch (FileNotFoundException e1)
    {
      e1.printStackTrace();
    }

    pw.print(m_fullLog);
    pw.close();

  }

  private void cancelled(StreamDisplayThread stdOut, StreamDisplayThread errOut)
  {
    m_scanProc.destroy();
    m_stdLog = stdOut.getLog();
    m_errorLog = errOut.getLog();
    m_fullLog = m_stdLog + System.getProperty("line.separator")
        + System.getProperty("line.separator") + m_errorLog;

    // Windows Task Kill
    if (PreferencesClerk.getOS() == OS.WINDOWS)
    {

      int pid = stdOut.getPID();

      System.out.println("Windows: Trying to kill pid " + pid);

      try
      {
        Runtime.getRuntime().exec("TaskKill /PID " + pid + " /F");
      }
      catch (IOException e)
      {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

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
