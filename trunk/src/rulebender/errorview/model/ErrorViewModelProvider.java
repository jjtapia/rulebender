package rulebender.errorview.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import rulebender.editors.bngl.BNGLEditor;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.errorview.view.ErrorView;

public class ErrorViewModelProvider implements ISelectionListener, IPartListener2
{
	private List<BNGLError> m_errors;
	
	private ErrorView m_view;

	public ErrorViewModelProvider(ErrorView view) {
		m_errors = new ArrayList<BNGLError>();
		
		m_view = view;
		
		//m_errors.add(new BNGLError("test1", 2, "This is a test and should always be there."));
		
		// Register the class as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);
		// Register the class as a listener for workbench selections and part events.
		m_view.getSite().getPage().addPartListener(this);
		
	}

	public List<BNGLError> getErrors() {
		return m_errors;
	}
	
	public void addErrors(BNGLError error)
	{
		
	}
	
	public void fileClosed(String path)
	{
	}

	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
	{
	}
	
	@Override
	public void partClosed(IWorkbenchPartReference partRef) 
	{
		
		if(partRef.getId().equals("rulebender.editors.bngl"))
		{
			System.out.println("File opened seen in error view"+((BNGLEditor)partRef.getPart(false)).getTitle());
			
			BNGLModel model = ((BNGLEditor) partRef.getPart(false)).getModel();
		
			// remove the errors
			removeErrors(model.getPathID());
		
			// refresh.
			m_view.refresh();
		}
	}
	
	@Override
	public void partOpened(IWorkbenchPartReference partRef) 
	{
		
		if(partRef.getId().equals("rulebender.editors.bngl"))
		{
			System.out.println("File opened seen in error view"+((BNGLEditor)partRef.getPart(false)).getTitle());
			
			BNGLModel model = ((BNGLEditor) partRef.getPart(false)).getModel();
		
				// Create a property changed listener for when files are saved.
			PropertyChangeListener pcl = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent propertyChangeEvent) 
				{
					String filePath = ((BNGLModel) propertyChangeEvent.getSource()).getPathID();
					String propertyName = propertyChangeEvent.getPropertyName();
					
					if(propertyName.equals(BNGLModel.ERRORS))
					{
						//Update the display object that is associated with the path and ast. 
						System.out.println("Property Changed on Errors");
						updateTableForPathAndErrors(filePath, (ArrayList<BNGLError>) propertyChangeEvent.getNewValue());
					}
				}
			};
			
			model.addPropertyChangeListener(pcl);
		
		// generate the display and add the initial cmap to the registry.
		updateTableForPathAndErrors(model.getPathID(), model.getErrors());
				
		} // end if it's an editor block
	}
	
	
	
	/**
	 * Updates the errors associated with a specific file.
	 * 
	 * @param pathID
	 * @param errors
	 */
	private void updateTableForPathAndErrors(String pathID,	ArrayList<BNGLError> errors) 
	{
		// remove the bugs
		removeErrors(pathID);
		
		// Add the new ones.
		m_errors.addAll(errors);
		
		// refresh the view
		m_view.refresh();
	}

	/**
	 * Removes all of the errors associated with a path.
	 * @param pathID
	 * @param errors
	 */
	private void removeErrors(String pathID)
	{
		// Remove the old ones.
		ArrayList<BNGLError> toRemove = new ArrayList<BNGLError>();
				
		for(BNGLError be : m_errors)
		{
			if(be.getFilePath().equals(pathID))
			{
				toRemove.add(be);
			}
		}
		
		m_errors.removeAll(toRemove);
	}
	
	
	////////////////////////////////////////////////////////////////
	// Not using these.

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {}


	@Override
	public void partHidden(IWorkbenchPartReference partRef) {}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {}
}

