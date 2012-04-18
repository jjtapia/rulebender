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

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import resultviewer.ui.ResultViewer;

import editor.contactmap.CMap;
import editor.influencegraph.IMap;

class CurrentFile
{
	String filename;
	String filepath;//halfpath
	String prevcontent;
	BNGTextArea textarea;
	CTabItem tabitem;
	String ss1;//content in file
	String ss2;//content in editor
	int ostype;
	
	Shell parscanShell;
	Text parscantext1;
	Text parscantext2;
	Text parscantext3;
	Text parscantext4;
	Text parscantext5;
	Text parscantext6;
	Text parscantext7;
	Button parscancheckBox1;
	Button parscancheckBox2;
	
	ParScanInput lastpsinput = new ParScanInput();
	boolean firstparscan = true;
	
	//result viewer
	ResultViewer resViewer;
	
	CurrentFile(String fpath, String fname, int inostype, boolean empty)
	{
		ostype = inostype;
		if(fpath==null)
		{
			filepath = null;
			filename = fname;
			BNGEditor.showfilepath.setText("");
			if(empty)
			{
				tabitem = new CTabItem(BNGEditor.textfolder, SWT.CLOSE); 
				textarea = new BNGTextArea(BNGEditor.textfolder, SWT.NONE);
				tabitem.setControl(textarea);
				tabitem.setText(filename);
				textarea.textarea.setText("");
				ss1 = textarea.doc.get();
				textarea.textchanges.remove(0);
				textarea.textarea.setFocus();
			}	
			else
			{
				tabitem = new CTabItem(BNGEditor.textfolder, SWT.CLOSE); 
				File tempfile = new File("Template");
				textarea = new BNGTextArea(BNGEditor.textfolder, SWT.NONE);
				tabitem.setControl(textarea);
				tabitem.setText(filename);
				BufferedReader br;
				try {
					br = new BufferedReader(new FileReader(tempfile));
					String str="";
					try {
						while(br.ready()) 
		    			{ 
		    				str = str + br.readLine()+textarea.textarea.getLineDelimiter();
		    			} 
						br.close(); 
						textarea.textarea.setText("");
						textarea.textarea.append(str);
						textarea.textchanges.remove(0);
						textarea.textchanges.remove(0);
						ss1 = textarea.doc.get();
					} catch (IOException e) {} 
	    		} catch (FileNotFoundException e) {} 
	    		textarea.textarea.setFocus();
			}
		}
		else
		{
			filepath = fpath;
			filename = fname;
			tabitem = new CTabItem(BNGEditor.textfolder, SWT.CLOSE); 
			textarea = new BNGTextArea(BNGEditor.textfolder, SWT.NONE);
			tabitem.setControl(textarea);
			tabitem.setText(filename);
			BufferedReader br;
			try {
				br = new BufferedReader(new FileReader(new File(fpath,fname)));
				String str="";
				try {
					while(br.ready()) 
	    			{ 
	    				str = str + br.readLine()+textarea.textarea.getLineDelimiter();
	    			} 
					br.close(); 
					textarea.textarea.setText("");
					textarea.textarea.append(str);
					textarea.textchanges.remove(0);
					textarea.textchanges.remove(0);
					ss1 = textarea.doc.get();
				} catch (IOException e) {} 
    		} catch (FileNotFoundException e) {} 
    		textarea.textarea.setFocus();
		}
		textarea.textarea.setFocus();
	}
	
