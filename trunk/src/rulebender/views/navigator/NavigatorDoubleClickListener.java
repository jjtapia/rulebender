package rulebender.views.navigator;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import rulebender.editors.common.PathEditorInput;
import rulebender.models.filebrowser2.FileNode;
import rulebender.models.filebrowser2.TreeNode;

public class NavigatorDoubleClickListener implements IDoubleClickListener
{

	public void doubleClick(DoubleClickEvent event) 
	{
		TreeViewer viewer = (TreeViewer) event.getViewer();
		IStructuredSelection selection = (IStructuredSelection) event.getSelection();
	
		//System.out.println("Double click on " + ((FileBrowserTreeNodeInterface) selection).getName());
		
		//viewer.setExpandedState((FileBrowserTreeNodeInterface) selection, !viewer.getExpandedState((FileBrowserTreeNodeInterface)selection));
	
		if (selection.isEmpty()) 
		{
			return;
		}

		Object[] selections = selection.toArray();
		
		for (int sel = 0; sel < selections.length; sel++) 
		{
			TreeNode node = (TreeNode) selections[sel];
			
			if (node.getNodeType().equalsIgnoreCase("FolderNode")) 
			{
				continue;
			}
			
			FileNode fNode = (FileNode) node;
			
			System.out.println("Opening File: " + fNode.getPath());
			
			File file = new File(fNode.getPath());
			
			// Get an IEditorInput object for the File object.
			IEditorInput input = createEditorInput(file);
			
			// Get the String ID for the editor that should be used to open 
			// the file. 
			String editorId = getEditorId(file);
			
			// Get the workbench page that is active so that we can access 
			// the editor that is in it. 
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			// If the editor view is not initialized correctly then an error
			// can be thrown. 
			try 
			{
				// Open the input with the specific editor.
				page.openEditor(input, editorId);
			}
			catch (PartInitException e) 
			{
				e.printStackTrace();
			}
		}
	}
	
	  /**
     * 
     * @param file The File object that needs to be opened in an editor.
     * @return the String ID of the editor that should open the file.
     */
	private String getEditorId(File file) 
	{
		
		// Get the editor registry (defined in plugin.xml)
		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getWorkbench().getEditorRegistry();;
		
		// Get the IEditorDescriptor object for the editor that is 
		// registered to edit the file.  (The registration in plugin.xml
		// is based on filename extension)
		IEditorDescriptor descriptor = editorRegistry.getDefaultEditor(file.getName());	
		
		// If the descriptor is not null and is not equal to the default editor...
		// (I had some trouble opening files with the default.)
		if (descriptor != null && !descriptor.getId().equals("org.eclipse.ui.DefaultTextEditor"))
		{
			// Return the suggested editor ID
			return descriptor.getId();
		}
		
		// If the descriptor is null, or the default is needed, then we 
		// use the simple editor. 
		return "rulebender.editors.simple"; //$NON-NLS-1$
	}

	/**
	 * Create the IEditorInput object from a File.
	 * 
	 * @param file The file for which we need an IEditorInput.
	 * @return The IEditorInput object.
	 */
	private IEditorInput createEditorInput(File file) 
	{
		// Get the IPath for the file.
		IPath location= new Path(file.getAbsolutePath());
		
		// Get an instance of PathEditorInput based on the IPath 
		PathEditorInput input= new PathEditorInput(location);
		
		// Return it.  (PathEditorInput implements IEditorInput)
		return input;
	}
}
	
