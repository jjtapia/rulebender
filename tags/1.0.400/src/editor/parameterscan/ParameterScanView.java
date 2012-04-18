package editor.parameterscan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import editor.BNGEditor;

public class ParameterScanView extends Composite
{
	private ParameterScanController controller;
	
	// A static Composite object that is the form itself. 
	//private static Composite parScanForm;
	
	// These are the gui objects that receive the values given by the user.
	final Text paramNameInput;
	final Text paramMinValueInput;
	final Text paramMaxValueInput;
	final Text pointsToScanInput;
	final Button logScaleInput;
	final Button steadyStateInput;
	final Text simTimeInput;
	final Text timePointsInput;
	
	/**
	 * 
	 * @param parent
	 */
	public ParameterScanView(ParameterScanController controller_in, final Composite parent)
	{
		// Call the superclass
		super(parent, SWT.NONE);
	
		controller = controller_in;
		
		// parameter scan
		ParameterScanView parScanForm = this;
		
		// grid layout
		parScanForm.setLayout(new GridLayout(5, true));

		GridData labelGridData = new GridData(GridData.FILL_HORIZONTAL);
		labelGridData.horizontalSpan = 4;

		GridData textGridData = new GridData(GridData.FILL_HORIZONTAL);

		// parameter name
		Label paramNameLabel = new Label(parScanForm, SWT.NONE);
		paramNameLabel.setText("  Parameter Name (Alphanumeric Word)");
		paramNameLabel.setLayoutData(labelGridData);

		paramNameInput = new Text(parScanForm, SWT.BORDER);
		paramNameInput.setLayoutData(textGridData);

		// min value
		Label paramMinValueLabel = new Label(parScanForm, SWT.NONE);
		paramMinValueLabel.setText("  Parameter Min Value (Real Number)");
		paramMinValueLabel.setLayoutData(labelGridData);

		paramMinValueInput = new Text(parScanForm, SWT.BORDER);
		paramMinValueInput.setLayoutData(textGridData);

		// max value
		Label paramMaxValueLabel = new Label(parScanForm, SWT.NONE);
		paramMaxValueLabel.setText("  Parameter Max Value (Real Number > Min Value)");
		paramMaxValueLabel.setLayoutData(labelGridData);

		paramMaxValueInput = new Text(parScanForm, SWT.BORDER);
		paramMaxValueInput.setLayoutData(textGridData);

		// number of points
		Label pointsToScanLabel = new Label(parScanForm, SWT.NONE);
		pointsToScanLabel.setText("  Number of Points to Scan (Positive Integer)");
		pointsToScanLabel.setLayoutData(labelGridData);

		pointsToScanInput = new Text(parScanForm, SWT.BORDER);
		pointsToScanInput.setLayoutData(textGridData);

		// log scale
		Label logScaleLabel = new Label(parScanForm, SWT.NONE);
		logScaleLabel.setText("  Log Scale ? (Error if checked & Min Value <= 0)");
		logScaleLabel.setLayoutData(labelGridData);

		logScaleInput = new Button(parScanForm, SWT.CHECK);

		// steady state
		Label steadyStateLabel = new Label(parScanForm, SWT.NONE);
		steadyStateLabel.setText("  Steady State ?");
		steadyStateLabel.setLayoutData(labelGridData);

		steadyStateInput = new Button(parScanForm, SWT.CHECK);

		// simulation time
		Label simTimeLabel = new Label(parScanForm, SWT.NONE);
		simTimeLabel.setText("  Simulation Time (Positive Real Number)");
		simTimeLabel.setLayoutData(labelGridData);

		simTimeInput = new Text(parScanForm, SWT.BORDER);
		simTimeInput.setLayoutData(textGridData);

		// number of time points
		Label timePointsLabel = new Label(parScanForm, SWT.NONE);
		timePointsLabel.setText("  Number of Time Points (Positive Integer)");
		timePointsLabel.setLayoutData(labelGridData);
		timePointsInput = new Text(parScanForm, SWT.BORDER);

		timePointsInput.setLayoutData(textGridData);


		// empty label to contro layout
		new Label(parScanForm, SWT.NONE).setText("");
		new Label(parScanForm, SWT.NONE).setText("");
		new Label(parScanForm, SWT.NONE).setText("");
		
		// cancel button
		Button cancelbutton = new Button(parScanForm, SWT.NONE);
		cancelbutton.setText("Cancel");
		GridData gridData = new GridData();
		gridData.widthHint = 80;
		cancelbutton.setLayoutData(gridData);
		
		// add listener for cancel button
		cancelbutton.addMouseListener(new MouseListener()
		{
			public void mouseDoubleClick(MouseEvent arg0) {}
			public void mouseDown(MouseEvent arg0) {}
			public void mouseUp(MouseEvent arg0) {
				controller.disposeOfWindow();
			}});

		// OK button
		Button okButton = new Button(parScanForm, SWT.NONE);
		okButton.setText("OK");
		
		okButton.setLayoutData(gridData);

		// pack
		parScanForm.pack();

		// add listener for OK button
		okButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) 
			{
				// Create the data object that will hold the user values.
				final ParameterScanData scanData = new ParameterScanData();
				
				boolean verified = true;
				try {
					if (paramNameInput.getText().trim().length() == 0)
						verified = false;
					if (Float.parseFloat(paramMaxValueInput.getText().trim()) <= Float
							.parseFloat(paramMinValueInput.getText().trim()))
						verified = false;
					if (Integer.parseInt(pointsToScanInput.getText().trim()) <= 0)
						verified = false;
					if (Float.parseFloat(paramMinValueInput.getText().trim()) <= 0
							&& logScaleInput.getSelection())
						verified = false;
					if (Float.parseFloat(simTimeInput.getText().trim()) <= 0)
						verified = false;
					if (Integer.parseInt(timePointsInput.getText().trim()) <= 0)
						verified = false;
				} catch (NumberFormatException e) {
					verified = false;
				}

				if (!verified) {
					MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(),
							SWT.ICON_INFORMATION);
					mb.setText("Error Info");
					mb.setMessage("There exists invalid arguments, please check again !");
					mb.open();
					return;
				} 
				else 
				{	
					scanData.setName(paramNameInput.getText());
					scanData.setMinValue(Float.parseFloat(paramMinValueInput.getText()));
					scanData.setMaxValue(Float.parseFloat(paramMaxValueInput.getText()));
					scanData.setPointsToScan(Integer.parseInt(pointsToScanInput.getText()));
					scanData.setSimulationTime(Float.parseFloat(simTimeInput.getText()));
					scanData.setNumTimePoints(Integer.parseInt(timePointsInput.getText()));
					scanData.setLogScale(logScaleInput.getSelection());
					scanData.setSteadyState(steadyStateInput.getSelection());
				}
	
