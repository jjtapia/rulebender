package rulebender.simulate.parameterscan;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import rulebender.core.utility.Console;
import rulebender.preferences.PreferencesClerk;
import rulebender.simulate.BioNetGenUtility;
import rulebender.simulate.ResultsFileUtility;
import rulebender.simulate.view.SimulateView;

public class ParameterScanComposite extends Composite {
	// A static Composite object that is the form itself.
	// private static Composite parScanForm;

	// These are the gui objects that receive the values given by the user.
	final Text paramNameInput;
	final Text paramMinValueInput;
	final Text paramMaxValueInput;
	final Text pointsToScanInput;
	final Button logScaleInput;
	final Button steadyStateInput;
	final Text simTimeInput;
	final Text timePointsInput;
	final Text methodInput;
	final Button verboseInput;

	private SimulateView m_parentView;

	/**
	 * 
	 * @param parent
	 */
	public ParameterScanComposite(final Composite parent, SimulateView view) {
		// Call the superclass
		super(parent, SWT.NONE);

		m_parentView = view;

		// parameter scan
		ParameterScanComposite parScanForm = this;

		// grid layout
		parScanForm.setLayout(new GridLayout(6, true));

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
		paramMaxValueLabel
		    .setText("  Parameter Max Value (Real Number > Min Value)");
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

		// simulation method
		Label methodLabel = new Label(parScanForm, SWT.NONE);
		methodLabel.setText("  Simulation Method (ode, ssa, pla)");
		methodLabel.setLayoutData(labelGridData);

		methodInput = new Text(parScanForm, SWT.BORDER);
		methodInput.setLayoutData(textGridData);

		// steady state
		Label verboseLabel = new Label(parScanForm, SWT.NONE);
		verboseLabel.setText("  Verbose Output ?");
		verboseLabel.setLayoutData(labelGridData);

		verboseInput = new Button(parScanForm, SWT.CHECK);

		// empty label to control layout
		new Label(parScanForm, SWT.NONE).setText("");
		new Label(parScanForm, SWT.NONE).setText("");
		new Label(parScanForm, SWT.NONE).setText("");

		GridData gridData = new GridData();
		gridData.horizontalSpan = 5;
		gridData.horizontalAlignment = SWT.FILL;

		// OK button
		Button okButton = new Button(parScanForm, SWT.NONE);
		okButton.setText("Run");

		okButton.setLayoutData(gridData);

		// pack
		parScanForm.pack();

		// add listener for OK button
		okButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// do nothing
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				// Create the data object that will hold the user values.
				final ParameterScanData scanData = new ParameterScanData();

				boolean verified = true;
				String errorMessage = "Errors in Input: \n\t";
				// String currentPath = m_parentView.getSelectedFile();
				try {
					/*
					 * if(currentPath.equals("") || !(new File(currentPath)).exists()) {
					 * verified = false; errorMessage += "Invalid file path.\n\t\t"; }
					 */
					// parameter name
					if (paramNameInput.getText().trim().length() == 0) {
						verified = false;
						errorMessage += "Please specify a parameter name.\n\t";
					}

					// par_min
					if (paramMinValueInput.getText().trim().length() == 0) {
						verified = false;
						errorMessage += "Please specify a min parameter value.\n\t";
					}
					// make sure par_min > 0
					else if (Float.parseFloat(paramMinValueInput.getText().trim()) < 0) {
						verified = false;
						errorMessage += "Min value cannot be less than zero.\n\t";
					}
					// make sure par_min > 0 if log scale selected
					else if (Float.parseFloat(paramMinValueInput.getText().trim()) == 0
					    && logScaleInput.getSelection()) {
						verified = false;
						errorMessage += "Min value cannot be zero when log scale is selected.\n\t";
					}

					// par_max
					if (paramMaxValueInput.getText().trim().length() == 0) {
						verified = false;
						errorMessage += "Please specify a max parameter value.\n\t";
					}

					// make sure par_max >= par_min
					if (Float.parseFloat(paramMaxValueInput.getText().trim()) < Float
					    .parseFloat(paramMinValueInput.getText().trim())) {
						verified = false;
						errorMessage += "Max value must be >= min value.\n\t";
					}

					// number of scan points
					if (pointsToScanInput.getText().trim().length() == 0) {
						verified = false;
						errorMessage += "Please specify number of scan points.\n\t";
					} else if (Integer.parseInt(pointsToScanInput.getText().trim()) <= 0) {
						verified = false;
						errorMessage += "Number of scan points must be greater than zero.\n\t";
					}

					// t_end
					if (simTimeInput.getText().trim().length() == 0) {
						verified = false;
						errorMessage += "Please specify a simulation time.\n\t";
					} else if (Float.parseFloat(simTimeInput.getText().trim()) <= 0) {
						verified = false;
						errorMessage += "Simulation time must be positive.\n\t";
					}

					// n_steps
					if (timePointsInput.getText().trim().length() == 0) {
						verified = false;
						errorMessage += "Please specify number of output time points.\n\t";
					} else if (Integer.parseInt(timePointsInput.getText().trim()) <= 0) {
						verified = false;
						errorMessage += "Output time points must be positive.\n\t";
					}

					// method
					if (methodInput.getText().trim().length() == 0) {
						verified = false;
						errorMessage += "Please specify a simulation method.\n\t";
					}
				} catch (NumberFormatException e) {
					verified = false;
				}

				if (!verified) {
					MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(),
					    SWT.ICON_INFORMATION);
					mb.setText("Error Info");
					mb.setMessage(errorMessage);
					mb.open();
					return;
				} else {
					scanData.setName(paramNameInput.getText());
					scanData.setMinValue(Float.parseFloat(paramMinValueInput.getText()));
					scanData.setMaxValue(Float.parseFloat(paramMaxValueInput.getText()));
					scanData.setPointsToScan(Integer.parseInt(pointsToScanInput.getText()));
					scanData.setSimulationTime(Float.parseFloat(simTimeInput.getText()));
					scanData.setNumTimePoints(Integer.parseInt(timePointsInput.getText()));
					scanData.setLogScale(logScaleInput.getSelection());
					scanData.setSteadyState(steadyStateInput.getSelection());
					scanData.setMethod(methodInput.getText());
					scanData.setVerbose(verboseInput.getSelection());
				}

