package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import visualizationviewer.VisualizationViewerController;

public class VisualizeAction implements ActionInterface {

	public String getName() {
		return "Visualize";
	}

	public String getShortName() {
		return "Visualize";
	}

	public boolean hasComposite() {
		// TODO Auto-generated method stub
		return false;
	}

	public Composite getComposite(Composite parent) {
		return null;
	}

	public void executeAction() {
		VisualizationViewerController.loadVisualizationViewController()
				.openVisualizationViewerFrame();
	}

	public Point getSize() {
		return null;
	}

}
