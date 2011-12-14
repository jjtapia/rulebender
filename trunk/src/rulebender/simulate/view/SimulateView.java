package rulebender.simulate.view;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;

import rulebender.core.utility.Console;
import rulebender.preferences.PreferencesClerk;
import rulebender.simulate.BioNetGenUtility;
import rulebender.simulate.parameterscan.ParameterScanComposite;

public class SimulateView extends ViewPart {

	private Composite main, actionSelect, stackedComposite, parameterScanComposite, runFileComposite;
	private StackLayout stackLayout;
	
	private SimulateViewSelectionListener listener;
	
	private String m_selectedFile;
	
	private Text fileText;
	
	public SimulateView() 
	{
		// Empty Constructor
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		/* 
		 * Need the all encompassing composite.
		 */
		
		main = new Composite(parent, SWT.NULL);
		main.setLayout(new FillLayout(SWT.VERTICAL));
		
		/*
		 * Create the composite that will hold the filename
		 * and action select widgets
		 */
		actionSelect = new Composite(main, SWT.NULL);
		actionSelect.setLayout(new FillLayout(SWT.VERTICAL));
		
		// Create the filename Text widget
		Label fileLabel = new Label(actionSelect, SWT.NULL);
		//fileLabel.setSize(actionSelect.getBorderWidth()-50, 10);
		fileLabel.setText("File:");
		
		fileText = new Text(actionSelect, SWT.BORDER);
		fileText.setEditable(true);
		//fileName.setSize(actionSelect.getBorderWidth()-50, 20);
		
		// Add a verifier that makes sure that the text forms a 
		// correct file path.
		fileText.addListener (SWT.Verify, new Listener () {
			public void handleEvent (Event e) 
			{
				String string = e.text;
				
				if(!string.endsWith(".bngl") || !(new File(string)).exists())
				{
					e.doit = false;
				}
				else
				{
					setSelectedFile(string);
				}
			}
		});
		
		// The model will be selected by either writing the path into the text box,
		// or selecting the node in the navigator. 
		
		// Need a way to select the method (either run model or parameter scan for now)
		final Combo combo = new Combo (actionSelect, SWT.READ_ONLY);
		combo.setItems (new String [] {"Run File", "Parameter Scan"});
		
		combo.select(0);
		
		//Rectangle clientArea = shell.getClientArea ();
		//combo.setBounds (clientArea.x, clientArea.y, 200, 200);
		
		combo.addSelectionListener(new SelectionListener() {
		      public void widgetSelected(SelectionEvent e) 
		      {
		        //System.out.println("Selected index: " + combo.getSelectionIndex() + ", selected item: " + combo.getItem(combo.getSelectionIndex()) + ", text content in the text field: " + combo.getText());
		        
				if (combo.getText().equals("Run File"))
				{
					stackLayout.topControl = runFileComposite;
					stackedComposite.layout();
				}
				else if (combo.getText().equals("Parameter Scan"))
				{
					stackLayout.topControl = parameterScanComposite;
					stackedComposite.layout();
				}
		      }

		      public void widgetDefaultSelected(SelectionEvent e) 
		      {
		        //System.out.println("Default selected index: " + combo.getSelectionIndex() + ", selected item: " + (combo.getSelectionIndex() == -1 ? "<null>" : combo.getItem(combo.getSelectionIndex())) + ", text content in the text field: " + combo.getText());
		      }
		    });
		
		/*
		 * The StackedLayout for the actual Actions
		 */
		stackedComposite = new Composite(main, SWT.NULL);
		stackLayout = new StackLayout();
		stackedComposite.setLayout(stackLayout);
		
		parameterScanComposite = new ParameterScanComposite(stackedComposite, this);
		
		runFileComposite = new Composite(stackedComposite, SWT.NULL);
		runFileComposite.setLayout(new FillLayout());
		Button runButton = new Button(runFileComposite, SWT.PUSH);
		
		runButton.setText("Run");
		
		runButton.addSelectionListener(new SelectionListener(){

			public void widgetSelected(SelectionEvent e) 
			{
				Console.displayOutput(Console.getConsoleLineDelimeter() + "Running File...");
				
				// Run the parameter scan.  This returns a boolean, but for now I am ignoring it.	
				BioNetGenUtility.runBNGLFile(getSelectedFile(), PreferencesClerk.getFullBNGPath());	
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}});
		
		
		stackLayout.topControl = runFileComposite;
		stackedComposite.layout();
		
		/*
		 *  Pack everything.
		 */
		actionSelect.pack();
		parameterScanComposite.pack();
		runFileComposite.pack();
		stackedComposite.pack();
		main.pack();
		
		listener = new SimulateViewSelectionListener(this);
	}

	@Override
	public void setFocus() 
	{
		// TODO Auto-generated method stub
	}
	
	public String getSelectedFile()
	{
		return m_selectedFile;
	}
	
	/**
	 * Sets the text box with the text of a potential bngl file to execute.
	 * The text validators of the text box call the setSelectedFile private setter
	 * after the file has been validated.
	 * @param file
	 */
	public void setSelectedFileText(String file)
	{
		System.out.println("Simulation View informed of bngl file text: " + file);
		fileText.setText(file);
	}
	
	private void setSelectedFile(String file)
	{
		System.out.println("bngl file set: " + file);
		m_selectedFile = file;
	}
}
