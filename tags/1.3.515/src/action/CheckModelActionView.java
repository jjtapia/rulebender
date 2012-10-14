package action;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;

import editor.BNGEditor;

public class CheckModelActionView implements ActionInterface
{

  public String getName()
  {
    return "Check Model";
  }


  public String getShortName()
  {
    return "Check";
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
    BNGEditor.getInputfiles().get(BNGEditor.getFileselection()).check();
  }


  public Point getSize()
  {
    return null;
  }

}
