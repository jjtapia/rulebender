package action;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ActionFrameFactory
{
  public static void constructAndShowActionFrame(ActionInterface action_in)
  {
    ActionInterface action = action_in;

    Shell shell = new Shell(Display.getDefault());
    shell.setSize(action.getSize());
    shell.setLayout(new FillLayout());

    Composite comp = action.getComposite(shell);
    if (comp == null)
    {
      return;
    }
    shell.layout();
    shell.open();
  }
}
