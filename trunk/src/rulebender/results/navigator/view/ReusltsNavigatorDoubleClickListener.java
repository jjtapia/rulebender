package rulebender.results.navigator.view;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import rulebender.editors.common.PathEditorInput;
import rulebender.navigator.model.FileNode;
import rulebender.navigator.model.TreeNode;
import rulebender.results.view.ResultsView;

public class ReusltsNavigatorDoubleClickListener implements	IDoubleClickListener 
{
	
	public void doubleClick(DoubleClickEvent event) 
	{
			// Get a reference to the selection.
			IStructuredSelection selection = (IStructuredSelection) event.getSelection();
		
			//System.out.println("Double click on " + ((FileBrowserTreeNodeInterface) selection).getName());
			
			//viewer.setExpandedState((FileBrowserTreeNodeInterface) selection, !viewer.getExpandedState((FileBrowserTreeNodeInterface)selection));
		
			// Don't do anything for an empty selection.
			if (selection.isEmpty()) 
			{
				return;
			}

			// Get the selected objects as an Object array.
			Object[] selections = selection.toArray();
			
			// For each selected Object in the array.
			for (int sel = 0; sel < selections.length; sel++) 
			{
				// Get the tree node that was selected.
				TreeNode node = (TreeNode) selections[sel];
				
				// If it is a folder node, then skip it. 
				if (node.getNodeType().equalsIgnoreCase("FolderNode")) 
				{
					continue;
				}
				
				// If it is a file node  
				if(node.getNodeType().equalsIgnoreCase("FileNode"))
				{
					// Get a reference. 
					FileNode fNode = (FileNode) node;
					
					// DEBUG
					System.out.println("Opening Results File: " + fNode.getPath());
					
					// Get a reference to the file
					//FIXME  This is really ugly and makes a requirement between these two views, but I am in a hurry. 
					ResultsView resultsView =  (ResultsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("rulebender.results.view.ResultsView");
					resultsView.openFile(fNode);
					
					// Use the selection service?  
				}
			}
		}
}
