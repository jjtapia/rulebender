package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import editor.BNGEditor;

public class GenerateModelGuideAction implements ActionInterface{

	public String getName() {
		return "Generate Model Guide";
	}
	
	public String getShortName() {
		return "Modelguide";
	}

	public boolean hasComposite() {
		return false;
	}

	public Composite getComposite(Composite parent) {
		return null;
	}

	public void executeAction() {
		BNGEditor.getInputfiles().get(BNGEditor.getFileselection()).generateModelGuide();
	}

	public Point getSize() {
		return null;
	}

}
