package org.bng.simulate;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.bng.simulate.bngexecution.BNGExecutionJob;
import org.bng.simulate.parameterscan.ParameterScanData;
import org.bng.simulate.parameterscan.ParameterScanJob;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import editor.BNGEditor;

/**
 * @author mr_smith22586
 * 
 */
public class BioNetGenUtility {
	// Private constructor for uninstantiability
	private BioNetGenUtility() {
		throw new AssertionError();
	}

	/**
	 * Runs a parameter scan and puts the results in a directory called
	 * 'results' in the same folder as the model.
	 * 
	 * @param filePath
	 * @param data
	 * @param bngFullPath
	 * @param scriptFullPath
	 * @return true if job submitted, false otherwise.
	 */
	public static boolean parameterScan(String filePath,
			ParameterScanData data, String bngPath, String scriptFullPath,
			String resultsPath) {
		ParameterScanJob job = new ParameterScanJob(filePath, bngPath,
				scriptFullPath, data, resultsPath);

		try {
			new ProgressMonitorDialog(Display.getDefault().getActiveShell())
					.run(true, true, job);
		} catch (InvocationTargetException e) {
			// handle exception
			e.printStackTrace();
		}
		// This has to be caught before the interruptedException.
		// I needed to throw an exception for a failed simulation,
		// but I cannot change the method signature of the IRunnableWithProgress
		catch (SimulationErrorException e) {

			System.out
					.println("Shell null? " + BNGEditor.getMainEditorShell() == null ? "YES"
							: "NO");
			MessageBox errorMessage = new MessageBox(
					BNGEditor.getMainEditorShell(), SWT.ICON_ERROR | SWT.OK);
			errorMessage.setText("Completed With Errors");
			errorMessage.setMessage("Simulation Finished with Errors.\n\n"
					+ e.getErrorMessage());

			int f = errorMessage.open();

			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();

			try {
				deleteRecursive(new File(resultsPath));
			} catch (FileNotFoundException fnfe) {
				// TODO Auto-generated catch block
				fnfe.printStackTrace();
			}

			System.out.println("Deleting: " + resultsPath);
			BNGEditor.displayOutput("Simulation Cancelled!\n\n");

			return false;
		}

		return true;
	}

	/**
	 * 
	 * @param filePath
	 */
	public static boolean runBNGLFile(String filePath, String bngFullPath,
			String resultsPath) {
		// String name = "Executing file: " +
		// filePath.substring(filePath.lastIndexOf(System.getProperty("file.separator"))+1,
		// filePath.indexOf(".bngl"));
		BNGExecutionJob job = new BNGExecutionJob(filePath, bngFullPath,
				resultsPath);

		try {
			new ProgressMonitorDialog(Display.getDefault().getActiveShell())
					.run(true, true, job);
		} catch (InvocationTargetException e) {
			// handle exception
			e.printStackTrace();
		}
		// This has to be caught before the interruptedException.
		// I needed to throw an exception for a failed simulation,
		// but I cannot change the method signature of the IRunnableWithProgress
		catch (SimulationErrorException e) {

			System.out.println("Box????");
			System.out
					.println("Shell null? " + BNGEditor.getMainEditorShell() == null ? "YES"
							: "NO");
			MessageBox errorMessage = new MessageBox(
					BNGEditor.getMainEditorShell(), SWT.ICON_ERROR | SWT.OK);
			errorMessage.setText("Completed With Errors");
			errorMessage.setMessage("Simulation Finished with Errors.\n\n"
					+ e.getErrorMessage());

			int f = errorMessage.open();

			System.out.println("returned: " + f);

			return false;
		} catch (InterruptedException e) {
			try {
				deleteRecursive(new File(resultsPath));
			} catch (FileNotFoundException fnfe) {
				// TODO Auto-generated catch block
				fnfe.printStackTrace();
			}

			System.out.println("Deleting: " + resultsPath);
			BNGEditor.displayOutput("Simulation Cancelled!\n\n");
			return false;
		}

		return true;
	}

	private static boolean deleteRecursive(File path)
			throws FileNotFoundException {
		if (!path.exists())
			throw new FileNotFoundException(path.getAbsolutePath());
		boolean ret = true;
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				ret = ret && deleteRecursive(f);
			}
		}
		return ret && path.delete();
	}
}
