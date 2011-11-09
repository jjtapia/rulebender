package rulebender.navigator.actions;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import rulebender.filebrowser2.models.FileNode;
import rulebender.filebrowser2.models.FolderNode;
import rulebender.filebrowser2.models.TreeNode;
import rulebender.navigator.views.ModelTreeView;


public class DeleteFileAction extends Action
{
	
	private static final ImageDescriptor m_deleteImage = ImageDescriptor.createFromImage(AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/delete_obj.gif").createImage());
	
	private IStructuredSelection m_selectionToDelete;
	private ModelTreeView m_view;

	public DeleteFileAction(IStructuredSelection toDelete, ModelTreeView view)
	{
		setSelectionToDelete(toDelete);
		setView(view);
	}
	
	public void run()
	{	
		// Create a message box to ask if the user really wants to delete the file(s)
		 MessageBox messageBox = new MessageBox(Display.getDefault().getActiveShell(), SWT.ICON_QUESTION
		             | SWT.YES | SWT.NO);
			
		// Set the text in the title bar and label
		if(m_selectionToDelete.size() > 1)
		{
			messageBox.setText("Delete Files?");
			messageBox.setMessage("Do you really want to delete these files?  (Cannot be undone)");
		}
		else
		{
			messageBox.setText("Delete File?");
			messageBox.setMessage("Do you really want to delete this file? (Cannot be undone)");
		}
			        
		// Open the message box and get their response. 
        int response = messageBox.open();
        
        // If they say no then do nothing.
        if (response == SWT.NO)
        {
        	return;
        }
        // If they say yes, then continue with the deletion
        else
        {
        	//All is well so create the directory
        	for(Object selection : m_selectionToDelete.toList())
        	{
        		if(selection instanceof FolderNode)
        		{
        			// Delete the directory and everything in it.
        			removeDirectory(new File(((FolderNode) selection).getPath()));		
        		}
        		if(selection instanceof FileNode)
        		{
        			// Delete the file.
       				(new File(((FileNode) selection).getPath())).delete(); 
        		}
        	}

    		// refresh the tree
    		// TODO optimize: We could pass in the ISelection and only refresh the subtree
    		m_view.rebuildWholeTree();		
        }
	}
	
	private void setSelectionToDelete(IStructuredSelection toDelete)
	{
		m_selectionToDelete = toDelete;
	}

	private void setView(ModelTreeView view)
	{
		m_view = view;
	}
	
	public String getText()
	{
		return "Delete";
	}
	
	public ImageDescriptor getImageDescriptor()
	{
		return m_deleteImage;
	}
	
	 /**
	  Remove a directory and all of its contents.

	  The results of executing File.delete() on a File object
	  that represents a directory seems to be platform
	  dependent. This method removes the directory
	  and all of its contents.

	  @return true if the complete directory was removed, false if it could not be.
	  If false is returned then some of the files in the directory may have been removed.

	  @author http://www.java2s.com/Tutorial/Java/0180__File/Removeadirectoryandallofitscontents.htm
	*/
	public static boolean removeDirectory(File directory) {

	  // System.out.println("removeDirectory " + directory);

	  if (directory == null)
	    return false;
	  if (!directory.exists())
	    return true;
	  if (!directory.isDirectory())
	    return false;

	  String[] list = directory.list();

	  // Some JVMs return null for File.list() when the
	  // directory is empty.
	  if (list != null) {
	    for (int i = 0; i < list.length; i++) {
	      File entry = new File(directory, list[i]);

	      //        System.out.println("\tremoving entry " + entry);

	      if (entry.isDirectory())
	      {
	        if (!removeDirectory(entry))
	          return false;
	      }
	      else
	      {
	        if (!entry.delete())
	          return false;
	      }
	    }
	  }

	  return directory.delete();
	}
}

