package rulebender;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/** 
 * This class is for the simulate perspective.  The perspectives can be
 * configured manually here by adding views programatically, but I did all of 
 * that in the plugin.xml file under the org.eclipse.ui.perspectiveExtensions
 *extension point. 
 */
public class SimulationPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) 
	{
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
	}

}
