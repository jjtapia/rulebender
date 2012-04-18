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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import resultviewer.ui.ResultViewer;

import editor.contactmap.CMap;
import editor.influencegraph.IMap;

class CurrentFile {
	String filename;
	String filepath;// halfpath
	String prevcontent;
	BNGTextArea textarea;
	CTabItem tabitem;
	String ss1;// content in file
	String ss2;// content in editor
	int ostype; // 1:windows 2:man 3:other

	Shell parscanShell;
	Text parscantext1;
	Text parscantext2;
	Text parscantext3;
	Text parscantext4;
	Text parscantext5;
	Text parscantext6;
	Button parscancheckBox1;
	Button parscancheckBox2;

	ParScanInput lastpsinput = new ParScanInput();
	boolean firstparscan = true;

	// result viewer
	private ResultViewer resViewer;

	CurrentFile(String fpath, String fname, int inostype, boolean empty) {
		ostype = inostype;
		if (fpath == null) {
			filepath = null;
			
			// add suffix for the file
			if (!(fname.endsWith(".bngl") || fname.endsWith(".BNGL"))) {
				fname += ".bngl";
			}
			filename = fname;
		
			BNGEditor.showfilepath.setText("");
			if (empty) {
				tabitem = new CTabItem(BNGEditor.textfolder, SWT.CLOSE);
				textarea = new BNGTextArea(BNGEditor.textfolder, tabitem, SWT.NONE);
				tabitem.setControl(textarea);
				tabitem.setText("*"+filename);
				textarea.textarea.setText("");
				ss1 = textarea.doc.get();
				textarea.textchanges.remove(0);
				textarea.textarea.setFocus();
			} else {
				tabitem = new CTabItem(BNGEditor.textfolder, SWT.CLOSE);
				File tempfile = new File("Template");
				textarea = new BNGTextArea(BNGEditor.textfolder, tabitem, SWT.NONE);
				tabitem.setControl(textarea);
				tabitem.setText("*"+filename);
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(tempfile));
					String str = "";
					try {
						while (br.ready()) {
							str = str + br.readLine()
									+ textarea.textarea.getLineDelimiter();
						}
						br.close();
						textarea.textarea.setText("");
						textarea.textarea.append(str);
						textarea.textchanges.remove(0);
						textarea.textchanges.remove(0);
						ss1 = textarea.doc.get();
					} catch (IOException e) {
					}
				} catch (FileNotFoundException e) {
				}
				textarea.textarea.setFocus();
			}
		} else {
			filepath = fpath;
			filename = fname;
			tabitem = new CTabItem(BNGEditor.textfolder, SWT.CLOSE);
			textarea = new BNGTextArea(BNGEditor.textfolder, tabitem, SWT.NONE);
			tabitem.setControl(textarea);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(new File(fpath, fname)));
				String str = "";
				try {
					while (br.ready()) {
						str = str + br.readLine()
								+ textarea.textarea.getLineDelimiter();
					}
					br.close();
					textarea.textarea.setText("");
					textarea.textarea.append(str);
					textarea.textchanges.remove(0);
					textarea.textchanges.remove(0);
					ss1 = textarea.doc.get();
					tabitem.setText(filename);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			textarea.textarea.setFocus();
		}
		textarea.textarea.setFocus();
	}

	void saveas() {
		boolean tosave = false;
		String fpath = "";
		String fname = "";
		File file = null;

		FileDialog savediag = new FileDialog(BNGEditor.sShell, SWT.SAVE);
		savediag.setFilterExtensions(new String[] { "*.bngl", "*.txt", "*.*" });
		String tempstr = filename;
		if (tempstr.indexOf('.') != -1
				&& (tempstr.endsWith("bngl") || tempstr.endsWith("txt")))
			tempstr = tempstr.substring(0, tempstr.indexOf('.'));
		savediag.setFileName(tempstr);
		savediag.open();
		fpath = savediag.getFilterPath();
		fname = savediag.getFileName();
		if (fpath.equals("") || fpath == null)
			return;

		if (!(fname.endsWith(".bngl") || fname.endsWith(".BNGL"))) {
			fname += ".bngl";
		}

		// check if the file has already exists
		file = new File(fpath, fname);

		if (file.exists()) {
			MessageBox replaceMsg = new MessageBox(BNGEditor.sShell,
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
						new FileWriter(new File(fpath, fname))));
				pw.write(textarea.doc.get());
				pw.flush();
				pw.close();

				// open the new saved file, close the original one
				CurrentFile newF = new CurrentFile(fpath, fname, this.ostype,
						true);
				newF.setResViewer(this.getResViewer());
				tabitem.setText(fname);
				BNGEditor.replaceInputfiles(this, newF);

			} catch (Exception esave) {
				esave.printStackTrace();
			}
		}
	}

	void save() {
		if (filepath == null) {
			boolean tosave = false;
			String fpath = "";
			String fname = "";
			File file = null;

			FileDialog savediag = new FileDialog(BNGEditor.sShell, SWT.SAVE);
			savediag.setFilterExtensions(new String[] { "*.bngl", "*.txt",
					"*.*" });
			String tempstr = filename;
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
				MessageBox replaceMsg = new MessageBox(BNGEditor.sShell,
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
					pw.write(textarea.doc.get());
					pw.flush();
					pw.close();
					ss1 = textarea.doc.get();

					filepath = fpath;
					filename = fname;
					if (ostype == 1) {
						BNGEditor.showfilepath.setText(filepath + "\\"
								+ filename);
					} else {
						BNGEditor.showfilepath.setText(filepath + "/"
								+ filename);
					}
					// change the name of the file on the tab
					tabitem.setText(filename);

				} catch (Exception esave) {
					esave.printStackTrace();
				}
			}
			
		} else {
			PrintWriter pw;
			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(
						new File(filepath, filename))));
				if (!textarea.doc.get().endsWith(
						textarea.doc.getDefaultLineDelimiter()))
					pw.write(textarea.doc.get()
							+ textarea.doc.getDefaultLineDelimiter());
				else
					pw.write(textarea.doc.get());
				pw.flush();
				pw.close();
				// eliminate the star mark on the tab
				tabitem.setText(filename);
				ss1 = textarea.doc.get();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	void close() {
		ss2 = textarea.doc.get();
		if (ss1 != null && ss2 != null && ss1.equals(ss2))
			return;
		MessageBox mb = new MessageBox(BNGEditor.sShell, SWT.ICON_QUESTION
				| SWT.YES | SWT.NO);
		mb.setText("Save before close ?");
		mb.setMessage("File " + filename
				+ " has been changed. Save before close ?");

		if (mb.open() == SWT.YES) {
			save();
		}
	}

	void check() {
		if (!(filename.endsWith(".bngl") || filename.endsWith(".BNGL"))) {
			BNGEditor.console.setText("Please Open a Model With Suffix \".bngl\".");
			return;
		}
		BNGEditor.console.setText("");
		String ss2 = textarea.doc.get();
		if (!ss1.equals(ss2) || filepath == null) {
			MessageBox mb = new MessageBox(BNGEditor.sShell, SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Save before Check ?");
			mb.setMessage("File "
					+ filename
					+ " has been changed. File must be saved before Check. Save before Check ?");
			if (mb.open() == SWT.YES) {
				save();
				ss2 = textarea.doc.get();
				if (ss1.equals(ss2) && filepath != null)
					runbng(false);
				else
					BNGEditor.console
							.setText("File Save NOT Successful, Model Not Checked.");
			} else
				BNGEditor.console.setText("Model NOT Checked.");
		} else {
			runbng(false);
		}
	}

	void run() {
		if (!(filename.endsWith(".bngl") || filename.endsWith(".BNGL"))) {
			BNGEditor.console.setText("Please Open a Model With Suffix \".bngl\".");
			return;
		}
		BNGEditor.console.setText("");
		String ss2 = textarea.doc.get();
		if (!ss1.equals(ss2) || filepath == null) {
			MessageBox mb = new MessageBox(BNGEditor.sShell, SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Save before Run ?");
			mb.setMessage("File "
					+ filename
					+ " has been changed. File must be saved before Run. Save before Run ?");
			if (mb.open() == SWT.YES) {
				save();
				ss2 = textarea.doc.get();
				if (ss1.equals(ss2) && filepath != null)
					runbng(true);
				else
					BNGEditor.console
							.setText("File Save NOT Successful, Model Not Runned.");
			} else
				BNGEditor.console.setText("Model NOT Runned.");
		} else {
			runbng(true);
		}
	}

	/*
	 * Parameter Scan Modified on July 26th 2010
	 */

	void parscan() {
		if (!(filename.endsWith(".bngl") || filename.endsWith(".BNGL"))) {
			BNGEditor.console.setText("Please Open a Model With Suffix \".bngl\".");
			return;
		}
		
		BNGEditor.console.setText("");
		String ss2 = textarea.doc.get();
		if (!ss1.equals(ss2) || filepath == null) {
			MessageBox mb = new MessageBox(BNGEditor.sShell, SWT.ICON_QUESTION
					| SWT.YES | SWT.NO);
			mb.setText("Save before Parameter Scan ?");
			mb.setMessage("File "
					+ filename
					+ " has been changed. File must be saved before Parameter Scan. Save before Parameter Scan ?");
			if (mb.open() == SWT.YES) {
				save();
			} else {
				BNGEditor.console.setText("Parameter Scan NOT Runned.");
				return;
			}
		}

		ss2 = textarea.doc.get();
		if (!ss1.equals(ss2) || filepath == null) {
			BNGEditor.console
					.setText("File Save NOT Successful, Parameter Scan Not Runned.");
			return;
		}
		save();

		// parameter scan
		parscanShell = new Shell(BNGEditor.sShell, SWT.DIALOG_TRIM
				| SWT.APPLICATION_MODAL);
		parscanShell.setText("Parameter Scan");
		// grid layout
		parscanShell.setLayout(new GridLayout(5, true));

		GridData labelData = new GridData(GridData.FILL_HORIZONTAL);
		labelData.horizontalSpan = 4;

		GridData textData = new GridData(GridData.FILL_HORIZONTAL);

		// parameter name
		Label parscanlabel1 = new Label(parscanShell, SWT.NONE);
		parscanlabel1.setText("  Parameter Name (Alphanumeric Word)");
		parscanlabel1.setLayoutData(labelData);

		parscantext1 = new Text(parscanShell, SWT.BORDER);
		parscantext1.setLayoutData(textData);

		// min value
		Label parscanlabel2 = new Label(parscanShell, SWT.NONE);
		parscanlabel2.setText("  Parameter Min Value (Real Number)");
		parscanlabel2.setLayoutData(labelData);

		parscantext2 = new Text(parscanShell, SWT.BORDER);
		parscantext2.setLayoutData(textData);

		// max value
		Label parscanlabel3 = new Label(parscanShell, SWT.NONE);
		parscanlabel3
				.setText("  Parameter Max Value (Real Number > Min Value)");
		parscanlabel3.setLayoutData(labelData);

		parscantext3 = new Text(parscanShell, SWT.BORDER);
		parscantext3.setLayoutData(textData);

		// number of points
		Label parscanlabel4 = new Label(parscanShell, SWT.NONE);
		parscanlabel4.setText("  Number of Points to Scan (Positive Integer)");
		parscanlabel4.setLayoutData(labelData);

		parscantext4 = new Text(parscanShell, SWT.BORDER);
		parscantext4.setLayoutData(textData);

		// log scale
		Label parscanlabel5 = new Label(parscanShell, SWT.NONE);
		parscanlabel5
				.setText("  Log Scale ? (Error if checked & Min Value <= 0)");
		parscanlabel5.setLayoutData(labelData);

		parscancheckBox1 = new Button(parscanShell, SWT.CHECK);

		// steady state
		Label parscanlabel6 = new Label(parscanShell, SWT.NONE);
		parscanlabel6.setText("  Steady State ?");
		parscanlabel6.setLayoutData(labelData);

		parscancheckBox2 = new Button(parscanShell, SWT.CHECK);

		// simulation time
		Label parscanlabel7 = new Label(parscanShell, SWT.NONE);
		parscanlabel7.setText("  Simulation Time (Positive Real Number)");
		parscanlabel7.setLayoutData(labelData);

		parscantext5 = new Text(parscanShell, SWT.BORDER);
		parscantext5.setLayoutData(textData);

		// number of time points
		Label parscanlabel8 = new Label(parscanShell, SWT.NONE);
		parscanlabel8.setText("  Number of Time Points (Positive Integer)");
		parscanlabel8.setLayoutData(labelData);

		parscantext6 = new Text(parscanShell, SWT.BORDER);
		parscantext6.setLayoutData(textData);


		// empty label to contro layout
		new Label(parscanShell, SWT.NONE).setText("");
		new Label(parscanShell, SWT.NONE).setText("");
		new Label(parscanShell, SWT.NONE).setText("");

		// OK button
		Button parscanbutton1 = new Button(parscanShell, SWT.NONE);
		parscanbutton1.setText("OK");
		GridData gridData = new GridData();
		gridData.widthHint = 80;
		parscanbutton1.setLayoutData(gridData);

		// Cancel Button
		Button parscanbutton2 = new Button(parscanShell, SWT.NONE);
		parscanbutton2.setText("Cancel");
		parscanbutton2.setLayoutData(gridData);

		// pack
		parscanShell.pack();

		if (!firstparscan) {
			parscantext1.setText(lastpsinput.par1);
			parscantext2.setText(lastpsinput.par2);
			parscantext3.setText(lastpsinput.par3);
			parscantext4.setText(lastpsinput.par4);
			parscantext5.setText(lastpsinput.par7);
			parscantext6.setText(lastpsinput.par8);
			if (lastpsinput.par5)
				parscancheckBox1.setSelection(true);
			if (lastpsinput.par6)
				parscancheckBox2.setSelection(true);
		}

		// add listener for OK button
		parscanbutton1.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				boolean verified = true;
				try {
					if (parscantext1.getText().trim().length() == 0)
						verified = false;
					if (Float.parseFloat(parscantext3.getText().trim()) <= Float
							.parseFloat(parscantext2.getText().trim()))
						verified = false;
					if (Integer.parseInt(parscantext4.getText().trim()) <= 0)
						verified = false;
					if (Float.parseFloat(parscantext2.getText().trim()) <= 0
							&& parscancheckBox1.getSelection())
						verified = false;
					if (Float.parseFloat(parscantext5.getText().trim()) <= 0)
						verified = false;
					if (Integer.parseInt(parscantext6.getText().trim()) <= 0)
						verified = false;
				} catch (NumberFormatException e) {
					verified = false;
				}

				if (!verified) {
					MessageBox mb = new MessageBox(BNGEditor.sShell,
							SWT.ICON_INFORMATION);
					mb.setText("Error Info");
					mb.setMessage("There exists invalid arguments, please check again !");
					mb.open();
					return;
				} else {
					firstparscan = false;
					lastpsinput.par1 = parscantext1.getText();
					lastpsinput.par2 = parscantext2.getText();
					lastpsinput.par3 = parscantext3.getText();
					lastpsinput.par4 = parscantext4.getText();
					lastpsinput.par7 = parscantext5.getText();
					lastpsinput.par8 = parscantext6.getText();
					lastpsinput.par5 = parscancheckBox1.getSelection();
					lastpsinput.par6 = parscancheckBox2.getSelection();
				}

				if (BNGEditor.bngfpath == null || BNGEditor.bngfname == null) {
					parscanShell.dispose();
					MessageBox mb = new MessageBox(BNGEditor.sShell,
							SWT.ICON_INFORMATION);
					mb.setText("Error Info");
					mb.setMessage("Error finding BNG, Please Set BNG Path");
					mb.open();
					BNGEditor.setbngpath();
					return;
				}
				
				// slash for different operating system
				String tmpSlash = "";
				if (ostype == 1) {
					tmpSlash = "\\";
				} else {
					tmpSlash = "/";
				}
				
				File bngfile = new File(BNGEditor.bngfpath + tmpSlash + BNGEditor.bngfname);
				if (!bngfile.exists()) {
					MessageBox mb = new MessageBox(BNGEditor.sShell,
							SWT.ICON_INFORMATION);
					mb.setText("Error Info");
					mb.setMessage("Error finding BNG, Please Set BNG Path");
					mb.open();
					BNGEditor.setbngpath();
					bngfile = null;
					return;
				}
				bngfile = null;
				
				String tempstr = "", tempstr1;
				try {
					BufferedReader br = new BufferedReader(new FileReader(
							new File(BNGEditor.bngfpath, "scan_var.pl")));
					try {
						while (br.ready()) {
							tempstr = tempstr + br.readLine()
									+ textarea.textarea.getLineDelimiter();
						}
					} catch (IOException e) {
					}
				} catch (FileNotFoundException e) {
				}
				Pattern p = Pattern
						.compile("exec\\s*=.*(\".*BNGPATH.*\\.[Pp][Ll]\\s*\")");
				Matcher m = p.matcher(tempstr);
				if (m.find()) {
					if (ostype == 1) {
						tempstr1 = "\"";
						String strtemp = BNGEditor.bngfpath;
						while (strtemp.indexOf('\\') != -1) {
							tempstr1 = tempstr1
									+ strtemp.substring(0,
											strtemp.indexOf('\\')) + "\\\\";
							strtemp = strtemp.substring(
									strtemp.indexOf('\\') + 1, strtemp.length());
						}
						tempstr1 = tempstr1 + strtemp + "\\\\"
								+ BNGEditor.bngfname + "\"";
					} else
						tempstr1 = "\"" + BNGEditor.bngfpath + "/"
								+ BNGEditor.bngfname + "\"";
					tempstr = tempstr.substring(0, tempstr.indexOf(m.group(1)))
							+ tempstr1
							+ tempstr.substring(tempstr.indexOf(m.group(1))
									+ m.group(1).length(), tempstr.length());
				}

				// get the model name
				String modelName = filename;
				if (modelName.lastIndexOf('.') != -1) // Returns the index
														// within this string of
														// the last occurrence
														// of the specified
														// character.
					modelName = modelName.substring(0,
							modelName.lastIndexOf('.'));

				

				// parentDir
				String parentDir = System.getProperty("user.dir");
				String resultsFolderPath = parentDir + tmpSlash + "BNGResults";
				if (ostype == 2) {
					// the release application on Mac
					String appDir = "";
					if (parentDir.indexOf("/Contents/Resources") != -1) {
						appDir = parentDir.substring(0, parentDir.indexOf("/Contents/Resources"));
						appDir = appDir.substring(0, appDir.lastIndexOf("/"));
						resultsFolderPath = appDir + tmpSlash + "BNGResults";
					}	
				}

				// resultsFolder
				File resultsFolder = new File(resultsFolderPath + tmpSlash);
				if (!resultsFolder.isDirectory()) {
					resultsFolder.mkdir();
				}

				// path for ModifiedParScan.pl
				String modifiedParScanPath = "";

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
					pw.write(tempstr);
					pw.flush();
					pw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				// create prefix for parameter scan results
				String prefix = "";
				String prefixBase = parscantext1.getText() + "_";
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
				String paraName = parscantext1.getText().trim();
				String minV = parscantext2.getText().trim();
				String maxV = parscantext3.getText().trim();
				String pointsNum = parscantext4.getText().trim();
				String logScale = "false";
				String steady = "false";
				String simulationTime = parscantext5.getText().trim();
				String timePoints = parscantext6.getText().trim();

				// create an executable command
				ArrayList<String> instruction = new ArrayList<String>();
				// Windows
				if (ostype == 1) {
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

				if (parscancheckBox1.getSelection()) {
					instruction.add("-log");
					logScale = "true";
				}
				if (parscancheckBox2.getSelection()) {
					instruction.add("-steady_state");
					steady = "true";
				}

				instruction.add("-n_steps");
				instruction.add(timePoints);
				instruction.add("-prefix");
				instruction.add(prefix);
				instruction.add("-t_end");
				instruction.add(simulationTime);
				instruction.add(filepath + tmpSlash + filename);
				instruction.add(paraName);
				instruction.add(minV);
				instruction.add(maxV);
				instruction.add(pointsNum);

				// close the shell
				parscanShell.dispose();
				String stemp1, stemp2 = "";
				Process pr;

				String[] execInstruction = { "why", "why" };
				execInstruction = instruction.toArray(execInstruction);

				System.out.println("execInstruction: ");

				for (String s : execInstruction)
					System.out.print(s + " ");
				System.out.println();

				try {
					// run the process
					if (ostype == 1) {
						// exec bat
						String batfilename = "callBNG-win.bat";
						File batfile = new File(batfilename);
						PrintWriter pwf = new PrintWriter(batfile);
						String modelDisk = filepath.substring(0, 1);
						
						pwf.write(modelDisk + ":\n");
						pwf.write("cd " + modelDisk + ":\\" + "\n");
						String cmd_model = filepath + tmpSlash + filename;
						
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
							else if (cur.equals(filepath + tmpSlash + filename)) {
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
					} else {
						// run command
						pr = Runtime.getRuntime().exec(execInstruction);
					}
					
					
					BufferedReader br1 = new BufferedReader(
							new InputStreamReader(pr.getInputStream()));
					BufferedReader br2 = new BufferedReader(
							new InputStreamReader(pr.getErrorStream()));
					
					// show the parameters in console
					String parameters = "";
					parameters += "Parameter Name: " + paraName + "\n";
					parameters += "Minimum Value: " + minV + "\n";
					parameters += "Maximum Name: " + maxV + "\n";
					parameters += "Number of Points to Scan: " + pointsNum + "\n";
					parameters += "Log Scale: " + logScale + "\n";
					parameters += "Steady State: " + steady + "\n";
					parameters += "Simulation Time: " + simulationTime + "\n";
					parameters += "Number of Time Points: " + timePoints + "\n";
					
					BNGEditor.console.append(parameters);
					BNGEditor.console.redraw();
					BNGEditor.console.update();
					
					// show running results
					while ((stemp1 = br1.readLine()) != null) {
						stemp2 = stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1
								+ BNGEditor.console.getLineDelimiter());

						BNGEditor.console.redraw();
						BNGEditor.console.update();

					}
					BNGEditor.console.redraw();
					BNGEditor.console.update();

					while ((stemp1 = br2.readLine()) != null) {
						stemp2 = stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1
								+ BNGEditor.console.getLineDelimiter());

						BNGEditor.console.redraw();
						BNGEditor.console.update();

					}
					BNGEditor.console.redraw();
					BNGEditor.console.update();

					br1.close();
					br2.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				// save the console results to log file
				String prefixPath = "";
				if (ostype == 1) {
					// when executing the bat file,
					// the working directory has been changed
					prefixPath = filepath.substring(0, 2);
				}
				else {
					prefixPath = parentDir;
				}
				File logFile = new File( prefixPath + tmpSlash + prefix
						+ tmpSlash + modelName + "_console.log");
				PrintWriter pw2;
				try {
					pw2 = new PrintWriter(logFile);
					pw2.print(BNGEditor.console.getText());
					pw2.close();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
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
					System.out.println("Got Error when moving files.");
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
						System.out.println("Got Error when moving files.");
						e.printStackTrace();
					}
					System.gc();
					tfile.delete();
				}

				srcFolder.delete();

				/*
				 * String[] psresult = new String[1];
				 * psresult[0]=prefix+".scan"; if(!stemp2.contains("open") &&
				 * !stemp2.contains("ABORT") && !stemp2.contains("line")) { try
				 * {
				 * 
				 * new Viewer(null,psresult,new String[0]); } catch (IOException
				 * e) {} }
				 */

				Rectangle bounds = new Rectangle(0, 0, 680, 768);

				Rectangle disBounds = Display.getCurrent().getBounds();
				if (disBounds.width / 2 > 800) {
					bounds.width = 800;
				}

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
				} else {
					resViewer.getShell().setVisible(true);
					resViewer.refresh();
					resViewer.openDefaultFile(modelName + "\t" + "para_scan"
							+ "\t" + prefix, ".scan");
				}

			}
		});

		// add listener for cancel button
		parscanbutton2.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent arg0) {
			}

			public void widgetSelected(SelectionEvent arg0) {
				parscanShell.dispose();
			}
		});

		parscanShell.open();
	}

	void runbng(boolean viewresults) {
		// the bngfpath or bngfname can not be empty
		if (BNGEditor.bngfpath == null || BNGEditor.bngfname == null) {
			BNGEditor.console.setText("Error finding BNG.");
			BNGEditor.setbngpath();
		} 
		else {
			String tmpSlash = "";
			if (ostype == 1) {
				tmpSlash = "\\";
			}
			else {
				tmpSlash = "/";
			}
			// if the bng file does not exist, return
			File bngfile = new File(BNGEditor.bngfpath + tmpSlash + BNGEditor.bngfname);
			if (!bngfile.exists()) {
				BNGEditor.console.setText("Error finding BNG.");
				BNGEditor.setbngpath();
				bngfile = null;
				return;
			}
			bngfile = null;
		
			String stemp1, stemp2 = "";
			String str1 = filepath, str2 = filename;
			String regex1 = "at line (\\d+) of file";
			Pattern p1 = Pattern.compile(regex1);
			Matcher m;
			Boolean result;
			int ti;

			BNGEditor.console.setText("");
			String[] fnames;

			String curTime = getCurrentDateAndTime(); // get current time

			/*
			 * First, move all the old result files (.net, .cdat, .gdat) to the
			 * corresponding folder; Second, run BioNetGen to generate new
			 * result files; Third, rename the new generated result files with
			 * current time.
			 */
			
			// parentDir
			String parentDir = System.getProperty("user.dir");
			String resultsFolderPath = parentDir + tmpSlash + "BNGResults";

			if (ostype == 2) {
				// the release application on Mac
				String appDir = "";
				if (parentDir.indexOf("/Contents/Resources") != -1) {
					appDir = parentDir.substring(0, parentDir.indexOf("/Contents/Resources"));
					appDir = appDir.substring(0, appDir.lastIndexOf("/"));
					resultsFolderPath = appDir + tmpSlash + "BNGResults";
				}	
			}

			try {
				// Run BioNetGen
				Process p;
				
				ArrayList<String> runcommand = new ArrayList<String>();
				
				if (ostype == 1) {
					runcommand.add("cmd");
					runcommand.add("/c");
				}

				runcommand.add("perl");
				runcommand.add(BNGEditor.bngfpath+tmpSlash+BNGEditor.bngfname);

				if (viewresults == false) {
					// Check, not run
					runcommand.add("-check");
				}
				
				runcommand.add(filepath + tmpSlash + filename);		

				String[] command = { "tmp", "tmp" };
				command = runcommand.toArray(command);
				
				// run the process
				if (ostype == 1) {
					// exec bat
					String batfilename = "callBNG-win.bat";
					File batfile = new File(batfilename);
					PrintWriter pw = new PrintWriter(batfile);
					String modelDisk = filepath.substring(0, 1);
					
					pw.write(modelDisk + ":\n");
					pw.write("cd " + modelDisk + ":\\" + "\n");
					String cmd_bng = BNGEditor.bngfpath+tmpSlash+BNGEditor.bngfname;
					String cmd_model = filepath + tmpSlash + filename;
					
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

				BufferedReader br = new BufferedReader(new InputStreamReader(
						p.getInputStream()));
				BufferedReader br2 = new BufferedReader(new InputStreamReader(
						p.getErrorStream()));

				while ((stemp1 = br.readLine()) != null) {
					stemp2 = stemp2 + stemp1 + "\n";
					BNGEditor.console.append(stemp1
							+ BNGEditor.console.getLineDelimiter());

					BNGEditor.console.redraw();
					BNGEditor.console.update();

				}
				BNGEditor.console.redraw();
				BNGEditor.console.update();

				while ((stemp1 = br2.readLine()) != null) {
					stemp2 = stemp2 + stemp1 + "\n";
					BNGEditor.console.append(stemp1
							+ BNGEditor.console.getLineDelimiter());

					BNGEditor.console.redraw();
					BNGEditor.console.update();

				}
				BNGEditor.console.redraw();
				BNGEditor.console.update();

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
				textarea.textarea.setFocus();
				textarea.selectErrorLine(ti - 1);
			} else if (viewresults){
				/*
				 * str1=filepath; str2 = filename; if(str2.lastIndexOf('.')!=-1)
				 * str2 = str2.substring(0, str2.lastIndexOf('.')); try { new
				 * Viewer(filepath, new java.io.File(str1).list(new
				 * myfilefilter1(str2)), new java.io.File(str1).list(new
				 * myfilefilter2(str2))); } catch (IOException e) {}
				 */
				
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
					pw.print(BNGEditor.console.getText());
					pw.close();

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

					// create a new folder by the time suffix of those result
					// files
					java.io.File timefolder = new java.io.File(
							resultsFolderPath + tmpSlash + modelName + tmpSlash
									+ curTime + tmpSlash);
					if (!timefolder.isDirectory()) {
						timefolder.mkdir();
					}

					// rename new generated files with current time
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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.gc();
						if (!tfile.getName().endsWith(".bngl")) {
							tfile.delete();
						}
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				Rectangle bounds = new Rectangle(0, 0, 680, 768);

				Rectangle disBounds = Display.getCurrent().getBounds();
				if (disBounds.width / 2 > 800) {
					bounds.width = 800;
				}

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
	void ShowCMap() {
		// The original Contact Map display used a png produced by dot
		// If dot is not found, then the contact map cannot be shown.
		//
		// Commented out because dot is not longer needed
		// -ams 5/1/10
		/*
		 * if(BNGEditor.DOTfpath == null || BNGEditor.DOTfname == null) {
		 * if(BNGEditor.ostype != 1) {
		 * BNGEditor.console.setText("Error finding DOT, Contact Map NOT Shown"
		 * ); BNGEditor.setdotpath(); return; } }
		 */

		// If some quality of the textarea is not 'valid' then do not display
		// the cmap
		// -ams 5/1/10
		BNGEditor.console.setText("");
		if (!textarea.blockinfo[6].valid) {
			BNGEditor.console
					.setText("Reaction rule block not valid, Contact Map NOT shown.");
		}

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
			if (textarea.blockinfo[2].valid
					&& textarea.doc
							.get(textarea.blockinfo[2].caretbegin2,
									textarea.blockinfo[2].caretend1
											- textarea.blockinfo[2].caretbegin2)
							.trim().length() != 0) {
				new CMap(textarea.doc.get(
						textarea.blockinfo[2].caretbegin2,
						textarea.blockinfo[2].caretend1
								- textarea.blockinfo[2].caretbegin2).trim(),
						textarea.doc.get(
								textarea.blockinfo[6].caretbegin2,
								textarea.blockinfo[6].caretend1
										- textarea.blockinfo[6].caretbegin2)
								.trim(), true);
			} else if (textarea.blockinfo[4].valid
					&& textarea.doc
							.get(textarea.blockinfo[4].caretbegin2,
									textarea.blockinfo[4].caretend1
											- textarea.blockinfo[4].caretbegin2)
							.trim().length() != 0) {
				new CMap(textarea.doc.get(
						textarea.blockinfo[4].caretbegin2,
						textarea.blockinfo[4].caretend1
								- textarea.blockinfo[4].caretbegin2).trim(),
						textarea.doc.get(
								textarea.blockinfo[6].caretbegin2,
								textarea.blockinfo[6].caretend1
										- textarea.blockinfo[6].caretbegin2)
								.trim(), false);
			} else
				BNGEditor.console
						.setText("Molecule info block not valid, Contact Map NOT shown.");
		} catch (org.eclipse.jface.text.BadLocationException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This is what is called when the 'Influence Graph' button is clicked. -ams
	 * 5/1/10
	 */
	void ShowIMap() {
		// IMap uses dot, so it must be present
		// -ams 5/1/10
		if (BNGEditor.getDOTfpath() == null || BNGEditor.getDOTfname() == null) {
			BNGEditor.console
					.setText("Error finding DOT, Influence Graph NOT Shown.");
			BNGEditor.setdotpath();
			return;
		}
		
		String tmpSlash = "";
		if (ostype == 1) {
			tmpSlash = "\\";
		}else {
			tmpSlash = "/";
		}
		
		// if the dot file does not exist, return
		File dotfile = new File(BNGEditor.getDOTfpath() + tmpSlash + BNGEditor.getDOTfname());
		if (!dotfile.exists()) {
			BNGEditor.console.setText("Error finding DOT, Influence Graph NOT Shown.");
			BNGEditor.setdotpath();
			dotfile = null;
			return;
		}
		dotfile = null;

		// Similar to the CMap, several conditions must be met to produce the
		// IMap. Some
		// Conditions will use a different IMap constructor.
		// -ams 5/1/10
		BNGEditor.console.setText("");
		if (!textarea.blockinfo[6].valid) {
			BNGEditor.console
					.setText("Reaction rule block not valid, Influence Graph NOT shown.");
		}
		try {
			if (textarea.blockinfo[2].valid
					&& textarea.doc
							.get(textarea.blockinfo[2].caretbegin2,
									textarea.blockinfo[2].caretend1
											- textarea.blockinfo[2].caretbegin2)
							.trim().length() != 0)
				new IMap(textarea.doc.get(
						textarea.blockinfo[2].caretbegin2,
						textarea.blockinfo[2].caretend1
								- textarea.blockinfo[2].caretbegin2).trim(),
						textarea.doc.get(
								textarea.blockinfo[6].caretbegin2,
								textarea.blockinfo[6].caretend1
										- textarea.blockinfo[6].caretbegin2)
								.trim(), true);
			else if (textarea.blockinfo[4].valid
					&& textarea.doc
							.get(textarea.blockinfo[4].caretbegin2,
									textarea.blockinfo[4].caretend1
											- textarea.blockinfo[4].caretbegin2)
							.trim().length() != 0)
				new IMap(textarea.doc.get(
						textarea.blockinfo[4].caretbegin2,
						textarea.blockinfo[4].caretend1
								- textarea.blockinfo[4].caretbegin2).trim(),
						textarea.doc.get(
								textarea.blockinfo[6].caretbegin2,
								textarea.blockinfo[6].caretend1
										- textarea.blockinfo[6].caretbegin2)
								.trim(), false);
			else
				BNGEditor.console
						.setText("Molecule info block not valid, Influence Graph shown.");
		} catch (org.eclipse.jface.text.BadLocationException e) {
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

	class ParScanInput {
		String par1;
		String par2;
		String par3;
		String par4;
		boolean par5;
		boolean par6;
		String par7;
		String par8;
	}
}