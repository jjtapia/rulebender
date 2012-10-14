package org.bng.simulate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;

import editor.ConfigurationManager;

/**
 * 
 * @author adammatthewsmith
 * 
 * @param <T>
 */
public class CommandRunner<T extends CommandInterface> {
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
	 *            The CommandInterface object that should be executed.
	 * @param workingDirectory
	 *            The working directory where it should be executed.
	 * @param monitor
	 */
	public CommandRunner(T command, File workingDirectory, String name,
			IProgressMonitor monitor) {
		m_command = command;
		m_workingDirectory = workingDirectory;
		m_name = name;

		m_monitor = monitor;

	}

	public boolean run() throws SimulationErrorException {
		m_fullLog = "";

		if (!m_workingDirectory.isDirectory()) {
			m_workingDirectory.mkdirs();
		}

		System.out.println("Running command in: " + m_workingDirectory);
		System.out.println("Using BNGFullPath: "
				+ m_command.getBNGFullPath().substring(
						0,
						m_command.getBNGFullPath().lastIndexOf(
								System.getProperty("file.separator"))));

		ProcessBuilder pb = new ProcessBuilder(m_command.getCommand());
		Map<String, String> env = pb.environment();
		env.put("CYGWIN", "nodosfilewarning");
		env.put("BNGPATH",
				m_command.getBNGFullPath().substring(
						0,
						m_command.getBNGFullPath().lastIndexOf(
								System.getProperty("file.separator"))));
		// env.remove("OTHERVAR");
		// env.put("VAR2", env.get("VAR1") + "suffix");
		pb.directory(m_workingDirectory);
		// Process p = pb.start();

		// pb.environment().

		// Run the command
		try {
			m_scanProc = pb.start();
		} catch (IOException e) {
			e.printStackTrace();
		}

		pb.redirectErrorStream(true);
		StreamDisplayThread stdOut = new StreamDisplayThread(m_name,
				m_scanProc.getInputStream(), true);
		StreamDisplayThread errOut = new StreamDisplayThread(m_name,
				m_scanProc.getErrorStream(), true);

		stdOut.start();
		errOut.start();

		boolean done = false;

		while (!done) {
			// Check to see if it has been cancelled.
			if (m_monitor.isCanceled()) {
				cancelled(stdOut, errOut);
				return false;
			}

			// Try to read the exit value. If the process is not done, then
			// an exception will be thrown.
			try {
				m_scanProc.exitValue();
				done = true;
			} catch (IllegalThreadStateException e) {
				System.out.println("Process not done...");
			}

			try {
				// Wait before you try again.
				System.out.println("\tSleeping...");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
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

		if (!m_errorLog.equals("")) {
			throw new SimulationErrorException(m_errorLog);
		}

		return true;
	}

	private void writeLogToResults() {
		System.out.println("Writing log to " + m_workingDirectory);

		String logFileName = m_workingDirectory
				+ System.getProperty("file.separator") + "sim_log.log";

		File logFile = new File(logFileName);
		logFile.deleteOnExit();

		PrintWriter pw = null;
		try {
			pw = new PrintWriter(logFile);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

		pw.print(m_fullLog);
		pw.close();

	}

	private void cancelled(StreamDisplayThread stdOut,
			StreamDisplayThread errOut) {

		m_scanProc.destroy();

		m_stdLog = stdOut.getLog();
		m_errorLog = errOut.getLog();
		m_fullLog = m_stdLog + System.getProperty("line.separator")
				+ System.getProperty("line.separator") + m_errorLog;

		// Windows Task Kill
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1) {

			int pid = stdOut.getPID();

			System.out.println("Windows: Trying to kill pid " + pid);

			try {
				Runtime.getRuntime().exec("TaskKill /PID " + pid + " /F");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		System.out.println("Command cancelled.");

		// BNGEditor.displayOutput("\n\n******************\n\n" + m_fullLog);
	}

	public String getErrorLog() {
		return m_errorLog;
	}

	public String getSTDLog() {
		return m_stdLog;
	}

	public String getFullLog() {
		return m_fullLog;
	}

	public void kill() {
		m_scanProc.destroy();
	}
}
