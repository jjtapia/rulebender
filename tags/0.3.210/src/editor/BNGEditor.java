package editor;
/**
 * BNGEditor.java
 * 
 * This file creates the gui for the BNG IDE.  
 * 
 * 
 * @author Yao Sun - Original code
 * @author Adam M. Smith - Additions and documentation
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Adapter;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Group;

import javax.xml.parsers.*;
import org.w3c.dom.*;

import resultviewer.ui.ResultViewer;

public class BNGEditor 
{	
	// Visual Elements
	static Shell sShell = null;  //  @jve:decl-index=0:visual-constraint="-1,21"
	private Menu menuBar = null;
	private Menu submf1 = null;
	private Menu mfsub = null;
	private Menu mesub = null;
	private Menu mosub = null;
	private Menu mvsub = null;
	private Menu mrsub = null;
	private Menu mhsub = null;
	private ToolBar toolBar = null;
	private Composite composite1 = null;
	private SashForm sashForm = null;
	static CTabFolder textfolder = null;
	static TabFolder consolefolder = null;
	public static Text console = null;
	private Composite composite2 = null;
	static Text showfilepath = null;
	private static int fileselection = -1;
	private static ArrayList<CurrentFile> inputfiles= new ArrayList<CurrentFile>();  //  @jve:decl-index=0:
	private static int ostype; // 1:windows 2:mac 3:other
	static String bngfpath = null,bngfname = null;  //  @jve:decl-index=0:
	private static String DOTfpath = null;
	private static String DOTfname = null;
	
	Text newfiletext;
	Button templatefile;
	Button emptyfile;
	Shell newfilediag;
	static Shell BNGPathShell;
	static Text BNGPathtext1;
	static Shell DOTPathShell;
	static Text DOTPathtext1;
	private Printer printer;
	
	static Menu popmenu; 
	static ArrayList<Pattern> keywords = new ArrayList<Pattern>();  //  @jve:decl-index=0:
	static Font globalfont;
	static int blocknum;
	
	private static ResultViewer resViewer;
	private Integer emptyFileCount = 0;
	
	
	/**
	 * Basically everything in this method is for setting up menus, buttons, 
	 * and the appropriate actionListeners and activities.
	 * -ams
	 */
	private void createSShell() {
		String stemp = System.getProperty("os.name");
 	    if(stemp.contains("Windows") || stemp.contains("WINDOWS") || stemp.contains("windows"))
 		    setOstype(1);
 	    else if (stemp.contains("Mac") || stemp.contains("MAC") || stemp.contains("mac"))
 		    setOstype(2);
 	    else
 	    	setOstype(3);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		sShell = new Shell();
		sShell.setFont(globalfont);
		sShell.setText("BNG Editor");
		createToolBar();
		sShell.setLayout(gridLayout);
		createComposite2();
		createComposite1();
		sShell.setSize(new Point(706, 518));
		AddKeyWords();
		
		File pathfile = new File("BNGPath");
    	if(!pathfile.exists())
			try {
				pathfile.createNewFile();
				
				String tmpSlash = "";
				if (ostype == 1) {
					tmpSlash = "\\";
				}
				else {
					tmpSlash = "/";
				}
				String parentDir = System.getProperty("user.dir");
				String bngpath = "";
				
				if (ostype == 2) {
					//mac
					String appDir = "";
					if (parentDir.indexOf("/Contents/Resources") != -1) {
						appDir = parentDir.substring(0, parentDir.indexOf("/Contents/Resources"));
						appDir = appDir.substring(0, appDir.lastIndexOf("/"));
						bngpath = appDir + tmpSlash + "BioNetGen-2.1.6"
						+ tmpSlash + "Perl2";
					}
					else {
						bngpath = parentDir + tmpSlash + "BioNetGen-2.1.6"
						+ tmpSlash + "Perl2";
					}
					
					
				}
				else {
				bngpath = parentDir + tmpSlash + "BioNetGen-2.1.6"
						+ tmpSlash + "Perl2";
				}
				String bngname = "BNG2.pl";
				File bngfile = new File(bngpath + tmpSlash + bngname);
				if (bngfile.exists()) {
					PrintWriter pw = new PrintWriter(pathfile);

					bngfpath = bngpath;
					pw.write(bngpath);
					pw.write("\n");
					bngfname = bngname;
					pw.write(bngname);
					pw.write("\n");
					pw.close();
				}
				bngfile = null;
			} catch (IOException e1) {}
			else
			{
				try {
				    BufferedReader br1 = new BufferedReader(new FileReader(pathfile));
				    bngfpath = br1.readLine();
				    bngfname = br1.readLine();
		    	} catch (FileNotFoundException e1) {} catch (IOException e1) {}
			}
    	
    	pathfile = new File("DOTPath");
    	if(!pathfile.exists())
			try {
				pathfile.createNewFile();
			} catch (IOException e1) {}
			else
			{
				try {
				    BufferedReader br1 = new BufferedReader(new FileReader(pathfile));
				    setDOTfpath(br1.readLine());
				    setDOTfname(br1.readLine());
		    	} catch (FileNotFoundException e1) {} catch (IOException e1) {}
			}
		
    	// Add things to a menu
		popmenu= new Menu(sShell,SWT.POP_UP);
		MenuItem mp1 = new MenuItem(popmenu,SWT.PUSH);
		mp1.setText("Undo");
		mp1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.Undo();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator11 = new MenuItem(popmenu, SWT.SEPARATOR);
		MenuItem push = new MenuItem(popmenu, SWT.PUSH);
		push.setText("Cut");
		push.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.cut();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mp3 = new MenuItem(popmenu, SWT.PUSH);
		mp3.setText("Copy");
		mp3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.copy();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mp4 = new MenuItem(popmenu, SWT.PUSH);
		mp4.setText("Paste");
		mp4.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.paste();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator12 = new MenuItem(popmenu, SWT.SEPARATOR);
		MenuItem mp5 = new MenuItem(popmenu, SWT.PUSH);
		mp5.setText("Select All");
		mp5.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.selectall();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		
		menuBar = new Menu(sShell, SWT.BAR);
		MenuItem mf = new MenuItem(menuBar, SWT.CASCADE);
		mf.setText("&File");
		MenuItem me = new MenuItem(menuBar, SWT.CASCADE);
		me.setText("&Edit");
		MenuItem mo = new MenuItem(menuBar, SWT.CASCADE);
		mo.setText("F&ormat");
		MenuItem mv = new MenuItem(menuBar, SWT.CASCADE);
		mv.setText("&View");
		MenuItem mr = new MenuItem(menuBar, SWT.CASCADE);
		mr.setText("&Run");
		MenuItem mh = new MenuItem(menuBar, SWT.CASCADE);
		mh.setText("&Help");
		mhsub = new Menu(mh);
		MenuItem mth1 = new MenuItem(mhsub, SWT.PUSH);
		mth1.setText("&About BNG Editor");
		mth1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				MessageBox mb = new MessageBox(sShell, SWT.ICON_INFORMATION);
				mb.setMessage("This is a BNG Editor written by\n\nYao Sun\nDept. of Computer Science\nUniv. of Pittsburgh");
				mb.setText("About BNG Editor");
				mb.open();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		mh.setMenu(mhsub);
		mrsub = new Menu(mr);
		MenuItem mtr1 = new MenuItem(mrsub, SWT.PUSH);
		mtr1.setText("&Set BNG Path");
		mtr1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				setbngpath();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator5 = new MenuItem(mrsub, SWT.SEPARATOR);
		MenuItem mtr2 = new MenuItem(mrsub, SWT.PUSH);
		mtr2.setText("&Check Model");
		mtr2.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).check();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mtr3 = new MenuItem(mrsub, SWT.PUSH);
		mtr3.setText("&Run Model");
		mtr3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).run();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator6 = new MenuItem(mrsub, SWT.SEPARATOR);
		MenuItem mtr4 = new MenuItem(mrsub, SWT.PUSH);
		mtr4.setText("&Parameter Scan");
		mtr4.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).parscan();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		mr.setMenu(mrsub);
		mvsub = new Menu(mv);
		MenuItem mv3 = new MenuItem(mvsub, SWT.PUSH);
		mv3.setText("&Set DOT Path");
		mv3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				setdotpath();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator7 = new MenuItem(mvsub, SWT.SEPARATOR);
		MenuItem mv1 = new MenuItem(mvsub, SWT.PUSH);
		mv1.setText("&Contact Map");
		mv1.setEnabled(true);
		mv1.addSelectionListener(new org.eclipse.swt.events.SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).ShowCMap();
			}
		});
		MenuItem mv2 = new MenuItem(mvsub, SWT.PUSH);
		mv2.setText("&Influence Map");
		mv2.setEnabled(true);
		mv2.addSelectionListener(new org.eclipse.swt.events.SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).ShowIMap();
			}
		});
		
		
		// Add menu item to open the result viewer directly.
		
		MenuItem openViewerSeperator = new MenuItem(mvsub, SWT.SEPARATOR);
		MenuItem openViewerItem = new MenuItem(mvsub, SWT.PUSH);
		openViewerItem.setText("&Open Result Viewer");
		openViewerItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				setResultViewerPath();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		
		// set font
		mv.setMenu(mvsub);
		mosub = new Menu(mo);
		MenuItem mo1 = new MenuItem(mosub, SWT.PUSH);
		mo1.setText("&Font...");
		mo1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				
				FontDialog fontdlg = new FontDialog(sShell);
				FontData fontData = fontdlg.open();
			    if (fontData != null) {
			      Font font = new Font(sShell.getDisplay(), fontData);
			      inputfiles.get(fileselection).textarea.textarea.setFont(font);
					inputfiles.get(fileselection).textarea.arc1.redraw();
					inputfiles.get(fileselection).textarea.arc2.redraw();
			    }
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		mo.setMenu(mosub);
		mesub = new Menu(me);
		MenuItem mte1 = new MenuItem(mesub, SWT.PUSH);
		mte1.setText("&Undo\tCtrl+Z");
		//SWT.MOD1, the first modifier, is set to SWT.CONTROL on Windows and to SWT.COMMAND on the Mac
		mte1.setAccelerator(SWT.MOD1 + 'Z');
		mte1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.Undo();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator2 = new MenuItem(mesub, SWT.SEPARATOR);
		MenuItem mte2 = new MenuItem(mesub, SWT.PUSH);
		mte2.setText("Cu&t\tCtrl+X");
		mte2.setAccelerator(SWT.MOD1 + 'X');
		mte2.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.cut();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mte3 = new MenuItem(mesub, SWT.PUSH);
		mte3.setText("&Copy\tCtrl+C");
		mte3.setAccelerator(SWT.MOD1 + 'C');
		mte3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.copy();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mte4 = new MenuItem(mesub, SWT.PUSH);
		mte4.setText("&Paste\tCtrl+V");
		mte4.setAccelerator(SWT.MOD1 + 'V');
		mte4.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.paste();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator3 = new MenuItem(mesub, SWT.SEPARATOR);
		MenuItem mte5 = new MenuItem(mesub, SWT.PUSH);
		mte5.setText("&Find...\tCtrl+F");
		mte5.setAccelerator(SWT.MOD1 + 'F');
		mte5.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.find();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mte6 = new MenuItem(mesub, SWT.PUSH);
		mte6.setText("&Replace...\tCtrl+R");
		mte6.setAccelerator(SWT.MOD1 + 'R');
		mte6.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.replace();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator4 = new MenuItem(mesub, SWT.SEPARATOR);
		MenuItem mte7 = new MenuItem(mesub, SWT.PUSH);
		mte7.setText("Select &All\tCtrl+A");
		mte7.setAccelerator(SWT.MOD1 + 'A');
		mte7.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.selectall();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		/*
		MenuItem mte8 = new MenuItem(mesub, SWT.PUSH);
		mte8.setText("&Delete All\tCtrl+D");
		mte8.setAccelerator(SWT.MOD1 + 'D');
		mte8.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.deleteall();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		*/
		me.setMenu(mesub);
		mfsub = new Menu(mf);
		MenuItem mtf1 = new MenuItem(mfsub, SWT.PUSH);
		mtf1.setText("&New...\tCtrl+N");
		mtf1.setAccelerator(SWT.MOD1 + 'N');
		mtf1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {   
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {   
				newfilediag = new Shell(sShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				newfilediag.setText("New...");
				// set grid layout
				newfilediag.setLayout(new GridLayout(3, true));
				
				// file name
				Label label1 = new Label(newfilediag, SWT.NONE);
				label1.setText("File name:");
				
				newfiletext = new Text(newfilediag, SWT.BORDER);
				GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				newfiletext.setLayoutData(gridData);
				newfiletext.setText("untitled" + emptyFileCount.toString() + ".bngl");
				
				// type
				Label label2 = new Label(newfilediag, SWT.NONE);
				label2.setText("Type:");
				
				// empty file
				emptyfile = new Button(newfilediag, SWT.RADIO);
				emptyfile.setSelection(true);
				emptyfile.setText("Empty File");
				
				// empty label to control layout
				new Label(newfilediag, SWT.NONE).setText("");
				new Label(newfilediag, SWT.NONE).setText("");
				
				// template file
				templatefile = new Button(newfilediag, SWT.RADIO);
				templatefile.setText("Template File");
				
				
				// empty label to control layout
				new Label(newfilediag, SWT.NONE).setText("");
				new Label(newfilediag, SWT.NONE).setText("");

				// ok button
				Button okbutton = new Button(newfilediag, SWT.NONE);
				gridData = new GridData();
				gridData.widthHint = 80;
				okbutton.setText("OK");
				okbutton.setLayoutData(gridData);
				
				// cancel button
				Button cancelbutton = new Button(newfilediag, SWT.NONE);
				cancelbutton.setText("Cancel");
				cancelbutton.setLayoutData(gridData);

				// add listener for ok button
				okbutton.addMouseListener(new MouseListener()
				{
					public void mouseDoubleClick(MouseEvent arg0) {}
					public void mouseDown(MouseEvent arg0) {}
					public void mouseUp(MouseEvent arg0) {
						String temp = null;
						CurrentFile tempfile;
						if(newfiletext.getText().trim().equals(""))
						{
							MessageBox mb = new MessageBox(sShell, SWT.ICON_INFORMATION);
							mb.setMessage("File name can not be empty !");
							mb.setText("Error info");
							mb.open();
							return;
						}
						if(emptyfile.getSelection()) {
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),true);
							emptyFileCount++;
							emptyFileCount = emptyFileCount % 100;
						}
						else
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),false);
						
						// pass the resViewer to current file
						tempfile.setResViewer(resViewer);
						
						inputfiles.add(tempfile);
						fileselection = inputfiles.size()-1;
						textfolder.setSelection(fileselection);
						newfilediag.dispose();
					}
				});
				
				// add listener for cancel button
				cancelbutton.addMouseListener(new MouseListener()
				{
					public void mouseDoubleClick(MouseEvent arg0) {}
					public void mouseDown(MouseEvent arg0) {}
					public void mouseUp(MouseEvent arg0) {
						newfilediag.dispose();	
					}});
				
				
				newfilediag.pack();
				newfilediag.open();
			}
		
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mtf3 = new MenuItem(mfsub, SWT.PUSH);
		mtf3.setText("&Open...\tCtrl+O");
		mtf3.setAccelerator(SWT.MOD1 + 'O');
		mtf3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog opendiag = new FileDialog(sShell, SWT.OPEN);
				opendiag.setFilterExtensions(new String[]{"*.bngl","*.txt","*.*"});
				opendiag.open();
				String fpath = opendiag.getFilterPath(), fname = opendiag.getFileName();
				if(fname.equals("")||fname==null)
					return;
				else
				{
					CurrentFile tempfile;
					boolean fileexist = false;
					for(int i = 0;i<inputfiles.size();i++)
						if(inputfiles.get(i).filepath!=null)
							if(inputfiles.get(i).filepath.equals(fpath) && inputfiles.get(i).filename.equals(fname))
								{
									MessageBox mb = new MessageBox(sShell, SWT.ICON_INFORMATION);
									mb.setMessage("File has already been opened !");
									mb.setText("Error info");
									mb.open();
									textfolder.setSelection(i);
									fileexist = true;
									return;
								}
					
					if(!fileexist)
					{
						tempfile = new CurrentFile(fpath,fname,getOstype(),false);
						inputfiles.add(tempfile);
						fileselection = inputfiles.size()-1;
						textfolder.setSelection(fileselection);	
						if(getOstype()==1)
							showfilepath.setText(fpath+"\\"+fname);
						else
							showfilepath.setText(fpath+"/"+fname);
					}
				}
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator8 = new MenuItem(mfsub, SWT.SEPARATOR);
		MenuItem mtf4 = new MenuItem(mfsub, SWT.PUSH);
		mtf4.setText("&Save\tCtrl+S");
		mtf4.setAccelerator(SWT.MOD1 + 'S');
		mtf4.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).save();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mtf5 = new MenuItem(mfsub, SWT.PUSH);
		mtf5.setText("Save &As...");
		mtf5.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).saveas();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mtf8 = new MenuItem(mfsub, SWT.PUSH);
		mtf8.setText("Sa&ve All");
		mtf8.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection()==null)
					return;
				for(int i=0;i<inputfiles.size();i++)
					inputfiles.get(i).save();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator9 = new MenuItem(mfsub, SWT.SEPARATOR);
		MenuItem mtf7 = new MenuItem(mfsub, SWT.PUSH);
		mtf7.setText("&Close");
		mtf7.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).close();
				inputfiles.remove(fileselection);	
				textfolder.getItem(fileselection).dispose();	
				for(int i=0;i<inputfiles.size();i++)
					if(inputfiles.get(i).tabitem.equals(textfolder.getSelection()))
					{
						fileselection = i;
						break;
					}	
				if(fileselection>=inputfiles.size())
				{
					fileselection = -1;
					showfilepath.setText("");
					return;
				}
				if(inputfiles.get(fileselection).filepath==null)
					showfilepath.setText("");
				else
					if(getOstype() == 1)
						showfilepath.setText(inputfiles.get(fileselection).filepath+"\\"+inputfiles.get(fileselection).filename);
					else
						showfilepath.setText(inputfiles.get(fileselection).filepath+"/"+inputfiles.get(fileselection).filename);
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		final MenuItem mtf2 = new MenuItem(mfsub, SWT.PUSH);
		mtf2.setText("C&lose All");
		mtf2.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				closeallfiles(); 
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator10 = new MenuItem(mfsub, SWT.SEPARATOR);
		MenuItem mtf9 = new MenuItem(mfsub, SWT.PUSH);
		mtf9.setText("&Print...");
		mtf9.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				if (printer == null)
					printer = new Printer();
				inputfiles.get(fileselection).textarea.textarea.print(printer).run();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator1 = new MenuItem(mfsub, SWT.SEPARATOR);
		MenuItem mtf6 = new MenuItem(mfsub, SWT.PUSH);
		mtf6.setText("E&xit");
		mtf6.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				closeallfiles();
				System.exit(0); 
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		mf.setMenu(mfsub);
		sShell.setMenuBar(menuBar);
		sShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {//close all files before exit
			public void shellClosed(org.eclipse.swt.events.ShellEvent e) {
				closeallfiles(); 
				System.exit(0);
			}
		});
	}
	
	/**
	 * This sets up the syntax highlighting.  
	 * 
	 * -ams
	 */
	void AddKeyWords()
	{
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+parameters)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+parameters)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+functions)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+functions)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+molecule([ \\t\\x0B\\f]+|_)types)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+molecule([ \\t\\x0B\\f]+|_)types)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+compartments)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+compartments)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+(seed([ \\t\\x0B\\f]+|_))?species)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+(seed([ \\t\\x0B\\f]+|_))?species)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+observables)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+observables)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+reaction([ \\t\\x0B\\f]+|_)rules)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+reaction([ \\t\\x0B\\f]+|_)rules)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+actions)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+actions)[#\\s]"));
		keywords.add(Pattern.compile("(#.*)[\n\r]"));
		keywords.add(Pattern.compile("\\s+(generate_network)[\\(\\s]"));
		keywords.add(Pattern.compile("\\s+(simulate_ssa)[\\(\\s]"));
		keywords.add(Pattern.compile("\\s+(simulate_ode)[\\(\\s]"));
		keywords.add(Pattern.compile("\\s+(setConcentration)[\\(\\s]"));
		keywords.add(Pattern.compile("\\s+(saveConcentrations)[\\(\\s]"));
		keywords.add(Pattern.compile("\\s+(resetConcentrations)[\\(\\s]"));	
		keywords.add(Pattern.compile("\\s+(setOption)[\\(\\s]"));	
		keywords.add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+model)[#\\s]"));
		keywords.add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+model)[#\\s]"));
		blocknum = 8;
	}
	
	/** 
	 * Create the toolbar.
	 */
	private void createToolBar() {
		toolBar = new ToolBar(sShell, SWT.NONE);
		//toolBar.setFont(globalfont);
		
		ToolItem newButton = new ToolItem(toolBar, SWT.PUSH);
		newButton.setText("New");
		newButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {   
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {   
				newfilediag = new Shell(sShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				newfilediag.setText("New...");
				// set grid layout
				newfilediag.setLayout(new GridLayout(3, true));
				
				// file name
				Label label1 = new Label(newfilediag, SWT.NONE);
				label1.setText("File name:");
				
				newfiletext = new Text(newfilediag, SWT.BORDER);
				GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
				gridData.horizontalSpan = 2;
				newfiletext.setLayoutData(gridData);
				newfiletext.setText("untitled" + emptyFileCount.toString() + ".bngl");
				
				// type
				Label label2 = new Label(newfilediag, SWT.NONE);
				label2.setText("Type:");
				
				// empty file
				emptyfile = new Button(newfilediag, SWT.RADIO);
				emptyfile.setSelection(true);
				emptyfile.setText("Empty File");
				
				// empty label to control layout
				new Label(newfilediag, SWT.NONE).setText("");
				new Label(newfilediag, SWT.NONE).setText("");
				
				// template file
				templatefile = new Button(newfilediag, SWT.RADIO);
				templatefile.setText("Template File");
				
				
				// empty label to control layout
				new Label(newfilediag, SWT.NONE).setText("");
				new Label(newfilediag, SWT.NONE).setText("");

				// ok button
				Button okbutton = new Button(newfilediag, SWT.NONE);
				gridData = new GridData();
				gridData.widthHint = 80;
				okbutton.setText("OK");
				okbutton.setLayoutData(gridData);
				
				// cancel button
				Button cancelbutton = new Button(newfilediag, SWT.NONE);
				cancelbutton.setText("Cancel");
				cancelbutton.setLayoutData(gridData);

				// add listener for ok button
				okbutton.addMouseListener(new MouseListener()
				{
					public void mouseDoubleClick(MouseEvent arg0) {}
					public void mouseDown(MouseEvent arg0) {}
					public void mouseUp(MouseEvent arg0) {
						String temp = null;
						CurrentFile tempfile;
						if(newfiletext.getText().trim().equals(""))
						{
							MessageBox mb = new MessageBox(sShell, SWT.ICON_INFORMATION);
							mb.setMessage("File name can not be empty !");
							mb.setText("Error info");
							mb.open();
							return;
						}
						if(emptyfile.getSelection()) {
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),true);
							emptyFileCount++;
							emptyFileCount = emptyFileCount % 100;
						}
						else
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),false);
						
						// pass the resViewer to current file
						tempfile.setResViewer(resViewer);
						
						inputfiles.add(tempfile);
						fileselection = inputfiles.size()-1;
						textfolder.setSelection(fileselection);
						newfilediag.dispose();
					}
				});
				
				// add listener for cancel button
				cancelbutton.addMouseListener(new MouseListener()
				{
					public void mouseDoubleClick(MouseEvent arg0) {}
					public void mouseDown(MouseEvent arg0) {}
					public void mouseUp(MouseEvent arg0) {
						newfilediag.dispose();	
					}});
				
				
				newfilediag.pack();
				newfilediag.open();
			}
		
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		
		ToolItem openButton = new ToolItem(toolBar, SWT.PUSH);
		openButton.setText("Open");
		openButton.addSelectionListener((new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				FileDialog opendiag = new FileDialog(sShell, SWT.OPEN);
				opendiag.setFilterExtensions(new String[]{"*.bngl","*.txt","*.*"});
				opendiag.open();
				String fpath = opendiag.getFilterPath(), fname = opendiag.getFileName();
				if(fname.equals("")||fname==null)
					return;
				else
				{
					CurrentFile tempfile;
					boolean fileexist = false;
					for(int i = 0;i<inputfiles.size();i++)
						if(inputfiles.get(i).filepath!=null)
							if(inputfiles.get(i).filepath.equals(fpath) && inputfiles.get(i).filename.equals(fname))
								{
									MessageBox mb = new MessageBox(sShell, SWT.ICON_INFORMATION);
									mb.setMessage("File has already been opened !");
									mb.setText("Error info");
									mb.open();
									textfolder.setSelection(i);
									fileexist = true;
									return;
								}
					
					if(!fileexist)
					{
						tempfile = new CurrentFile(fpath,fname,getOstype(),false);
						// pass the resViewer to current file
						tempfile.setResViewer(resViewer);
						
						inputfiles.add(tempfile);
						fileselection = inputfiles.size()-1;
						textfolder.setSelection(fileselection);	
						if(getOstype()==1)
							showfilepath.setText(fpath+"\\"+fname);
						else
							showfilepath.setText(fpath+"/"+fname);
					}
				}
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		}));
		
		ToolItem toolItem1 = new ToolItem(toolBar, SWT.PUSH);
		toolItem1.setText("Save");
		toolItem1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).save();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		ToolItem toolItem9 = new ToolItem(toolBar, SWT.PUSH);
		toolItem9.setText("Save All");
		toolItem9.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection()==null)
					return;
				for(int i=0;i<inputfiles.size();i++)
					inputfiles.get(i).save();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		/*
		ToolItem toolItem2 = new ToolItem(toolBar, SWT.PUSH);
		toolItem2.setText("Find");
		toolItem2.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.find();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});*/
		ToolItem toolItem3 = new ToolItem(toolBar, SWT.PUSH);
		toolItem3.setText("Find");
		toolItem3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.replace();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		ToolItem toolItem4 = new ToolItem(toolBar, SWT.PUSH);
		toolItem4.setText("Contact Map");
		toolItem4.setEnabled(true);
		toolItem4.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).ShowCMap();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {}
		});
		ToolItem toolItem5 = new ToolItem(toolBar, SWT.PUSH);
		toolItem5.setText("Influence Map");
		toolItem5.setEnabled(true);
		toolItem5.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).ShowIMap();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {}
		});
		ToolItem toolItem6 = new ToolItem(toolBar, SWT.PUSH);
		toolItem6.setText("Check");
		toolItem6.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).check();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		ToolItem toolItem7 = new ToolItem(toolBar, SWT.PUSH);
		toolItem7.setText("Run");
		toolItem7.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).run();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		ToolItem toolItem8 = new ToolItem(toolBar, SWT.PUSH);
		toolItem8.setText("Par Scan");
		toolItem8.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).parscan();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
	}

	private void createComposite1() {
		GridData sashdata = new GridData();
		sashdata.widthHint = GridData.FILL_BOTH;
		sashdata.heightHint = GridData.FILL_BOTH;
		sashdata.grabExcessHorizontalSpace = true;
		sashdata.grabExcessVerticalSpace = true;
		composite1 = new Composite(sShell, SWT.NONE);
		composite1.setFont(globalfont);
		composite1.setLayout(new FillLayout());
		createSashForm();
		composite1.setLayoutData(sashdata);	
	}
	
	private void createSashForm() {
		sashForm = new SashForm(composite1, SWT.VERTICAL);	
		sashForm.setFont(globalfont);
		createTextfolder();
		createConsolefolder();
		sashForm.setWeights(new int[]{3,1});
	}
	
	private void createTextfolder() {
		textfolder = new CTabFolder(sashForm, SWT.NONE);
		textfolder.setSimple(true);
		//textfolder.setFont(globalfont);
		textfolder.setBackground(new Color(Display.getCurrent(), 240, 240, 240));
		textfolder.setBounds(new Rectangle(0, 0, 644, 224));
		
		//add listener for textfolder
		textfolder.addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent arg0) {
				String filename = "untitled" + emptyFileCount.toString() + ".bngl";
				CurrentFile tempfile = new CurrentFile(null,filename,getOstype(),true);
				emptyFileCount++;
				emptyFileCount = emptyFileCount % 100;
				
				// pass the resViewer to current file
				tempfile.setResViewer(resViewer);
				
				inputfiles.add(tempfile);
				fileselection = inputfiles.size()-1;
				textfolder.setSelection(fileselection);			
			}

			public void mouseDown(MouseEvent arg0) {
				
			}

			public void mouseUp(MouseEvent arg0) {
				
			}
			
		});
	}
	
	private void createConsolefolder() {
		consolefolder = new TabFolder(sashForm, SWT.NONE);
		consolefolder.setBounds(new Rectangle(0, 227, 644, 224));
		TabItem tabItem = new TabItem(consolefolder, SWT.NONE);
		tabItem.setText("Console");
		console = new Text(consolefolder, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		console.setEditable(false);
		console.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
		tabItem.setControl(console);
	}

	private void createComposite2() {
		GridData tempdata = new GridData();
		tempdata.widthHint = GridData.FILL_BOTH;
		tempdata.grabExcessHorizontalSpace = true;
		//tempdata.grabExcessVerticalSpace = true;
		composite2 = new Composite(sShell, SWT.NONE);
		composite2.setFont(globalfont);
		composite2.setLayoutData(tempdata);
		composite2.setLayout(new FillLayout());
		showfilepath = new Text(composite2, SWT.BORDER);
		showfilepath.setEditable(false);
	}
	
	
	/**
	 * Main method.  Creates the display, sets the font, instantiates the editor,
	 * creates a shell, checks for updates, waits for input.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Display display = Display.getDefault();
		FontData fontdata = display.getSystemFont().getFontData()[0];
		fontdata.setHeight(9);
		globalfont = new Font(display,fontdata);
		BNGEditor thisClass = new BNGEditor();
		thisClass.createSShell();
		thisClass.sShell.open();
		init();
		
		
		checkUpdate();
		
		
		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		display.dispose();
	}
	
	/**
	 * checkUpdate retrieves the version file from the server (right now
	 * on my website) to see if there is a new version available.  If there 
	 * is a new version, a shell is created and the user can choose to
	 * travel to the page.
	 * 
	 * @author Adam M. Smith
	 */
	private static void checkUpdate()
	{
		URL updateFileURL = null;
		URLConnection con = null;
		InputStream inStream = null;
		String currentVersionString = null;
		String changes = "";
		File xmlFile = null;
		BufferedReader input = null;
		
		try 
		{
			// Create a URL
			updateFileURL =
				new URL("http://vis.cs.pitt.edu/resources/docs/versions.vt");
			
			// Create a connection using the url
			con = updateFileURL.openConnection();
			
			// Not sure what this does.
			//con.setDoInput(true);
			
			// Get the input stream from the connection and URL
			inStream = con.getInputStream();
			
			// Create a buffered reader from the input stream of the
			// connection and url
			input =
				new BufferedReader(new InputStreamReader(inStream));
			
		} 
		catch (MalformedURLException e) 
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	
		// Get the line from the file .
	    // The line is just the version of the most recent software.
		ArrayList<VersionTracker> versions = new ArrayList<VersionTracker>();
		
		String readVersion = null;
		String readChanges = null;
		
		try {
			while(input.ready())
			{
				System.out.println("input is ready");
				String currentLine = input.readLine();
				
				if(currentLine.toLowerCase().contains("version="))
				{
					readVersion = currentLine.split("\"")[1];
				}
				
				readChanges="";
				
				currentLine = input.readLine();
				
				if(currentLine.toLowerCase().contains("changes="))
				{
					readChanges += currentLine.substring(currentLine.indexOf('\"')+1);
					
					
					if(readChanges.charAt(readChanges.length()-1) != '\"')
					{
						do 
						{
						  currentLine = input.readLine().trim();
						  readChanges += currentLine+"\n";
						  
						}  while(currentLine != null && currentLine.charAt(currentLine.length()-1) != '\"');
					}
					
					readChanges = readChanges.substring(0, readChanges.length()-1);
				}
			
				versions.add(new VersionTracker(readVersion, readChanges));
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String allNewChanges = "";
		boolean foundfirst = false;
		
		for(VersionTracker vt : versions)
		{
			// If the version on the server is older,
			// then call getNewVersionDialogue
			if(vt.compare() < 0 && !foundfirst)
			{
				foundfirst = true;
				allNewChanges += vt.versionString+vt.changesString;
			}
			else if(vt.compare() < 0)
			{
				allNewChanges += "\n\n"+vt.versionString+vt.changesString;
			}
		}
		
		if(foundfirst)
		{
			NewVersionWindow.run(Display.getDefault(), allNewChanges);
		}
	}
	
	
	void closeallfiles()
	{
		if(textfolder.getSelection() == null)
			return;
		while(fileselection!=-1)
		{
			//System.out.println(fileselection);
			inputfiles.get(fileselection).close();
			inputfiles.remove(fileselection);	
			textfolder.getItem(fileselection).dispose();
			for(int i=0;i<inputfiles.size();i++)
				if(inputfiles.get(i).tabitem.equals(textfolder.getSelection()))
				{
					fileselection = i;
					break;
				}	
			if(fileselection>=inputfiles.size())
			{
				fileselection = -1;
				showfilepath.setText("");
				return;
			}
			if(inputfiles.get(fileselection).filepath==null)
				showfilepath.setText("");
			else
				if(getOstype() == 1)
					showfilepath.setText(inputfiles.get(fileselection).filepath+"\\"+inputfiles.get(fileselection).filename);
				else
					showfilepath.setText(inputfiles.get(fileselection).filepath+"/"+inputfiles.get(fileselection).filename);
		}
	}
	
	static void init()
	{
		textfolder.setFocus();
		textfolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0) {
				fileselection = textfolder.getSelectionIndex();
				if(inputfiles.get(fileselection).filepath==null)
					showfilepath.setText("");
				else
					if(getOstype() == 1)
						showfilepath.setText(inputfiles.get(fileselection).filepath+"\\"+inputfiles.get(fileselection).filename);
					else
						showfilepath.setText(inputfiles.get(fileselection).filepath+"/"+inputfiles.get(fileselection).filename);
			}
		});
		
		textfolder.addCTabFolder2Listener(new CTabFolder2Adapter()
		{
			public void close(CTabFolderEvent arg0) {
				for(int i=0;i<inputfiles.size();i++)
					if(inputfiles.get(i).tabitem.equals(arg0.item))
					{
						inputfiles.get(i).close();
						inputfiles.remove(i);
						break;
					}
				for(int i=0;i<inputfiles.size();i++)
					if(inputfiles.get(i).tabitem.equals(textfolder.getSelection()))
					{
						fileselection = i;
						break;
					}	
				if(fileselection>=inputfiles.size())
				{
					fileselection = -1;
					showfilepath.setText("");
					return;
				}
				if(inputfiles.get(fileselection).filepath==null)
					showfilepath.setText("");
				else
					if(getOstype() == 1)
						showfilepath.setText(inputfiles.get(fileselection).filepath+"\\"+inputfiles.get(fileselection).filename);
					else
						showfilepath.setText(inputfiles.get(fileselection).filepath+"/"+inputfiles.get(fileselection).filename);
			}
		});
		
		showfilepath.addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				if(fileselection!=-1)
					inputfiles.get(fileselection).textarea.textarea.setFocus();
				else
					textfolder.setFocus();
			}
		});
	}
	
	public static void setbngpath()
	{
		BNGPathShell = new Shell(sShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		BNGPathShell.setText("Set BNG Path");
		// set grid layout
		BNGPathShell.setLayout(new GridLayout(4, true));
		
		GridData gridData = null;
		
		// current BNG path
		Label BNGPathlabel1 = new Label(BNGPathShell, SWT.NONE);
		BNGPathlabel1.setText("Current BNG Path:");
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		BNGPathlabel1.setLayoutData(gridData);
		
		BNGPathtext1 = new Text(BNGPathShell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		BNGPathtext1.setLayoutData(gridData);

		if(bngfpath !=null && bngfname != null )
			if(getOstype() == 1)
				BNGPathtext1.setText(bngfpath + "\\" + bngfname);
			else
				BNGPathtext1.setText(bngfpath + "/" + bngfname);
		
		// change button
		Button BNGPathbutton1 = new Button(BNGPathShell, SWT.NONE);
		BNGPathbutton1.setText("Change...");
		gridData = new GridData();
		gridData.widthHint = 100;
		BNGPathbutton1.setLayoutData(gridData);
		
		// add listener for change button
		BNGPathbutton1.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog opendiag = new FileDialog(sShell, SWT.OPEN);
				opendiag.setFilterExtensions(new String[]{"*.pl","*.PL","*.*"});
				opendiag.open();
				String fpath = opendiag.getFilterPath(), fname = opendiag.getFileName();
				if(fname.equals("")||fname==null)
					return;
				if(getOstype() == 1)
					BNGPathtext1.setText(fpath + "\\" + fname);
				else
					BNGPathtext1.setText(fpath + "/" + fname);
			}				
		});
		
		// empty label to control layout
		new Label(BNGPathShell, SWT.NONE).setText("");
		new Label(BNGPathShell, SWT.NONE).setText("");
		
		// ok button
		Button BNGPathbutton2 = new Button(BNGPathShell, SWT.NONE);
		BNGPathbutton2.setText("OK");
		BNGPathbutton2.setLayoutData(gridData);
		
		// cancel button
		Button BNGPathbutton3 = new Button(BNGPathShell, SWT.NONE);
		BNGPathbutton3.setText("Cancel");
		BNGPathbutton3.setLayoutData(gridData);
		
		// add listener for cancel button
		BNGPathbutton3.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				BNGPathShell.dispose();
			}					
		});
		
		// add listener for ok button
		BNGPathbutton2.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				if(BNGPathtext1.getText().trim().length() == 0)
				{
					MessageBox mb = new MessageBox(BNGPathShell, SWT.ICON_INFORMATION);
					mb.setMessage("BNG Path cannot be empty. ");
					mb.setText("Error Info");
					mb.open();
					return;
				}
				else
				{
					PrintWriter pw;
					try {
						String temppath = BNGPathtext1.getText().trim();
						if(getOstype() == 1)
						{
							bngfpath = temppath.substring(0, temppath.lastIndexOf('\\'));
							bngfname = temppath.substring(temppath.lastIndexOf('\\')+1, temppath.length());
						}
						else
						{
							bngfpath = temppath.substring(0, temppath.lastIndexOf('/'));
							bngfname = temppath.substring(temppath.lastIndexOf('/')+1, temppath.length());
						}
						pw = new PrintWriter(new BufferedWriter(new FileWriter("BNGPath")));
						pw.write(bngfpath + "\n" + bngfname + "\n"); 
		                pw.flush();
					} catch (IOException e1) {}   
					BNGPathShell.dispose();
					console.setText("BNG Path Changed !");
				}
			}					
		});
		
		BNGPathShell.pack();
		BNGPathShell.open();
	}
	
	public static void setdotpath()
	{
		DOTPathShell = new Shell(sShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		DOTPathShell.setText("Set DOT Path");
		// set grid layout
		DOTPathShell.setLayout(new GridLayout(4, true));
		
		GridData gridData = null;
	
		// current dot path
		Label DOTPathlabel1 = new Label(DOTPathShell, SWT.NONE);
		DOTPathlabel1.setText("Current DOT Path:");
		gridData = new GridData();
		gridData.horizontalSpan = 4;
		DOTPathlabel1.setLayoutData(gridData);
		
		DOTPathtext1 = new Text(DOTPathShell, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 3;
		DOTPathtext1.setLayoutData(gridData);
		
		if(getDOTfpath() !=null && getDOTfname() != null )
			if(getOstype() == 1)
				DOTPathtext1.setText(getDOTfpath() + "\\" + getDOTfname());
			else
				DOTPathtext1.setText(getDOTfpath() + "/" + getDOTfname());
		else
			if(getOstype() != 1)
				DOTPathtext1.setText("/usr/local/bin/dot");
		
		// change button
		Button DOTPathbutton1 = new Button(DOTPathShell, SWT.NONE);
		DOTPathbutton1.setText("Change...");
		gridData = new GridData();
		gridData.widthHint = 100;
		DOTPathbutton1.setLayoutData(gridData);
		
		
		// add listener for change button
		DOTPathbutton1.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				FileDialog opendiag = new FileDialog(sShell, SWT.OPEN);
				if(getOstype() == 1)
					opendiag.setFilterExtensions(new String[]{"*.exe","*.EXE","*.*"});
				opendiag.open();
				String fpath = opendiag.getFilterPath(), fname = opendiag.getFileName();
				if(fname.equals("")||fname==null)
					return;
				if(getOstype() == 1)
					DOTPathtext1.setText(fpath + "\\" + fname);
				else
					DOTPathtext1.setText(fpath + "/" + fname);
			}				
		});
		
		// empty label to control layout
		new Label(DOTPathShell, SWT.NONE).setText("");
		new Label(DOTPathShell, SWT.NONE).setText("");
		
		// ok button
		Button DOTPathbutton2 = new Button(DOTPathShell, SWT.NONE);
		DOTPathbutton2.setText("OK");
		DOTPathbutton2.setLayoutData(gridData);
		
		// cancel button
		Button DOTPathbutton3 = new Button(DOTPathShell, SWT.NONE);
		DOTPathbutton3.setText("Cancel");
		DOTPathbutton3.setLayoutData(gridData);
		
		
		// add listener for cancel button
		DOTPathbutton3.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				DOTPathShell.dispose();
			}					
		});
		
		
		// add listener for ok button
		DOTPathbutton2.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				if(DOTPathtext1.getText().trim().length() == 0)
				{
					MessageBox mb = new MessageBox(DOTPathShell, SWT.ICON_INFORMATION);
					mb.setMessage("DOT Path cannot be empty. ");
					mb.setText("Error Info");
					mb.open();
					return;
				}
				else
				{
					PrintWriter pw;
					try {
						String temppath = DOTPathtext1.getText().trim();
						if(getOstype() == 1)
						{
							setDOTfpath(temppath.substring(0, temppath.lastIndexOf('\\')));
							setDOTfname(temppath.substring(temppath.lastIndexOf('\\')+1, temppath.length()));
						}
						else
						{
							setDOTfpath(temppath.substring(0, temppath.lastIndexOf('/')));
							setDOTfname(temppath.substring(temppath.lastIndexOf('/')+1, temppath.length()));
						}
						pw = new PrintWriter(new BufferedWriter(new FileWriter("DOTPath")));
						pw.write(getDOTfpath() + "\n" + getDOTfname() + "\n"); 
		                pw.flush();
					} catch (IOException e1) {}   
					DOTPathShell.dispose();
					console.setText("DOT Path Changed !");
				}
			}					
		});
		
		DOTPathShell.pack();
		DOTPathShell.open();
	}
	
	/*
	 * Set the root directory for the result viewer, and create a result viewer.
	 */
	public static void setResultViewerPath() {
		// slash for different operating system
		String tmpSlash = "";
		if (ostype == 1) {
			tmpSlash = "\\";
		} else {
			tmpSlash = "/";
		}

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
		File resultsFolder = new File(resultsFolderPath);
		if (!resultsFolder.isDirectory()) {
			resultsFolder.mkdir();
		}

		// create a result viewer
		Rectangle bounds = new Rectangle(0, 0, 680, 768);
		Rectangle disBounds = Display.getCurrent().getBounds();
		if (disBounds.width / 2 > 800) {
			bounds.width = 800;
		}

		if (resViewer == null || resViewer.getShell() == null) {
			resViewer = new ResultViewer(resultsFolderPath, "", "", bounds);
			
			for (int i = 0; i < inputfiles.size(); i++) {
				inputfiles.get(i).setResViewer(resViewer);
			}

			// open
			// resViewer.setBlockOnOpen(true);
			resViewer.open();

			if (resViewer != null && resViewer.getGraphFrame() != null
					&& resViewer.getGraphFrame().isShowing()) {
				resViewer.getGraphFrame().dispose(); // close the graph
			}
			
			for (int i = 0; i < inputfiles.size(); i++) {
				inputfiles.get(i).setResViewer(resViewer);
			}
		}

	}

	public static ResultViewer getResViewer() {
		return resViewer;
	}

	public static void setResViewer(ResultViewer resViewer) {
		BNGEditor.resViewer = resViewer;
		for (int i = 0; i < inputfiles.size(); i++) {
			inputfiles.get(i).setResViewer(resViewer);
		}
	}

	public static void setOstype(int ostype) {
		BNGEditor.ostype = ostype;
	}

	public static int getOstype() {
		return ostype;
	}

	public static void setDOTfname(String dOTfname) {
		DOTfname = dOTfname;
	}

	public static String getDOTfname() {
		return DOTfname;
	}

	public static void setDOTfpath(String dOTfpath) {
		DOTfpath = dOTfpath;
	}

	public static String getDOTfpath() {
		return DOTfpath;
	}


	/*
	 * After "save as", close the original one, open the new saved one
	 */
	public static void replaceInputfiles(CurrentFile oldF, CurrentFile newF) {
		int oldIndex = inputfiles.indexOf(oldF);
		textfolder.getItem(oldIndex).dispose();
		inputfiles.set(oldIndex, newF);
		textfolder.setSelection(oldIndex);
		if (ostype == 1) {
			showfilepath.setText(newF.filepath + "\\" + newF.filename);
		}
		else {
			showfilepath.setText(newF.filepath + "/" + newF.filename);
		}
	}
	
	

}