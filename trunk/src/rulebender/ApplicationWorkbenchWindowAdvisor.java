package rulebender;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

/**
 * Eclipse RCP Generated Class.  This class manages the windows of the 
 * workbench.  You can interact with the various configurer objects that
 * are passed in to the initialization methods. Some manual settings
 * have been changed here: see comments in the code.
 */
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
		
		// Set the size of the window.
		configurer.setInitialSize(new Point(1024, 768));
		
		// Use the coolbar toolbar?  The coolbar let the user move the buttons around.
		// It is what is used by eclipse by default I think.
		configurer.setShowCoolBar(false);
		
		// Use the status line?  The status line is in the bottom left of the window
		// and can be used by any view to display information.
		configurer.setShowStatusLine(true);
		
		// Show the progress indicator for jobs registered
		// through the jobs api?  This shows a progress indicator on the window
		// when the Jobs API is used.  
		configurer.setShowProgressIndicator(true);
		
		// Show the perspectives bar? The Perspectives bar displays
		// perspectives that the user can switch to. 
		configurer.setShowPerspectiveBar(true);
				
		// Set the name of the application.
		configurer.setTitle("RuleBender");
		
		// The Preference Store manages program options that are stored
		// as key value pairs.
		IPreferenceStore prefStore = PlatformUI.getPreferenceStore();

		// Set the buttons that appear in the perspective switcher.  These
		// id values are for the modeling, simulation, and analysis perspectives.
		prefStore.setValue(IWorkbenchPreferenceConstants.PERSPECTIVE_BAR_EXTRAS, "rulebender.perspective,rulebender.simulate.SimulatePerspective,rulebender.ResultsPerspective");
		
		// Do not use the traditionaly style tabs.  I think that this is just a cosmetic option.
		prefStore.setValue(IWorkbenchPreferenceConstants.SHOW_TRADITIONAL_STYLE_TABS, false);
	}
	
	public void postWindowCreate()
	{
		// The PreferenceManager manages the preference panes in the program that
		// are accessible by clicking "RuleBender" (OSX) or "Window" in the menu and then
		// selecting "Preferences...".
		PreferenceManager pm = PlatformUI.getWorkbench().getPreferenceManager();
		
		// Remove the team preferences.  Any plugins that are included in the rulebender product
		// can contribute to the PreferenceManager, so we must manually remove the ones that we do 
		// not want to include.
		pm.remove("org.eclipse.team.ui.TeamPreferences");
	}
}
