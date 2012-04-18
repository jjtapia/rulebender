package resultviewer.ui;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;

public class ExitAction extends Action
{
  ApplicationWindow window;

  public ExitAction(ApplicationWindow w)
  {
    window = w;
    setText("E&xit");
  }

  public void run()
  {
    window.close();
  }
}

