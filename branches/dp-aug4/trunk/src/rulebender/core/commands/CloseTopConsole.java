package rulebender.core.commands;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

import rulebender.core.utility.Console;

/**
 * This is an incomplete class for closing a console. In Eclipse, 
 * consoles are attached to Launches (search for Launch Framework in Eclipse help),
 * but I have not implemented that yet so our consoles don't get the free close button.
 * @author adammatthewsmith
 *
 */
public class CloseTopConsole implements IHandler {

	public void addHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

	public void dispose() {
		// TODO Auto-generated method stub

	}

	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		Console.closeTopConsole();
		return null;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isHandled() {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
		// TODO Auto-generated method stub

	}

}
