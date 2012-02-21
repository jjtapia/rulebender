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

import rulebender.core.workspace.PickWorkspaceDialog;
import rulebender.navigator.views.ModelTreeView;


public class NewProjectAction extends Action
{
	private static final ImageDescriptor m_newFileImage = ImageDescriptor.createFromImage(AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/views/newfile.gif").createImage());
	
	private static File m_parent = new File(PickWorkspaceDialog.getLastSetWorkspaceDirectory());
	private NewFolderAction m_action; 
	
	public NewProjectAction(ModelTreeView view)
	{
		m_action = new NewFolderAction(m_parent, view);
	}
	
	public void run()
	{
		m_action.run();
	}
	
	public ImageDescriptor getImageDescriptor()
	{
		return m_newFileImage;
	}
	
	public String getText()
	{
		return "New Project";
	}
}

