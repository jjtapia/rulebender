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
import java.io.PrintWriter;
import java.util.ArrayList;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Group;

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
	private static int ostype;
	static String bngfpath = null,bngfname = null;  //  @jve:decl-index=0:
	private static String DOTfpath = null;
	private static String DOTfname = null;
	
	Text newfiletext;
	Button templatefile;
	Button emptyfile;
	Shell newfilediag;
	Combo fontcombo1;
	Shell fontShell;
	static Shell BNGPathShell;
	static Text BNGPathtext1;
	static Shell DOTPathShell;
	static Text DOTPathtext1;
	private Printer printer;
	
	static Menu popmenu; 
	static ArrayList<Pattern> keywords = new ArrayList<Pattern>();  //  @jve:decl-index=0:
	static Font globalfont;
	static int blocknum;
	
	// shell for result viewer path
	static Shell ViewerPathShell;
	
	static ResultViewer resViewer;
	
	
	/**
	 * Basically everything in this method is for setting up menus, buttons, 
	 * and the appropriate actionListeners and activities.
	 * -ams
	 */
	private void createSShell() {
		String stemp = System.getProperty("os.name");
 	    if(stemp.contains("Windows") || stemp.contains("WINDOWS") || stemp.contains("windows"))
 		    setOstype(1);
 	    else 
 		    setOstype(2);
		
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
		
		/*
		 * **************************************************
		 *  Add menu item to open the result viewer directly.
		 * **************************************************
		 */
		MenuItem sopenViewerSeperator = new MenuItem(mvsub, SWT.SEPARATOR);
		MenuItem openViewerItem = new MenuItem(mvsub, SWT.PUSH);
		openViewerItem.setText("&Open Result Viewer");
		openViewerItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				setResultViewerPath();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		
		mv.setMenu(mvsub);
		mosub = new Menu(mo);
		MenuItem mo1 = new MenuItem(mosub, SWT.PUSH);
		mo1.setText("&Font...");
		mo1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				fontShell = new Shell(sShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				fontShell.setFont(globalfont);
				fontShell.setText("Font...");
				fontShell.setSize(new Point(252, 123));
				fontShell.setLayout(null);
				Label fontlabel1 = new Label(fontShell, SWT.NONE);
				fontlabel1.setFont(globalfont);
				fontlabel1.setBounds(new Rectangle(27, 18, 89, 22));
				fontlabel1.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
				fontlabel1.setText("Set Font Size :");
				fontcombo1 = new Combo(fontShell, SWT.NONE);
				fontcombo1.setFont(globalfont);
				fontcombo1.setText("10");
				fontcombo1.setBounds(new Rectangle(27, 47, 89, 23));
				fontcombo1.add("10");
				fontcombo1.add("12");
				fontcombo1.add("14");
				fontcombo1.add("16");
				fontcombo1.add("18");
				fontcombo1.add("24");
				fontcombo1.add("36");
				Button fontbutton1 = new Button(fontShell, SWT.NONE);
				fontbutton1.setFont(globalfont);
				fontbutton1.setBounds(new Rectangle(137, 18, 67, 22));
				fontbutton1.setText("OK");
				fontbutton1.addSelectionListener(new SelectionListener()
				{
					public void widgetDefaultSelected(SelectionEvent arg0) {}
					public void widgetSelected(SelectionEvent arg0) {
						int fontsize;
						MessageBox mb;
						try{
							fontsize = Integer.parseInt(fontcombo1.getText());
							if(fontsize<6 || fontsize>50)
							{
								mb = new MessageBox(fontShell, SWT.ICON_INFORMATION);
								mb.setMessage("Font Size too big or small, font size should be between 6 and 50.");
								mb.setText("Error Info");
								mb.open();
								return;
							}
							inputfiles.get(fileselection).textarea.textarea.setFont(new Font(Display.getCurrent(), "SEGOE UI", fontsize, SWT.NONE));
							inputfiles.get(fileselection).textarea.arc1.redraw();
							inputfiles.get(fileselection).textarea.arc2.redraw();
							fontShell.dispose();
						}catch(NumberFormatException e)
						{
							mb = new MessageBox(fontShell, SWT.ICON_INFORMATION);
							mb.setMessage("Error recognizing font size, please enter digits only !");
							mb.setText("Error Info");
							mb.open();
						}
					}	
				});
				Button fontbutton2 = new Button(fontShell, SWT.NONE);
				fontbutton2.setFont(globalfont);
				fontbutton2.setBounds(new Rectangle(137, 47, 67, 22));
				fontbutton2.setText("Cancel");
				fontbutton2.addSelectionListener(new SelectionListener()
				{
					public void widgetDefaultSelected(SelectionEvent arg0) {}
					public void widgetSelected(SelectionEvent arg0) {
						fontShell.dispose();
					}
				});
				fontShell.open();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		mo.setMenu(mosub);
		mesub = new Menu(me);
		MenuItem mte1 = new MenuItem(mesub, SWT.PUSH);
		mte1.setText("&Undo\tCtrl+Z");
		mte1.setAccelerator(SWT.CTRL + 'Z');
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
		mte2.setAccelerator(SWT.CTRL + 'X');
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
		mte3.setAccelerator(SWT.CTRL + 'C');
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
		mte4.setAccelerator(SWT.CTRL + 'V');
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
		mte5.setAccelerator(SWT.CTRL + 'F');
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
		mte6.setAccelerator(SWT.CTRL + 'R');
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
		mte7.setAccelerator(SWT.CTRL + 'A');
		mte7.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.selectall();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mte8 = new MenuItem(mesub, SWT.PUSH);
		mte8.setText("&Delete All\tCtrl+D");
		mte8.setAccelerator(SWT.CTRL + 'D');
		mte8.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				inputfiles.get(fileselection).textarea.deleteall();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		me.setMenu(mesub);
		mfsub = new Menu(mf);
		MenuItem mtf1 = new MenuItem(mfsub, SWT.PUSH);
		mtf1.setText("&New...\tCtrl+N");
		mtf1.setAccelerator(SWT.CTRL + 'N');
		mtf1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {   
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {   
				newfilediag = new Shell(sShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				newfilediag.setFont(globalfont);
				newfilediag.setText("New...");
				newfilediag.setSize(new Point(340, 170));
				Button okbutton = new Button(newfilediag, SWT.NONE);
				okbutton.setFont(globalfont);
				okbutton.setBounds(new Rectangle(62, 104, 85, 28));
				okbutton.setLocation(new Point(142,102));
				okbutton.setText("OK");
				okbutton.setFont(globalfont);
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
						if(emptyfile.getSelection())
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),true);
						else
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),false);
						
						inputfiles.add(tempfile);
						fileselection = inputfiles.size()-1;
						textfolder.setSelection(fileselection);
						newfilediag.dispose();
					}
				});
				Button cancelbutton = new Button(newfilediag, SWT.NONE);
				cancelbutton.setFont(globalfont);
				cancelbutton.setLocation(new Point(235, 102));
				cancelbutton.setSize(new Point(87, 28));
				cancelbutton.setText("Cancel");
				cancelbutton.addMouseListener(new MouseListener()
				{
					public void mouseDoubleClick(MouseEvent arg0) {}
					public void mouseDown(MouseEvent arg0) {}
					public void mouseUp(MouseEvent arg0) {
						newfilediag.dispose();	
					}});
				Group group1 = new Group(newfilediag, SWT.NONE);
				group1.setFont(globalfont);
				group1.setBounds(new Rectangle(212, 13, 110, 71));
				emptyfile = new Button(group1, SWT.RADIO);
				emptyfile.setFont(globalfont);
				emptyfile.setBounds(new Rectangle(11, 9, 88, 27));
				emptyfile.setSelection(true);
				emptyfile.setText("Empty File");
				templatefile = new Button(group1, SWT.RADIO);
				templatefile.setFont(globalfont);
				templatefile.setSize(new Point(90, 23));
				templatefile.setText("Template File");
				templatefile.setLocation(new Point(11, 40));
				Label label1 = new Label(newfilediag, SWT.NONE);
				label1.setFont(globalfont);
				label1.setText("Please specify file name:");
				label1.setLocation(new Point(25, 30));
				label1.setSize(new Point(132, 20));
				newfiletext = new Text(newfilediag, SWT.BORDER);
				newfiletext.setFont(globalfont);
				newfiletext.setBounds(new Rectangle(25, 48, 176, 23));
				newfiletext.setLocation(new Point(25,55));
				newfilediag.open();
			}
		
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mtf3 = new MenuItem(mfsub, SWT.PUSH);
		mtf3.setText("&Open...\tCtrl+O");
		mtf3.setAccelerator(SWT.CTRL + 'O');
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
		mtf4.setAccelerator(SWT.CTRL + 'S');
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
	 * Not entirely sure what this is doing.  It adds compiled regex
	 * phrases to the 'keywords' arraylist
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
	 * -ams
	 */
	private void createToolBar() {
		toolBar = new ToolBar(sShell, SWT.NONE);
		//toolBar.setFont(globalfont);
		
		ToolItem newButton = new ToolItem(toolBar, SWT.PUSH);
		newButton.setText("New");
		newButton.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {   
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {   
				newfilediag = new Shell(sShell,SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
				newfilediag.setFont(globalfont);
				newfilediag.setText("New...");
				newfilediag.setSize(new Point(340, 170));
				Button okbutton = new Button(newfilediag, SWT.NONE);
				okbutton.setFont(globalfont);
				okbutton.setBounds(new Rectangle(62, 104, 85, 28));
				okbutton.setLocation(new Point(142,102));
				okbutton.setText("OK");
				okbutton.setFont(globalfont);
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
						if(emptyfile.getSelection())
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),true);
						else
							tempfile = new CurrentFile(temp,newfiletext.getText(),getOstype(),false);
						
						inputfiles.add(tempfile);
						fileselection = inputfiles.size()-1;
						textfolder.setSelection(fileselection);
						newfilediag.dispose();
					}
				});
				Button cancelbutton = new Button(newfilediag, SWT.NONE);
				cancelbutton.setFont(globalfont);
				cancelbutton.setLocation(new Point(235, 102));
				cancelbutton.setSize(new Point(87, 28));
				cancelbutton.setText("Cancel");
				cancelbutton.addMouseListener(new MouseListener()
				{
					public void mouseDoubleClick(MouseEvent arg0) {}
					public void mouseDown(MouseEvent arg0) {}
					public void mouseUp(MouseEvent arg0) {
						newfilediag.dispose();	
					}});
				Group group1 = new Group(newfilediag, SWT.NONE);
				group1.setFont(globalfont);
				group1.setBounds(new Rectangle(212, 13, 110, 71));
				emptyfile = new Button(group1, SWT.RADIO);
				emptyfile.setFont(globalfont);
				emptyfile.setBounds(new Rectangle(11, 9, 88, 27));
				emptyfile.setSelection(true);
				emptyfile.setText("Empty File");
				templatefile = new Button(group1, SWT.RADIO);
				templatefile.setFont(globalfont);
				templatefile.setSize(new Point(90, 23));
				templatefile.setText("Template File");
				templatefile.setLocation(new Point(11, 40));
				Label label1 = new Label(newfilediag, SWT.NONE);
				label1.setFont(globalfont);
				label1.setText("Please specify file name:");
				label1.setLocation(new Point(25, 30));
				label1.setSize(new Point(132, 20));
				newfiletext = new Text(newfilediag, SWT.BORDER);
				newfiletext.setFont(globalfont);
				newfiletext.setBounds(new Rectangle(25, 48, 176, 23));
				newfiletext.setLocation(new Point(25,55));
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
		});
		ToolItem toolItem3 = new ToolItem(toolBar, SWT.PUSH);
		toolItem3.setText("Replace");
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
	
	public static void main(String[] args) {
		Display display = Display.getDefault();
		FontData fontdata = display.getSystemFont().getFontData()[0];
		fontdata.setHeight(9);
		globalfont = new Font(display,fontdata);
		BNGEditor thisClass = new BNGEditor();
		thisClass.createSShell();
		thisClass.sShell.open();
		init();

		while (!thisClass.sShell.isDisposed()) {
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		display.dispose();
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
		BNGPathShell.setFont(globalfont);
		BNGPathShell.setText("Set BNG Path");
		BNGPathShell.setSize(new Point(381, 159));
		BNGPathShell.setLayout(null);
		Label BNGPathlabel1 = new Label(BNGPathShell, SWT.NONE);
		BNGPathlabel1.setFont(globalfont);
		BNGPathlabel1.setBounds(new Rectangle(15, 16, 124, 21));
		BNGPathlabel1.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		BNGPathlabel1.setText("Current BNG Path:");
		BNGPathtext1 = new Text(BNGPathShell, SWT.BORDER);
		BNGPathtext1.setFont(globalfont);
		BNGPathtext1.setBounds(new Rectangle(15, 46, 241, 23));
		if(bngfpath !=null && bngfname != null )
			if(getOstype() == 1)
				BNGPathtext1.setText(bngfpath + "\\" + bngfname);
			else
				BNGPathtext1.setText(bngfpath + "/" + bngfname);
		Button BNGPathbutton1 = new Button(BNGPathShell, SWT.NONE);
		BNGPathbutton1.setFont(globalfont);
		BNGPathbutton1.setBounds(new Rectangle(274, 45, 68, 23));
		BNGPathbutton1.setText("Change...");
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
		Button BNGPathbutton3 = new Button(BNGPathShell, SWT.NONE);
		BNGPathbutton3.setFont(globalfont);
		BNGPathbutton3.setBounds(new Rectangle(274, 82, 68, 23));
		BNGPathbutton3.setText("Cancel");
		BNGPathbutton3.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				BNGPathShell.dispose();
			}					
		});
		Button BNGPathbutton2 = new Button(BNGPathShell, SWT.NONE);
		BNGPathbutton2.setFont(globalfont);
		BNGPathbutton2.setBounds(new Rectangle(189, 82, 68, 23));
		BNGPathbutton2.setText("OK");
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
		BNGPathShell.open();
	}
	
	public static void setdotpath()
	{
		DOTPathShell = new Shell(sShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		DOTPathShell.setFont(globalfont);
		DOTPathShell.setText("Set DOT Path");
		DOTPathShell.setSize(new Point(381, 159));
		DOTPathShell.setLayout(null);
		Label DOTPathlabel1 = new Label(DOTPathShell, SWT.NONE);
		DOTPathlabel1.setFont(globalfont);
		DOTPathlabel1.setBounds(new Rectangle(15, 16, 124, 21));
		DOTPathlabel1.setFont(new Font(Display.getDefault(), "Segoe UI", 10, SWT.NORMAL));
		DOTPathlabel1.setText("Current DOT Path:");
		DOTPathtext1 = new Text(DOTPathShell, SWT.BORDER);
		DOTPathtext1.setFont(globalfont);
		DOTPathtext1.setBounds(new Rectangle(15, 46, 241, 23));
		if(getDOTfpath() !=null && getDOTfname() != null )
			if(getOstype() == 1)
				DOTPathtext1.setText(getDOTfpath() + "\\" + getDOTfname());
			else
				DOTPathtext1.setText(getDOTfpath() + "/" + getDOTfname());
		else
			if(getOstype() != 1)
				DOTPathtext1.setText("/usr/local/bin/dot");
		Button DOTPathbutton1 = new Button(DOTPathShell, SWT.NONE);
		DOTPathbutton1.setFont(globalfont);
		DOTPathbutton1.setBounds(new Rectangle(274, 45, 68, 23));
		DOTPathbutton1.setText("Change...");
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
		Button DOTPathbutton3 = new Button(DOTPathShell, SWT.NONE);
		DOTPathbutton3.setFont(globalfont);
		DOTPathbutton3.setBounds(new Rectangle(274, 82, 68, 23));
		DOTPathbutton3.setText("Cancel");
		DOTPathbutton3.addSelectionListener(new SelectionListener()
		{
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				DOTPathShell.dispose();
			}					
		});
		Button DOTPathbutton2 = new Button(DOTPathShell, SWT.NONE);
		DOTPathbutton2.setFont(globalfont);
		DOTPathbutton2.setBounds(new Rectangle(189, 82, 68, 23));
		DOTPathbutton2.setText("OK");
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
		DOTPathShell.open();
	}
	
	//TODO
	/*
	 * Set the root directory for the result viewer, and create a result viewer.
	 */
	public static void setResultViewerPath() {
		ViewerPathShell = new Shell(sShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		ViewerPathShell.setText("Enter Directory Path to Open Result Viewer");
		
		// set layout
		ViewerPathShell.setLayout(new GridLayout(5, true));
		
		/* create content on the shell */
		
		//label
		new Label(ViewerPathShell, SWT.NONE).setText("Directory Path: ");
		
		// text to enter the directory path
		final Text dirPath = new Text(ViewerPathShell, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_BOTH);
	    data.horizontalSpan = 3;
	    dirPath.setLayoutData(data);
	    
	    // button to choose the directory
	    Button chooseBnt = new Button(ViewerPathShell, SWT.PUSH);
	    chooseBnt.setText("Choose");
	    
	    chooseBnt.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	            // User has selected a directory
	            DirectoryDialog dlg = new DirectoryDialog(ViewerPathShell, SWT.OPEN);
	            String fn = dlg.open();
	            if (fn != null) {
	            	dirPath.setText(fn);
	            }
	          }
	    });
	    
	    Label tmpLabel = new Label(ViewerPathShell, SWT.NONE);
	    tmpLabel.setText(" ");
	    tmpLabel.setLayoutData(data);
	    
	    // OK button
	    Button okBnt = new Button(ViewerPathShell, SWT.PUSH);
	    okBnt.setText("Open");
	    okBnt.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		
	    		// close the current one
	    		if (resViewer != null && resViewer.getShell() != null
						&& resViewer.getShell().isVisible() == true) {
					resViewer.getShell().dispose();

					if (resViewer.getGraphFrame() != null
							&& resViewer.getGraphFrame().isShowing()) {
						resViewer.getGraphFrame().dispose(); // close the graph
					}
				}    		
	    		//create a result viewer
	    		resViewer = new ResultViewer(dirPath.getText(), "");
	    		
	    		// close the path dialog
	    		ViewerPathShell.dispose();
	    		
	    		// open
	    		resViewer.setBlockOnOpen(true);
				resViewer.open();

				if (resViewer.getGraphFrame() != null
						&& resViewer.getGraphFrame().isShowing()) {
					resViewer.getGraphFrame().dispose(); // close the graph
				}
	          }
	    });

	    
	    // CANCEL button
	    Button cancelBnt = new Button(ViewerPathShell, SWT.PUSH);
	    cancelBnt.setText("Cancel");
	    cancelBnt.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent event) {
	    		// close the path dialog
	    		ViewerPathShell.dispose();
	          }
	    });
	    
		
		// pack and open
		ViewerPathShell.pack();
		ViewerPathShell.open();
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

}