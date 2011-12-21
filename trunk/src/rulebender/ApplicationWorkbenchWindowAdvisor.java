package rulebender;

import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) 
	{
		return new ApplicationActionBarAdvisor(configurer);
	}

	public void preWindowOpen() 
	{	
		// The IWorkbenchWindowConfigurer instancse is used to set 
		// many options for startup.
		IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		
		// Set the size
		configurer.setInitialSize(new Point(1024, 768));
		
		// Use the coolbar toolbar?
		configurer.setShowCoolBar(false);
		
		// Use the status line?
		configurer.setShowStatusLine(true);
		
		// Show the progress indicator for jobs registered
		// through the jobs api?
		configurer.setShowProgressIndicator(true);
		
		// Show the perspectives bar?
		configurer.setShowPerspectiveBar(true);
				
		// Set the name of the application.
		configurer.setTitle("RuleBender");
		
		IPreferenceStore prefStore = PlatformUI.getPreferenceStore();

		prefStore.setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_EXTRAS, "rulebender.perspective,rulebender.simulate.SimulatePerspective,rulebender.ResultsPerspective");
		prefStore.setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
	}
	
	public void postWindowCreate()
	{
		
		PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
		
		pm.remove("org.eclipse.team.ui.TeamPreferences");
	}
}
