package action;

import org.bng.simulate.parameterscan.ParameterScanController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import visualizationviewer.VisualizationViewerController;

import editor.BNGEditor;
import editor.ConfigurationManager;
import editor.CurrentFile;

public class OpenAction implements ActionInterface {

	public String getName() {
		return "Open";
	}

	public String getShortName() {
		return "Open";
	}

	public boolean hasComposite() {
		return false;
	}

	public Composite getComposite(Composite parent) {
		return null;
	}

	public void executeAction() {
		Shell mainEditorShell = BNGEditor.getMainEditorShell();

		FileDialog opendiag = new FileDialog(mainEditorShell, SWT.OPEN);
		opendiag.setFilterExtensions(new String[] { "*.bngl", "*.txt", "*.*" });
		opendiag.setFilterPath(ConfigurationManager.getConfigurationManager()
				.getWorkspacePath());
		opendiag.open();
		String fpath = opendiag.getFilterPath(), fname = opendiag.getFileName();

		if (fname.equals("") || fname == null)
			return;
		else {
			CurrentFile tempfile;
			boolean fileexist = false;
			for (int i = 0; i < BNGEditor.getInputfiles().size(); i++)
				if (BNGEditor.getInputfiles().get(i).getFilepath() != null)
					if (BNGEditor.getInputfiles().get(i).getFilepath()
							.equals(fpath)
							&& BNGEditor.getInputfiles().get(i).getFileName()
									.equals(fname)) {
						MessageBox mb = new MessageBox(mainEditorShell,
								SWT.ICON_INFORMATION);
						mb.setMessage("File has already been opened !");
						mb.setText("Error info");
						mb.open();
						BNGEditor.getTextFolder().setSelection(i);
						fileexist = true;
						return;
					}

			if (!fileexist) {
				tempfile = new CurrentFile(fpath, fname, ConfigurationManager
						.getConfigurationManager().getOSType(), false, -1);
				BNGEditor.getInputfiles().add(tempfile);
				BNGEditor
						.setFileselection(BNGEditor.getInputfiles().size() - 1);

				// Tell the viewer that we have a focus file.
				VisualizationViewerController.loadVisualizationViewController()
						.fileBecomesFocus(tempfile);

				ParameterScanController.getParameterScanController()
						.fileSelectionChange(tempfile);

				BNGEditor.getTextFolder().setSelection(
						BNGEditor.getFileselection());
				if (ConfigurationManager.getConfigurationManager().getOSType() == 1)
					BNGEditor.getShowfilepath().setText(fpath + "\\" + fname);
				else
					BNGEditor.getShowfilepath().setText(fpath + "/" + fname);
			}
		}
	}

	public Point getSize() {
		return null;
	}

}
