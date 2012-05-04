package rulebender.core.utility;

import java.util.HashMap;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class Console 
{
	private static HashMap<String, MessageConsole> m_messageConsoles = new HashMap<String, MessageConsole>();
	
	// Private Constructor for static library.
	private Console()
	{
		throw new AssertionError();
	}
	
	/**
	 * Display a String directly to a console.
	 * 
	 * @param console
	 * @param output
	 */
	public static void displayOutput(String console, String output)
	{
		// FIXME Completely overridding the different consoles
		console = "RuleBender Console";
		getMessageConsoleStream(console).println(output);
	}
	
	/**
	 * Get the MessageConsoleStream so that it can be printed to directly. 
	 * @param console
	 * @return
	 */
	public static MessageConsoleStream getMessageConsoleStream(String console)
	{
		// Get the stream
		return getMessageConsole(console).newMessageStream();    
	}
	
	private static MessageConsole getMessageConsole(String console)
	{
		// Try to get it based on the name.
		MessageConsole messageConsole = m_messageConsoles.get(console);
		
		// If it doesn't exist
		if(messageConsole == null)
		{
			// Create it
			messageConsole = new MessageConsole(console, null);  
			
			// Add it to the hashmap.
			m_messageConsoles.put(console, messageConsole);
		
			// Register it. 
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(  
					new IConsole[] { messageConsole });
		}
		
		return messageConsole; 
	}

	public static String getConsoleLineDelimeter() 
	{
		return "\n";
	}

	/**
	 * FIXME need to find out which one is on the top before this works.
	 */
	public static void closeTopConsole() 
	{
		MessageConsole toRemove = null;
		
		ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] {toRemove});
		
	}

	public static void clearConsole(String title) 
	{
		getMessageConsole(title).clearConsole();
		
	}  
}