				// TODO Possibly show console,

				// Display console output.
				// Console.displayOutput(
				// m_parentView.getSelectedFile().getFullPath().toOSString(),
				// Console.getConsoleLineDelimeter() + "Running Parameter Scan...");

				Console.displayOutput(m_parentView.getSelectedFile().getRawLocation()
				    .toOSString(), Console.getConsoleLineDelimeter()
				    + "Running Parameter Scan...");

				Console.showConsole(m_parentView.getSelectedFile().getRawLocation()
				    .toOSString());

				// Run the parameter scan. This returns a boolean, but for now I am
				// ignoring it.
				BioNetGenUtility.parameterScan(m_parentView.getSelectedFile(),
				    scanData, PreferencesClerk.getBNGPath(),
				    PreferencesClerk.getBNGPath() + "Perl2/scan_var.pl",
				    ResultsFileUtility
				        .getParameterScanResultsDirectoryForIFile(m_parentView
				            .getSelectedFile()));
			}
		});
	}

	public void setFormText(ParameterScanData data) {
		if (data != null) {
			paramNameInput.setText(data.getName());
			paramMinValueInput.setText(data.getMinValue() + "");
			paramMaxValueInput.setText(data.getMaxValue() + "");
			pointsToScanInput.setText(data.getPointsToScan() + "");
			simTimeInput.setText(data.getSimulationTime() + "");
			timePointsInput.setText(data.getNumTimePoints() + "");
			if (data.isLogScale()) {
				logScaleInput.setSelection(true);
			}
			if (data.isSteadyState()) {
				steadyStateInput.setSelection(true);
			}
			methodInput.setText(data.getMethod() + "");
			if (data.isVerbose()) {
				verboseInput.setSelection(true);
			}
		}

		else {
			paramNameInput.setText("");
			paramMinValueInput.setText("");
			paramMaxValueInput.setText("");
			pointsToScanInput.setText("");
			simTimeInput.setText("");
			timePointsInput.setText("");
			logScaleInput.setSelection(false);
			steadyStateInput.setSelection(false);
			methodInput.setText("");
			verboseInput.setSelection(false);
		}
	}
}