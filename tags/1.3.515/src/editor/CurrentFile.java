package editor;

/**
 * CurrentFile.java
 * 
 * This source file defines the model for a file that is being edited in the 
 * BNG IDE.
 * 
 * @author Yao Sun - All original code
 * @author Adam M. Smith - Additions and documentations marked with '-ams <date>'
 */


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javax.swing.table.TableModel;

import org.bng.simulate.BioNetGenUtility;
import org.bng.simulate.ResultsFileUtility;
import org.bng.simulate.parameterscan.ParameterScanData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import prefuse.visual.VisualItem;

import resultviewer.ui.ResultViewer;
import visualizationviewer.VisualizationViewerController;

import editor.contactmap.CMapModel;
import editor.contactmap.CMapVisual;
import editor.influencegraph.IMapModel;
import editor.influencegraph.IMapVisual;


public class CurrentFile {
	private String filename;
	private String filepath;// halfpath
	String prevcontent;
	private BNGTextArea bngTextArea;
	CTabItem tabitem;
	String ss1;// content in file
	String ss2;// content in editor
	int ostype; // 1:windows 2:man 3:other
	
	boolean makeNewVisualModels;
	
	ParameterScanData lastpsinput = null;
	
	// A reference to the current model of the IMap
	IMapModel iModel;
	
	// A reference to the current visual model of the IMap
	IMapVisual iVisualModel;
	
	CMapModel cModel;
	CMapVisual cVisualModel;
	
	// result viewer
	private ResultViewer resViewer;

