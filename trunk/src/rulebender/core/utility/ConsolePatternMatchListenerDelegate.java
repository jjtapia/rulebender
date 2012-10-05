package rulebender.core.utility;

import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

import rulebender.logging.Logger;
import rulebender.logging.Logger.LOG_LEVELS;

public class ConsolePatternMatchListenerDelegate implements
		IPatternMatchListenerDelegate {
	
	TextConsole console_;

	@Override
	public void connect(TextConsole console) 
	{
		console_ = console;
		Logger.log(LOG_LEVELS.INFO, this.getClass(), 
		    "The Console Pattern Matching Delegate is Connected!");
	}

	@Override
	public void disconnect()
	{
		console_ = null;

	}

	// Credit : http://kickjava.com/src/org/eclipse/jdt/internal/debug/ui/console/JavaConsoleTracker.java.htm#ixzz1xiKpC4sy
	@Override
	public void matchFound(final PatternMatchEvent event) 
	{
		final int offset = event.getOffset();
		final int length = event.getLength();
		
		Console.addHyperlink(console_.getName(), offset, length);
	}
}
