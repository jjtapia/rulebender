package rulebender.core.utility;

import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.PatternMatchEvent;
import org.eclipse.ui.console.TextConsole;

public class ConsolePatternMatchListenerDelegate implements
		IPatternMatchListenerDelegate {
	
	TextConsole console_;

	@Override
	public void connect(TextConsole console) 
	{
		console_ = console;
		System.out.println("The Console Pattern Matching Delegate is Connected!");
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
		System.out.println("A match has been found ********!");
		final int offset = event.getOffset();
		final int length = event.getLength();
			 
		System.out.println("Source: " + event.getSource());
		System.out.println("matched: " + event.toString());
		
		int placeholder = 10;
		
		Console.addHyperlink(console_.getName(), offset, length, placeholder);
	}
}
