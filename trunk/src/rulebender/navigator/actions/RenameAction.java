package rulebender.navigator.actions;

import java.io.File;

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


public class RenameAction extends Action
{
	//private static final ImageDescriptor m_newFolderImage = ImageDescriptor.createFromImage(AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/views/newfolder_wiz.gif").createImage());
	
	private File m_current;
	private ModelTreeView m_view;

	public RenameAction(File current, ModelTreeView view)
	{
		setDirToMake(current);
		setView(view);
	}
	
	public void run()
	{
		// Create the shell
		final Shell nameBox = new Shell(Display.getDefault().getActiveShell(),  SWT.APPLICATION_MODAL
		        | SWT.DIALOG_TRIM);
				
		// Set the text in the title bar
		nameBox.setText("Set New Name");
		
		// set grid layout
		nameBox.setLayout(new GridLayout(3, true));

		// Grid info for the layout.
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		
		// file name
		Label label1 = new Label(nameBox, SWT.NONE);
		label1.setText("Name:");
				
		// Set the text inside of the new file textbox.
		final Text newFileText = new Text(nameBox, SWT.BORDER);
		newFileText.setLayoutData(gridData);
		newFileText.setText(m_current.getName());
				
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
				
				String error = null;
				
				if(m_current.isDirectory())
				{
					error = folderFilter(newFileText.getText());
				}
				else
				{
					error = fileFilter(newFileText.getText());
				}
				
				if(!error.equals(""))
				{
					MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION);
					mb.setMessage(error);
					mb.setText("Error");
					mb.open();
					return;
				}
				
				// Now check to make sure there is not an existing folder of that name.
				String[] siblings;
				
				if(m_current.isDirectory())
				{
					siblings = m_current.list();
				}
				else
				{
					siblings = new File(m_current.getParent()).list();
				}
				
				for(String file : siblings)
				{
					if(file.equals(newFileText.getText()))
					{
						MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_INFORMATION);
						mb.setMessage("Cannot have duplicate directory names!");
						mb.setText("Error");
						mb.open();
						return;
					}
				}
				
				// All is well so create the directory
				
				String abstractPath = m_current.getAbsolutePath();
				
				abstractPath = abstractPath.substring(0, abstractPath.lastIndexOf(System.getProperty("file.separator"))+1) 
					 +  newFileText.getText();	
				
				File newName = new File(abstractPath);
				m_current.renameTo(newName);

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
		m_current = parent;
	}

	private void setView(ModelTreeView view)
	{
		m_view = view;
	}
	
	public ImageDescriptor getImageDescriptor()
	{
		return null; //m_newFolderImage;
	}
	
	public String getText()
	{
		return "Rename...";
	}
	
	private String fileFilter(String name)
	{
		String error = "";
		
		// Must be < 255 characters
		if(name.length() >= 255)
		{
			error += ("Too many characters! Used " + name.length() + " of 255\n");
		}
		
		// Cannot have empty file name			
		if(name.trim().equals(""))
		{
		
			error += "Folder name can not be empty!\n";
		}
		
		// Cannot contain special characters 
		if(name.contains("/") || 
				name.contains("\\") ||
				name.contains("?") ||
				name.contains("%") ||
				name.contains("*") ||
				name.contains(":") ||
				name.contains("|") ||
				name.contains("\"") ||
				name.contains("<") ||
				name.contains(">"))
		{
			error += "Illegal Character! (/ \\ ? % * : | \" < >)\n";
		}
	
		return error;	
	}
	
	private String folderFilter(String name)
	{
		String error = fileFilter(name);
		
		if(name.contains("."))
		{
			error += "Folders cannot contain \".\"";
		}
		
		return error;
	}
}

