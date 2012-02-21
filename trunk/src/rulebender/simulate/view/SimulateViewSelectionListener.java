package rulebender.simulate.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import rulebender.navigator.model.FileNode;
import rulebender.navigator.model.TreeNode;
import rulebender.navigator.views.ModelTreeView;

public class SimulateViewSelectionListener implements ISelectionListener {

	private SimulateView m_view;
	
	public SimulateViewSelectionListener(SimulateView view)
	{
		setView(view);
		
		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);		
	
	}
	
	public void selectionChanged(IWorkbenchPart part, ISelection in_selection) 
	{
		System.out.println(part.getClass().toString());
		
		// Check if it is from the model navigator
		if(part.getClass() == ModelTreeView.class)
		{	
			IStructuredSelection selection = (IStructuredSelection) in_selection;
			
			// Don't do anything for an empty selection.
			if (selection.isEmpty()) 
			{
				return;
			}

			// Get the selected objects as an Object array.
			Object[] selections = selection.toArray();
			
			// Get the tree node that was selected. (just the first)
			TreeNode node = (TreeNode) selections[0];
				
			// If it is a folder node, then skip it. 
			if (node.getNodeType().equalsIgnoreCase("FolderNode")) 
			{
				return;
			}
				
			// If it is a file node  
			if(node.getNodeType().equalsIgnoreCase("FileNode"))
			{
				// Get a reference. 
				FileNode fNode = (FileNode) node;
				
				String file = fNode.getPath();
				
				if(file.endsWith(".bngl"))	
				{
					// Tell the view
					m_view.setSelectedFileText(file);
				}
			}
		}
	}

	/**
	 * @param m_view the m_view to set
	 */
	private void setView(SimulateView m_view) 
	{
		this.m_view = m_view;
	}


}
