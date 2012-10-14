package editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import visualizationviewer.VisualizationViewerController;

/**
 * This class implements a shell window that allows the user to open a file.
 * 
 * @author ams292
 * 
 */
public class NewFileDialogue
{

  //
  Shell parentShell, newFileDialogue;
  Text newFileText;
  BNGEditor editor;
  Button emptyFile;

  private static VisualizationViewerController visViewController;


  /**
   * Constructor. Sets up the shell but does not show it.
   * 
   * @param mainEditorShell
   *          The shell that will hold the created shell object
   * @param editorIn
   *          The BNGEditor object for which this shell will open files
   */
  public NewFileDialogue(Shell mainEditorShell, BNGEditor editorIn)
  {

    visViewController = VisualizationViewerController
        .loadVisualizationViewController();

    // Set Locals
    parentShell = mainEditorShell;
    editor = editorIn;

    // Create the shell
    newFileDialogue = new Shell(parentShell, SWT.DIALOG_TRIM
        | SWT.APPLICATION_MODAL);

    // Set the text in the title bar
    newFileDialogue.setText("New...");

    // set grid layout
    newFileDialogue.setLayout(new GridLayout(3, true));

    // Grid info for the layout.
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
    gridData.horizontalSpan = 2;

    // file name
    Label label1 = new Label(newFileDialogue, SWT.NONE);
    label1.setText("File name:");

    // Set the text inside of the new file textbox.
    newFileText = new Text(newFileDialogue, SWT.BORDER);
    newFileText.setLayoutData(gridData);
    newFileText.setText("untitled" + editor.getEmptyFileCount() + ".bngl");

    // label for the type of file.
    Label label2 = new Label(newFileDialogue, SWT.NONE);
    label2.setText("Type:");

    // Create button for the empty file choice
    emptyFile = new Button(newFileDialogue, SWT.RADIO);
    emptyFile.setSelection(true);
    emptyFile.setText("Empty File");

    // empty label to control layout
    new Label(newFileDialogue, SWT.NONE).setText("");
    new Label(newFileDialogue, SWT.NONE).setText("");

    // Create a button for the template file choice
    Button templateFileButton = new Button(newFileDialogue, SWT.RADIO);
    templateFileButton.setText("Template File");

    // empty label to control layout
    new Label(newFileDialogue, SWT.NONE).setText("");
    new Label(newFileDialogue, SWT.NONE).setText("");

    // cancel button
    Button cancelbutton = new Button(newFileDialogue, SWT.NONE);
    cancelbutton.setText("Cancel");
    gridData = new GridData();
    gridData.widthHint = 100;
    cancelbutton.setLayoutData(gridData);

    // add listener for cancel button
    cancelbutton.addMouseListener(new MouseListener()
    {
      public void mouseDoubleClick(MouseEvent arg0)
      {
      }


      public void mouseDown(MouseEvent arg0)
      {
      }


      public void mouseUp(MouseEvent arg0)
      {
        newFileDialogue.dispose();
      }
    });

    // ok button
    Button okbutton = new Button(newFileDialogue, SWT.NONE);
    okbutton.setText("OK");
    okbutton.setLayoutData(gridData);

    // add listener for ok button
    okbutton.addMouseListener(new MouseListener()
    {
      public void mouseDoubleClick(MouseEvent arg0)
      {
      }


      public void mouseDown(MouseEvent arg0)
      {
      }


      public void mouseUp(MouseEvent arg0)
      {
        String temp = null;
        CurrentFile tempfile;
        if (newFileText.getText().trim().equals(""))
        {
          MessageBox mb = new MessageBox(parentShell, SWT.ICON_INFORMATION);
          mb.setMessage("File name can not be empty !");
          mb.setText("Error info");
          mb.open();
          return;
        }
        if (emptyFile.getSelection())
        {
          tempfile = new CurrentFile(temp, newFileText.getText(),
              ConfigurationManager.getConfigurationManager().getOSType(), true,
              -1);
          editor.incrementEmptyFileCount();
        }
        else
        {
          tempfile = new CurrentFile(temp, newFileText.getText(),
              ConfigurationManager.getConfigurationManager().getOSType(),
              false, -1);
          editor.incrementEmptyFileCount();
        }

        // pass the resViewer to current file
        tempfile.setResViewer(editor.getResViewer());
        visViewController.fileBecomesFocus(tempfile);

        editor.addToInputFiles(tempfile);

        editor.getTextFolder().setSelection(editor.getInputFiles().size() - 1);
        editor.setFileSelection(editor.getInputFiles().size() - 1);

        newFileDialogue.dispose();
      }
    });
  }


  public void show()
  {
    newFileDialogue.pack();
    newFileDialogue.open();
  }
}
