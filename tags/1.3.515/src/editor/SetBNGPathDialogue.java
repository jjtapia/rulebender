package editor;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SetBNGPathDialogue 
{
	Shell parentShell, setBNGPathDialogue;
	Text newFileText;
	BNGEditor editor;
	Button emptyFile;
	
	public SetBNGPathDialogue(final Shell mainEditorShell, BNGEditor editorIn)
	{
		// Set Locals
		parentShell = mainEditorShell;
		editor = editorIn;
		
		// Create the shell
		setBNGPathDialogue = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		
		// Set the text in the title bar
		setBNGPathDialogue.setText("Set (absolute) Path to BNG Root");
		
		// set grid layout
		setBNGPathDialogue.setLayout(new GridLayout(5, true));
		
		// current BNG path
		Label BNGPathlabel1 = new Label(setBNGPathDialogue, SWT.NONE);
		BNGPathlabel1.setText("Current (absolute) BNG Path:");
		GridData gridData = new GridData();
		gridData.horizontalSpan = 1;
		BNGPathlabel1.setLayoutData(gridData);
		
		final Text pathTextBox = new Text(setBNGPathDialogue, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.horizontalSpan = 3;
		pathTextBox.setLayoutData(gridData);

		// If the path in the configuration file is not null
		if(ConfigurationManager.getConfigurationManager().getBNGFPath() != null && ConfigurationManager.getConfigurationManager().getBNGFName() != null )
		{
			// Get the path from the manager
			String path = ConfigurationManager.getConfigurationManager().getBNGFPath();
			
			if(path.contains("Perl2"))
			{
				// Get up to the Perl2 directory.
				path = path.substring(0, path.indexOf("Perl2"));
			}

			// Set the text
			pathTextBox.setText(path);
		}
		
		// change button
		Button BNGPathbutton1 = new Button(setBNGPathDialogue, SWT.NONE);
		BNGPathbutton1.setText("Change...");
		gridData = new GridData();
		gridData.widthHint = 100;
		BNGPathbutton1.setLayoutData(gridData);
		
		// add listener for change button
		BNGPathbutton1.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) 
			{
				// Create the dialogue that gets a directory
				DirectoryDialog opendiag = new DirectoryDialog(mainEditorShell, SWT.OPEN);
				
				// Get the returned name.
				String fpath = opendiag.open(); 
				
				// Set the text box to contain the text for the path.
				if(fpath!=null)
					pathTextBox.setText(fpath);
			}				
		});
		
		// empty label to control layout
		new Label(setBNGPathDialogue, SWT.NONE).setText("");
		new Label(setBNGPathDialogue, SWT.NONE).setText("");
		new Label(setBNGPathDialogue, SWT.NONE).setText("");
		
		// cancel button
		Button cancelButton = new Button(setBNGPathDialogue, SWT.NONE);
		cancelButton.setText("Cancel");
		cancelButton.setLayoutData(gridData);
		
		// add listener for cancel button
		cancelButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				setBNGPathDialogue.dispose();
			}					
		});
		
		// ok button
		Button okButton = new Button(setBNGPathDialogue, SWT.NONE);
		okButton.setText("OK");
		okButton.setLayoutData(gridData);
		
		// add listener for ok button
		okButton.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) 
			{
				// Add on the Perl2/BNG2.pl directory
				String completedPath = pathTextBox.getText().trim();
				
				while(completedPath.substring(completedPath.length()-1, completedPath.length()).equals("\"") || 
						completedPath.substring(completedPath.length()-1, completedPath.length()).equals("/"))
				{
					completedPath = completedPath.substring(0, completedPath.length()-1);
				}
				
				// Windows.
				if(ConfigurationManager.getConfigurationManager().getOSType() == 1)
				{
					completedPath += "\\BNG2.pl";
				}
				// Everything else.
				else
				{
					completedPath += "/BNG2.pl";
				}
				
				// Check if it is empty
				if(pathTextBox.getText().trim().length() == 0)
				{
					MessageBox mb = new MessageBox(setBNGPathDialogue, SWT.ICON_INFORMATION);
					mb.setMessage("BNG Path cannot be empty. ");
					mb.setText("Error Info");
					mb.open();
					return;
				}
				// Check if the files are there. 
				else if (!(new File(completedPath)).exists())
				{
					System.out.println(completedPath);
					MessageBox mb = new MessageBox(setBNGPathDialogue, SWT.ICON_INFORMATION);
					mb.setMessage("Error finding BioNetGen executables in given directory");
					mb.setText("Error Info");
					mb.open();
					return;

				}
				
				// Good to go!
				else
				{	
					if(ConfigurationManager.getConfigurationManager().getOSType() == 1)
					{
						ConfigurationManager.getConfigurationManager().setBNGFPath(completedPath.substring(0, completedPath.lastIndexOf('\\')));
						ConfigurationManager.getConfigurationManager().setBNGFName(completedPath.substring(completedPath.lastIndexOf('\\')+1, completedPath.length()));
					}
					else
					{
						ConfigurationManager.getConfigurationManager().setBNGFPath(completedPath.substring(0, completedPath.lastIndexOf('/')));
						ConfigurationManager.getConfigurationManager().setBNGFName(completedPath.substring(completedPath.lastIndexOf('/')+1, completedPath.length()));
					}
				
					BNGEditor.displayOutput("\nBNG Path Changed to " + completedPath + "!\n");
				}
				
				setBNGPathDialogue.dispose();
			}
		});
	}
	
	public void show()
	{
		setBNGPathDialogue.pack();
		setBNGPathDialogue.open();
	}
}
