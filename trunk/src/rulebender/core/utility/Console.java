package rulebender.core.utility;

import java.util.HashMap;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
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

import rulebender.editors.bngl.BNGLEditor;
import rulebender.editors.dat.DATMultiPageEditor;
import rulebender.logging.Logger;
import rulebender.logging.Logger.LOG_LEVELS;

public class Console 
  implements IStartup, ISelectionListener, IPartListener2
{
  private static Console m_instance;

  private static HashMap<String, MessageConsole> m_messageConsoles = 
      new HashMap<String, MessageConsole>();
  private static HashMap<String, BNGLEditor> m_editors = 
      new HashMap<String, BNGLEditor>();


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
   * @throws Exception 
   */
  public static void displayOutput(String console, String output)
  {
    System.out.println("Get called for " + console + "\n\toutput: " + output);
    
    getMessageConsoleStream(console).println(output);
  }


  /**
   * Adds a hyperlink to a line in an editor.  
   * 
   * @param name
   * @param offset
   * @param length
   * @param lineNum
   */
  public static void addHyperlink(final String name, final int offset,
      final int length)
  {
    IHyperlink link = new IHyperlink()
    {
      @Override
      public void linkActivated()
      {
        Logger.log(LOG_LEVELS.INFO, this.getClass(), "Link activated for "
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


        
        // ABORT:\s+.*\s+at line \d+
        Logger.log(LOG_LEVELS.INFO, this.getClass(), "text: " + errorText);

        String[] textArray = errorText.split("\\s+");

        // TODO
        // verify this.
        int line = Integer.parseInt(textArray[textArray.length - 1]);

        // Open the editor. This will open the file in an editor if it is not
        // opened already, and will bring it in focus once it is opened.
        openFile(name);

        // Get the editor. Even if the editor was not in the HashMap before,
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
        IFile fileToBeOpened = ResourcesPlugin.getWorkspace().getRoot()
            .getFile(path);

        // Get the editor input
        IEditorInput editorInput = new FileEditorInput(fileToBeOpened);

        // Get the active window and page.
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
    System.out.println("Getting the stream");
    
   
    return getMessageConsole(console).newMessageStream();
  }


  private static MessageConsole getMessageConsole(String console)
  {
    
    System.out.println("Getting: " + console);

    if (console.startsWith("/"))
    {
      Thread.dumpStack();
    }
    
    
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
  
  public static void showConsole(String title)
  {
    Logger.log(LOG_LEVELS.WARNING, Console.class, "Showing: " + title);
    getMessageConsole(title).activate();
  }


  private void addEditor(BNGLEditor editor)
  {
    String path = ((IFileEditorInput) editor.getEditorInput()).getFile()
        .getFullPath().toOSString();

    m_editors.put(path, editor);

    Logger.log(LOG_LEVELS.INFO, this.getClass(), 
        "Added editor to hashmap: " + path);
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

          // Register the view as a listener for workbench selections.
             PlatformUI
             .getWorkbench()
             .getActiveWorkbenchWindow()
             .getActivePage()
             .addPostSelectionListener(Console.this);

             // Register the view as a part listener.
             PlatformUI
             .getWorkbench()
             .getActiveWorkbenchWindow()
             .getActivePage()
             .addPartListener(Console.this);
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
      Logger.log(LOG_LEVELS.ERROR, Console.class, 
          "Requested to go to line number <= 0, or editor part is not a " +
          "TextEditor");
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


  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
    // If it's from the BNGLEditor then we want to set the contact map.
    if (part instanceof BNGLEditor)
    {
      focusEditor(part, selection);
    }
    else if (part instanceof DATMultiPageEditor)
    {
      focusResults(part, selection); 
    }
    // If it's not a bngl file
    else
    {
      Logger.log(LOG_LEVELS.WARNING, Console.class, "Selection changed, but I don't care.");
    }    
  }
  
  private void focusResults(IWorkbenchPart part, ISelection selection)
  {
    // TODO strip out the bngl file name and keep displaying that console.
    
    // Get the string that represents the current file.
    String osString = ((FileEditorInput) ((BNGLEditor) part).getEditorInput())
        .getFile().getFullPath().toOSString();

    showConsole(osString);
  }
  
  /**
   * 
   * @param part
   * @param selection
   */
  private void focusEditor(IWorkbenchPart part, ISelection selection)
  {
    // Get the string that represents the current file.
    String osString = ((FileEditorInput) ((BNGLEditor) part).getEditorInput())
        .getFile().getFullPath().makeRelative().toOSString();

    showConsole(osString);
  }
  

  @Override
  public void partActivated(IWorkbenchPartReference partRef)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void partBroughtToTop(IWorkbenchPartReference partRef)
  {
    // TODO Auto-generated method stub
  }

  @Override
  public void partClosed(IWorkbenchPartReference partRef)
  {
    IWorkbenchPart part = partRef.getPart(false);
    
    // If it's an editor
    if (part instanceof BNGLEditor)
    {
      String path = ((IFileEditorInput) ((BNGLEditor) part).getEditorInput())
          .getFile().getFullPath().toOSString();

      m_editors.remove(path);
    }    
  }


  @Override
  public void partDeactivated(IWorkbenchPartReference partRef)
  {
    // TODO Auto-generated method stub
  }


  @Override
  public void partOpened(IWorkbenchPartReference partRef)
  {
    IWorkbenchPart part = partRef.getPart(false);
    
    // If it's an editor
    if (part instanceof BNGLEditor)
    {
      addEditor((BNGLEditor) part);
    }    
  }


  @Override
  public void partHidden(IWorkbenchPartReference partRef)
  {
    // TODO Auto-generated method stub
  }


  @Override
  public void partVisible(IWorkbenchPartReference partRef)
  {
    // TODO Auto-generated method stub
  }


  @Override
  public void partInputChanged(IWorkbenchPartReference partRef)
  {
    // TODO Auto-generated method stub
  }
}