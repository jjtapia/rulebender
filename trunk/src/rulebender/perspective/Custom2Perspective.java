package rulebender.perspective;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Custom2Perspective implements IPerspectiveFactory {

	/**
	 * Intializes the Custom2 perspective - model browser and timeline view
	 */
	@Override
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
	}

}
