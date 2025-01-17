package rulebender.editors.dat;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.ide.IDE;

import rulebender.editors.dat.view.ChartComposite;

/**
 * An example showing how to create a multi-page editor. This example has 3
 * pages:
 * <ul>
 * <li>page 0 contains the chart for the results file.
 * <li>page 1 contains the text for the data.
 * </ul>
 */
public class DATMultiPageEditor extends MultiPageEditorPart implements
    IResourceChangeListener
{

  /** The text editor used in page 0. */
  private TextEditor editor;

  /**
   * Creates a multi-page editor example.
   */
  public DATMultiPageEditor()
  {
    super();
    ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
  }


  /**
   * Creates page 0 of the multi-page editor, which contains a text editor.
   */
  void createTextEditor()
  {
    try
    {
      editor = new TextEditor()
      {
        @Override
        public boolean isEditable()
        {
          return false;
        }


        @Override
        public boolean isEditorInputModifiable()
        {
          return false;
        }


        @Override
        public boolean isEditorInputReadOnly()
        {
          return true;
        }


        @Override
        public boolean isDirty()
        {
          return false;
        }
      };
      int index = addPage(editor, getEditorInput());
      setPageText(index, editor.getTitle());
      setPartName(editor.getTitle());
    }

    catch (PartInitException e)
    {
      ErrorDialog.openError(getSite().getShell(),
          "Error creating nested text editor", null, e.getStatus());
    }
  }


  /**
   * Creates page 1 of the multi-page editor, which allows you to change the
   * font used in page 2.
   */
  void createEditorGui()
  {

    // Composite composite = new Composite(getContainer(), SWT.NONE);

    ChartComposite chart = new ChartComposite(getContainer(), getEditorInput(),
        getSite());

    int index = addPage(chart);

    setPageText(index, "Chart");
  }


  /**
   * Creates the pages of the multi-page editor.
   */
  protected void createPages()
  {
    createEditorGui();
    createTextEditor();
  }


  /**
   * The <code>MultiPageEditorPart</code> implementation of this
   * <code>IWorkbenchPart</code> method disposes all nested editors. Subclasses
   * may extend.
   */
  public void dispose()
  {
    ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
    super.dispose();
  }


  /**
   * Saves the multi-page editor's document.
   */
  public void doSave(IProgressMonitor monitor)
  {
    getEditor(1).doSave(monitor);
  }


  /**
   * Saves the multi-page editor's document as another file. Also updates the
   * text for page 0's tab, and updates this multi-page editor's input to
   * correspond to the nested editor's.
   */
  public void doSaveAs()
  {
    IEditorPart editor = getEditor(0);
    editor.doSaveAs();
    setPageText(0, editor.getTitle());
    setInput(editor.getEditorInput());
  }


  /*
   * (non-Javadoc) Method declared on IEditorPart
   */
  public void gotoMarker(IMarker marker)
  {
    setActivePage(0);
    IDE.gotoMarker(getEditor(0), marker);
  }


  /**
   * The <code>MultiPageEditorExample</code> implementation of this method
   * checks that the input is an instance of <code>IFileEditorInput</code>.
   */
  public void init(IEditorSite site, IEditorInput editorInput)
      throws PartInitException
  {
    if (!(editorInput instanceof IFileEditorInput))
      throw new PartInitException("Invalid Input: Must be IFileEditorInput");
    super.init(site, editorInput);
  }


  /*
   * (non-Javadoc) Method declared on IEditorPart.
   */
  public boolean isSaveAsAllowed()
  {
    return true;
  }


  /**
   * Calculates the contents of page 2 when the it is activated.
   */
  protected void pageChange(int newPageIndex)
  {
    super.pageChange(newPageIndex);
    if (newPageIndex == 2)
    {

    }
  }


  /**
   * Closes all project files on project close.
   */
  public void resourceChanged(final IResourceChangeEvent event)
  {
    if (event.getType() == IResourceChangeEvent.PRE_CLOSE)
    {
      Display.getDefault().asyncExec(new Runnable()
      {
        public void run()
        {
          IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
          for (int i = 0; i < pages.length; i++)
          {
            if (((FileEditorInput) editor.getEditorInput()).getFile()
                .getProject().equals(event.getResource()))
            {
              IEditorPart editorPart = pages[i].findEditor(editor
                  .getEditorInput());
              pages[i].closeEditor(editorPart, true);
            }
          }
        }
      });
    }
  }
}
