package rulebender.navigator.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;


import rulebender.editors.dat.model.FileNode;
import rulebender.results.view.ResultsView;

public class CompareAction extends Action 
{
	private String RESULTS_ID = "rulebender.ResultsPerspective";
	
	private ResultsView resultsView = (ResultsView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("rulebender.results.view.ResultsView");
	
	private static final ImageDescriptor m_deleteImage = ImageDescriptor.createFromImage(AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/views/compare.gif").createImage());
	
	private IStructuredSelection m_selectionToCompare;

	public CompareAction(IStructuredSelection toCompare)
	{
		setSelectionToCompare(toCompare);
	}
	
	@Override
	public void run()
	{	
		// Get the selection (Should just be one at this point) 
		//TODO if I ever allow multiple selections this will have to change.
    	Object selection = m_selectionToCompare.toList().get(0);
    	
		if(selection instanceof FileNode)
		{	
			resultsView.compareWith((FileNode) selection); 
		}
		
	}
	
	
	@Override
	public boolean isEnabled()
	{
		System.out.println("Compare isEnabled()");
		IPerspectiveDescriptor activePerspective  = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
		
		//FIXME This is another ugly connection.
		if(activePerspective.getId().equals(RESULTS_ID) && resultsView.isComparable())
		{
			// Get the selection (Should just be one at this point) 
			//TODO if I ever allow multiple selections this will have to change.
	    	Object selection = m_selectionToCompare.toList().get(0);

			String selectFileName = ((FileNode) selection).getName();

			if (selectFileName.endsWith(".cdat") || 
				  selectFileName.endsWith(".gdat") || 
				  selectFileName.endsWith(".scan")) 
			{
				System.out.println("Could Compare");
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public String getText()
	{
		return "Compare with Current";
	}

	@Override
	public ImageDescriptor getImageDescriptor()
	{
		return m_deleteImage;
	}
	
	private void setSelectionToCompare(IStructuredSelection toDelete)
	{
		m_selectionToCompare = toDelete;
	
	
	}
}
