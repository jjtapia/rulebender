package rulebender.core.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;

import rulebender.contactmap.view.ContactMapView;
import rulebender.core.utility.Console;
import rulebender.influencegraph.view.InfluenceGraphView;

public class Test extends AbstractHandler
{

	MessageConsole messageConsole;
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		  
		
		Console.displayOutput("testConsole","Rebuilding Contact Map");
		
		/*
		 * Update a view
		 */
		IViewReference[] views = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
		
		for(IViewReference view : views)
		{
			if(view.getView(true) instanceof ContactMapView)
			{
				((ContactMapView)view.getView(true)).tempRefresh();
			}
			
			else if(view.getView(true) instanceof InfluenceGraphView)
			{
				((InfluenceGraphView) view.getView(true)).tempRefresh();
			}
		}
	
		
		/*
		 * Status Line 
		 */
		/*
		IStatusLineManager statusline = getWindowConfigurer()
				.getActionBarConfigurer().getStatusLineManager();
		statusline.setMessage(null, "Status line is ready");
		
		IActionBars bars = getViewSite().getActionBars();
		   bars.getStatusLineManager().setMessage("Hello");
		
		   */
		return null;
		
	}
	 
	public boolean isEnabled() 
	{
		return true;
	}
}
