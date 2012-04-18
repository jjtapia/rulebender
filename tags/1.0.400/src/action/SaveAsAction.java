package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import editor.BNGEditor;

public class SaveAsAction implements ActionInterface {

	public String getName() 
	{
		return "Save As";
	}
	
	public String getShortName() 
	{
		return "SaveAs";
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
		
		(BNGEditor.getInputfiles().get(BNGEditor.getFileselection())).saveas();
	}

	public Point getSize() 
	{
		return null;
	}

}
