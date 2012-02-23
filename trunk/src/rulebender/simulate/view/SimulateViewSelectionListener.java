package rulebender.simulate.view;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.navigator.CommonNavigator;

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
		if(part.getClass() == CommonNavigator.class)
		{	
			IStructuredSelection selection = (IStructuredSelection) in_selection;
			
			// Don't do anything for an empty selection.
			if (selection.isEmpty()) 
			{
				return;
			}
			
			IResource selectedObject = (IResource) selection.getFirstElement();
			
			// If it is a folder node, then skip it. 
			if (selectedObject.getFileExtension() != null &&
					selectedObject.getFileExtension().equals("bngl")) 
			{
				m_view.setSelectedFileText(selectedObject.getRawLocation().makeAbsolute().toOSString());
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