	void saveas()
	{
		FileDialog savediag = new FileDialog(BNGEditor.sShell, SWT.SAVE);
		savediag.setFilterExtensions(new String[]{"*.bngl","*.txt","*.*"});
		String tempstr = filename;
		if(tempstr.indexOf('.')!=-1 && (tempstr.endsWith("bngl") || tempstr.endsWith("txt")))
			tempstr = tempstr.substring(0, tempstr.indexOf('.'));
		savediag.setFileName(tempstr);
		savediag.open();
		String fpath = savediag.getFilterPath(), fname = savediag.getFileName();
		if(fpath.equals("")||fpath==null)
			return;
		try 
		{
			PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter(new File(fpath,fname)))); 
			pw.write(textarea.doc.get()); 
			pw.flush();
		} catch(Exception esave){} 
	}
	
	void save()
	{
		if(filepath == null)
		{
			FileDialog savediag = new FileDialog(BNGEditor.sShell, SWT.SAVE);
			savediag.setFilterExtensions(new String[]{"*.bngl","*.txt","*.*"});
			String tempstr = filename;
			if(tempstr.indexOf('.')!=-1 && (tempstr.endsWith("bngl") || tempstr.endsWith("txt")))
				tempstr = tempstr.substring(0, tempstr.indexOf('.'));
			savediag.setFileName(tempstr);
			savediag.open();
			String fpath = savediag.getFilterPath(), fname = savediag.getFileName();
			if(fpath.equals("")||fpath==null)
				return;
			try 
			{
				PrintWriter pw=new PrintWriter(new BufferedWriter(new FileWriter(new File(fpath,fname)))); 
				pw.write(textarea.doc.get()); 
				pw.flush();
				ss1 = textarea.doc.get();
			} catch(Exception esave){} 
			filepath = fpath;
			filename = fname;
			if(ostype == 1)
				BNGEditor.showfilepath.setText(filepath+"\\"+filename);
			else
				BNGEditor.showfilepath.setText(filepath+"/"+filename);
		}
		else
		{
			PrintWriter pw;
			try {
				pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(filepath,filename))));
				if(!textarea.doc.get().endsWith(textarea.doc.getDefaultLineDelimiter()))
					pw.write(textarea.doc.get()+textarea.doc.getDefaultLineDelimiter()); 
				else
					pw.write(textarea.doc.get());
				pw.flush();
				ss1 = textarea.doc.get();
			} catch (IOException e) {} 
		}
	}
	
	void close()
	{
		ss2 = textarea.doc.get();
		if(ss1.equals(ss2))
			return;
		MessageBox mb = new MessageBox(BNGEditor.sShell,SWT.ICON_QUESTION|SWT.YES|SWT.NO); 
		mb.setText("Save before close ?");
		mb.setMessage("File "+filename+" has been changed. Save before close ?");
	
		if(mb.open() == SWT.YES)
		{
			save();
		}
	}
	
	void check()
	{
		BNGEditor.console.setText("");
    	String ss2=textarea.doc.get(); 
        if(!ss1.equals(ss2) || filepath == null) 
        {
        	MessageBox mb = new MessageBox(BNGEditor.sShell,SWT.ICON_QUESTION|SWT.YES|SWT.NO); 
    		mb.setText("Save before Check ?");
    		mb.setMessage("File "+filename+" has been changed. File must be saved before Check. Save before Check ?");
    		if(mb.open() == SWT.YES)
    		{
    			save();
    			ss2=textarea.doc.get(); 
    			if(ss1.equals(ss2) && filepath != null) 
    				runbng(false);
    			else
    				BNGEditor.console.setText("File Save NOT Successful, Model Not Checked.");
    		}
    		else
    			BNGEditor.console.setText("Model NOT Checked.");
        }
        else
        {
        	runbng(false);
        }
	}
	
	void run()
	{
		BNGEditor.console.setText("");
    	String ss2=textarea.doc.get(); 
        if(!ss1.equals(ss2) || filepath == null) 
        {
        	MessageBox mb = new MessageBox(BNGEditor.sShell,SWT.ICON_QUESTION|SWT.YES|SWT.NO); 
    		mb.setText("Save before Run ?");
    		mb.setMessage("File "+filename+" has been changed. File must be saved before Run. Save before Run ?");
    		if(mb.open() == SWT.YES)
    		{
    			save();
    			ss2=textarea.doc.get(); 
    			if(ss1.equals(ss2) && filepath != null) 
    				runbng(true);
    			else
    				BNGEditor.console.setText("File Save NOT Successful, Model Not Runned.");
    		}
    		else
    			BNGEditor.console.setText("Model NOT Runned.");
        }
        else
        {
        	runbng(true);
        }
	}
	
	/*
	 *  Parameter Scan
	 *  Modified on July 26th 2010
	 */
	
	void parscan()
	{
		BNGEditor.console.setText("");
    	String ss2=textarea.doc.get(); 
        if(!ss1.equals(ss2) || filepath == null) 
        {
        	MessageBox mb = new MessageBox(BNGEditor.sShell,SWT.ICON_QUESTION|SWT.YES|SWT.NO); 
    		mb.setText("Save before Parameter Scan ?");
    		mb.setMessage("File "+filename+" has been changed. File must be saved before Parameter Scan. Save before Parameter Scan ?");
    		if(mb.open() == SWT.YES)
    		{
    			save();
    		}
    		else
    		{
    			BNGEditor.console.setText("Parameter Scan NOT Runned.");
    			return;
    		}
        }
        
        ss2=textarea.doc.get(); 
		if(!ss1.equals(ss2) || filepath == null) 
		{
			BNGEditor.console.setText("File Save NOT Successful, Parameter Scan Not Runned.");
			return;
		}
		save();
		parscanShell = new Shell(BNGEditor.sShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		parscanShell.setFont(BNGEditor.globalfont);
		parscanShell.setSize(new Point(493, 358));
		parscanShell.setText("Parameter Scan");
		parscanShell.setLayout(null);
		parscanShell.setBounds(new Rectangle(100, 100, 493, 358));
		Label parscanlabel1 = new Label(parscanShell, SWT.NONE);
		parscanlabel1.setFont(BNGEditor.globalfont);
		parscanlabel1.setBounds(new Rectangle(23, 19, 300, 20));
		parscanlabel1.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel1.setText("Parameter Name (Alphanumeric Word)");
		Label parscanlabel2 = new Label(parscanShell, SWT.NONE);
		parscanlabel2.setFont(BNGEditor.globalfont);
		parscanlabel2.setBounds(new Rectangle(23, 47, 300, 20));
		parscanlabel2.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel2.setText("Parameter Min Value (Real Number)");
		Label parscanlabel3 = new Label(parscanShell, SWT.NONE);
		parscanlabel3.setFont(BNGEditor.globalfont);
		parscanlabel3.setBounds(new Rectangle(23, 75, 300, 20));
		parscanlabel3.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel3.setText("Parameter Max Value (Real Number > Min Value)");
		Label parscanlabel4 = new Label(parscanShell, SWT.NONE);
		parscanlabel4.setFont(BNGEditor.globalfont);
		parscanlabel4.setBounds(new Rectangle(23, 103, 300, 20));
		parscanlabel4.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel4.setText("Number of Points to Scan (Positive Integer)");
		Label parscanlabel5 = new Label(parscanShell, SWT.NONE);
		parscanlabel5.setFont(BNGEditor.globalfont);
		parscanlabel5.setBounds(new Rectangle(23, 131, 300, 20));
		parscanlabel5.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel5.setText("Log Scale ? (Error if checked & Min Value <= 0)");
		Label parscanlabel6 = new Label(parscanShell, SWT.NONE);
		parscanlabel6.setFont(BNGEditor.globalfont);
		parscanlabel6.setBounds(new Rectangle(23, 159, 300, 20));
		parscanlabel6.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel6.setText("Steady State ?");
		Label parscanlabel7 = new Label(parscanShell, SWT.NONE);
		parscanlabel7.setFont(BNGEditor.globalfont);
		parscanlabel7.setBounds(new Rectangle(23, 187, 300, 20));
		parscanlabel7.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel7.setText("Simulation Time (Positive Real Number)");
		Label parscanlabel8 = new Label(parscanShell, SWT.NONE);
		parscanlabel8.setFont(BNGEditor.globalfont);
		parscanlabel8.setBounds(new Rectangle(23, 215, 300, 20));
		parscanlabel8.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel8.setText("Number of Time Points (Positive Integer)");
		Label parscanlabel9 = new Label(parscanShell, SWT.NONE);
		parscanlabel9.setFont(BNGEditor.globalfont);
		parscanlabel9.setBounds(new Rectangle(23, 243, 300, 20));
		parscanlabel9.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		parscanlabel9.setText("Output File Prefix (Alphanumeric Word)");
		parscantext1 = new Text(parscanShell, SWT.BORDER);
		parscantext1.setFont(BNGEditor.globalfont);
		parscantext1.setBounds(new Rectangle(349, 18, 98, 22));
		parscantext2 = new Text(parscanShell, SWT.BORDER);
		parscantext2.setFont(BNGEditor.globalfont);
		parscantext2.setBounds(new Rectangle(349, 46, 98, 22));
		parscantext3 = new Text(parscanShell, SWT.BORDER);
		parscantext3.setFont(BNGEditor.globalfont);
		parscantext3.setBounds(new Rectangle(349, 74, 98, 22));
		parscantext4 = new Text(parscanShell, SWT.BORDER);
		parscantext4.setFont(BNGEditor.globalfont);
		parscantext4.setBounds(new Rectangle(349, 102, 98, 22));
		parscantext5 = new Text(parscanShell, SWT.BORDER);
		parscantext5.setFont(BNGEditor.globalfont);
		parscantext5.setBounds(new Rectangle(349, 186, 98, 22));
		parscantext6 = new Text(parscanShell, SWT.BORDER);
		parscantext6.setFont(BNGEditor.globalfont);
		parscantext6.setBounds(new Rectangle(349, 214, 98, 22));
		parscantext7 = new Text(parscanShell, SWT.BORDER);
		parscantext7.setFont(BNGEditor.globalfont);
		parscantext7.setBounds(new Rectangle(349, 242, 98, 22));
		parscancheckBox1 = new Button(parscanShell, SWT.CHECK);
		parscancheckBox1.setFont(BNGEditor.globalfont);
		parscancheckBox1.setBounds(new Rectangle(349, 133, 13, 16));
		parscancheckBox2 = new Button(parscanShell, SWT.CHECK);
		parscancheckBox2.setFont(BNGEditor.globalfont);
		parscancheckBox2.setBounds(new Rectangle(349, 161, 13, 16));
		if(!firstparscan)
		{
			parscantext1.setText(lastpsinput.par1);
			parscantext2.setText(lastpsinput.par2);
			parscantext3.setText(lastpsinput.par3);
			parscantext4.setText(lastpsinput.par4);
			parscantext5.setText(lastpsinput.par7);
			parscantext6.setText(lastpsinput.par8);
			if(lastpsinput.par5) 
				parscancheckBox1.setSelection(true);
			if(lastpsinput.par6) 
				parscancheckBox2.setSelection(true);
		}
		Button parscanbutton1 = new Button(parscanShell, SWT.NONE);
		parscanbutton1.setFont(BNGEditor.globalfont);
		parscanbutton1.setBounds(new Rectangle(195, 281, 115, 28));
		parscanbutton1.setText("OK");
		parscanbutton1.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				boolean verified = true;
				try{
					if(parscantext1.getText().trim().length() == 0)
						verified = false;
					if(Float.parseFloat(parscantext3.getText().trim())<=Float.parseFloat(parscantext2.getText().trim()))
						verified = false;
					if(Integer.parseInt(parscantext4.getText().trim())<=0)
						verified = false;
					if(Float.parseFloat(parscantext2.getText().trim())<=0 && parscancheckBox1.getSelection())
						verified = false;
					if(Float.parseFloat(parscantext5.getText().trim())<=0)
						verified = false;
					if(Integer.parseInt(parscantext6.getText().trim())<=0)
						verified = false;
					if(parscantext7.getText().trim().length() == 0)
						verified = false;
				}catch(NumberFormatException e){verified = false;}
				
				if(!verified)
				{
					MessageBox mb = new MessageBox(BNGEditor.sShell,SWT.ICON_INFORMATION); 
		    		mb.setText("Error Info");
		    		mb.setMessage("There exists invalid arguments, please check again !");
		    		mb.open();
		    		return;
				}
				else
				{
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
		        
    			if(BNGEditor.bngfpath == null || BNGEditor.bngfname == null)
    			{
    				parscanShell.dispose();
    				MessageBox mb = new MessageBox(BNGEditor.sShell,SWT.ICON_INFORMATION); 
    		    	mb.setText("Error Info");
    		    	mb.setMessage("Error finding BNG, Please Set BNG Path");
    		    	mb.open();
    				BNGEditor.setbngpath();
    				return;
    			}
    			String tempstr = "",tempstr1;
    			try {
    				BufferedReader br = new BufferedReader(new FileReader(new File(BNGEditor.bngfpath,"scan_var.pl")));
    				try {
    					while(br.ready())
    					{
    						tempstr = tempstr + br.readLine() + textarea.textarea.getLineDelimiter();
    					}
    				} catch (IOException e) {}
    			} catch (FileNotFoundException e) {}
    			Pattern p = Pattern.compile("exec\\s*=.*(\".*BNGPATH.*\\.[Pp][Ll]\\s*\")");
    			Matcher m = p.matcher(tempstr);
    			if(m.find())
    			{
    				if(ostype == 1)
    				{
    					tempstr1 = "\""; String strtemp = BNGEditor.bngfpath;
    					while(strtemp.indexOf('\\')!=-1)
    					{
    						tempstr1 = tempstr1+strtemp.substring(0, strtemp.indexOf('\\'))+"\\\\";
    						strtemp = strtemp.substring(strtemp.indexOf('\\')+1, strtemp.length());
    					}
    					tempstr1 = tempstr1 + strtemp+"\\\\" + BNGEditor.bngfname + "\"";
    				}
    				else
    					tempstr1 = "\""+BNGEditor.bngfpath + "/" + BNGEditor.bngfname + "\"";
    				tempstr = tempstr.substring(0, tempstr.indexOf(m.group(1)))+ tempstr1+ tempstr.substring(tempstr.indexOf(m.group(1))+m.group(1).length(), tempstr.length());
    			}
    			
    			//TODO test on different operating systems
    			
    			// get the model name
				String modelName = filename;
				if(modelName.lastIndexOf('.')!=-1) //Returns the index within this string of the last occurrence of the specified character.
					modelName = modelName.substring(0, modelName.lastIndexOf('.'));
				
				// slash for different operating system
				String tmpSlash = "";
				if(ostype == 1) {
					tmpSlash = "\\";
				}
				else {
					tmpSlash = "/";
				}
				
				// path for ModifiedParScan.pl
				String modifiedParScanPath = "";
				
    			PrintWriter pw;
    			try {
    				
    				//pw = new PrintWriter(new BufferedWriter(new FileWriter(new File("ModifiedParScan.pl"))));
	
					//create a folder with the name of model if not exists
					java.io.File modelFolder = new java.io.File(filepath + tmpSlash + modelName);
					if (!modelFolder.isDirectory()) {
						modelFolder.mkdir();
					}
					
    				//create the folder para_scan under the folder of model name if not exists
					java.io.File parascanFolder = new java.io.File(filepath + tmpSlash + modelName + tmpSlash + "para_scan");
					if (!parascanFolder.isDirectory()) {
						parascanFolder.mkdir();
					}
					
					// path for ModifiedParScan.pl
					modifiedParScanPath = filepath + tmpSlash + modelName + tmpSlash + "para_scan" + tmpSlash + "ModifiedParScan.pl";
				
    				pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(modifiedParScanPath))));
    				pw.write(tempstr); 
    				pw.flush();
    				pw.close();
    			} catch (IOException e) {} 
    			
    			String instruction;
    			if(ostype==1)
    				instruction = "cmd.exe /c perl "+ modifiedParScanPath +" ";
    			else
    				instruction = "perl " + modifiedParScanPath +" ";
    			if(parscancheckBox1.getSelection())
    				instruction = instruction + "-log" + " ";
    			if(parscancheckBox2.getSelection())
    				instruction = instruction + "-steady_state" + " ";
    			
    			if(ostype == 1)
    				instruction = instruction + "-n_steps "+ parscantext6.getText().trim()+ " -prefix " + parscantext7.getText().trim() + " -t_end " + parscantext5.getText().trim() + " " + filepath+"\\"+filename + " " + parscantext1.getText().trim() + " " + parscantext2.getText().trim() + " " + parscantext3.getText().trim() + " " + parscantext4.getText().trim();
    			else
    				instruction = instruction + "-n_steps "+ parscantext6.getText().trim()+ " -prefix " + parscantext7.getText().trim() + " -t_end " + parscantext5.getText().trim() + " " + filepath+"/"+filename + " " + parscantext1.getText().trim() + " " + parscantext2.getText().trim() + " " + parscantext3.getText().trim() + " " + parscantext4.getText().trim();
    			
    			String prefix = parscantext7.getText().trim();
    			parscanShell.dispose();
    			String stemp1,stemp2 = "";
    			Process pr;
    			
    			try {
					pr = Runtime.getRuntime().exec(instruction);
					BufferedReader br1 = new BufferedReader ( new InputStreamReader(pr.getInputStream())); 
					BufferedReader br2 = new BufferedReader ( new InputStreamReader(pr.getErrorStream()));
		    		while ( (stemp1= br1.readLine())!=null) 
					{
		    			stemp2= stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1+BNGEditor.console.getLineDelimiter());
						BNGEditor.console.redraw();
						BNGEditor.console.update();
					}
		    		
					while ( (stemp1= br2.readLine())!=null) 
					{
						stemp2= stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1+BNGEditor.console.getLineDelimiter());
						BNGEditor.console.redraw();
						BNGEditor.console.update();
					}
	    		} catch (IOException e) {}
	    		

	    		
	    		// move new generated files to the para_scan folder 
	    		String parentDir = System.getProperty("user.dir");
	    		java.io.File srcFolder = new java.io.File(parentDir + tmpSlash + prefix);
	    		java.io.File destFolder = new java.io.File(filepath + tmpSlash + modelName + tmpSlash + "para_scan");

	    		// SCAN file
	    		java.io.File scanfile = new java.io.File(parentDir + tmpSlash + prefix + ".scan");
    			java.io.File newScanFile = new java.io.File(destFolder + tmpSlash + scanfile.getName());
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

	    		for(int i = 0; i < files.length; i++) {
	    			java.io.File tfile = files[i];
	    			
	    			// create the prefix folder
	    			java.io.File prefixFolder = new java.io.File(destFolder + tmpSlash + prefix);
	    			if(!prefixFolder.isDirectory()) {
	    				prefixFolder.mkdir();
	    			}
	    			
	    			//move files
	    			java.io.File newFile = new java.io.File(destFolder + tmpSlash + prefix + tmpSlash + tfile.getName());
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
	    		String[] psresult = new String[1];	    		
	    		psresult[0]=prefix+".scan";
	    		if(!stemp2.contains("open") && !stemp2.contains("ABORT") && !stemp2.contains("line"))
	    		{
	    			try {
	    				
						new Viewer(null,psresult,new String[0]);
					} catch (IOException e) {}
	    		}
	    		*/
	    		
	    		/* open the viewer */
	    		// close the result viewer of BNGEditor class
				if (BNGEditor.resViewer != null && BNGEditor.resViewer.getShell() != null
						&& BNGEditor.resViewer.getShell().isVisible() == true) {
					BNGEditor.resViewer.getShell().dispose();

					if (BNGEditor.resViewer.getGraphFrame() != null
							&& BNGEditor.resViewer.getGraphFrame().isShowing()) {
						BNGEditor.resViewer.getGraphFrame().dispose(); // close the graph
					}
				}
				
				// close the old result viewer
				if (resViewer != null && resViewer.getShell() != null
						&& resViewer.getShell().isVisible() == true) {
					resViewer.getShell().dispose();

					if (resViewer.getGraphFrame() != null
							&& resViewer.getGraphFrame().isShowing()) {
						resViewer.getGraphFrame().dispose(); // close the graph
					}
				}
		
				// create a result viewer
				resViewer = new ResultViewer(filepath, modelName + " " + prefix);
				resViewer.setBlockOnOpen(true);
				resViewer.open();

				if (resViewer.getGraphFrame() != null
						&& resViewer.getGraphFrame().isShowing()) {
					resViewer.getGraphFrame().dispose(); // close the graph
				}
			}	
		});
		Button parscanbutton2 = new Button(parscanShell, SWT.NONE);
		parscanbutton2.setFont(BNGEditor.globalfont);
		parscanbutton2.setBounds(new Rectangle(332, 281, 115, 28));
		parscanbutton2.setText("Cancel");
		parscanbutton2.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				parscanShell.dispose();
			}	
		});
		parscanShell.open();
	}
	
	void runbng(boolean viewresults)
	{
		if(BNGEditor.bngfpath == null || BNGEditor.bngfname == null)
		{
			BNGEditor.console.setText("Error finding BNG, Model NOT Runned");
			BNGEditor.setbngpath();
		}
		else
		{
			String stemp1,stemp2="";
			String str1=filepath,str2 = filename;
			String regex1 = "at line (\\d+) of file";
			Pattern p1=Pattern.compile(regex1);
			Matcher m;
			Boolean result;
			int ti;
			
			BNGEditor.console.setText("");
			String[] fnames;
			
			String curTime = getCurrentDateAndTime(); //get current time
			
			/*
			 * First, move all the old result files (.net, .cdat, .gdat) to the corresponding folder;
			 * Second, run BioNetGen to generate new result files;
			 * Third, rename the new generated result files with current time.
			 */
			//TODO test
			if(ostype == 1) // Windows Systems 
			{
				try {

					//Run BioNetGen
					Process p;
					if(viewresults)
						p = Runtime.getRuntime().exec("cmd.exe /c perl "+BNGEditor.bngfpath+"\\"+BNGEditor.bngfname+" "+filepath+"\\"+filename);
					else
						p = Runtime.getRuntime().exec("cmd.exe /c perl "+BNGEditor.bngfpath+"\\"+BNGEditor.bngfname+" -check "+filepath+"\\"+filename);
					BufferedReader br = new BufferedReader ( new InputStreamReader(p.getInputStream())); 
					BufferedReader br2 = new BufferedReader ( new InputStreamReader(p.getErrorStream()));
					while ( (stemp1= br.readLine())!=null) 
					{
						stemp2= stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1+BNGEditor.console.getLineDelimiter());
						BNGEditor.console.redraw();
						BNGEditor.console.update();
					}
					while ( (stemp1= br2.readLine())!=null) 
					{
						stemp2= stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1+BNGEditor.console.getLineDelimiter());
						BNGEditor.console.redraw();
						BNGEditor.console.update();
					}
					
					br.close();
					br2.close();
					
					// get the model name
					String modelName = str2;
					if(modelName.lastIndexOf('.')!=-1) //Returns the index within this string of the last occurrence of the specified character.
						modelName = modelName.substring(0, modelName.lastIndexOf('.'));

					//create a folder with the name of model if not exists
					java.io.File modelFolder = new java.io.File(str1+"\\"+modelName);
					if (!modelFolder.isDirectory()) {
						modelFolder.mkdir();
					}
					
					// create a new folder by the time suffix of those result files
					java.io.File timefolder = new java.io.File(str1 + "\\"
							+ modelName + "\\" + curTime);
					if (!timefolder.isDirectory()) {
						timefolder.mkdir();
					}
					
					// rename new generated files with current time 
					fnames = new java.io.File(str1).list(new resultfilefilter(modelName));
					
					for(int i=0;i<fnames.length;i++)
					{
						File tfile = new File(str1+"\\"+fnames[i]);
						String fileName = fnames[i].substring(0, fnames[i].lastIndexOf('.'));; // get the name of the file
						String fileSuffix = fnames[i].substring(fnames[i].lastIndexOf('.'), fnames[i].length()); // get the suffix of the file
						String newFileName = fileName + "_" + curTime + fileSuffix; // get new file name
						File newFile = new File(str1+"\\"+modelName + "\\"
								+ curTime + "\\" +newFileName);
						copyUseChannel(tfile, newFile);
						System.gc();
						tfile.delete();
					}					
	
				} catch (IOException e) {}
			}
			
			else // Unix and Linux Systems
			{
				try {
					
					//Run BioNetGen
					Process p;
					if(viewresults)
						p = Runtime.getRuntime().exec("perl " + BNGEditor.bngfpath+"/"+BNGEditor.bngfname+" "+filepath+"/"+filename);
					else
						p = Runtime.getRuntime().exec("perl " + BNGEditor.bngfpath+"/"+BNGEditor.bngfname+" -check "+filepath+"/"+filename);
					BufferedReader br = new BufferedReader ( new InputStreamReader(p.getInputStream())); 
					BufferedReader br2 = new BufferedReader ( new InputStreamReader(p.getErrorStream()));
					while ( (stemp1= br.readLine())!=null) 
					{
						stemp2= stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1+BNGEditor.console.getLineDelimiter());
						BNGEditor.console.redraw();
						BNGEditor.console.update();
					}
					while ( (stemp1= br2.readLine())!=null) 
					{
						stemp2= stemp2 + stemp1 + "\n";
						BNGEditor.console.append(stemp1+BNGEditor.console.getLineDelimiter());
						BNGEditor.console.redraw();
						BNGEditor.console.update();
					}
					
					br.close();
					br2.close();
					
					// get the model name
					String modelName = str2;
					if(modelName.lastIndexOf('.')!=-1) //Returns the index within this string of the last occurrence of the specified character.
						modelName = modelName.substring(0, modelName.lastIndexOf('.'));
					
					//create a folder with the name of model if not exists
					java.io.File modelFolder = new java.io.File(str1+"/"+modelName);
					if (!modelFolder.isDirectory()) {
						modelFolder.mkdir();
					}				
					
					// create a new folder by the time suffix of those result files
					java.io.File timefolder = new java.io.File(str1+"/"+modelName+"/"+curTime);
					if (!timefolder.isDirectory()) {
						timefolder.mkdir();
					}
					
					// rename new generated files with current time				
					fnames = new java.io.File(str1).list(new resultfilefilter(modelName)); 
					for(int i=0;i<fnames.length;i++)
					{
						File tfile = new File(str1+"/"+fnames[i]);
						String fileName = fnames[i].substring(0, fnames[i].lastIndexOf('.'));; // get the name of the file
						String fileSuffix = fnames[i].substring(fnames[i].lastIndexOf('.'), fnames[i].length()); // get the suffix of the file
						String newFileName = fileName + "_" + curTime + fileSuffix; // get new file name
						File newFile = new File(str1+"/"+modelName+"/"+curTime+"/"+newFileName);
						copyUseChannel(tfile, newFile);
						System.gc();
						tfile.delete();
					}
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			 
			// transfer the results to the Viewer
			m = p1.matcher(stemp2);
			result = m.find();
			if(result)
			{
				ti = Integer.parseInt(m.group(1)); 
				textarea.textarea.setFocus();
				textarea.selectErrorLine(ti-1);
			}
			else if(viewresults)
			{
				/*
				str1=filepath; str2 = filename;
				if(str2.lastIndexOf('.')!=-1)
					str2 = str2.substring(0, str2.lastIndexOf('.'));
				try {
					new Viewer(filepath, new java.io.File(str1).list(new myfilefilter1(str2)), new java.io.File(str1).list(new myfilefilter2(str2)));
				} catch (IOException e) {}
				*/
				
				
				// close the result viewer of BNGEditor class
				if (BNGEditor.resViewer != null && BNGEditor.resViewer.getShell() != null
						&& BNGEditor.resViewer.getShell().isVisible() == true) {
					BNGEditor.resViewer.getShell().dispose();

					if (BNGEditor.resViewer.getGraphFrame() != null
							&& BNGEditor.resViewer.getGraphFrame().isShowing()) {
						BNGEditor.resViewer.getGraphFrame().dispose(); // close the graph
					}
				}
				
				// close the old result viewer
				if (resViewer != null && resViewer.getShell() != null
						&& resViewer.getShell().isVisible() == true) {
					resViewer.getShell().dispose();

					if (resViewer.getGraphFrame() != null
							&& resViewer.getGraphFrame().isShowing()) {
						resViewer.getGraphFrame().dispose(); // close the graph
					}
				}
				String modelName = str2;
				if(modelName.lastIndexOf('.')!=-1) //Returns the index within this string of the last occurrence of the specified character.
					modelName = modelName.substring(0, modelName.lastIndexOf('.'));
				
				// create a result viewer
				resViewer = new ResultViewer(filepath, modelName + " " + curTime);
				resViewer.setBlockOnOpen(true);
				resViewer.open();

				if (resViewer.getGraphFrame() != null
						&& resViewer.getGraphFrame().isShowing()) {
					resViewer.getGraphFrame().dispose(); // close the graph
				}
				
			}
		}
	}
	
	/*
	 * result file filter
	 */
	class resultfilefilter implements FilenameFilter 
	{
		private String str1;

		public resultfilefilter(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if(name.startsWith(str1)&&name.endsWith(".net")) 
		    	return true;
		   	if(name.startsWith(str1)&&name.endsWith(".cdat"))
		   		return true;
		 	if(name.startsWith(str1)&&name.endsWith(".gdat"))
		    	return true;
		 	if(name.startsWith(str1)&&name.endsWith(".scan"))
		    	return true;
		 	if(name.startsWith(str1)&&name.endsWith(".m"))
		    	return true;
		 	if(name.startsWith(str1)&&name.endsWith(".log"))
		    	return true;
		 	if(name.startsWith(str1)&&name.endsWith(".xml"))
		    	return true;
			return false;
		}
	}
	
	class myfilefilter implements FilenameFilter 
	{
		private String str1;

		public myfilefilter(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if(name.startsWith(str1)&&name.endsWith(".net")) 
		    	return true;
		   	if(name.startsWith(str1)&&name.endsWith(".cdat"))
		   		return true;
		 	if(name.startsWith(str1)&&name.endsWith(".gdat"))
		    	return true;
			return false;
		}
	}

	class myfilefilter1 implements FilenameFilter 
	{
		private String str1;

		public myfilefilter1(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if(name.startsWith(str1)&&name.endsWith(".cdat"))
				return true;
			if(name.startsWith(str1)&&name.endsWith(".gdat"))
				return true;
			return false;
		}
	}
	
	class myfilefilter2 implements FilenameFilter 
	{
		private String str1;

		public myfilefilter2(String prefix) {
			str1 = prefix;
		}

		public boolean accept(File dir, String name) {
			if(name.startsWith(str1)&&name.endsWith(".net"))
				return true;
			return false;
		}
	}
	
	/**
	 * Called when the Contact Map button is pressed.
	 * 
	 * -ams
	 */
	void ShowCMap()
	{
		// The original Contact Map display used a png produced by dot
		// If dot is not found, then the contact map cannot be shown.
		//
		// Commented out because dot is not longer needed
		// -ams 5/1/10
		/*
		  if(BNGEditor.DOTfpath == null || BNGEditor.DOTfname == null)
		{
			if(BNGEditor.ostype != 1)
			{
				BNGEditor.console.setText("Error finding DOT, Contact Map NOT Shown");
				BNGEditor.setdotpath();
				return;
			}
		}
		*/
		
		// If some quality of the textarea is not 'valid' then do not display 
		// the cmap
		// -ams 5/1/10
		BNGEditor.console.setText("");
		if(!textarea.blockinfo[6].valid)
		{
			BNGEditor.console.setText("Reaction rule block not valid, Contact Map NOT shown.");
		}
		
		/* This mess checks a ton of factors in the textarea and calls the 
		 * CMap constructor with 1 of 2 sets of parameters. 
		 *
		 * The CMap constructor looks like 
		 * 'CMap(String molestr, String rulestr, boolean moleculetype)'
		 * 
		 * The difference in the calls is
		 * 
		 * 1. The first one uses a valid and nonempty textarea.blockinfo[2] 
		 * and the second uses a valid and nonempty textarea.blockinfo[4]
		 * for the molestr.
		 * 
		 * 2. The first passes true to moleculetype and the second passes false.
		 *
		 * -ams 5/1/10 
		 */
		try {
			if(textarea.blockinfo[2].valid && textarea.doc.get(textarea.blockinfo[2].caretbegin2, textarea.blockinfo[2].caretend1-textarea.blockinfo[2].caretbegin2).trim().length()!=0)
			{
				new CMap(textarea.doc.get(textarea.blockinfo[2].caretbegin2, textarea.blockinfo[2].caretend1-textarea.blockinfo[2].caretbegin2).trim(), textarea.doc.get(textarea.blockinfo[6].caretbegin2, textarea.blockinfo[6].caretend1-textarea.blockinfo[6].caretbegin2).trim(), true);
			}
			else if(textarea.blockinfo[4].valid && textarea.doc.get(textarea.blockinfo[4].caretbegin2, textarea.blockinfo[4].caretend1-textarea.blockinfo[4].caretbegin2).trim().length()!=0)
			{
				new CMap(textarea.doc.get(textarea.blockinfo[4].caretbegin2, textarea.blockinfo[4].caretend1-textarea.blockinfo[4].caretbegin2).trim(), textarea.doc.get(textarea.blockinfo[6].caretbegin2, textarea.blockinfo[6].caretend1-textarea.blockinfo[6].caretbegin2).trim(), false);
			}
			else
				BNGEditor.console.setText("Molecule info block not valid, Contact Map NOT shown.");
		} 
		catch (org.eclipse.jface.text.BadLocationException e) 
		{
			e.printStackTrace();
		}
	}
	
	
	/**
	 * This is what is called when the 'Influence Graph' button is clicked.
	 * -ams 5/1/10
	 */
	void ShowIMap()
	{
		// IMap uses dot, so it must be present
		// -ams 5/1/10
		if(BNGEditor.getDOTfpath() == null || BNGEditor.getDOTfname() == null)
		{
			if(BNGEditor.getOstype() != 1)
			{
				BNGEditor.console.setText("Error finding DOT, Influence Graph NOT Shown");
				BNGEditor.setdotpath();
				return;
			}
		}
		
		// Similar to the CMap, several conditions must be met to produce the IMap.  Some
		// Conditions will use a different IMap constructor.
		// -ams 5/1/10
		BNGEditor.console.setText("");
		if(!textarea.blockinfo[6].valid)
		{
			BNGEditor.console.setText("Reaction rule block not valid, Influence Graph NOT shown.");
		}
		try {
			if(textarea.blockinfo[2].valid && textarea.doc.get(textarea.blockinfo[2].caretbegin2, textarea.blockinfo[2].caretend1-textarea.blockinfo[2].caretbegin2).trim().length()!=0)
				new IMap(textarea.doc.get(textarea.blockinfo[2].caretbegin2, textarea.blockinfo[2].caretend1-textarea.blockinfo[2].caretbegin2).trim(), textarea.doc.get(textarea.blockinfo[6].caretbegin2, textarea.blockinfo[6].caretend1-textarea.blockinfo[6].caretbegin2).trim(), true);
			else if(textarea.blockinfo[4].valid && textarea.doc.get(textarea.blockinfo[4].caretbegin2, textarea.blockinfo[4].caretend1-textarea.blockinfo[4].caretbegin2).trim().length()!=0)
				new IMap(textarea.doc.get(textarea.blockinfo[4].caretbegin2, textarea.blockinfo[4].caretend1-textarea.blockinfo[4].caretbegin2).trim(), textarea.doc.get(textarea.blockinfo[6].caretbegin2, textarea.blockinfo[6].caretend1-textarea.blockinfo[6].caretbegin2).trim(), false);
			else
				BNGEditor.console.setText("Molecule info block not valid, Influence Graph shown.");
		} catch (org.eclipse.jface.text.BadLocationException e) {}
	}
	
	private String getCurrentDateAndTime() {
		Date dateNow = new Date();
		SimpleDateFormat  dateFormat=new SimpleDateFormat ("dd-MMM-yy HH-mm-ss");
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
	
	class ParScanInput
	{
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