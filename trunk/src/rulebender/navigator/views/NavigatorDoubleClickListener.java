package rulebender.navigator.views;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.PlatformUI;

public class NavigatorDoubleClickListener implements IDoubleClickListener
{
	
	private EditorDoubleClickDelegate m_editorDelegate;
	private SimulateDoubleClickDelegate m_simulateDelegate;
	private ResultsDoubleClickDelegate m_resultsDelegate;

	private String EDIT_ID = "rulebender.perspective";
	private String SIMULATE_ID = "rulebender.simulate.SimulatePerspective";
	private String RESULTS_ID = "rulebender.ResultsPerspective";
	
	NavigatorDoubleClickListener()
	{
		m_editorDelegate = new EditorDoubleClickDelegate();
		m_simulateDelegate = new SimulateDoubleClickDelegate();
		m_resultsDelegate = new ResultsDoubleClickDelegate();
	}
	
	public void doubleClick(DoubleClickEvent event) 
	{
		 IPerspectiveDescriptor activePerspective  = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getPerspective();
		 
		 if(activePerspective.getId().equals(EDIT_ID))
		 {
			 System.out.println("Editor");
			 m_editorDelegate.doubleClick(event);
		 }
		 
		 else if(activePerspective.getId().equals(SIMULATE_ID))
		 {
			 System.out.println("Simulate");
			 m_simulateDelegate.doubleClick(event);
		 }
		 
		 else if(activePerspective.getId().equals(RESULTS_ID))
		 {
			 System.out.println("Results");
			 m_resultsDelegate.doubleClick(event);
		 }
	
	}
}
	
