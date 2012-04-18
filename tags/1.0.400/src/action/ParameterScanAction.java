package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import editor.parameterscan.ParameterScanController;
import editor.parameterscan.ParameterScanView;

/**
 * This class implements the ActionInterface and is instantiated in ActionListCreator.java.
 * 
 * The items in ActionListCreator.java are added to the gui in BNGEditor.java
 * 
 * @author mr_smith22586
 *
 */
public class ParameterScanAction implements ActionInterface 
{

	public String getName() 
	{	
		return "Parameter Scan";
	}
	
	public String getShortName() 
	{	
		return "ParaScan";
	}

	/*****
	 *  Setup the parscan
	 *****/
	public Composite getComposite(Composite parent) 
	{
		// This should be the first time the view is retrieved.
		Composite parScanForm = ParameterScanController.getParameterScanController().getView(parent);
		return parScanForm;
	}

	public boolean hasComposite() {
		return true;
	}

	public void executeAction() {}

	public Point getSize() 
	{
		return new Point(500, 325);
	}

}
