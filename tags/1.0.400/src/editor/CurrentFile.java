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
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.TableModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;

import resultviewer.ui.ResultViewer;
import visualizationviewer.VisualizationViewerController;

import editor.contactmap.CMapModel;
import editor.contactmap.CMapVisual;
import editor.influencegraph.IMapModel;
import editor.influencegraph.IMapVisual;
import editor.parameterscan.ParameterScanData;

public class CurrentFile {
	private String filename;
	private String filepath;// halfpath
	String prevcontent;
	private BNGTextArea textarea;
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
				tabitem.setControl(getTextarea());
				tabitem.setText("*"+getFilename());
				getTextarea().textarea.setText("");
				ss1 = getTextarea().doc.get();
				getTextarea().textchanges.remove(0);
				getTextarea().textarea.setFocus();
			} else {
				if (tabIndex == -1) {
					tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE);
				}
				else {
					tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE, tabIndex);
				}
				File tempfile = new File("Template");
				setTextarea(new BNGTextArea(BNGEditor.getTextFolder(), tabitem, SWT.NONE));
				tabitem.setControl(getTextarea());
				tabitem.setText("*"+getFilename());
				
				// Here the template is read in from a file.
				/*
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(tempfile));
					String str = "";
					try {
						while (br.ready()) {
							str = str + br.readLine()
									+ getTextarea().textarea.getLineDelimiter();
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
						"begin model" + getTextarea().textarea.getLineDelimiter() +
						"begin parameters" + getTextarea().textarea.getLineDelimiter() +
						"end parameters" + getTextarea().textarea.getLineDelimiter() + getTextarea().textarea.getLineDelimiter() +
						"begin molecule types" + getTextarea().textarea.getLineDelimiter() + 
						"end molecule types" + getTextarea().textarea.getLineDelimiter() + getTextarea().textarea.getLineDelimiter() +
						"begin seed species" + getTextarea().textarea.getLineDelimiter() +
						"end  seed species" + getTextarea().textarea.getLineDelimiter() + getTextarea().textarea.getLineDelimiter() +
						"begin observables" + getTextarea().textarea.getLineDelimiter() +
						"end observables" + getTextarea().textarea.getLineDelimiter() + getTextarea().textarea.getLineDelimiter() +
						"begin reaction rules" + getTextarea().textarea.getLineDelimiter() +
						"end reaction rules" + getTextarea().textarea.getLineDelimiter() + getTextarea().textarea.getLineDelimiter() +
						"end model" + getTextarea().textarea.getLineDelimiter() + getTextarea().textarea.getLineDelimiter() +
						"#ACTIONS";
				
				getTextarea().textarea.setText("");
				getTextarea().textarea.append(str);
				getTextarea().textchanges.remove(0);
				getTextarea().textchanges.remove(0);
				ss1 = getTextarea().doc.get();
				
				getTextarea().textarea.setFocus();
			}
		}
		// The file path is not null
		else {
			setFilepath(fpath);
			setFilename(fname);
			if (tabIndex == -1) {
				tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE);
			}
			else {
				tabitem = new CTabItem(BNGEditor.getTextFolder(), SWT.CLOSE, tabIndex);
			}
			setTextarea(new BNGTextArea(BNGEditor.getTextFolder(), tabitem, SWT.NONE));
			tabitem.setControl(getTextarea());
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(new File(fpath, fname)));
				String str = "";
				try {
					while (br.ready()) {
						str = str + br.readLine()
								+ getTextarea().textarea.getLineDelimiter();
					}
					br.close();
					getTextarea().textarea.setText("");
					getTextarea().textarea.append(str);
					getTextarea().textchanges.remove(0);
					getTextarea().textchanges.remove(0);
					ss1 = getTextarea().doc.get();
					tabitem.setText(getFilename());
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			getTextarea().textarea.setFocus();
		}
		getTextarea().textarea.setFocus();	
		
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
		String tempstr = getFilename();
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
				pw.write(getTextarea().doc.get());
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
			String tempstr = getFilename();
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
					pw.write(getTextarea().doc.get());
					pw.flush();
					pw.close();
					ss1 = getTextarea().doc.get();

					setFilepath(fpath);
					setFilename(fname);
					if (ostype == 1) {
						BNGEditor.getShowfilepath().setText(getFilepath() + "\\"
								+ getFilename());
					} else {
						BNGEditor.getShowfilepath().setText(getFilepath() + "/"
								+ getFilename());
					}
					// change the name of the file on the tab
					tabitem.setText(getFilename());

				} catch (Exception esave) {
					esave.printStackTrace();
				}
			}
			
		} else {
			PrintWriter pw;
			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(
						new File(getFilepath(), getFilename()))));
				if (!getTextarea().doc.get().endsWith(
						getTextarea().doc.getDefaultLineDelimiter()))
					pw.write(getTextarea().doc.get()
							+ getTextarea().doc.getDefaultLineDelimiter());
				else
					pw.write(getTextarea().doc.get());
				pw.flush();
				pw.close();
				// eliminate the star mark on the tab
				tabitem.setText(getFilename());
				ss1 = getTextarea().doc.get();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		// Tell the VisViewerController that new models have been produced
		produceNewModels();
	}

	void close() {
		ss2 = getTextarea().doc.get();
		if (ss1 != null && ss2 != null && ss1.equals(ss2))
			return;
		MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
				| SWT.YES | SWT.NO);
		mb.setText("Save before close ?");
		mb.setMessage("File " + getFilename()
				+ " has been changed. Save before close ?");

		if (mb.open() == SWT.YES) {
			save();
		}
	}

	public void check() 
	{
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Checking Model...");

		if (!(getFilename().endsWith(".bngl") || getFilename().endsWith(".BNGL"))) 
		{
			//BNGEditor.consoleText.setText("Please Open a Model With Suffix \".bngl\".");
			BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");
			return;
		}
		//BNGEditor.displayOutput("");
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());
		
		String ss2 = getTextarea().doc.get();
		if (!ss1.equals(ss2) || getFilepath() == null) {
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Save before Check ?");
			mb.setMessage("File "
					+ getFilename()
					+ " has been changed. File must be saved before Check. Save before Check ?");
			if (mb.open() == SWT.YES) {
				save();
				ss2 = getTextarea().doc.get();
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
		
		if (!(getFilename().endsWith(".bngl") || getFilename().endsWith(".BNGL"))) 
		{
			//BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");
			BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");

			return;
		}
		
		//BNGEditor.displayOutput("");
		BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());
		
		String ss2 = getTextarea().doc.get();
		if (!ss1.equals(ss2) || getFilepath() == null) {
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Save before Run ?");
			mb.setMessage("File "
					+ getFilename()
					+ " has been changed. File must be saved before Run. Save before Run ?");
			if (mb.open() == SWT.YES) 
			{
				save();
				ss2 = getTextarea().doc.get();
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
	 * Parameter Scan Modified on July 26th 2010
	 *
	 * This should be called from the runParameterScan method of the 
	 * ParameterScanController class.
	 * It will be passed information about the parameter scan, run the scan, and then
	 * load the results viewer.
	 */
	public void parscan(ParameterScanData data) 
	{
		BNGEditor.displayOutput("Running parameter scan...");
		if (!(getFilename().endsWith(".bngl") || getFilename().endsWith(".BNGL"))) {
			//BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");
			BNGEditor.displayOutput("Please Open a Model With Suffix \".bngl\".");
			return;
		}
		
		//BNGEditor.displayOutput("");
		String ss2 = getTextarea().doc.get();
		if (!ss1.equals(ss2) || getFilepath() == null) 
		{	
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			
			mb.setText("Save before Parameter Scan ?");
			
			mb.setMessage("File " + getFilename()
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
		
		ss2 = getTextarea().doc.get();
		
		if (!ss1.equals(ss2) || getFilepath() == null) 
		{
			BNGEditor.displayOutput("File save not successful. Parameter scan not run.");
			return;
		}
		
		save();

		// Run ===========================================================
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
		
		System.out.println();
		
		
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
		
		// It looks like this block reads in the scan_var.pl file to the
		// tempstr variable.
		String modifiedPerlScript = "", modifiedExecLine;
		try {
			BufferedReader br = new BufferedReader(new FileReader(
					new File(ConfigurationManager.getConfigurationManager().getBNGFPath(), "scan_var.pl")));
			try 
			{
				while (br.ready()) {
					modifiedPerlScript = modifiedPerlScript + br.readLine()
							+ getTextarea().textarea.getLineDelimiter();
				}
			} 
			catch (IOException e) 
			{}
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		
		// Now that we have the perl file read in, this tries to match
		// some execute regexp.  I think basically this is making a 
		// new perl script for each platform.
		Pattern p = Pattern.compile("exec\\s*=.*(\".*BNGPATH.*\\.[Pp][Ll]\\s*\")");
		Matcher m = p.matcher(modifiedPerlScript);
		
		// If the pattern matches
		if (m.find()) 
		{	
			// If the os is windows
			if (ConfigurationManager.getConfigurationManager().getOSType() == 1) 
			{
				modifiedExecLine = "\"";
				
				String tmpBNGFPath = ConfigurationManager.getConfigurationManager().getBNGFPath();
				
				while (tmpBNGFPath.indexOf('\\') != -1) 
				{
					modifiedExecLine = modifiedExecLine
							+ tmpBNGFPath.substring(0,
									tmpBNGFPath.indexOf('\\')) + "\\\\";
					tmpBNGFPath = tmpBNGFPath.substring(
							tmpBNGFPath.indexOf('\\') + 1, tmpBNGFPath.length());
				}
				modifiedExecLine = modifiedExecLine + tmpBNGFPath + "\\\\"
						+ ConfigurationManager.getConfigurationManager().getBNGFName() + "\"";
				
				//Potentially...
				modifiedExecLine = "perl " + modifiedExecLine;
			} 
			
			// If the os is mac/linux
			else
				modifiedExecLine = "\"" + ConfigurationManager.getConfigurationManager().getBNGFPath() + "/"
						+ ConfigurationManager.getConfigurationManager().getBNGFName() + "\"";
			
			System.out.println("******New exec line: " + modifiedExecLine);
			
			modifiedPerlScript = modifiedPerlScript.substring(0, modifiedPerlScript.indexOf(m.group(1)))
					+ modifiedExecLine
					+ modifiedPerlScript.substring(modifiedPerlScript.indexOf(m.group(1))
							+ m.group(1).length(), modifiedPerlScript.length());
			
		}
		// At this point the new perl script is in tempstr and will be 
		// written out later.

		// get the model name
		String modelName = getFilename();
		
		// If there is a '.' in the modelName,
		// then remove the '.' and whatever is after it.
		if (modelName.lastIndexOf('.') != -1) 
			modelName = modelName.substring(0,
					modelName.lastIndexOf('.'));

		// parentDir
		String parentDir = System.getProperty("user.dir");
		//String resultsFolderPath = parentDir + tmpSlash + "BNGResults";
		
		String resultsFolderPath = ConfigurationManager.getConfigurationManager().getWorkspacePath()
		   + ConfigurationManager.getConfigurationManager().getSlash() + "BNGResults";

		/*
		if (ConfigurationManager.getConfigurationManager().getOSType() == 2) {
			// the release application on Mac
			String appDir = "";
			if (parentDir.indexOf("/Contents/MacOS") != -1) {
				appDir = parentDir.substring(0, parentDir.indexOf("/Contents/MacOS"));
				appDir = appDir.substring(0, appDir.lastIndexOf("/"));
				resultsFolderPath = appDir + tmpSlash + "BNGResults";
			}	
		}
		 */
		
		// resultsFolder
		File resultsFolder = new File(resultsFolderPath + tmpSlash);
		if (!resultsFolder.isDirectory()) {
			resultsFolder.mkdir();
		}

		// path for ModifiedParScan.pl
		String modifiedParScanPath = "";

		// Write out the ModifiedParScan.pl file.
		PrintWriter pw;
		try {

			// pw = new PrintWriter(new BufferedWriter(new
			// FileWriter(new File("ModifiedParScan.pl"))));

			// create a folder with the name of model if not exists
			java.io.File modelFolder = new java.io.File(
					resultsFolderPath + tmpSlash + modelName + tmpSlash);
			if (!modelFolder.isDirectory()) {
				modelFolder.mkdir();
			}

			// create the folder para_scan under the folder of model
			// name if not exists
			java.io.File parascanFolder = new java.io.File(
					resultsFolderPath + tmpSlash + modelName + tmpSlash
							+ "para_scan" + tmpSlash);
			if (!parascanFolder.isDirectory()) {
				parascanFolder.mkdir();
			}

			// path for ModifiedParScan.pl
			modifiedParScanPath = resultsFolderPath + tmpSlash
					+ modelName + tmpSlash + "para_scan" + tmpSlash
					+ "ModifiedParScan.pl";

			pw = new PrintWriter(new BufferedWriter(new FileWriter(
					new File(modifiedParScanPath))));
			pw.write(modifiedPerlScript);
			pw.flush();
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// create prefix for parameter scan results
		String prefix = "";
		String prefixBase = data.getName() + "_";
		boolean found = false;
		Integer prefixId = 0;
		while (found == false) {
			prefixId++;
			String prefixIdStr = prefixId.toString();
			// add zeros before the number
			while (prefixIdStr.length() < 3) {
				prefixIdStr = "0" + prefixIdStr;
			}
			prefix = prefixBase + prefixIdStr;
			prefix = prefix.trim();
			File prefixFolder = new File(resultsFolderPath + tmpSlash
					+ modelName + tmpSlash + "para_scan" + tmpSlash + prefix);
			if (!prefixFolder.isDirectory()) {
				found = true;
				break;
			}
		}
		
		String paraName = data.getName().trim();
		String minV = (data.getMinValue()+"").trim();
		String maxV = (data.getMaxValue()+"").trim();
		String pointsNum = (data.getPointsToScan()+"").trim();
		//String logScale = "false";
		//String steady = "false";
		String logScale = data.isLogScale() ? "true" : "false";
		String steady = data.isSteadyState() ? "true" : "false";
		String simulationTime = (data.getSimulationTime()+"").trim();
		String timePoints = (data.getNumTimePoints()+"").trim();

		// create an executable command
		ArrayList<String> instruction = new ArrayList<String>();
		// Windows
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1) {
			instruction.add("cmd.exe");
			instruction.add("/c");
			instruction.add("perl");
			instruction.add(modifiedParScanPath);
		} 
		
		// OSX
		else 
		{
			instruction.add("perl");
			instruction.add(modifiedParScanPath);
		}

		if (data.isLogScale()) 
		{
			instruction.add("-log");
		}
		if (data.isSteadyState())
		{
			instruction.add("-steady_state");
		}

		instruction.add("-n_steps");
		instruction.add(timePoints);
		instruction.add("-prefix");
		instruction.add(prefix);
		instruction.add("-t_end");
		instruction.add(simulationTime);
		
		// Windows
		//if (BNGEditor.getOstype() == 1) 
		//{
			//instruction.add("/c"+getFilepath() + tmpSlash + getFilename()); //current file path);
		//}
		//else
		//{
			instruction.add(getFilepath() + tmpSlash + getFilename()); //current file path
		//}
		instruction.add(paraName); // parameter name	
		instruction.add(minV); // min value
		instruction.add(maxV); // max value
		instruction.add(pointsNum); // number of points

		String stemp1, stemp2 = "";
		Process pr;
		
		// For some reason, I had to pass a reference to an object of the same type
		// to the toArray method...
		String[] execInstruction = { "stupid?", "stupid?" };
		execInstruction = instruction.toArray(execInstruction);
		/*
		System.out.println("exec:\n\t");
		for (String s : execInstruction)
		{
			System.out.print(s + " ");
		}
		System.out.println();
		*/
		
		// run the process
		try {
			// Windows
			if (ConfigurationManager.getConfigurationManager().getOSType() == 1) {
				// exec bat
				String batfilename = "callBNG-win.bat";
				File batfile = new File(batfilename);
				PrintWriter pwf = new PrintWriter(batfile);
				String modelDisk = getFilepath().substring(0, 1);
				
				pwf.write(modelDisk + ":\n");
				pwf.write("cd " + modelDisk + ":\\" + "\n");
				String cmd_model = getFilepath() + tmpSlash + getFilename();
				
				// eliminate "C:\" three characters
				cmd_model = cmd_model.substring(3);
				cmd_model = convertStyleUsingPOSIX(cmd_model);
				
				// the first two instruction are "cmd.exe" and "\c"
				for (int i = 2; i < instruction.size(); i++) {
					String cur = instruction.get(i);
					if (cur.equals(modifiedParScanPath)) {
						// using " " to support space in directory name
						pwf.write("\"" + modifiedParScanPath + "\"" + " ");
					}
					else if (cur.equals(getFilepath() + tmpSlash + getFilename())) {
						// using POSIX path style
						// using " " to support space in directory name
						pwf.write("\"" + cmd_model + "\"" + " ");
					}
					else {
						pwf.write(cur + " ");
					}
				}
				
				pwf.write("\n");
				pwf.close();
				
				// run bat file
				pr = Runtime.getRuntime().exec(batfilename);
			}
			
			// mac and linux
			else 
			{
				pr = Runtime.getRuntime().exec(execInstruction);
			}
			
			
			BufferedReader br1 = new BufferedReader(
					new InputStreamReader(pr.getInputStream()));
			BufferedReader br2 = new BufferedReader(
					new InputStreamReader(pr.getErrorStream()));
			
			// show the parameters in console
			String parameters = "";
			parameters += "Parameter Name: " + paraName + BNGEditor.getConsoleLineDelimeter();
			parameters += "Minimum Value: " + minV + BNGEditor.getConsoleLineDelimeter();
			parameters += "Maximum Value: " + maxV + BNGEditor.getConsoleLineDelimeter();
			parameters += "Number of Points to Scan: " + pointsNum + BNGEditor.getConsoleLineDelimeter();
			parameters += "Log Scale: " + logScale + BNGEditor.getConsoleLineDelimeter();
			parameters += "Steady State: " + steady + BNGEditor.getConsoleLineDelimeter();
			parameters += "Simulation Time: " + simulationTime + BNGEditor.getConsoleLineDelimeter();
			parameters += "Number of Time Points: " + timePoints + BNGEditor.getConsoleLineDelimeter();
			
			BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+parameters);
			//BNGEditor.consoleText.redraw();
		//	BNGEditor.consoleText.update();
			
			// show running results
			while ((stemp1 = br1.readLine()) != null) {
				stemp2 = stemp2 + stemp1 + BNGEditor.getConsoleLineDelimeter();
				BNGEditor.displayOutput(stemp1 + BNGEditor.getConsoleLineDelimeter());

				//BNGEditor.consoleText.redraw();
				//BNGEditor.consoleText.update();

			}
			//BNGEditor.consoleText.redraw();
			//BNGEditor.consoleText.update();

			while ((stemp1 = br2.readLine()) != null) {
				stemp2 = stemp2 + stemp1 + BNGEditor.getConsoleLineDelimeter();
				BNGEditor.displayOutput(stemp1
						+ BNGEditor.getConsoleLineDelimeter());

				//BNGEditor.consoleText.redraw();
				//BNGEditor.consoleText.update();

			}
			//BNGEditor.consoleText.redraw();
			//BNGEditor.consoleText.update();

			br1.close();
			br2.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// save the console results to log file
		String prefixPath = "";
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1) {
			// when executing the bat file,
			// the working directory has been changed
			prefixPath = getFilepath().substring(0, 2);
		}
		else {
			prefixPath = parentDir;
		}
		File logFile = new File( prefixPath + tmpSlash + prefix
				+ tmpSlash + modelName + "_console.log");
		PrintWriter pw2;
		try {
			pw2 = new PrintWriter(logFile);
			pw2.print(stemp2);
			pw2.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			BNGEditor.displayOutput("Error in Paramater Scan: Aborting"+BNGEditor.getConsoleLineDelimeter());
			return;
		}

		// move new generated files to the para_scan folder
		java.io.File srcFolder = new java.io.File(prefixPath + tmpSlash
				+ prefix + tmpSlash);
		java.io.File destFolder = new java.io.File(resultsFolderPath
				+ tmpSlash + modelName + tmpSlash + "para_scan"
				+ tmpSlash);

		// SCAN file
		java.io.File scanfile = new java.io.File(prefixPath + tmpSlash
				+ prefix + ".scan");
		java.io.File newScanFile = new java.io.File(destFolder
				+ tmpSlash + scanfile.getName());
		try {
			copyUseChannel(scanfile, newScanFile);
		} catch (IOException e) {
			System.out.println("Error moving files.");
			e.printStackTrace();
		}
		System.gc();
		scanfile.delete();

		// folder named with 'prefix'
		java.io.File[] files = srcFolder.listFiles();

		for (int i = 0; i < files.length; i++) {
			java.io.File tfile = files[i];

			// create the prefix folder
			java.io.File prefixFolder = new java.io.File(destFolder
					+ tmpSlash + prefix);
			if (!prefixFolder.isDirectory()) {
				prefixFolder.mkdir();
			}

			// move files
			java.io.File newFile = new java.io.File(destFolder
					+ tmpSlash + prefix + tmpSlash + tfile.getName());
			try {
				copyUseChannel(tfile, newFile);
			} catch (IOException e) {
				System.out.println("Error moving files.");
				e.printStackTrace();
			}
			System.gc();
			tfile.delete();
		}

		srcFolder.delete();

		
		// results ========================================================
		Rectangle bounds = new Rectangle(0, 0, 600, 600);

		if (resViewer == null || resViewer.getShell() == null) {

			// create a result viewer
			resViewer = new ResultViewer(resultsFolderPath, modelName
					+ "\t" + "para_scan" + "\t" + prefix, ".scan",
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
	
			resViewer.openDefaultFile(modelName + "\t" + "para_scan"
					+ "\t" + prefix, ".scan");
		}
	}

	public void runbng(boolean viewresults) 
	{
		BNGEditor.displayOutput("Running BioNetGen Simulator...");
		// the bngfpath or bngfname can not be empty
		if (ConfigurationManager.getConfigurationManager().getBNGFPath() == null || ConfigurationManager.getConfigurationManager().getBNGFName() == null) {
			BNGEditor.displayOutput("Error finding BNG.");
			(new SetBNGPathDialogue(BNGEditor.getMainEditorShell(), BNGEditor.getEditor())).show();
		} 
		else {
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
		
			String stemp1, stemp2 = "";
			// file path and file name
			String str1 = getFilepath(), str2 = getFilename();
			String regex1 = "at line (\\d+) of file";
			Pattern p1 = Pattern.compile(regex1);
			Matcher m;
			Boolean result;
			int ti;

			BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter());

			String curTime = getCurrentDateAndTime(); // get current time

			// workspace path
			String workspacePath = ConfigurationManager.getConfigurationManager().getWorkspacePath();
			//String resultsFolderPath = parentDir + tmpSlash + "BNGResults";
			String resultsFolderPath = workspacePath + tmpSlash + "BNGResults";
			
			// file names in the current directory with the same name of model
			String[] fnames;
			
			// bng output files path list
			// read from BNG output because the directory is not fixed;
			// sometimes it depends on whether the user specify "Prefix" in the BNGL file
			ArrayList<String> outputPaths = new ArrayList<String>();
			// bng read-from files path list
			// which should be copy to the results directory and also keep in original directory
			ArrayList<String> inputPaths = new ArrayList<String>();

			try {
				// Run BioNetGen
				Process p;
				
				// run command
				ArrayList<String> runcommand = new ArrayList<String>();
				
				if (ostype == 1) {
					runcommand.add("cmd");
					runcommand.add("/c");
				}

				runcommand.add("perl");
				runcommand.add(ConfigurationManager.getConfigurationManager().getBNGFPath()+tmpSlash+ConfigurationManager.getConfigurationManager().getBNGFName());

				if (viewresults == false) {
					// Check model, not run
					runcommand.add("-check");
				}
				
				runcommand.add(getFilepath() + tmpSlash + getFilename());		

				String[] command = { "tmp", "tmp" };
				command = runcommand.toArray(command);
				
				// run the process
				if (ostype == 1) {
					// on Windows
					// write commands to a batch file and then execute the batch file
					String batfilename = "callBNG-win.bat";
					File batfile = new File(batfilename);
					PrintWriter pw = new PrintWriter(batfile);
					// disk name (C, or D, or ...)
					String modelDisk = getFilepath().substring(0, 1);
					
					pw.write(modelDisk + ":\n");
					pw.write("cd " + modelDisk + ":\\" + "\n");
					String cmd_bng = ConfigurationManager.getConfigurationManager().getBNGFPath()+tmpSlash+ConfigurationManager.getConfigurationManager().getBNGFName();
					String cmd_model = getFilepath() + tmpSlash + getFilename();
					
					// eliminate "C:\" three characters
					cmd_model = cmd_model.substring(3);
					cmd_model = convertStyleUsingPOSIX(cmd_model);
					// using " " to support space in directory name
					pw.write("perl " + "\"" + cmd_bng + "\"" + " " + "\"" + cmd_model + "\"" + "\n");
					pw.close();
					
					// run bat file
					p = Runtime.getRuntime().exec(batfilename);
				} else {
					// run command
					p = Runtime.getRuntime().exec(command);
				}

				// display the output of BNG
				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						p.getErrorStream()));

				while ((stemp1 = br.readLine()) != null) {
					stemp2 = stemp2 + stemp1 + BNGEditor.getConsoleLineDelimeter();
					BNGEditor.displayOutput(stemp1
							+ BNGEditor.getConsoleLineDelimeter());
					
					// add bng output file path to path list if not exists
					String pathstr = bngOutputFilePathExtractor(stemp1);
					if (!pathstr.equals("") && !outputPaths.contains(pathstr)) {
						// relative path
						if (pathstr.indexOf(tmpSlash) == -1) {
							pathstr = workspacePath + tmpSlash + pathstr;
						}
						outputPaths.add(pathstr);
					}
					
					// add bng input file path to path list if not exists
					pathstr = bngInputFilePathExtractor(stemp1);
					if (!pathstr.equals("") && !inputPaths.contains(pathstr)) {
						// relative path
						if (pathstr.indexOf(tmpSlash) == -1) {
							pathstr = workspacePath + tmpSlash + pathstr;
						}
						inputPaths.add(pathstr);
					}

					//BNGEditor.consoleText.redraw();
					//BNGEditor.consoleText.update();

				}
				//BNGEditor.consoleText.redraw();
				//BNGEditor.consoleText.update();

				while ((stemp1 = br2.readLine()) != null) {
					stemp2 = stemp2 + stemp1 + BNGEditor.getConsoleLineDelimeter();
					BNGEditor.displayOutput(stemp1
							+ BNGEditor.getConsoleLineDelimeter());

					//BNGEditor.consoleText.redraw();
					//BNGEditor.consoleText.update();

				}
				//BNGEditor.consoleText.redraw();
				//BNGEditor.consoleText.update();

				br.close();
				br2.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			// transfer the results to the Viewer
			m = p1.matcher(stemp2);
			result = m.find();
			if (result) {
				ti = Integer.parseInt(m.group(1));
				getTextarea().textarea.setFocus();
				getTextarea().selectErrorLine(ti - 1);
			} 
			
			if (viewresults){
				
				// get the model name
				String modelName = str2;
				if (modelName.lastIndexOf('.') != -1) {
					modelName = modelName.substring(0,
							modelName.lastIndexOf('.'));
				}

				// save the console results to log file
				File logFile = new File(str1 + tmpSlash + modelName
						+ "_console.log");
				PrintWriter pw;
				try {
					pw = new PrintWriter(logFile);
					pw.print(stemp2);
					pw.close();
					
					// add log file to output files list
					outputPaths.add(logFile.getAbsolutePath());

					// resultFolder
					java.io.File resultsFolder = new java.io.File(
							resultsFolderPath);
					if (!resultsFolder.isDirectory()) {
						resultsFolder.mkdir();
					}

					// create a folder with the name of model if not exists
					java.io.File modelFolder = new java.io.File(
							resultsFolderPath + tmpSlash + modelName + tmpSlash);
					if (!modelFolder.isDirectory()) {
						modelFolder.mkdir();
					}

					// create a new folder by the current time
					java.io.File timefolder = new java.io.File(
							resultsFolderPath + tmpSlash + modelName + tmpSlash
									+ curTime + tmpSlash);
					if (!timefolder.isDirectory()) {
						timefolder.mkdir();
					}
					

					// move output files to new directory
					for (int i = 0; i < outputPaths.size(); i++) {
						File tfile = new File(outputPaths.get(i));
						String fileName = tfile.getName();
						File newFile = new File(resultsFolderPath + tmpSlash
								+ modelName + tmpSlash + curTime + tmpSlash
								+ fileName);
						try {
							// copy files to new directory
							copyUseChannel(tfile, newFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.gc();

						// delete all result files in current directory
						tfile.delete();
					}

					// copy input files to new directory
					for (int i = 0; i < inputPaths.size(); i++) {
						File tfile = new File(inputPaths.get(i));
						String fileName = tfile.getName();
						File newFile = new File(resultsFolderPath + tmpSlash
								+ modelName + tmpSlash + curTime + tmpSlash
								+ fileName);
						try {
							// copy files to new directory
							copyUseChannel(tfile, newFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.gc();
					}
					
					
					// move other files with the prefix of model name to new directory
					fnames = new java.io.File(str1 + tmpSlash)
							.list(new Resultfilefilter(modelName));
					for (int i = 0; i < fnames.length; i++) {
						File tfile = new File(str1 + tmpSlash + fnames[i]);
						String fileName = tfile.getName();
						File newFile = new File(resultsFolderPath + tmpSlash
								+ modelName + tmpSlash + curTime + tmpSlash
								+ fileName);
						try {
							copyUseChannel(tfile, newFile);
						} catch (IOException e) {
							e.printStackTrace();
						}
						System.gc();
						if (!tfile.getName().endsWith(".bngl") && !tfile.getName().endsWith(".net")) {
							tfile.delete();
						}
					}
					
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
				
				Rectangle bounds = new Rectangle(0, 0, 600, 600);

				if (resViewer == null || resViewer.getShell() == null) {
					// create a result viewer
					resViewer = new ResultViewer(resultsFolderPath, modelName
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
	
		// If some quality of the textarea is not valid then do not display
		// the cmap
		// -ams 5/1/10
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
			
			if (getTextarea().blockinfo[2].valid
					&& getTextarea().doc
							.get(getTextarea().blockinfo[2].caretbegin2,
									getTextarea().blockinfo[2].caretend1
											- getTextarea().blockinfo[2].caretbegin2)
							.trim().length() != 0) {
				block2valid = true;
			}
			
			if (getTextarea().blockinfo[3].valid
					&& getTextarea().doc
							.get(getTextarea().blockinfo[3].caretbegin2,
									getTextarea().blockinfo[3].caretend1
											- getTextarea().blockinfo[3].caretbegin2)
							.trim().length() != 0) {
				block3valid = true;
			}
			
			if (getTextarea().blockinfo[4].valid
					&& getTextarea().doc
							.get(getTextarea().blockinfo[4].caretbegin2,
									getTextarea().blockinfo[4].caretend1
											- getTextarea().blockinfo[4].caretbegin2)
							.trim().length() != 0) {
				block4valid = true;
			}
			
			if (getTextarea().blockinfo[6].valid
					&& getTextarea().doc
							.get(getTextarea().blockinfo[6].caretbegin2,
									getTextarea().blockinfo[6].caretend1
											- getTextarea().blockinfo[6].caretbegin2)
							.trim().length() != 0) {
				block6valid = true;
			}
			
			if (block2valid) 
			{
			
				cModel = new CMapModel(getTextarea().doc.get(
						getTextarea().blockinfo[2].caretbegin2,
						getTextarea().blockinfo[2].caretend1
								- getTextarea().blockinfo[2].caretbegin2).trim(),true);
				
				//compartments info
				if (block3valid) {
					cModel.addCompartmentsInfo(getTextarea().doc.get(
								getTextarea().blockinfo[3].caretbegin2,
								getTextarea().blockinfo[3].caretend1
										- getTextarea().blockinfo[3].caretbegin2)
								.trim());
					
					// add seed species info to parse initial compartments
					if (block4valid) {
						cModel.addSeedSpeciesInfo(getTextarea().doc.get(
								getTextarea().blockinfo[4].caretbegin2,
								getTextarea().blockinfo[4].caretend1
										- getTextarea().blockinfo[4].caretbegin2).trim());
					}
					
				}
				
				// add rules info
				if (block6valid){
				cModel.addRulesInfo(getTextarea().doc.get(
						getTextarea().blockinfo[6].caretbegin2,
						getTextarea().blockinfo[6].caretend1
								- getTextarea().blockinfo[6].caretbegin2)
						.trim());
				}
				
			} 
			else if (block4valid) 
			{
				cModel = new CMapModel(getTextarea().doc.get(
						getTextarea().blockinfo[4].caretbegin2,
						getTextarea().blockinfo[4].caretend1
								- getTextarea().blockinfo[4].caretbegin2).trim(), false);
				
				//compartments info
				if (block3valid) {
					cModel.addCompartmentsInfo(getTextarea().doc.get(
								getTextarea().blockinfo[3].caretbegin2,
								getTextarea().blockinfo[3].caretend1
										- getTextarea().blockinfo[3].caretbegin2)
								.trim());
					
				}
				
				// add rules info
				if (block6valid) {
				cModel.addRulesInfo(getTextarea().doc.get(
						getTextarea().blockinfo[6].caretbegin2,
						getTextarea().blockinfo[6].caretend1
								- getTextarea().blockinfo[6].caretbegin2)
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
			if (getTextarea().blockinfo[2].valid
					&& getTextarea().doc
							.get(getTextarea().blockinfo[2].caretbegin2,
									getTextarea().blockinfo[2].caretend1
											- getTextarea().blockinfo[2].caretbegin2)
							.trim().length() != 0) {
				block2valid = true;
			}
			
			if (getTextarea().blockinfo[4].valid
					&& getTextarea().doc
							.get(getTextarea().blockinfo[4].caretbegin2,
									getTextarea().blockinfo[4].caretend1
											- getTextarea().blockinfo[4].caretbegin2)
							.trim().length() != 0) {
				block4valid = true;
			}
			
			if (getTextarea().blockinfo[6].valid
					&& getTextarea().doc
							.get(getTextarea().blockinfo[6].caretbegin2,
									getTextarea().blockinfo[6].caretend1
											- getTextarea().blockinfo[6].caretbegin2)
							.trim().length() != 0) {
				block6valid = true;
			}
				
			if (block2valid) {
				if (block6valid) {
					iModel = new IMapModel(getTextarea().doc.get(
							getTextarea().blockinfo[2].caretbegin2,
							getTextarea().blockinfo[2].caretend1
									- getTextarea().blockinfo[2].caretbegin2).trim(),
							getTextarea().doc.get(
									getTextarea().blockinfo[6].caretbegin2,
									getTextarea().blockinfo[6].caretend1
											- getTextarea().blockinfo[6].caretbegin2)
									.trim(), true);
				}
				else {
					BNGEditor.displayOutput(BNGEditor.getConsoleLineDelimeter()+"Reaction rule block not valid, Influence Graph NOT shown.");
				}
			}
			else if (block4valid) {
				if (block6valid) {
					iModel  = new IMapModel(getTextarea().doc.get(
							getTextarea().blockinfo[4].caretbegin2,
							getTextarea().blockinfo[4].caretend1
									- getTextarea().blockinfo[4].caretbegin2).trim(),
							getTextarea().doc.get(
									getTextarea().blockinfo[6].caretbegin2,
									getTextarea().blockinfo[6].caretend1
											- getTextarea().blockinfo[6].caretbegin2)
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
	
	private String convertStyleUsingPOSIX(String path) {
		String results = "";
		for (int i = 0; i < path.length(); i++) {
			if (path.charAt(i) == '\\') {
				results += '/';
			}
			else {
				results += path.charAt(i);
			}
		}
		return results;
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
			// If the model was not created, then show the console so that
			// the user can see the error. 
			BNGEditor.getEditor().focusConsole();
		}
		
		// If there was no error producing the cmap.
		else
		{
			// Tell the visualization view controller that there was a new model created.
			// The controller will then call the methods to generate the visual model 
			// if it is necessary at this time.
			VisualizationViewerController.loadVisualizationViewController().informOfNewCMapModel();
		}
		
		// There could have been errors in the imap.
		if(iModel == null)
		{
			BNGEditor.getEditor().focusConsole();
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
		return getFilename();
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

	public String getFilename() {
		return filename;
	}

	public void setTextarea(BNGTextArea textarea) {
		this.textarea = textarea;
	}

	public BNGTextArea getTextarea() {
		return textarea;
	}
}