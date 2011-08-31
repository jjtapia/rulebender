package rulebender.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Test extends AbstractHandler
{

	MessageConsole messageConsole;
	
	public Object execute(ExecutionEvent event) throws ExecutionException
	{
		  
		/*
		 * Console Output
		 */
		messageConsole = getMessageConsole();  
		MessageConsoleStream msgConsoleStream = messageConsole.newMessageStream();  
  
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(  
				new IConsole[] { messageConsole });  
  
		msgConsoleStream.println("It works");  
  
		
		
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
