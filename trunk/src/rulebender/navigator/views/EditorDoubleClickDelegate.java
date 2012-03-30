package rulebender.navigator.views;

import java.io.File;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;

import rulebender.core.utility.FileInputUtility;
import rulebender.navigator.model.FileNode;
import rulebender.navigator.model.TreeNode;

public class EditorDoubleClickDelegate 
{

	public void doubleClick(DoubleClickEvent event) 
	{
	System.out.println("Double click");
		//TreeViewer viewer = (TreeViewer) event.getViewer();
		
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
				System.out.println("Opening File: " + fNode.getPath());
				
				// Get a reference to the file
				File file = new File(fNode.getPath());
				
				FileInputUtility.openFileInEditor(file);
			}
		}
	}	
}
