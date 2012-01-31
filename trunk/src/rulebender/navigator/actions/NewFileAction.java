package rulebender.navigator.actions;

import java.io.File;
import java.io.IOException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import rulebender.navigator.views.ModelTreeView;


public class NewFileAction extends Action
{
	private static final ImageDescriptor m_newFileImage = ImageDescriptor.createFromImage(AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/newfile.gif").createImage());
	
	private File m_parent;
	private ModelTreeView m_view;

	public NewFileAction(File parent, ModelTreeView view)
	{
		setDirToMake(parent);
		setView(view);
	}
	
	public void run()
	{
		// Create the shell
		final Shell nameBox = new Shell(Display.getDefault().getActiveShell(),  SWT.APPLICATION_MODAL
		        | SWT.DIALOG_TRIM);
				
		// Set the text in the title bar
		nameBox.setText("Set File Name");
		
		// set grid layout
		nameBox.setLayout(new GridLayout(3, true));

		// Grid info for the layout.
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		// file name
		Label label1 = new Label(nameBox, SWT.NONE);
		label1.setText("File name:");
				
		// Set the text inside of the new file textbox.
		final Text newFileText = new Text(nameBox, SWT.BORDER);
		newFileText.setLayoutData(gridData);
		newFileText.setText("New File");
				
		// cancel button
		Button cancelbutton = new Button(nameBox, SWT.NONE);
		cancelbutton.setText("Cancel");
		gridData = new GridData();
		gridData.widthHint = 100;
		cancelbutton.setLayoutData(gridData);
				
		// add listener for cancel button
		cancelbutton.addMouseListener(new MouseListener()
		{
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) 
			{
				nameBox.dispose();	
			}
		});
				
		// ok button
		Button okButton = new Button(nameBox, SWT.NONE);
		okButton.setText("OK");
		okButton.setLayoutData(gridData);
		
		nameBox.setDefaultButton(okButton);
				
		// add listener for ok button
		okButton.addMouseListener(new MouseListener()
		{
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) 
			{
				// Must be < 255 characters
				if(newFileText.getText().length() >= 255)
				{
					MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION);
					mb.setMessage("Too many characters! Used " + newFileText.getText().length() + " of 255");
					mb.setText("Error");
					mb.open();
					return;
				}
				
				// Cannot have empty file name			
				if(newFileText.getText().trim().equals(""))
				{
					MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION);
					mb.setMessage("File name can not be empty!");
					mb.setText("Error");
					mb.open();
					return;
				}
				
				// Cannot contain special characters 
				if(newFileText.getText().contains("/") || 
						newFileText.getText().contains("\\") ||
						newFileText.getText().contains("?") ||
						newFileText.getText().contains("%") ||
						newFileText.getText().contains("*") ||
						newFileText.getText().contains(":") ||
						newFileText.getText().contains("|") ||
						newFileText.getText().contains("\"") ||
						newFileText.getText().contains("<") ||
						newFileText.getText().contains(">"))
				{
					MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION);
					mb.setMessage("Illegal Character! (/ \\ ? % * : | \" < > . )");
					mb.setText("Error");
					mb.open();
					return;
				}
				
				// Now check to make sure there is not an existing file of that name.
				for(String file : m_parent.list())
				{
					if(file.equals(newFileText.getText()))
					{
						MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION);
						mb.setMessage("Cannot have duplicate file names!");
						mb.setText("Error");
						mb.open();
						return;
					}
				}
				
				// All is well so create the file
				try 
				{
					(new File(m_parent.getPath() + System.getProperty("file.separator") + newFileText.getText())).createNewFile();
				} 
				catch (IOException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				// refresh the tree
				// TODO optimize: We could pass in the ISelection and only refresh the subtree
				m_view.rebuildWholeTree();
				
				//Dispose of the name box.
				nameBox.dispose();
			}
		});
		
		nameBox.pack();
		nameBox.open();
	}
	
	private void setDirToMake(File parent)
	{
		m_parent = parent;
	}

	private void setView(ModelTreeView view)
	{
		m_view = view;
	}
	
	public ImageDescriptor getImageDescriptor()
	{
		return m_newFileImage;
	}
	
	public String getText()
	{
		return "New File";
	}
}

