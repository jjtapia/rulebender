package rulebender.ui.menu;

import org.eclipse.ui.IWorkbenchWindow;

public class ShowViewContributionItem extends org.eclipse.ui.internal.ShowViewMenu
{

  public ShowViewContributionItem()
  {
    this("rulebender.ui.menu.showViewMenu");
  }
  
  public ShowViewContributionItem(String id)
  {
    super(org.eclipse.ui.PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
          id);
  }

}
