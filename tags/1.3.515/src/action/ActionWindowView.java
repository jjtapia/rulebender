package action;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;

import editor.BNGEditor;

public class ActionWindowView
{
  // Singleton instance
  private static ActionWindowView windowView;

  private Shell window;

  private Hashtable<String, Composite> actionComposites;

  private Composite actionPaneComposite;
  private final StackLayout stackLayout = new StackLayout();

  List actionList;


  /**
   * Singleton
   */
  private ActionWindowView()
  {
    actionComposites = new Hashtable<String, Composite>();

    final Display display = Display.getDefault();
    window = new Shell(display);
    window.setLayout(new FillLayout());
    window.setSize(400, 600);
    window.setText("Action List");

    SashForm leftRightSashForm = new SashForm(window, SWT.HORIZONTAL
        | SWT.BORDER);
    leftRightSashForm.setLayout(new FillLayout());

    // The scrolled Composite that holds the list of actions.
    ScrolledComposite listComposite = new ScrolledComposite(leftRightSashForm,
        SWT.VERTICAL | SWT.BORDER);
    // listComposite.setExpandVertical(true);
    // Composite listComposite = new Composite(leftRightSashForm,
    // SWT.BORDER);
    // listComposite.setLayout(new FillLayout());

    // The list itself. I cannot figure out yet how to have it fill the
    // scrolled
    // composite that is its parent.
    actionList = new List(listComposite, SWT.NONE);
    actionList.setBounds(0, 0, 300, 500);
    // for(int i = 0; i < 100; i++)
    // actionList.add("Test " + i);

    actionList.addSelectionListener(new SelectionListener()
    {

      public void widgetDefaultSelected(SelectionEvent arg0)
      {
        // TODO Auto-generated method stub

      }


      public void widgetSelected(SelectionEvent se)
      {
        System.out.println();
        String[] selections = ((List) se.getSource()).getSelection();
        for (String s : selections)
          System.out.println(s);

        stackLayout.topControl = actionComposites.get(selections[0]);
        actionPaneComposite.layout();
      }

    });

    // The Pane for the actual action forms.
    actionPaneComposite = new Composite(leftRightSashForm, SWT.NONE);
    actionPaneComposite.setLayout(stackLayout);

    // Set the proportion for each section of the sashform.
    leftRightSashForm.setWeights(new int[] { 30, 70 });

    // Prepare the window.
    window.pack();
  }


  /**
   * Public access to constructor
   */
  public static ActionWindowView getWindow()
  {
    if (windowView == null)
    {
      windowView = new ActionWindowView();
    }

    return windowView;
  }


  public void setVisible(boolean b)
  {
    window.open();
  }


  public void addAction(ActionInterface ai)
  {
    actionComposites.put(ai.getName(), ai.getComposite(actionPaneComposite));
    actionList.add(ai.getName());
  }

}
