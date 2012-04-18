package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

public interface ActionInterface 
{

	/**
	 * Must return the name of the action to put into the list.
	 */
	public String getName();
	
	/**
	 * Must return the shot name of the action to put into the toolbar.
	 */
	public String getShortName();
	
	
	/**
	 * If this returns true, then
	 * @return
	 */
	public boolean hasComposite();
	
	/**
	 * If hasComposite returns true, this is called to get a composite for the action.
	 */
	public Composite getComposite(Composite parent);
	
	/**
	 * If hasComposite returns false, this is called to execute the action.
	 */
	public void executeAction();
	
	/**
	 * Returns a point object that denotes the size of the frame needed. 
	 * If hasComposite returns false, this can be null.
	 * 
	 * @return a point object to denote the size of the frame needed.
	 */
	public Point getSize();
}
