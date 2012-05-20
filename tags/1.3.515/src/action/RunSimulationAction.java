package action;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import editor.BNGEditor;

public class RunSimulationAction implements ActionInterface 
{

	public String getName() 
	{
		return "Run Simulation";
	}
	
	public String getShortName() 
	{
		return "Run";
	}

	/**
	 * This returns a composite for running the simulation.  It is not used now.  To use it,
	 * make hasComposite return true. 
	 */
	public Composite getComposite(Composite parent) 
	{
		Composite toReturn = new Composite(parent, SWT.None);
		toReturn.setLayout(new FillLayout());
		
		GridData gd = new GridData();
		
		gd.widthHint = 100;
		
		toReturn.setLayoutData(gd);
		
		Button runButton = new Button(toReturn, SWT.NONE);
		runButton.addSelectionListener(new SelectionListener()
		{

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent arg0) 
			{
				if(BNGEditor.getEditor().getTextFolder().getSelection() == null)
					return;
				BNGEditor.getInputfiles().get(BNGEditor.getFileselection()).run();
				
			}
			
		});
		runButton.setText("Run Simulation");
		
		return toReturn;
	}

	
	public boolean hasComposite() 
	{
		return false;
	}

	public void executeAction() 
	{
		if(BNGEditor.getTextFolder().getSelection() == null)
			return;
		
		BNGEditor.getInputfiles().get(BNGEditor.getFileselection()).run();
	}

	public Point getSize() 
	{
		return null;
	}

	
}
