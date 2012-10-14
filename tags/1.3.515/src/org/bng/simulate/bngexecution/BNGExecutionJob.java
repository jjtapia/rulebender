package org.bng.simulate.bngexecution;

import java.io.File;

import org.bng.simulate.CommandRunner;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import editor.BNGEditor;

public class BNGExecutionJob implements IRunnableWithProgress {

	private String m_filePath;
	private String m_bngFullPath;
	private String m_resultsPath;

	public BNGExecutionJob(String filePath, String bngFullPath,
			String resultsPath) {
		setFilePath(filePath);
		setBNGFullPath(bngFullPath);
		setResultsPath(resultsPath);
	}

	public void run(IProgressMonitor monitor) throws InterruptedException {
		// Tell the monitor
		monitor.beginTask("Validation Files...", 4);

		if (!validateBNGLFile(m_filePath)) {

			BNGEditor.displayOutput("Error in file path.");
			return;
		}
		if (!validateBNGPath(m_bngFullPath)) {
			BNGEditor.displayOutput("Error in bng path.");
			return;
		}

		// MONITOR
		monitor.setTaskName("Setting up the results directory");
		monitor.worked(1);

		// Create the directory if necessary.
		(new File(m_resultsPath)).mkdirs();

		// TODO start here by copying the file to be simulated, then use that
		// file for the simulations.

		// MONITOR
		monitor.setTaskName("Generating Scripts...");
		monitor.worked(1);

		SimulateCommand simCommand = new SimulateCommand(m_filePath,
				m_bngFullPath, m_resultsPath, true);

		// MONITOR
		monitor.setTaskName("Running Simulation...");
		monitor.worked(1);

		// Run it in the commandRunner
		CommandRunner<SimulateCommand> runner = new CommandRunner<SimulateCommand>(
				simCommand, new File(m_resultsPath), "Simulation: "
						+ m_filePath, monitor);

		runner.run();

		if (monitor.isCanceled()) {
			throw new InterruptedException();
		} else {
			// MONITOR
			monitor.setTaskName("Done.");
			monitor.worked(1);
		}
	}

	private static boolean validateBNGLFile(String path) {
		if ((new File(path)).exists())
			return true;

		return false;
	}

	private static boolean validateBNGPath(String path) {
		if ((new File(path)).exists())
			return true;

		return false;
	}

	public void setFilePath(String path) {
		m_filePath = path;
	}

	public void setBNGFullPath(String path) {
		m_bngFullPath = path;
	}

	public void setResultsPath(String path) {
		m_resultsPath = path;
	}
}
