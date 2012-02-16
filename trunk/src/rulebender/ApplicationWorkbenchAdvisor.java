package rulebender;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.application.IWorkbenchConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchAdvisor extends WorkbenchAdvisor {

	private static final String PERSPECTIVE_ID = "rulebender.perspective";

	public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		return new ApplicationWorkbenchWindowAdvisor(configurer);
	}

	public String getInitialWindowPerspectiveId() {
		return PERSPECTIVE_ID;
	}
	
	
	//To get the resource workspace as input, override this method:
	// For common navigator framework
	public IAdaptable getDefaultPageInput() 
	{
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
			return workspace.getRoot();
	}
	
	//To get the correct adapters hooked up add this code to the initialize() method:
	// For common navigator framework
	public void initialize(IWorkbenchConfigurer configurer) 
	{
		org.eclipse.ui.ide.IDE.registerAdapters();
		
		configurer.setSaveAndRestore(true);
	}	
}
