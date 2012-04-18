package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import editor.BNGEditor;

public class SaveAction implements ActionInterface {

	public String getName() 
	{
		return "Save";
	}
	
	public String getShortName() 
	{
		return "Save";
	}

	public boolean hasComposite() 
	{
		return false;
	}

	public Composite getComposite(Composite parent) 
	{
		return null;
	}

	public void executeAction() 
	{

		if(BNGEditor.getTextFolder().getSelection() == null)
			return;

		(BNGEditor.getInputfiles().get(BNGEditor.getFileselection())).save();

	}

	public Point getSize() 
	{
		return null;
	}
}