				/*
				 * String[] psresult = new String[1];
				 * psresult[0]=prefix+".scan"; if(!stemp2.contains("open") &&
				 * !stemp2.contains("ABORT") && !stemp2.contains("line")) { try
				 * {
				 * 
				 * new Viewer(null,psresult,new String[0]); } catch (IOException
				 * e) {} }
				 */
			
			// save the data
			controller.saveData(scanData);
			
			controller.disposeOfWindow();
			
			// Make the console visible.
			BNGEditor.getEditor().focusConsole();
			BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter() + "Running Parameter Scan...");
			
			BNGEditor.getMainEditorShell().forceFocus();
			
			// send the data to the parscan method.
			controller.runParameterScan(scanData);
			}
		});
	}
	
	public void setFormText(ParameterScanData data)
	{
		if(data != null)
		{
			paramNameInput.setText(data.getName());
			paramMinValueInput.setText(data.getMinValue()+"");
			paramMaxValueInput.setText(data.getMaxValue()+"");
			pointsToScanInput.setText(data.getPointsToScan()+"");
			simTimeInput.setText(data.getSimulationTime()+"");
			timePointsInput.setText(data.getNumTimePoints()+"");
			if (data.isLogScale())
				logScaleInput.setSelection(true);
			if (data.isSteadyState())
				steadyStateInput.setSelection(true);
		}
		
		else
		{
			paramNameInput.setText("");
			paramMinValueInput.setText("");
			paramMaxValueInput.setText("");
			pointsToScanInput.setText("");
			simTimeInput.setText("");
			timePointsInput.setText("");
			logScaleInput.setSelection(false);
			steadyStateInput.setSelection(false);
		}
	}
}