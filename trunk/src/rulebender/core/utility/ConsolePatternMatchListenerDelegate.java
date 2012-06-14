package rulebender.core.utility;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.IPatternMatchListenerDelegate;
import org.eclipse.ui.console.MessageConsole;
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
		 try 
		 {
			 final int offset = event.getOffset();
			 final int length = event.getLength();
			 IHyperlink link = new IHyperlink(){

				@Override
				public void linkEntered() 
				{
				// TODO Auto-generated method stub	
				}

				@Override
				public void linkExited() 
				{
					// TODO Auto-generated method stub	
				}

				@Override
				public void linkActivated()
				{
					// TODO
					 // Get the line that matched, extract where the error is, 
					// jump to that line in the editor.
					String text = "";
					try {
						text = console_.getDocument().get(offset, length);
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Source: " + event.getSource());
					System.out.println("matched: " + event.toString());
					System.out.println("text: " + text);
					
					
				}};
			 console_.addHyperlink(link, offset, length); 
		 }
		 catch (BadLocationException e) 
		 {
		
		 }
	}
}
