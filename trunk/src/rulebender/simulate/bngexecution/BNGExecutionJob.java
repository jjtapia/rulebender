package rulebender.simulate.bngexecution;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
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
import rulebender.logging.Logger;
import rulebender.simulate.CommandRunner;
import rulebender.simulate.SimulationErrorException;

public class BNGExecutionJob extends Job {
	private String m_relativeFilePath;
	private String m_absoluteFilePath;
	private String m_bngFullPath;
	private String m_resultsPath;
	private IFile m_iFile; // store to get path info

	public BNGExecutionJob(String name, IFile ifile, String bngFullPath,
	    String resultsPath) {
		super(name);
		setAbsoluteFilePath(ifile.getLocation().makeAbsolute().toOSString());
		setRelativeFilePath(ifile.getFullPath().makeRelative().toOSString());
		setBNGFullPath(bngFullPath);
		setResultsPath(resultsPath);
		setFile(ifile);

		setProperty(IProgressConstants.KEEP_PROPERTY, true);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// Tell the monitor
		monitor.beginTask("Validation Files...", 5);

		if (!validateFileExists(m_absoluteFilePath)) {
			Console.displayOutput(m_absoluteFilePath, "Error in file path.");
			return Status.CANCEL_STATUS;
		}
		if (!validateFileExists(m_bngFullPath)) {
			Console.displayOutput(m_bngFullPath, "Error in BNG path. Use the "
			    + "Window->Preferences menu to set the correct path to BNG.");
			return Status.CANCEL_STATUS;
		}

		// MONITOR
		monitor.setTaskName("Setting up the results directory");
		monitor.worked(1);

		// Create the directory if necessary.
		(new File(m_resultsPath)).mkdirs();

		// MONITOR
		monitor.setTaskName("Generating Scripts...");
		monitor.worked(1);

		SimulateCommand simCommand = new SimulateCommand(m_absoluteFilePath,
		    m_bngFullPath, m_resultsPath, true);

		// MONITOR
		monitor.setTaskName("Running Simulation...");
		monitor.worked(1);

		// Run it in the commandRunner
		CommandRunner<SimulateCommand> runner = new CommandRunner<SimulateCommand>(
		    simCommand, new File(m_resultsPath), m_absoluteFilePath, monitor);

		try {
			runner.run();
		} catch (SimulationErrorException e) {
			// Logger.log(Logger.LOG_LEVELS.INFO, BNGExecutionJob.class,
			// "===== Simulation error exception!");
			updateTrees();
			return Status.CANCEL_STATUS;
		}

		if (monitor.isCanceled()) {
			undoSimulation();
			updateTrees();
			return Status.CANCEL_STATUS;
		} else {
			// MONITOR
			monitor.setTaskName("Opening Results File(s)...");
			monitor.worked(1);
		}

		try {
			copyBNGLFileToResults();
		} catch (IOException e) {
			// TODO do something about it.
			e.printStackTrace();
		}

		updateTrees();
		openSpeciesGraphView();
		showResults();
		monitor.setTaskName("Done.");
		monitor.worked(1);

		return Status.OK_STATUS;
	}

	private void openSpeciesGraphView() {
		Display.getDefault().asyncExec(new Runnable() {

			@Override
			public void run() {
				try {
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					    .showView("rulebender.speciesgraph.SpeciesGraphView");
					// PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("rulebender.editors.dat");
				} catch (PartInitException e) {
					e.printStackTrace();
				} // try-catch
			} // run

		});

	} // openSpeciesGraphView