	/**
	 * Constructor
	 **/
	public CurrentFile(String fpath, String fname, int inostype, boolean empty, int tabIndex) {
		ostype = inostype;
		
		if (fpath == null) 
		{
			setFilepath(null);
			
			// add suffix for the file
			if (!(fname.endsWith(".bngl") || fname.endsWith(".BNGL"))) {
				fname += ".bngl";
			}
			setFilename(fname);
		
			BNGEditor.getShowfilepath().setText("");
			if (empty) {
				if (tabIndex == -1) {
					tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE);
				}
				else {
					tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE, tabIndex);
				}
				setTextarea(new BNGTextArea(BNGEditor.getTextFolder(), tabitem, SWT.NONE));
				tabitem.setControl(getBNGTextArea());
				tabitem.setText("*"+getFileName());
				getBNGTextArea().getStyledTextArea().setText("");
				ss1 = getBNGTextArea().doc.get();
				getBNGTextArea().textchanges.remove(0);
				getBNGTextArea().getStyledTextArea().setFocus();
			} else {
				if (tabIndex == -1) {
					tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE);
				}
				else {
					tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE, tabIndex);
				}
				setTextarea(new BNGTextArea(BNGEditor.getTextFolder(), tabitem, SWT.NONE));
				tabitem.setControl(getBNGTextArea());
				tabitem.setText("*"+getFileName());
				
				// Here the template is read in from a file.
				/*
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(tempfile));
					String str = "";
					try {
						while (br.ready()) {
							str = str + br.readLine()
									+ getTextarea().getStyledTextArea().getLineDelimiter();
						}
						br.close();
						getTextarea().textarea.setText("");
						getTextarea().textarea.append(str);
						getTextarea().textchanges.remove(0);
						getTextarea().textchanges.remove(0);
						ss1 = getTextarea().doc.get();
					} catch (IOException e) {
					}
				} catch (FileNotFoundException e) {
				}
				*/
				
				// Here the template is from a string.
				String str = "# Basic BNGL Model Template\n\n" +
						"begin model" + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"begin parameters" + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"end parameters" + getBNGTextArea().getStyledTextArea().getLineDelimiter() + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"begin molecule types" + getBNGTextArea().getStyledTextArea().getLineDelimiter() + 
						"end molecule types" + getBNGTextArea().getStyledTextArea().getLineDelimiter() + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"begin seed species" + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"end  seed species" + getBNGTextArea().getStyledTextArea().getLineDelimiter() + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"begin observables" + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"end observables" + getBNGTextArea().getStyledTextArea().getLineDelimiter() + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"begin reaction rules" + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"end reaction rules" + getBNGTextArea().getStyledTextArea().getLineDelimiter() + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"end model" + getBNGTextArea().getStyledTextArea().getLineDelimiter() + getBNGTextArea().getStyledTextArea().getLineDelimiter() +
						"#ACTIONS";
				
				getBNGTextArea().getStyledTextArea().setText("");
				getBNGTextArea().getStyledTextArea().append(str);
				getBNGTextArea().textchanges.remove(0);
				getBNGTextArea().textchanges.remove(0);
				ss1 = getBNGTextArea().doc.get();
				
				getBNGTextArea().getStyledTextArea().setFocus();
			}
		}
		// The file path is not null
		else 
		{
			setFilepath(fpath);
			setFilename(fname);
			if (tabIndex == -1) {
				tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE);
			}
			else {
				tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE, tabIndex);
			}
			setTextarea(new BNGTextArea(BNGEditor.getTextFolder(), tabitem, SWT.NONE));
			tabitem.setControl(getBNGTextArea());
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(new File(fpath, fname)));
				String str = "";
				try {
					while (br.ready()) {
						str = str + br.readLine()
								+ getBNGTextArea().getStyledTextArea().getLineDelimiter();
					}
					br.close();
					getBNGTextArea().getStyledTextArea().setText("");
					getBNGTextArea().getStyledTextArea().append(str);
					getBNGTextArea().textchanges.remove(0);
					getBNGTextArea().textchanges.remove(0);
					ss1 = getBNGTextArea().doc.get();
					tabitem.setText(getFileName());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			getBNGTextArea().getStyledTextArea().setFocus();
		}
		getBNGTextArea().getStyledTextArea().setFocus();	
		
		produceNewModels();
		
		// set result viewer
		setResViewer(BNGEditor.getResViewer());
	}

	public void saveas() {
		boolean tosave = false;
		String fpath = "";
		String fname = "";
		File file = null;

		FileDialog savediag = new FileDialog(BNGEditor.getMainEditorShell(), SWT.SAVE);
		savediag.setFilterExtensions(new String[] { "*.bngl", "*.txt", "*.*" });
		String tempstr = getFileName();
		if (tempstr.indexOf('.') != -1
				&& (tempstr.endsWith("bngl") || tempstr.endsWith("txt")))
			tempstr = tempstr.substring(0, tempstr.indexOf('.'));
		savediag.setFileName(tempstr);
		savediag.setFilterPath(ConfigurationManager.getConfigurationManager().getWorkspacePath());
		String returnedPath = savediag.open();
		fpath = savediag.getFilterPath();
		fname = savediag.getFileName();
		
		if (fpath.equals("") || fpath == null || returnedPath == null)
			return;

		if (!(fname.endsWith(".bngl") || fname.endsWith(".BNGL"))) {
			fname += ".bngl";
		}
		
		// check if the file has already exists
		file = new File(fpath, fname);

		if (file.exists()) {
			MessageBox replaceMsg = new MessageBox(BNGEditor.getMainEditorShell(),
					SWT.ICON_QUESTION | SWT.YES | SWT.NO);
			replaceMsg.setMessage(fname
					+ " already exists. Do you want to replace it?");
			int option = replaceMsg.open();

			if (option == SWT.YES) 
			{
				tosave = true;
			}
		} else {
			tosave = true;
		}

		// save
		if (tosave == true) {
			try {
				PrintWriter pw = new PrintWriter(new BufferedWriter(
						new FileWriter(new File(fpath, fname))));
				pw.write(getBNGTextArea().doc.get());
				pw.flush();
				pw.close();

				// open the new saved file, close the original one
				int currentTabIndex = BNGEditor.getInputfiles().indexOf(this);
				CurrentFile newF = new CurrentFile(fpath, fname, this.ostype,
						true, currentTabIndex+1);
				newF.setResViewer(this.getResViewer());
				tabitem.setText(fname);
				BNGEditor.replaceInputfiles(this, newF);

			} catch (Exception esave) {
				esave.printStackTrace();
			}
		}
	}

	/**
	 * Save the file given the current name.
	 */
	public void save() {
		if (getFilepath() == null) {
			boolean tosave = false;
			String fpath = "";
			String fname = "";
			File file = null;

			FileDialog savediag = new FileDialog(BNGEditor.getMainEditorShell(), SWT.SAVE);
			savediag.setFilterExtensions(new String[] { "*.bngl", "*.txt",
					"*.*" });
			String tempstr = getFileName();
			if (tempstr.indexOf('.') != -1
					&& (tempstr.endsWith("bngl") || tempstr.endsWith("txt")))
				tempstr = tempstr.substring(0, tempstr.indexOf('.'));
			savediag.setFileName(tempstr);
			savediag.open();
			fpath = savediag.getFilterPath();
			fname = savediag.getFileName();
			if (fpath.equals("") || fpath == null) {
				return;
			}

			if (!(fname.endsWith(".bngl") || fname.endsWith(".BNGL"))) {
				fname += ".bngl";
			}

			// check if the file has already exists
			file = new File(fpath, fname);

			if (file.exists()) {
				MessageBox replaceMsg = new MessageBox(BNGEditor.getMainEditorShell(),
						SWT.ICON_QUESTION | SWT.YES | SWT.NO);
				replaceMsg.setMessage(fname
						+ " already exists. Do you want to replace it?");
				int option = replaceMsg.open();

				if (option == SWT.YES) {
					tosave = true;
				}
			} else {
				tosave = true;
			}

			// save
			if (tosave == true) {
				try {
					PrintWriter pw = new PrintWriter(new BufferedWriter(
							new FileWriter(file)));
					pw.write(getBNGTextArea().doc.get());
					pw.flush();
					pw.close();
					ss1 = getBNGTextArea().doc.get();

					setFilepath(fpath);
					setFilename(fname);
					if (ostype == 1) {
						BNGEditor.getShowfilepath().setText(getFilepath() + "\\"
								+ getFileName());
					} else {
						BNGEditor.getShowfilepath().setText(getFilepath() + "/"
								+ getFileName());
					}
					// change the name of the file on the tab
					tabitem.setText(getFileName());

				} catch (Exception esave) {
					esave.printStackTrace();
				}
			}
			
		} else {
			PrintWriter pw;
			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(
						new File(getFilepath(), getFileName()))));
				if (!getBNGTextArea().doc.get().endsWith(
						getBNGTextArea().doc.getDefaultLineDelimiter()))
					pw.write(getBNGTextArea().doc.get()
							+ getBNGTextArea().doc.getDefaultLineDelimiter());
				else
					pw.write(getBNGTextArea().doc.get());
				pw.flush();
				pw.close();
				// eliminate the star mark on the tab
				tabitem.setText(getFileName());
				ss1 = getBNGTextArea().doc.get();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Tell the VisViewerController that new models have been produced
		produceNewModels();
	}

	void close() {
		ss2 = getBNGTextArea().doc.get();
		if (ss1 != null && ss2 != null && ss1.equals(ss2))
			return;
		MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
				| SWT.YES | SWT.NO);
		mb.setText("Save before close ?");
		mb.setMessage("File " + getFileName()
				+ " has been changed. Save before close ?");

		if (mb.open() == SWT.YES) {
			save();
		}
	}

	public void check() 
	{
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Checking Model...");

		if (!(getFileName().endsWith(".bngl") || getFileName().endsWith(".BNGL"))) 
		{
			//BNGEditor.consoleText.setText("Please Open a Model With Suffix \".bngl\".");
			BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");
			return;
		}
		//BNGEditor.displayOutput("");
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());
		
		String ss2 = getBNGTextArea().doc.get();
		if (!ss1.equals(ss2) || getFilepath() == null) {
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Save before Check ?");
			mb.setMessage("File "
					+ getFileName()
					+ " has been changed. File must be saved before Check. Save before Check ?");
			if (mb.open() == SWT.YES) {
				save();
				ss2 = getBNGTextArea().doc.get();
				if (ss1.equals(ss2) && getFilepath() != null)
					runbng(false);
				else
					BNGEditor.displayOutput("File Save NOT Successful, Model Not Checked.");
			} 
			else
			{
				//BNGEditor.displayOutput("Model NOT Checked.");
				BNGEditor.displayOutput("Model NOT Checked.");
			}
		} else {
			runbng(false);
		}
	}

	public void run() 
	{
		BNGEditor.displayOutput("Running Model..."+ BNGEditor.getConsoleLineDelimeter());
		
		if (!(getFileName().endsWith(".bngl") || getFileName().endsWith(".BNGL"))) 
		{
			//BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");
			BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");

			return;
		}
		
		//BNGEditor.displayOutput("");
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());
		
		String ss2 = getBNGTextArea().doc.get();
		if (!ss1.equals(ss2) || getFilepath() == null) {
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Save before Run ?");
			mb.setMessage("File "
					+ getFileName()
					+ " has been changed. File must be saved before Run. Save before Run ?");
			if (mb.open() == SWT.YES) 
			{
				save();
				ss2 = getBNGTextArea().doc.get();
				if (ss1.equals(ss2) && getFilepath() != null)
				{
					runbng(true);
				}
				else
				{
					BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"File save NOT successful. Model not run.");
				}
			} 
			else
			{
				BNGEditor.displayOutput("Model NOT run.");
			}
			
		} else {
			runbng(true);
		}
	}

	
