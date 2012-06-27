package rulebender.core.utility;

import java.util.HashMap;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import de.ralfebert.rcp.tools.preferredperspective.PreferredPerspectivePartListener;

import rulebender.editors.bngl.BNGLEditor;

public class Console implements IPartListener, IStartup
{
	private static Console m_instance;
	
	private static HashMap<String, MessageConsole> m_messageConsoles = new HashMap<String, MessageConsole>();
	private static HashMap<String, BNGLEditor> m_editors = new HashMap<String, BNGLEditor>();
	
	/** Private Constructor for static library.
	 *  This should be private, but IStartup implementers cannot be private.  So...
	 *  use this as a singleton.
	 */ 
	@Deprecated
	public Console()
	{
		m_messageConsoles = new HashMap<String, MessageConsole>();
		m_editors = new HashMap<String, BNGLEditor>();
	}
	
	public static synchronized Console getConsoleInstance()
	{
		if (m_instance == null)
		{
			m_instance = new Console();
		}
		
		return m_instance;
	}
	
	/**
	 * Display a String directly to a console.
	 * 
	 * @param console
	 * @param output
	 */
	public static void displayOutput(String console, String output)
	{	
		getMessageConsoleStream(console).println(output);	
	}
	
	
	/**
	 * 
	 * @param name
	 * @param offset
	 * @param length
	 * @param lineNum
	 */
	public static void addHyperlink(
			final String name, 
			final int offset,
			final int length,
			final int lineNum)
	{
		IHyperlink link = new IHyperlink(){
					
				@Override
				public void linkEntered() 
				{	
					// Do Nothing
				}

				@Override
				public void linkExited() 
				{
					// Do Nothing	
				}

				@Override
				public void linkActivated()
				{
					System.out.println("Link activated for " + getMessageConsole(name).getName());
					
					// TODO
					 // Get the line that matched, extract where the error is, 
					// jump to that line in the editor.
					String text = "";
					
					try 
					{
						text = getMessageConsole(name).getDocument().get(offset, length);
					}
					catch (BadLocationException e) 
					{
						e.printStackTrace();
					}
					
					System.out.println("text: " + text);
					
					// TODO this is a test line.
					int line = 10;
					
					// is the file already opened?
					if(!m_editors.containsKey(name))
					{
						// Open the file in the editor
						System.out.println("It is not there.");
					}
					
					// TODO Bring the file to front and select the line.
					BNGLEditor editor = m_editors.get(name);
					
					editor.selectAndReveal(offset, length);
						
				}
				
			};
			 
			try 
			{
				getMessageConsole(name).addHyperlink(link, offset, length);
			} 
			catch (BadLocationException e) 
			{	
				e.printStackTrace();
			} 

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

	@Override
	public void partActivated(IWorkbenchPart part) 
	{
		// Do Nothing.
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) 
	{
		// Do Nothing	
	}

	@Override
	public void partClosed(IWorkbenchPart part) 
	{
		// If it's an editor
		if(part instanceof BNGLEditor)
		{
			String path = ((IFileEditorInput) ((BNGLEditor) part).getEditorInput()).
					getFile().
					getFullPath().
					toOSString();
	
			m_editors.remove(path);
		}
				
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) 
	{
		// Do Nothing
	}

	@Override
	public void partOpened(IWorkbenchPart part) 
	{
		System.out.println("**** Console sees the part opened.");
		
		// If it's an editor
		if(part instanceof BNGLEditor)
		{
			addEditor((BNGLEditor) part);
		}
		
	}
	
	private void addEditor(BNGLEditor editor)
	{
		String path = ((IFileEditorInput) editor.getEditorInput()).
				getFile().
				getFullPath().
				toOSString();

		m_editors.put(path, editor);
		
		System.out.println("Added editor to hashmap: " + path); 

	}

	/**
	 * Register as a part listener. 
	 */
	@Override
	public void earlyStartup() 
	{
		Display.getDefault().asyncExec(new Runnable() 
		{
            public void run() 
            {
                try 
                {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .addPartListener(Console.getConsoleInstance());
                } catch (Exception e) {
                //    log.error(e.getMessage(), e);
                }
            }

        });
		
	}  
}