	/**
	 * Display the generated GDAT and/or SCAN files after simulation
	 */
	private void showResults() {
		// Find all GDAT and SCAN files in output directory
		// System.out.println(m_resultsPath);

		String[] allfiles = new File(m_resultsPath).list();
		Vector<String> resultsFiles = new Vector<String>();

		for (String allfile : allfiles) {
			if (allfile.endsWith("gdat") || allfile.endsWith("scan")) {
				resultsFiles.add(allfile);
			}
		}

		// Open all results files
		for (int i = 0; i < resultsFiles.size(); i++) {

			// Get the full absolute path to the gdat file
			// String baseFileName = new File(m_relativeFilePath).getName();
			// System.out.println("baseFileName = "+ baseFileName);
			// String updated = baseFileName.replaceAll(".bngl", ".gdat");
			// final String gdatFileToOpen = m_resultsPath + updated;
			// System.out.println("gdatFileToOpen = "+ gdatFileToOpen);

			final String resultsFileToOpen = m_resultsPath + resultsFiles.get(i);

			// Calculate the path of the project folder for RuleBender
			// String workspacePath = Platform.getLocation().toString();
			// System.out.println("workspacePath: " + workspacePath);
			// String projectPath =
			// m_iFile.getProject().getLocation().toFile().getName();
			// System.out.println("projectPath: " + projectPath);
			// String completeProjectPath = new String(workspacePath + projectPath);
			// System.out.println("completeProjectPath = " + completeProjectPath);

			// Turn absolute gdat path into a path relative to RB project folder
			// final String relGdatFileToOpen = resultsFileToOpen.replaceAll(
			// completeProjectPath, "");
			// System.out.println("relGdatFileToOpen: " + relGdatFileToOpen);

			// Prepare to pass off file to an editor
			// IPath path = new Path(resultsFileToOpen);
			// IFile file = m_iFile.getProject().getFile(path);

			Logger.log(Logger.LOG_LEVELS.INFO, BNGExecutionJob.class,
			    resultsFileToOpen);

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IPath location = Path.fromOSString(resultsFileToOpen);
			IFile iFile = workspace.getRoot().getFileForLocation(location);

			if (!iFile.exists()) {
				return;
			}

			final IEditorInput editorInput = new FileEditorInput(iFile);

			// Figure out what page is open right now. There must be a better way to
			// do
			// this...
			IWorkbenchPage page_last = null;
			// IWorkbenchWindow w = null;
			for (IWorkbenchWindow window : PlatformUI.getWorkbench()
			    .getWorkbenchWindows()) {
				// w = window;
				for (IWorkbenchPage page : window.getPages()) {
					page_last = page;
				}
			}
			final IWorkbenchPage p = page_last;
			// final IWorkbenchWindow w2open = w;

			// In the UI thread open the gdat editor
			Display.getDefault().asyncExec(new Runnable() {

				@Override
				public void run() {
					try {
						p.openEditor(editorInput,
						    FileInputUtility.getEditorId(new File(resultsFileToOpen)));

					}

					catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		}
	}

	private void copyBNGLFileToResults() throws IOException {

		File sourceFile = new File(m_absoluteFilePath);
		File destFile = new File(m_resultsPath
		    + (new File(m_absoluteFilePath).getName()));

		// Don't copy BNGL if it already exists in the results directory, e.g., due
		// to call to writeModel()
		if (!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			FileChannel source = null;
			FileChannel destination = null;

			try {
				source = new FileInputStream(sourceFile).getChannel();
				destination = new FileOutputStream(destFile).getChannel();
				destination.transferFrom(source, 0, source.size());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (source != null) {
					try {
						source.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (destination != null) {
					try {
						destination.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	private boolean deleteRecursive(File path) throws FileNotFoundException {
		if (!path.exists()) {
			throw new FileNotFoundException(path.getAbsolutePath());
		}
		boolean ret = true;
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				ret = ret && deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}

	private void undoSimulation() {
		try {
			deleteRecursive(new File(m_resultsPath));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Console.displayOutput(m_absoluteFilePath, "Simulation Canceled!\n\n");
	}

	private void updateTrees() {
		// Update the resource tree.
		try {
			ResourcesPlugin.getWorkspace().getRoot()
			    .refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	private static boolean validateFileExists(String path) {
		if ((new File(path)).exists()) {
			return true;
		}

		return false;
	}

	public void setAbsoluteFilePath(String path) {
		m_absoluteFilePath = path;
	}

	public void setRelativeFilePath(String path) {
		m_relativeFilePath = path;
	}

	public void setBNGFullPath(String path) {
		m_bngFullPath = path;
	}

	public void setResultsPath(String path) {
		m_resultsPath = path;
	}

	public void setFile(IFile ifile) {
		m_iFile = ifile;
	}

}