/**
 * Given the passed in ParameterScanData, do some checks to the file,
 * run the parameter scan, and print the log to the console. 
 * @param data
 */
	public void parscan(ParameterScanData data) 
	{
		BNGEditor.displayOutput("Running parameter scan...");
		
		// Make sure that the opened file is a bngl file.
		if (!(getFileName().endsWith(".bngl") || getFileName().endsWith(".BNGL"))) 
		{
			BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".\n");
			return;
		}
		
		// Get the text of the bngl file
		String ss2 = getBNGTextArea().doc.get();
		
		// Make sure that the file is the same as what is in the editor
		// and that the file exists.  If either of these is not true, 
		// then the file needs to be saved.
		if (!ss1.equals(ss2) || getFilepath() == null) 
		{	
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			
			mb.setText("Save before Parameter Scan ?");
			
			mb.setMessage("File " + getFileName()
					+ " has been changed. File must be saved before "
					+ "Parameter Scan. Save file and run Parameter Scan ?");
			
			if (mb.open() == SWT.YES) 
			{
				save();
			} 
			
			else 
			{
				BNGEditor.displayOutput("Parameter scan NOT run.");
				return;
			}
		}
		
		// Get the string value of the text.
		ss2 = getBNGTextArea().doc.get();
		
		// If the text is still different from this object, or the file path
		// is still null, then the save was not successful.  
		if (!ss1.equals(ss2) || getFilepath() == null) 
		{
			BNGEditor.displayOutput("File save not successful. Parameter scan not run.");
			return;
		}
		
		// Save no matter what ????
		save();
		
		// If there is no path to bng, then prompt the user to set it. 
		if (ConfigurationManager.getConfigurationManager().getBNGFPath() == null || ConfigurationManager.getConfigurationManager().getBNGFName() == null)
		{
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(),
					SWT.ICON_INFORMATION);
		
			mb.setText("Error Info");
			mb.setMessage("Error finding BNG, Please Set BNG Path");
			mb.open();
			(new SetBNGPathDialogue(BNGEditor.getMainEditorShell(), BNGEditor.getEditor())).show();
			return;
		}
		
		// slash for different operating system
		String tmpSlash = "";
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1) 
		{
			tmpSlash = "\\";
		}
		else 
		{
			tmpSlash = "/";
		}
		
		// Make sure that BioNetGen is present
		File bngfile = new File(ConfigurationManager.getConfigurationManager().getBNGFPath() + tmpSlash + ConfigurationManager.getConfigurationManager().getBNGFName());
		
		// if the name of the bng executable is not found (BNG2.pl), then 
		// ask the user to set the path. 
		if (ConfigurationManager.getConfigurationManager().getBNGFName() == null || ConfigurationManager.getConfigurationManager().getBNGFPath() == null || !bngfile.exists()) 
		{
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(),
					SWT.ICON_INFORMATION);
			mb.setText("Error Info");
			mb.setMessage("Error finding BNG, Please Set BNG Path");
			mb.open();
			(new SetBNGPathDialogue(BNGEditor.getMainEditorShell(), BNGEditor.getEditor())).show();
			bngfile = null;
			return;
		}
		
		// Now that we know that BNG is available, we do not need the file reference.
		bngfile = null;
		
		String bngResultsPath = ConfigurationManager.getConfigurationManager().getWorkspacePath()
		   + ConfigurationManager.getConfigurationManager().getSlash() + "BNGResults";
		
		String paraName = data.getName().trim();
		String minV = (data.getMinValue()+"").trim();
		String maxV = (data.getMaxValue()+"").trim();
		String pointsNum = (data.getPointsToScan()+"").trim();
		String logScale = data.isLogScale() ? "true" : "false";
		String steady = data.isSteadyState() ? "true" : "false";
		String simulationTime = (data.getSimulationTime()+"").trim();
		String timePoints = (data.getNumTimePoints()+"").trim();

		//RUN -----------------------------

		// show the parameters in console
		String parameters = "Parameter Name: " + paraName + BNGEditor.getConsoleLineDelimeter();
		parameters += "Minimum Value: " + minV + BNGEditor.getConsoleLineDelimeter();
		parameters += "Maximum Value: " + maxV + BNGEditor.getConsoleLineDelimeter();
		parameters += "Number of Points to Scan: " + pointsNum + BNGEditor.getConsoleLineDelimeter();
		parameters += "Log Scale: " + logScale + BNGEditor.getConsoleLineDelimeter();
		parameters += "Steady State: " + steady + BNGEditor.getConsoleLineDelimeter();
		parameters += "Simulation Time: " + simulationTime + BNGEditor.getConsoleLineDelimeter();
		parameters += "Number of Time Points: " + timePoints + BNGEditor.getConsoleLineDelimeter();
		
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+parameters + "\n\nRunning...");
	
		String resultsFilePath = ResultsFileUtility.getParameterScanResultsDirectoryForFile(new File(getFilepath()+tmpSlash+getFileName()));
		boolean worked = BioNetGenUtility.parameterScan(getFilepath() + ConfigurationManager.getConfigurationManager().getSlash() + getFileName(),
									   data,
									   ConfigurationManager.getConfigurationManager().getBNGFPath()+tmpSlash,
									   ConfigurationManager.getConfigurationManager().getBNGFPath()+tmpSlash+"Perl2/scan_var.pl", 
									   resultsFilePath);
		
		
		Rectangle bounds = new Rectangle(0, 0, 600, 600);

		if(worked)
		{
			String chomped = resultsFilePath.substring(0, resultsFilePath.lastIndexOf(System.getProperty("file.separator")));
			System.out.println("Chomped: " + chomped);

			//This is used for the results viewer
			String curTime = chomped.substring(chomped.lastIndexOf(System.getProperty("file.separator"))+1, chomped.length());
	
			// get the model name
			String modelName = getFileName();
					
			//remove the .bngl suffix
			if (modelName.lastIndexOf('.') != -1)
			{
				modelName = modelName.substring(0,
						modelName.lastIndexOf('.'));
			}
	
			if (resViewer == null || resViewer.getShell() == null) 
			{
	
				// create a result viewer
				resViewer = new ResultViewer(bngResultsPath,
						//modelName + "\t" + "para_scan-"+ curTime + "\t" + data.getName()+"_000", ".scan", // I replaced command.getPrefix with the data.getName()_000 because I don't have access to the command anymore.
						modelName + "\t" + curTime + "\t" + data.getName()+"_000", ".scan",
						bounds);
				BNGEditor.setResViewer(resViewer);
				// resViewer.setBlockOnOpen(true);
				resViewer.open();
	
				if (resViewer != null && resViewer.getGraphFrame() != null
						&& resViewer.getGraphFrame().isShowing()) {
					resViewer.getGraphFrame().dispose(); // close the graph
				}
				BNGEditor.setResViewer(resViewer);
			} 
			else 
			{
				resViewer.getShell().setVisible(true);
				resViewer.refresh();
		
				resViewer.openDefaultFile(modelName + "\t" + curTime //"para_scan-"+ curTime
						+ "\t" + data.getName()+"_000", // I replaced command.getPrefix with the data.getName()_000 because I don't have access to the command anymore.
						".scan");
			}
		}
	}
	
	/**
	 * 
	 * @param viewresults
	 */
	public void runbng(boolean viewresults) 
	{
		BNGEditor.displayOutput("Running BioNetGen Simulator...");
		// the bngfpath or bngfname can not be empty
		if (ConfigurationManager.getConfigurationManager().getBNGFPath() == null || ConfigurationManager.getConfigurationManager().getBNGFName() == null) {
			BNGEditor.displayOutput("Error finding BNG.");
			(new SetBNGPathDialogue(BNGEditor.getMainEditorShell(), BNGEditor.getEditor())).show();
		} 
		// BNG is located so continue.
		else 
		{
			String tmpSlash = "";
			// Windows
			if (ostype == 1) {
				tmpSlash = "\\";
			}
			else {
				tmpSlash = "/";
			}
			// if the bng file does not exist, return
			File bngfile = new File(ConfigurationManager.getConfigurationManager().getBNGFPath() + tmpSlash + ConfigurationManager.getConfigurationManager().getBNGFName());
			if (!bngfile.exists()) {
				BNGEditor.displayOutput("Error finding BNG.");
				(new SetBNGPathDialogue(BNGEditor.getMainEditorShell(), BNGEditor.getEditor())).show();
				bngfile = null;
				return;
			}
			bngfile = null;
		
			String str2 = getFileName();
			
			BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());

			// workspace path
			String workspacePath = ConfigurationManager.getConfigurationManager().getWorkspacePath();
			
			// BNGResults folder path.
			String bngResultsPath = workspacePath + tmpSlash + "BNGResults";
		
			
			// The path to the actual results.		
			String resultsFilePath = ResultsFileUtility.getSimulationResultsDirectoryForFile(new File(getFilepath()+tmpSlash+getFileName()));
			
			boolean worked = BioNetGenUtility.runBNGLFile(getFilepath()+tmpSlash+getFileName(), 
								 ConfigurationManager.getConfigurationManager().getBNGFPath()+tmpSlash+ConfigurationManager.getConfigurationManager().getBNGFName(), 
								 resultsFilePath);
			
			if (viewresults && worked)
			{
				// get the model name
				String modelName = str2;
				if (modelName.lastIndexOf('.') != -1) {
					modelName = modelName.substring(0,
							modelName.lastIndexOf('.'));
				}

				Rectangle bounds = new Rectangle(0, 0, 600, 600);

				String chomped = resultsFilePath.substring(0, resultsFilePath.lastIndexOf(System.getProperty("file.separator")));
				System.out.println("Chomped: " + chomped);

				//This is used for the results viewer
				String curTime = chomped.substring(chomped.lastIndexOf(System.getProperty("file.separator"))+1, chomped.length());
				
				if (resViewer == null || resViewer.getShell() == null) 
				{
					// create a result viewer
					resViewer = new ResultViewer(bngResultsPath, modelName
							+ "\t" + curTime, ".gdat", bounds);
					BNGEditor.setResViewer(resViewer);

					// resViewer.setBlockOnOpen(true);
					resViewer.open();

					if (resViewer != null && resViewer.getGraphFrame() != null
							&& resViewer.getGraphFrame().isShowing()) {
						resViewer.getGraphFrame().dispose(); // close the graph
					}

					BNGEditor.setResViewer(resViewer);
				} else {
					resViewer.getShell().setVisible(true);
					resViewer.refresh();
					resViewer.openDefaultFile(modelName + "\t" + curTime,
							".gdat");
				}
			}
		}
	}
	
	/**
	 * Generate model guide for the CurrentFile
	 */
	public void generateModelGuide() {
		
		String tmpSlash = "";
		if (ostype == 1) {
			tmpSlash = "\\";
		}
		else {
			tmpSlash = "/";
		}
		
		// get the model name
		String modelName = filename;
		if (modelName.lastIndexOf('.') != -1) {
			modelName = modelName.substring(0,
					modelName.lastIndexOf('.'));
		}
		
		// write model guide to file
		File modelGuideFile = new File(filepath + tmpSlash + modelName + "_model_guide.txt");
		if (modelGuideFile.exists()) {
			modelGuideFile.delete();
		}
		modelGuideFile = new File(filepath + tmpSlash + modelName + "_model_guide.txt");
		PrintWriter pw;
		try {
			pw = new PrintWriter(modelGuideFile);
			pw.println("Model Guide\n");
			// BNGL model
			//pw.println(this.textarea.textarea.getText());
			
			//TODO arrange annotation order
			// CMAP
			if (cVisualModel == null) {
				getCMapVisualModel();
			}
			if (cVisualModel != null) {
				// aggregates
				Iterator iter = cVisualModel.getCMAPNetworkViewer().getVisualization().items("aggregates");
				while (iter.hasNext()) {
					VisualItem item = (VisualItem) iter.next();
					if (item.canGet("annotation", TableModel.class)) {
						TableModel annotation = (TableModel) item.get("annotation");
						if (annotation != null) {
							pw.println(annotation);
						}
					}
				}
				// nodes and edges
				iter = cVisualModel.getCMAPNetworkViewer().getVisualization().items("component_graph");
				while (iter.hasNext()) {
					VisualItem item = (VisualItem) iter.next();
					if (item.canGet("annotation", TableModel.class)) {
						TableModel annotation = (TableModel) item.get("annotation");
						if (annotation != null) {
							pw.println(annotation);
						}
					}
				}
			}
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// message telling the user finished
		MessageBox messageBox = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_INFORMATION);
        messageBox.setText("Notification");
        messageBox.setMessage("Model guide has been generated.");
        messageBox.open();
	}
	
	private String bngOutputFilePathExtractor(String line) {
		String path = "";
		String[] outputCommandWithDotEnd = { "Wrote network to ",
				"Time course of concentrations written to file ",
				"Time course of groups written to file ", 
				"Wrote SBML to ", 
				"Wrote BNG XML to ",
				"Wrote M-file"};
		String[] outputCommandWithoutDotEnd = {"Final network file written to "};

		for (int i = 0; i < outputCommandWithDotEnd.length; i++) {
			if (line.indexOf(outputCommandWithDotEnd[i]) != -1) {
				int cmdLen = outputCommandWithDotEnd[i].length();
				path = line.substring(line.indexOf(outputCommandWithDotEnd[i])
						+ cmdLen, line.length() - 1);
				return path;
			}
		}

		for (int i = 0; i < outputCommandWithoutDotEnd.length; i++) {
			if (line.indexOf(outputCommandWithoutDotEnd[i]) != -1) {
				int cmdLen = outputCommandWithoutDotEnd[i].length();
				path = line.substring(
						line.indexOf(outputCommandWithoutDotEnd[i]) + cmdLen,
						line.length());
				return path;
			}
		}

		return path;
	}
	
	private String bngInputFilePathExtractor(String line) {
		String path = "";
		String[] inputCommandWithoutDotEnd = {"Reading from file "};
		
		for (int i = 0; i < inputCommandWithoutDotEnd.length; i++) {
			if (line.indexOf(inputCommandWithoutDotEnd[i]) != -1) {
				int cmdLen = inputCommandWithoutDotEnd[i].length();
				path = line.substring(
						line.indexOf(inputCommandWithoutDotEnd[i]) + cmdLen,
						line.length());
				return path;
			}
		}
		
		return path;
	}

	public ResultViewer getResViewer() {
		return resViewer;
	}

	public void setResViewer(ResultViewer resViewer) {
		this.resViewer = resViewer;
	}

	/*
	 * result file filter
	 */
	class Resultfilefilter implements FilenameFilter {
		private String str1;

		public Resultfilefilter(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if (name.startsWith(str1) && name.endsWith(".net"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".cdat"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".gdat"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".scan"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".m"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".log"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".xml"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".cfg"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".rxn"))
				return true;
			if (name.equals(str1 + ".bngl"))
				return true;
			return false;
		}
	}

	class myfilefilter implements FilenameFilter {
		private String str1;

		public myfilefilter(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if (name.startsWith(str1) && name.endsWith(".net"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".cdat"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".gdat"))
				return true;
			return false;
		}
	}

	class myfilefilter1 implements FilenameFilter {
		private String str1;

		public myfilefilter1(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if (name.startsWith(str1) && name.endsWith(".cdat"))
				return true;
			if (name.startsWith(str1) && name.endsWith(".gdat"))
				return true;
			return false;
		}
	}

	class myfilefilter2 implements FilenameFilter {
		private String str1;

		public myfilefilter2(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if (name.startsWith(str1) && name.endsWith(".net"))
				return true;
			return false;
		}
	}

	/**
	 * Called when the Contact Map button is pressed.
	 * 
	 * -ams
	 */
	void generateCMapModel() {
	
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+ "Parsing Contact Map model...");

		/*
		 * This mess checks a ton of factors in the textarea and calls the CMap
		 * constructor with 1 of 2 sets of parameters.
		 * 
		 * The CMap constructor looks like 'CMap(String molestr, String rulestr,
		 * boolean moleculetype)'
		 * 
		 * The difference in the calls is
		 * 
		 * 1. The first one uses a valid and nonempty textarea.blockinfo[2] and
		 * the second uses a valid and nonempty textarea.blockinfo[4] for the
		 * molestr.
		 * 
		 * 2. The first passes true to moleculetype and the second passes false.
		 * 
		 * -ams 5/1/10
		 */
		try {
			boolean block2valid = false; // molecule types block
			boolean block3valid = false; // compartment info block
			boolean block4valid = false; // seed species block
			boolean block6valid = false; // rules block
			
			if (getBNGTextArea().blockinfo[2].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[2].caretbegin2,
									getBNGTextArea().blockinfo[2].caretend1
											- getBNGTextArea().blockinfo[2].caretbegin2)
							.trim().length() != 0) {
				block2valid = true;
			}
			
			if (getBNGTextArea().blockinfo[3].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[3].caretbegin2,
									getBNGTextArea().blockinfo[3].caretend1
											- getBNGTextArea().blockinfo[3].caretbegin2)
							.trim().length() != 0) {
				block3valid = true;
			}
			
			if (getBNGTextArea().blockinfo[4].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[4].caretbegin2,
									getBNGTextArea().blockinfo[4].caretend1
											- getBNGTextArea().blockinfo[4].caretbegin2)
							.trim().length() != 0) {
				block4valid = true;
			}
			
			if (getBNGTextArea().blockinfo[6].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
							.trim().length() != 0) {
				block6valid = true;
			}
			
			if (block2valid) 
			{
			
				cModel = new CMapModel(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[2].caretbegin2,
						getBNGTextArea().blockinfo[2].caretend1
								- getBNGTextArea().blockinfo[2].caretbegin2).trim(),true);
				
				//compartments info
				if (block3valid) {
					cModel.addCompartmentsInfo(getBNGTextArea().doc.get(
								getBNGTextArea().blockinfo[3].caretbegin2,
								getBNGTextArea().blockinfo[3].caretend1
										- getBNGTextArea().blockinfo[3].caretbegin2)
								.trim());
					
					// add seed species info to parse initial compartments
					if (block4valid) {
						cModel.addSeedSpeciesInfo(getBNGTextArea().doc.get(
								getBNGTextArea().blockinfo[4].caretbegin2,
								getBNGTextArea().blockinfo[4].caretend1
										- getBNGTextArea().blockinfo[4].caretbegin2).trim());
					}
					
				}
				
				// add rules info
				if (block6valid){
				cModel.addRulesInfo(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[6].caretbegin2,
						getBNGTextArea().blockinfo[6].caretend1
								- getBNGTextArea().blockinfo[6].caretbegin2)
						.trim());
				}
				
			} 
			else if (block4valid) 
			{
				cModel = new CMapModel(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[4].caretbegin2,
						getBNGTextArea().blockinfo[4].caretend1
								- getBNGTextArea().blockinfo[4].caretbegin2).trim(), false);
				
				//compartments info
				if (block3valid) {
					cModel.addCompartmentsInfo(getBNGTextArea().doc.get(
								getBNGTextArea().blockinfo[3].caretbegin2,
								getBNGTextArea().blockinfo[3].caretend1
										- getBNGTextArea().blockinfo[3].caretbegin2)
								.trim());
					
				}
				
				// add rules info
				if (block6valid) {
				cModel.addRulesInfo(getBNGTextArea().doc.get(
						getBNGTextArea().blockinfo[6].caretbegin2,
						getBNGTextArea().blockinfo[6].caretend1
								- getBNGTextArea().blockinfo[6].caretbegin2)
						.trim());
				}
			} 
			
			else {
				BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Molecule info block not valid, Contact Map NOT shown.");
			}
			
			BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Contact Map model finished parsing.");
		} 
		
		catch (org.eclipse.jface.text.BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is what is called when the 'Influence Graph' button is clicked. -ams
	 * 5/1/10
	 */
	void generateIGraphModel() {
		// Similar to the CMap, several conditions must be met to produce the
		// IMap. Some
		// Conditions will use a different IMap constructor.
		// -ams 5/1/10

		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Parsing file for Influence Graph...");
		
		try {
			boolean block2valid = false; // molecule types block
			boolean block4valid = false; // seed species block
			boolean block6valid = false; // rules block
			if (getBNGTextArea().blockinfo[2].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[2].caretbegin2,
									getBNGTextArea().blockinfo[2].caretend1
											- getBNGTextArea().blockinfo[2].caretbegin2)
							.trim().length() != 0) {
				block2valid = true;
			}
			
			if (getBNGTextArea().blockinfo[4].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[4].caretbegin2,
									getBNGTextArea().blockinfo[4].caretend1
											- getBNGTextArea().blockinfo[4].caretbegin2)
							.trim().length() != 0) {
				block4valid = true;
			}
			
			if (getBNGTextArea().blockinfo[6].valid
					&& getBNGTextArea().doc
							.get(getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
							.trim().length() != 0) {
				block6valid = true;
			}
				
			if (block2valid) {
				if (block6valid) {
					iModel = new IMapModel(getBNGTextArea().doc.get(
							getBNGTextArea().blockinfo[2].caretbegin2,
							getBNGTextArea().blockinfo[2].caretend1
									- getBNGTextArea().blockinfo[2].caretbegin2).trim(),
							getBNGTextArea().doc.get(
									getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
									.trim(), true);
				}
				else {
					BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Reaction rule block not valid, Influence Graph NOT shown.");
				}
			}
			else if (block4valid) {
				if (block6valid) {
					iModel  = new IMapModel(getBNGTextArea().doc.get(
							getBNGTextArea().blockinfo[4].caretbegin2,
							getBNGTextArea().blockinfo[4].caretend1
									- getBNGTextArea().blockinfo[4].caretbegin2).trim(),
							getBNGTextArea().doc.get(
									getBNGTextArea().blockinfo[6].caretbegin2,
									getBNGTextArea().blockinfo[6].caretend1
											- getBNGTextArea().blockinfo[6].caretbegin2)
									.trim(), false);
				}
				else {
					BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Reaction rule block not valid, Influence Graph NOT shown.");
				}
			}
			else {
				BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Molecule info block not valid, Influence Graph NOT shown.");
			}
			
			BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Influence Graph model finished parsing.");
		} catch (org.eclipse.jface.text.BadLocationException e) {
			e.printStackTrace();
		}
	}

	private String getCurrentDateAndTime() {
		Date dateNow = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yy_HH-mm-ss");
		String curTime = dateFormat.format(dateNow);
		return curTime;
	}

	/*
	 * File.renameTo(File) sometimes doesn't work
	 */
	private void copyUseChannel(File srcFile, File destFile) throws IOException {
		if ((!srcFile.exists()) || (srcFile.isDirectory())) {
			return;
		}

		if (!destFile.exists()) {
			new File(destFile.getAbsolutePath());
		}

		FileChannel out = null;
		FileChannel in = null;
		try {
			out = new FileOutputStream(destFile).getChannel();
			in = new FileInputStream(srcFile).getChannel();
			ByteBuffer buffer = ByteBuffer.allocate(102400);
			int position = 0;
			int length = 0;
			while (true) {
				length = in.read(buffer, position);
				if (length <= 0) {
					break;
				}
				// System.out.println("after read:"+buffer);
				buffer.flip();
				// System.out.println("after flip:"+buffer);
				out.write(buffer, position);
				position += length;
				buffer.clear();
				// System.out.println("after clear:"+buffer);
			}

		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
		}
	}

	public void produceNewModels()
	{
		
		//System.out.println("producing new models");
		
		// Clear the models
		cModel = null;
		cVisualModel = null;
		iModel = null;
		iVisualModel = null;
		
		// Try to generate new models.
		generateCMapModel();
		generateIGraphModel();
		
		// There could have been errors in the cmap.
		if(cModel == null)
		{
			// Do something about it.  We used to focus the console, but it is always shown now.
		}
		
		// If there was no error producing the cmap.
		else
		{ 	
			VisualizationViewerController.loadVisualizationViewController().informOfNewCMapModel();
		}
		
		// There could have been errors in the imap.
		if(iModel == null)
		{
			// Do something about it.  We used to focus the console, but it is always shown now.
		}
		
		// If there was no error producing the influence graph
		else
		{
			// Tell the visualization view controller that there was a new model created.
			// The controller will then call the methods to generate the visual model 
			// if it is necessary at this time.
			VisualizationViewerController.loadVisualizationViewController().informOfNewIGraphModel();
		}
	}
	
	public IMapVisual getIMapVisualModel()
	{
		if(iVisualModel == null)
		{
			if(iModel == null)
			{
				//System.out.println("THIS SHOULD NEVER HAPPEN AND IF IT DOES THEN THE MODEL SHOULD HAVE BEEN BUILT SOMEWHERE ELSE BECAUSE IT CANNOT HAPPEN HERE");
				//generateIMapModel();
			}
			else
			{
				iVisualModel = new IMapVisual(iModel, VisualizationViewerController.loadVisualizationViewController().getIGraphSize(),
						   							  VisualizationViewerController.loadVisualizationViewController().getIGraphOverviewSize());
			}
		}
		
		return iVisualModel;
	}
	
	public CMapVisual getCMapVisualModel()
	{
		//TODO 
		// It looks like the Display objects are resized automatically... I cannot see where this is happening.
		// If I want to manually do it, then I will need some logic here that checks to see if the display size
		// has changed. For now I'm going to ignore it because there are more important.
		if(cVisualModel == null)
		{
			if(cModel == null)
			{
				//System.out.println("THIS SHOULD NEVER HAPPEN AND IF IT DOES THEN THE MODEL SHOULD HAVE BEEN BUILT SOMEWHERE ELSE BECAUSE IT CANNOT HAPPEN HERE");
				// generateCMapModel();
			}
			else
			{
			
				cVisualModel = new CMapVisual(cModel, VisualizationViewerController.loadVisualizationViewController().getCMapSize(),
													  VisualizationViewerController.loadVisualizationViewController().getCMapOverviewSize());
				
			}
		}
		
		//TODO  This gets called several times for each time the cmap actually needs to be generated...
		//System.out.println("getCMapVisualModel");
		return cVisualModel;
	}

	public void setLastInput(ParameterScanData in)
	{
		lastpsinput = in;
	}

	public ParameterScanData getLastPSInput() 
	{
		return lastpsinput;
	}
	
	public String getFileName()
	{
		return filename;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public void setTextarea(BNGTextArea textarea) {
		this.bngTextArea = textarea;
	}

	public BNGTextArea getBNGTextArea() 
	{
		return bngTextArea;
	}
}