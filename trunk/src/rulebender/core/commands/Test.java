package rulebender.core.commands;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import rulebender.contactmap.view.ContactMapView;
import rulebender.influencegraph.view.InfluenceGraphView;
import rulebender.utility.Console;

public class Test extends AbstractHandler
{

	MessageConsole messageConsole;
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		  
		
		Console.displayOutput("Rebuilding Contact Map");
		
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

	private MessageConsole getMessageConsole() 
	{  
		if (messageConsole == null) 
		{  
			messageConsole = new MessageConsole("RuleBender", null);  
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(  
					new IConsole[] { messageConsole });  
		}  
	  
		return messageConsole;  
	}  
	 
	public boolean isEnabled() 
	{
		return true;
	}
}
