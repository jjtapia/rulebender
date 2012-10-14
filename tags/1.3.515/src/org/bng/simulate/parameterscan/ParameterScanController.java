package org.bng.simulate.parameterscan;

import org.eclipse.swt.widgets.Composite;

import editor.CurrentFile;

public class ParameterScanController {
	private static ParameterScanController controller;

	private CurrentFile currentFile;

	private ParameterScanView view;

	private ParameterScanController() {

	}

	public synchronized static ParameterScanController getParameterScanController() {
		if (controller == null)
			controller = new ParameterScanController();

		return controller;
	}

	protected void saveData(ParameterScanData scanData) {
		currentFile.setLastInput(scanData);
	}

	protected void runParameterScan(ParameterScanData scanData) {
		currentFile.parscan(scanData);
	}

	/*
	 * There is a bug here. If the shell that contains the view has been closed,
	 * then the view is disposed on the change of a file, but is not set to null
	 * here.
	 * 
	 * In the current setup, there is no access to the shell because it is just
	 * created in order to hold the view. When the window is closed, everything
	 * goes away and will be generated the next time the window is needed.
	 * 
	 * Solutions: 1. nullify the view when the shell is closed. - Will have to
	 * put a callback for the controller of the view. 2. save the shells for the
	 * actions and make it so they are not disposed when they are closed. - will
	 * cost more memory, but it will save time generating the windows.
	 */
	public void fileSelectionChange(CurrentFile cf) {
		currentFile = cf;

		// TODO This is a temporary solution.
		try {
			if (view != null)
				view.setFormText(currentFile.getLastPSInput());
		} catch (org.eclipse.swt.SWTException e) {
			// do nothing.
		}
	}

	public Composite getView(Composite parent) {
		if (currentFile == null) {
			return null;
		}
		view = new ParameterScanView(this, parent);
		view.setFormText(currentFile.getLastPSInput());
		return view;
	}

	public void disposeOfWindow() {
		view.getParent().dispose();
	}
}
