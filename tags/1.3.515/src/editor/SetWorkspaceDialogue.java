package editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SetWorkspaceDialogue {
	Shell parentShell, setWorkspaceDialogue;
	Text newFileText;
	BNGEditor editor;
	Button emptyFile;
	boolean canQuit;

	public SetWorkspaceDialogue(final Shell mainEditorShell,
			BNGEditor editorIn, final boolean canQuit) {
		// Set Locals
		parentShell = mainEditorShell;
		editor = editorIn;
		this.canQuit = canQuit;

		// Create the shell
		setWorkspaceDialogue = new Shell(parentShell, SWT.DIALOG_TRIM
				| SWT.PRIMARY_MODAL);

		// Set the text in the title bar
		setWorkspaceDialogue.setText("Set Workspace (absolute) Path");

		// set grid layout
		setWorkspaceDialogue.setLayout(new GridLayout(5, true));

		setWorkspaceDialogue.addShellListener(new ShellListener() {

			public void shellIconified(ShellEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void shellDeiconified(ShellEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void shellDeactivated(ShellEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void shellClosed(ShellEvent arg0) {
				if (!canQuit)
					System.exit(0);
			}

			public void shellActivated(ShellEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		// The label that says "Current Workspace"
		GridData lableGridData = new GridData(GridData.FILL_HORIZONTAL); // null
																			// possibly?
		lableGridData.horizontalSpan = 1;
		Label workspacePathlabel1 = new Label(setWorkspaceDialogue, SWT.NONE);
		workspacePathlabel1.setText("Current Workspace (absolute) Path:");
		workspacePathlabel1.setLayoutData(lableGridData);

		// The text box that holds the workspace path.
		final Text workspacePathtext1 = new Text(setWorkspaceDialogue,
				SWT.BORDER);
		GridData pathTextGridData = new GridData(GridData.FILL_HORIZONTAL); // null
																			// possibly?
		pathTextGridData.horizontalSpan = 3;
		pathTextGridData.horizontalAlignment = SWT.FILL;
		pathTextGridData.verticalAlignment = SWT.FILL;
		workspacePathtext1.setLayoutData(pathTextGridData);

		// If there is already a path then put the path in the text box.
		if (ConfigurationManager.getConfigurationManager().getWorkspacePath() != null) {
			workspacePathtext1.setText(ConfigurationManager
					.getConfigurationManager().getWorkspacePath());
		}

		// change button
		Button workspacePathbutton1 = new Button(setWorkspaceDialogue, SWT.NONE);
		workspacePathbutton1.setText("Change...");
		lableGridData = new GridData();
		lableGridData.widthHint = 100;
		workspacePathbutton1.setLayoutData(lableGridData);

		// add listener for change button
		workspacePathbutton1.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				// Create the Dialogue box
				DirectoryDialog opendiag = new DirectoryDialog(mainEditorShell,
						SWT.OPEN);

				// Get the path from the dialogue box.
				String fpath = opendiag.open();

				// Set the text in the text box to the path.
				workspacePathtext1.setText(fpath);
			}
		});

		// empty label to control layout
		new Label(setWorkspaceDialogue, SWT.NONE).setText("");
		new Label(setWorkspaceDialogue, SWT.NONE).setText("");
		new Label(setWorkspaceDialogue, SWT.NONE).setText("");

		// cancel button
		Button workspacePathbutton3 = new Button(setWorkspaceDialogue, SWT.NONE);
		workspacePathbutton3.setText("Cancel");
		workspacePathbutton3.setLayoutData(lableGridData);

		// ok button
		Button workspacePathbutton2 = new Button(setWorkspaceDialogue, SWT.NONE);
		workspacePathbutton2.setText("OK");
		workspacePathbutton2.setLayoutData(lableGridData);

		// add listener for cancel button
		workspacePathbutton3.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (ConfigurationManager.getConfigurationManager()
						.getWorkspacePath() == null) {
					MessageBox mb = new MessageBox(setWorkspaceDialogue,
							SWT.ICON_INFORMATION);
					mb.setMessage("Workspace Path cannot be empty. ");
					mb.setText("Error Info");
					mb.open();
					return;
				} else {
					setWorkspaceDialogue.dispose();
				}
			}
		});

		// add listener for ok button
		workspacePathbutton2.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				if (workspacePathtext1.getText().trim().length() == 0) {
					MessageBox mb = new MessageBox(setWorkspaceDialogue,
							SWT.ICON_INFORMATION);
					mb.setMessage("Workspace Path cannot be empty. ");
					mb.setText("Error Info");
					mb.open();
					return;
				} else {
					String temppath = workspacePathtext1.getText().trim();

					/*
					 * if(ConfigurationManager.getConfigurationManager().getOSType
					 * () == 1) {
					 * ConfigurationManager.getConfigurationManager().
					 * setBNGFPath(temppath.substring(0,
					 * temppath.lastIndexOf('\\')));
					 * ConfigurationManager.getConfigurationManager
					 * ().setBNGFName
					 * (temppath.substring(temppath.lastIndexOf('\\')+1,
					 * temppath.length())); } else {
					 * ConfigurationManager.getConfigurationManager
					 * ().setBNGFPath(temppath.substring(0,
					 * temppath.lastIndexOf('/')));
					 * ConfigurationManager.getConfigurationManager
					 * ().setBNGFName
					 * (temppath.substring(temppath.lastIndexOf('/')+1,
					 * temppath.length())); }
					 */

					ConfigurationManager.getConfigurationManager()
							.setWorkspacePath(temppath);

					setWorkspaceDialogue.dispose();
					BNGEditor.displayOutput("Workspace Path Changed !");

					System.out.println("new Workspace:" + temppath);

				}
			}
		});
	}

	public void show() {
		setWorkspaceDialogue.pack();
		setWorkspaceDialogue.open();

		Display d = setWorkspaceDialogue.getDisplay();
		while (setWorkspaceDialogue != null
				&& !setWorkspaceDialogue.isDisposed()) {
			if (d != null && !d.readAndDispatch()) {
				d.sleep();
			}
		}
	}
}
