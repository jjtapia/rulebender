package rulebender;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class ResultsPerspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) 
	{
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
	}

}
