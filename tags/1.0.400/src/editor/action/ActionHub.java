/**
 * ActionHub.java
 * This file implements the ActionHub class as a singleton.  There will be  
 * 
 */
package editor.action;

import java.util.ArrayList;

public class ActionHub 
{
	private static ActionHub theActionHub;
	
	private ArrayList<? extends ActionListenerInterface> listeners;
	
	/**
	 * Private constructor
	 */
	private ActionHub()
	{
		listeners = new ArrayList<ActionListenerInterface>();
	}
	
	public static synchronized ActionHub getActionHub()
	{
		if(theActionHub == null)
			theActionHub = new ActionHub();
		
		return theActionHub;
	}
	
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
	
	public void eventHappened(Action source)
	{
		if(theActionHub == null)
			theActionHub = new ActionHub();
		
		for (ActionListenerInterface o : theActionHub.listeners)
		{
			o.actionReceived(source);
		}
	}
	
	public void log(String msg)
	{
		System.out.println(msg);
	}
	
}
