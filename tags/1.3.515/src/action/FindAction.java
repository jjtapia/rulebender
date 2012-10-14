package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import editor.BNGEditor;

public class FindAction implements ActionInterface {

	public String getName() {
		return "Find";
	}

	public String getShortName() {
		return "Find";
	}

	public boolean hasComposite() {
		// TODO Auto-generated method stub
		return false;
	}

	public Composite getComposite(Composite parent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void executeAction() {
		if (BNGEditor.getTextFolder().getSelection() == null)
			return;

		BNGEditor.getInputfiles().get(BNGEditor.getFileselection())
				.getBNGTextArea().replace();
	}

	public Point getSize() {
		return null;
	}

}
