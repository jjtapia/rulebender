package rulebender.utility;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console 
{

	static MessageConsole messageConsole;
	
	public static void displayOutput(String output)
	{
		  
		/*
		 * Console Output
		 */
		messageConsole = getMessageConsole();
		
		MessageConsoleStream msgConsoleStream = messageConsole.newMessageStream();  
  
		ConsolePlugin.getDefault().getConsoleManager().addConsoles(  
				new IConsole[] { messageConsole });  
  
		msgConsoleStream.println(output);  
		
	}

	private static MessageConsole getMessageConsole() 
	{  
		if (messageConsole == null) 
		{  
			messageConsole = new MessageConsole("RuleBender", null);  
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(  
					new IConsole[] { messageConsole });  
		}  
	  
		return messageConsole;  
	}

	public static String getConsoleLineDelimeter() 
	{
		return "\n";
	}  
}
