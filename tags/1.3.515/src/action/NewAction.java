package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import editor.BNGEditor;
import editor.NewFileDialogue;

public class NewAction implements ActionInterface
{

  public String getName()
  {
    return "New";
  }


  public String getShortName()
  {
    return "New";
  }


  public boolean hasComposite()
  {
    return false;
  }


  public Composite getComposite(Composite parent)
  {
    return null;
  }


  public void executeAction()
  {
    NewFileDialogue newfilediag = new NewFileDialogue(
        BNGEditor.getMainEditorShell(), BNGEditor.getEditor());
    newfilediag.show();
  }


  public Point getSize()
  {
    return null;
  }

}
