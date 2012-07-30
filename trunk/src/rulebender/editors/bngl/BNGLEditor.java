package rulebender.editors.bngl;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import bngparser.BNGParseData;
import bngparser.BNGParserUtility;
import bngparser.grammars.BNGGrammar.prog_return;
import rulebender.core.utility.ANTLRFilteredPrintStream;
import rulebender.core.utility.Console;
import rulebender.editors.bngl.BNGLConfiguration;
import rulebender.editors.bngl.BNGLDocumentProvider;
import rulebender.editors.bngl.model.BNGLModel;
import rulebender.errorview.model.BNGLError;
import rulebender.logging.Logger;

/**
 * This class defines the editor for bngl.
 * 
 * The ISelectionListener implementation listens for selections in the tool, and
 * IPrefersPerspective is for loading a perspective when an editor is loaded
 * (this is not used at the moment). 
 * 
 * @author adammatthewsmith
 * 
 */
public class BNGLEditor extends TextEditor implements ISelectionListener,
    IResourceChangeListener
{
  // The model for the text
  private BNGLModel m_model;
  // The color manager for the syntax highlighting.
  private BNGLColorManager m_colorManager;

  // private String m_path;
  public BNGLEditor()
  {
    // Call the TextEditor constructor
    super();

    // Create the colormanager.
    m_colorManager = new BNGLColorManager();

    // Set the SourceViewerConfiguration which takes care of many different
    // types of configs and decoration.
    setSourceViewerConfiguration(new BNGLConfiguration(m_colorManager));

    // Set the DocumentProvider which handles the representation of the file
    // and how the text is partitioned.
    setDocumentProvider(new BNGLDocumentProvider());

    // Can't create the m_model here because we need the part name,
    // and the part name is not set until after the constructor.
    // So i used a lazy load in the getter.

    // Register with the ISelectionService
    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
        .addPostSelectionListener(this);

    // Register as a resource change listener.
    ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
  }

  /*
   * @Override public void initializeEditor() { super.initializeEditor(); }
   */

  @Override
  public void editorSaved()
  {
    clearMarkers("rulebender.markers.bnglerrormarker");

    setAST(getAST());
  }

  private void clearMarkers(String markerId)
  {
    // Get the ifile reference for this editor input.
    IFile file = ((FileEditorInput) ((IEditorInput) getEditorInput()))
        .getFile();

    try
    {
      file.deleteMarkers(markerId, true, IResource.DEPTH_INFINITE);
    }
    catch (CoreException e)
    {
      e.printStackTrace();
    }
  }

  private BNGParseData produceParseData()
  {
    // Get the text in the document.
    String text = this.getSourceViewer().getDocument().get();

    return BNGParserUtility.produceParserInfoForBNGLText(text);
  }

  private void setAST(prog_return ast)
  {
    if (m_model == null)
    {
      m_model = new BNGLModel(
          ((FileEditorInput) ((IEditorInput) getEditorInput())).getPath()
              .toOSString());
      m_model.setAST(ast);
    }
    else
    {
      m_model.setAST(ast);
    }
  }

  /**
   * Getter for the ast data structure. The
   * 
   * @return
   */
  public BNGLModel getModel()
  {
    if (m_model == null)
    {
      m_model = new BNGLModel(
          ((FileEditorInput) ((IEditorInput) getEditorInput())).getPath()
              .toOSString());
      m_model.setAST(getAST());
    }

    return m_model;
  }

  /**
   * Returns the AST for the text in the editor, or null if there are errors.
   * Also, the errors are reported to the console.
   * 
   * @return prog_return or NULL
   * 
   */
  private prog_return getAST()
  {
    // The abstract syntax tree that will be returned.
    // On a failure, it will be null.
    prog_return toReturn = null;

    // Save a link to the orinal error out.
    PrintStream old = System.err;

    Console.clearConsole(((FileEditorInput) ((IEditorInput) getEditorInput()))
        .getPath().toOSString());

    // Set the error out to a new printstream that will only display the antlr
    // output.
    ANTLRFilteredPrintStream errorStream = new ANTLRFilteredPrintStream(
        Console.getMessageConsoleStream(((FileEditorInput) ((IEditorInput) 
            getEditorInput())).getPath().toOSString()),
        ((FileEditorInput) ((IEditorInput) getEditorInput())).getPath()
            .toOSString(), old,
        ((FileEditorInput) ((IEditorInput) getEditorInput())).getPath()
            .toOSString());
    System.setErr(errorStream);

    try
    {
      toReturn = produceParseData().getParser().prog();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      System.out.println("Caught in the getAST Method.");
    }

    setErrors(errorStream.getErrorList());

    System.err.flush();
    System.setErr(old);

    return toReturn;
  }

  private void setErrors(ArrayList<BNGLError> errorList)
  {
    // Add the error list to the model
    m_model.setErrors(errorList);

    // Get the document.
    IDocument document = getDocumentProvider().getDocument(getEditorInput());

    // Get the ifile reference for this editor input.
    IFile file = ((FileEditorInput) ((IEditorInput) getEditorInput()))
        .getFile();

    // Create a reference to a region that will be used to hold information
    // about the error location.
    IRegion region = null;

    // Set the annotations.
    for (BNGLError error : errorList)
    {
      // Get the information about the location.
      try
      {
        region = document.getLineInformation(error.getLineNumber() - 1);
      }
      catch (BadLocationException exception)
      {
        exception.printStackTrace();
      }

      // make a marker
      IMarker marker = null;
      try
      {
        marker = file.createMarker("rulebender.markers.bnglerrormarker");
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        marker.setAttribute(IMarker.MESSAGE, error.getMessage());
        marker.setAttribute(IMarker.LINE_NUMBER, error.getLineNumber() - 1);
        marker.setAttribute(IMarker.CHAR_START, region.getOffset());
        marker.setAttribute(IMarker.CHAR_END,
            region.getOffset() + region.getLength());

        System.out.println("Made the marker!");
      }
      catch (Exception exception)
      {
        exception.printStackTrace();
      }
    }
  }

  public void dispose()
  {
    clearMarkers("rulebender.markers.bnglerrormarker");
    clearMarkers("rulebender.markers.textinstance");

    super.dispose();
  }

  @Override
  public boolean isEditable()
  {
    return true;
  }

  @Override
  public boolean isEditorInputModifiable()
  {
    return true;
  }

  @Override
  public boolean isEditorInputReadOnly()
  {
    return false;
  }

  @Override
  public void selectionChanged(IWorkbenchPart part, ISelection selection)
  {
    Logger.log(Logger.LOG_LEVELS.INFO, 
        this.getClass(),
        "Part: " + part.getTitle());
    Logger.log(Logger.LOG_LEVELS.INFO, 
        this.getClass(),
        "selection: " + selection.toString());
    Logger.log(Logger.LOG_LEVELS.INFO,
        this.getClass(),
        "empty selection? " + selection.isEmpty());
    Logger.log(Logger.LOG_LEVELS.INFO,
        this.getClass(),
        "structured selection? "
        + (selection instanceof IStructuredSelection));
    Logger.log(Logger.LOG_LEVELS.INFO, 
        this.getClass(),
        "text selection? "
        + (selection instanceof ITextSelection));

    // If it is an IStructuredSelection
    if (selection instanceof IStructuredSelection)
    {
      if (!selection.isEmpty())
      {
        // Get the object that was selected
        IStructuredSelection iSSelection = (IStructuredSelection) selection;
        Object item = iSSelection.getFirstElement();

        // If it's the object implements IBNLLinkedElement, ie if it
        // has methods to get the path of the source file and a regular
        // expression for text search.
        if (item instanceof IBNGLLinkedElement)
        {
          // Get the current path that is listening.
          String thisPath = ((FileEditorInput) ((IEditorInput) getEditorInput()))
              .getPath().toOSString();

          // If it is for this file.
          if (((IBNGLLinkedElement) item).getLinkedBNGLPath().equals(thisPath))
          {
            searchableTextObjectSelected((IBNGLLinkedElement) item);
          }
        }
        else if (item instanceof IBNGLLinkedElementCollection)
        {
          // Get the current path that is listening.
          String thisPath = ((FileEditorInput) ((IEditorInput) getEditorInput()))
              .getPath().toOSString();

          // If it is for this file.
          if (((IBNGLLinkedElementCollection) item).getLinkedBNGLPath().equals(
              thisPath))
          {
            searchableTextObjectCollectionSelected((IBNGLLinkedElementCollection) item);
          }
        }
        else
        {
          clearMarkers("rulebender.markers.textinstance");
        }
      }

      else
      {
        clearMarkers("rulebender.markers.textinstance");
      }

    }
    else if (selection instanceof ITextSelection)
    {
      //System.out.println(((ITextSelection) selection).toString());
    }
    else
    {
      clearMarkers("rulebender.markers.textinstance");
    }
  }

  private void searchableTextObjectCollectionSelected(
      IBNGLLinkedElementCollection collection)
  {
    clearMarkers("rulebender.markers.textinstance");

    for (IBNGLLinkedElement ele : collection.getCollection())
    {
      selectFromRegExp(ele.getRegex());
    }
  }

  private void searchableTextObjectSelected(IBNGLLinkedElement source)
  {
    clearMarkers("rulebender.markers.textinstance");
    selectFromRegExp(source.getRegex());
  }

  private void selectFromRegExp(String regExp)
  {
    Logger.log(Logger.LOG_LEVELS.INFO, 
        this.getClass(),
        "Search for regex: " + regExp);

    // Get the ifile reference for this editor input.
    IFile file = ((FileEditorInput) ((IEditorInput) getEditorInput()))
        .getFile();

    ITextEditor editor = (ITextEditor) this.getAdapter(ITextEditor.class);

    IDocumentProvider provider = editor.getDocumentProvider();
    IDocument document = provider.getDocument(this.getEditorInput());

    Pattern p = Pattern.compile(regExp);
    Matcher m = p.matcher(document.get());

    IMarker marker = null;

    while (m.find())
    {
      try
      {
        marker = file.createMarker("rulebender.markers.textinstance");
        marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
        marker.setAttribute(IMarker.CHAR_START, m.start());
        marker.setAttribute(IMarker.CHAR_END, m.end());
      }
      catch (Exception exception)
      {
        exception.printStackTrace();
      }
    }
  }

  public void setSelection(final int lineNumber)
  {
    doSetSelection(new ITextSelection()
    {

      @Override
      public boolean isEmpty()
      {
        // TODO Auto-generated method stub
        return false;
      }

      @Override
      public int getOffset()
      {
        // TODO Auto-generated method stub
        return 0;
      }

      @Override
      public int getLength()
      {
        // TODO Auto-generated method stub
        return 0;
      }

      @Override
      public int getStartLine()
      {
        return lineNumber;
      }

      @Override
      public int getEndLine()
      {
        return lineNumber;
      }

      @Override
      public String getText()
      {
        // TODO Auto-generated method stub
        return null;
      }
    });
  }

  // TODO this is not being called.
  /**
   * Closes all project files on project close.
   */
  public void resourceChanged(final IResourceChangeEvent event)
  {
    Logger.log(Logger.LOG_LEVELS.INFO, 
        this.getClass(),
        "Resource Changed Event: " + event.getType());

    if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
    {
      Display.getDefault().asyncExec(new Runnable()
      {
        public void run()
        {
          IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
          for (int i = 0; i < pages.length; i++)
          {
            if (((FileEditorInput) getEditorInput()).getFile().getProject()
                .equals(event.getResource()))
            {
              IEditorPart editorPart = pages[i].findEditor(getEditorInput());
              pages[i].closeEditor(editorPart, true);
            }
          }
        }
      });
    }
  }
}
