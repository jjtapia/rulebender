package rulebender.core.utility;

import java.util.HashMap;

import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IHyperlink;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;

import de.ralfebert.rcp.tools.preferredperspective.PreferredPerspectivePartListener;

import rulebender.editors.bngl.BNGLEditor;

public class Console implements IPartListener, IStartup
{
  private static Console m_instance;

  private static HashMap<String, MessageConsole> m_messageConsoles = new HashMap<String, MessageConsole>();
  private static HashMap<String, BNGLEditor> m_editors = new HashMap<String, BNGLEditor>();

  /**
   * Private Constructor for static library. This should be private, but
   * IStartup implementers cannot be private. So... use this as a singleton.
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
  public static void addHyperlink(final String name, final int offset,
      final int length, final int lineNum)
  {
    IHyperlink link = new IHyperlink()
    {

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
        //TODO logging.
        System.out.println("Link activated for "
            + getMessageConsole(name).getName());

      
        String errorText = "";

        try
        {
          errorText = getMessageConsole(name).getDocument().get(offset, length);
        }
        catch (BadLocationException e)
        {
          e.printStackTrace();
        }

        //ABORT:\s+.*\s+at line \d+
        System.out.println("text: " + errorText);

        String[] textArray = errorText.split("\\s+");
        
        //TODO 
        // verify this.
        int line = Integer.parseInt(textArray[textArray.length - 1]); 
        
        // Open the editor.  This will open the file in an editor if it is not
        // opened already, and will bring it in focus once it is opened.
        openFile(name);

        // Get the editor.  Even if the editor was not in the HashMap before,
        // the openFile call will still cause all of the part listening code
        // for the Console (see below) to fire.
        BNGLEditor editor = m_editors.get(name);

        // Go to the line in the editor.
        goToLine(editor, line);
      }

      /**
       * 
       * @param name
       */
      private void openFile(String name)
      {
        
        // Get an IPath for the relative file location.
        IPath path = new Path(name);
        
        // Get an IFile given the IPath
        IFile fileToBeOpened = 
            ResourcesPlugin.getWorkspace().getRoot().getFile(path);

        // Get the editor input
        IEditorInput editorInput = new FileEditorInput(fileToBeOpened);
        
        // Get the acive window and page.
        IWorkbenchWindow window = PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow();
        IWorkbenchPage page = window.getActivePage();

        // Open the file.
        try
        {
          page.openEditor(editorInput, "rulebender.editors.bngl");
        }
        catch (PartInitException e)
        {
          e.printStackTrace();
        }
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
   * 
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
    if (messageConsole == null)
    {
      // Create it
      messageConsole = new MessageConsole(console, null);

      // Add it to the hashmap.
      m_messageConsoles.put(console, messageConsole);

      // Register it.
      ConsolePlugin.getDefault().getConsoleManager()
          .addConsoles(new IConsole[] { messageConsole });
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

    ConsolePlugin.getDefault().getConsoleManager()
        .removeConsoles(new IConsole[] { toRemove });

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
    if (part instanceof BNGLEditor)
    {
      String path = ((IFileEditorInput) ((BNGLEditor) part).getEditorInput())
          .getFile().getFullPath().toOSString();

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
    if (part instanceof BNGLEditor)
    {
      addEditor((BNGLEditor) part);
    }

  }

  private void addEditor(BNGLEditor editor)
  {
    String path = ((IFileEditorInput) editor.getEditorInput()).getFile()
        .getFullPath().toOSString();

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
        }
        catch (Exception e)
        {
          // log.error(e.getMessage(), e);
        }
      }

    });
  }

  /**
   * From
   * http://stackoverflow.com/questions/2873879/eclipe-pde-jump-to-line-x-and
   * -highlight-it
   * 
   * @param editorPart
   * @param lineNumber
   */
  private static void goToLine(IEditorPart editorPart, int lineNumber)
  {
    if (!(editorPart instanceof ITextEditor) || lineNumber <= 0)
    {
      System.out.println("Not TextEditor or line number <=0");
      return;
    }
    ITextEditor editor = (ITextEditor) editorPart;
    IDocument document = editor.getDocumentProvider().getDocument(
        editor.getEditorInput());
    if (document != null)
    {
      IRegion lineInfo = null;
      try
      {
        // line count internaly starts with 0, and not with 1 like in
        // GUI
        lineInfo = document.getLineInformation(lineNumber - 1);
      }
      catch (BadLocationException e)
      {
        e.printStackTrace();
        // ignored because line number may not really exist in document,
        // we guess this...
      }
      if (lineInfo != null)
      {
        editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
      }
    }
  }
}
