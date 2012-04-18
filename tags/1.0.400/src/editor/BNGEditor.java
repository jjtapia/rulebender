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

import javax.swing.JOptionPane;

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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.SashForm;

import action.ActionFrameFactory;
import action.ActionInterface;
import action.ActionListCreator;
import action.ActionWindowView;
import action.OpenAction;
import action.ParameterScanAction;
import action.RunSimulationAction;
import action.ToolBarActionListCreator;

import editor.action.ActionHub;
import editor.parameterscan.ParameterScanController;
import editor.parameterscan.ParameterScanView;
import editor.version.VersionCheckerThread;
import resultviewer.ui.ResultViewer;
import visualizationviewer.VisualizationViewerController;

public class BNGEditor 
{	
	
	private static BNGEditor editor;

	private static Text console; 
	
	// Visual Elements
	private static Shell mainEditorShell = null;  //  @jve:decl-index=0:visual-constraint="-1,21"
		
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
	
	private static CTabFolder textfolder = null;
	private static Group consoleGroup = null;
	
	private Composite composite2 = null;
	
	private static Text showfilepath = null;
	
	private static int fileselection = -1;
	
	private static ArrayList<CurrentFile> inputfiles= new ArrayList<CurrentFile>();  //  @jve:decl-index=0:
	
	private Printer printer;
	
	private static Menu popmenu; 
	private static ArrayList<Pattern> keywords = new ArrayList<Pattern>();  //  @jve:decl-index=0:
	private static Font globalfont;
	private static int blocknum;
	
	private static ResultViewer resViewer;
	private static VisualizationViewerController visViewController;
	
	private Integer emptyFileCount = 0;
	
	private static ActionHub actionHub;
	
	/**
	 * Singleton
	 */
	private BNGEditor()
	{
		editor = this;
		
		
	}
	
	/**
	 * Factory for singleton
	 * @return
	 */
	public static synchronized BNGEditor getEditor()
	{
		if (editor == null)
			return new BNGEditor();
		
		return editor;
	}
	
	
	/**
	 * Basically everything in this method is for setting up menus, buttons, 
	 * and the appropriate actionListeners and activities.
	 * -ams
	 */
	private void createSShell() {
		
	    // Create the shell
		mainEditorShell = new Shell();
		
		// Set the font object
		mainEditorShell.setFont(getGlobalfont());
		mainEditorShell.setText("Model Editor");

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		mainEditorShell.setLayout(gridLayout);
		mainEditorShell.setSize(new Point(600, 600));
		
 	    
		createToolBar();
		createComposite2();
		createComposite1();
		
		AddKeyWords();
		
		setupEditorContextMenu();
    	
		setupMenuBar();
		
		actionHub = ActionHub.getActionHub();
		
		actionHub.log("Created hub from swt");
		
	}
	
	
	/**
	 * Constructs the menu bar.  Appears at the top of the screen for osx, or 
	 * below the titlebar linux and windows.
	 * 
	 */
	private void setupMenuBar()
	{
		Menu menuBar = null;
		menuBar = new Menu(mainEditorShell, SWT.BAR);
		MenuItem mf = new MenuItem(menuBar, SWT.CASCADE);
		mf.setText("&File");
		MenuItem me = new MenuItem(menuBar, SWT.CASCADE);
		me.setText("&Edit");
		MenuItem mo = new MenuItem(menuBar, SWT.CASCADE);
		mo.setText("F&ormat");
		MenuItem mv = new MenuItem(menuBar, SWT.CASCADE);
		mv.setText("&View");
		MenuItem mr = new MenuItem(menuBar, SWT.CASCADE);
		mr.setText("&Actions");
		MenuItem mh = new MenuItem(menuBar, SWT.CASCADE);
		mh.setText("&Help");
		
		MenuItem optionsMenuItem = new MenuItem(menuBar, SWT.CASCADE);
		optionsMenuItem.setText("O&ptions");
		
		/*
		 * Make a submenu to hold all of the options
		 */
		Menu optionsSubMenu = new Menu(optionsMenuItem);
		
		// Add the set BioNetGen path option
		MenuItem setBNGPathMenuItem = new MenuItem(optionsSubMenu, SWT.PUSH);
		setBNGPathMenuItem.setText("Set &BioNetGen Path");
		setBNGPathMenuItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) 
				{
					(new SetBNGPathDialogue(mainEditorShell, editor)).show();
				}
				public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) 
				{
					// Do nothing.
				}
			});
		
		// Add the set workspace path option
		MenuItem setWorkspaceMenuItem = new MenuItem(optionsSubMenu, SWT.PUSH);
		setWorkspaceMenuItem.setText("Set &Workspace Directory");
		setWorkspaceMenuItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
				public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) 
				{
					(new SetWorkspaceDialogue(mainEditorShell, editor, true)).show();
				}
				public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) 
				{
					// Do nothing.
				}
			});
		optionsMenuItem.setMenu(optionsSubMenu);
		
		mhsub = new Menu(mh);
		MenuItem mth1 = new MenuItem(mhsub, SWT.PUSH);
		mth1.setText("&About RuleBender");
		mth1.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				MessageBox mb = new MessageBox(mainEditorShell, SWT.ICON_INFORMATION);
				mb.setMessage("RuleBender written by\n\nAdam Smith, Wen Xu, Yao Sun\nDept. of Computer Science\nUniv. of Pittsburgh");
				mb.setText("About RuleBender");
				mb.open();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem tutorial = new MenuItem(mhsub, SWT.PUSH);
		tutorial.setText("Online Tutorial");
		tutorial.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) 
			{
				// Do nothing
			}

			public void widgetSelected(SelectionEvent arg0) 
			{	
				Program.launch("http://rulebender.cs.pitt.edu/html/tutorial.html");	
			}
		});
		mh.setMenu(mhsub);
		
		/*
		 * Begin the actions dropdown menu 
		 */
		
		mrsub = new Menu(mr);
		// Get the user defined actions.
		ArrayList<ActionInterface> actions = ActionListCreator.getActionItems();
		
		// For each user defined action in the list, add a button to the drop
		// down menu.
		for(final ActionInterface ai : actions)
		{
			MenuItem newActionMenuItem = new MenuItem(mrsub, SWT.PUSH);
			newActionMenuItem.setText(ai.getName());
			newActionMenuItem.addSelectionListener(new SelectionListener(){

				public void widgetDefaultSelected(SelectionEvent e){}

				public void widgetSelected(SelectionEvent e) 
				{
					if(textfolder.getSelection() == null)
						return;
					if(ai.hasComposite())
						ActionFrameFactory.constructAndShowActionFrame(ai);
					else
						ai.executeAction();
				}
			});
		}
		
		/*
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
		*/
		mr.setMenu(mrsub);
		/*
		 * End the actions dropdown menu 
		 */
		
		mvsub = new Menu(mv);
//		MenuItem separator7 = new MenuItem(mvsub, SWT.SEPARATOR);
		
		MenuItem mv1 = new MenuItem(mvsub, SWT.PUSH);
		
		mv1.setText("&Visualization Viewer");
		mv1.setEnabled(true);
		mv1.addSelectionListener(new org.eclipse.swt.events.SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent arg0) {}
			public void widgetSelected(SelectionEvent arg0) {
				
				if(textfolder.getSelection() == null)
					return;
				
				visViewController.openVisualizationViewerFrame();//inputfiles.get(fileselection));
				
			}
		});
		
		
		// Add menu item to open the result viewer directly.
		
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
				
				FontDialog fontdlg = new FontDialog(mainEditorShell);
				FontData fontData = fontdlg.open();
			    if (fontData != null) {
			      Font font = new Font(mainEditorShell.getDisplay(), fontData);
			      getInputfiles().get(getFileselection()).getTextarea().textarea.setFont(font);
					getInputfiles().get(getFileselection()).getTextarea().arc1.redraw();
					getInputfiles().get(getFileselection()).getTextarea().arc2.redraw();
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
				getInputfiles().get(getFileselection()).getTextarea().Undo();
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
				getInputfiles().get(getFileselection()).getTextarea().cut();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mte3 = new MenuItem(mesub, SWT.PUSH);
		mte3.setText("&Copy\tCtrl+C");
		//mte3.setAccelerator(SWT.MOD1 + 'C');
		mte3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).getTextarea().copy();
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
				getInputfiles().get(getFileselection()).getTextarea().paste();
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
				getInputfiles().get(getFileselection()).getTextarea().find();
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
				getInputfiles().get(getFileselection()).getTextarea().replace();
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
				getInputfiles().get(getFileselection()).getTextarea().selectall();
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
		MenuItem newFileMenuItem = new MenuItem(mfsub, SWT.PUSH);
		newFileMenuItem.setText("&New...\tCtrl+N");
		newFileMenuItem.setAccelerator(SWT.MOD1 + 'N');
		
		final BNGEditor tempthis = this;
		newFileMenuItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {   
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {   
				NewFileDialogue newfilediag = new NewFileDialogue(mainEditorShell, tempthis);
				newfilediag.show();
			}

			public void widgetDefaultSelected(SelectionEvent arg0) {
				// TODO Auto-generated method stub
				
			}});
		
		MenuItem mtf3 = new MenuItem(mfsub, SWT.PUSH);
		mtf3.setText("&Open...\tCtrl+O");
		mtf3.setAccelerator(SWT.MOD1 + 'O');
		mtf3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) 
			{
				(new OpenAction()).executeAction();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) 
			{
			}
		});
		MenuItem separator8 = new MenuItem(mfsub, SWT.SEPARATOR);
		MenuItem saveMenuItem = new MenuItem(mfsub, SWT.PUSH);
		saveMenuItem.setText("&Save\tCtrl+S");
		saveMenuItem.setAccelerator(SWT.MOD1 + 'S');
		saveMenuItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).save();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem saveAsMenuItem = new MenuItem(mfsub, SWT.PUSH);
		saveAsMenuItem.setText("Save &As...");
		saveAsMenuItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).saveas();
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
				for(int i=0;i<getInputfiles().size();i++)
					getInputfiles().get(i).save();
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
				getInputfiles().get(getFileselection()).close();
				getInputfiles().remove(getFileselection());	
				textfolder.getItem(getFileselection()).dispose();	
				for(int i=0;i<getInputfiles().size();i++)
					if(getInputfiles().get(i).tabitem.equals(textfolder.getSelection()))
					{
						setFileselection(i);
						// Tell the viewcontroller about the new focus file.
						visViewController.fileBecomesFocus(getInputfiles().get(getFileselection()));
						
						break;
					}	
				if(getFileselection()>=getInputfiles().size())
				{
					setFileselection(-1);
					getShowfilepath().setText("");
					// Tell the viewcontroller that there are no open files.
					visViewController.fileBecomesFocus(null);
					return;
				}
				if(getInputfiles().get(getFileselection()).getFilepath()==null)
					getShowfilepath().setText("");
				else
					if(ConfigurationManager.getConfigurationManager().getOSType() == 1)
						getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"\\"+getInputfiles().get(getFileselection()).getFilename());
					else
						getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"/"+getInputfiles().get(getFileselection()).getFilename());
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
				getInputfiles().get(getFileselection()).getTextarea().textarea.print(printer).run();
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
		mainEditorShell.setMenuBar(menuBar);
		mainEditorShell.addShellListener(new org.eclipse.swt.events.ShellAdapter() {//close all files before exit
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
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+parameters)[#\\s]")); //1
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+parameters)[#\\s]")); //2
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+functions)[#\\s]")); //3
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+functions)[#\\s]")); //4
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+molecule([ \\t\\x0B\\f]+|_)types)[#\\s]")); //5
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+molecule([ \\t\\x0B\\f]+|_)types)[#\\s]")); //6
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+compartments)[#\\s]")); //7
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+compartments)[#\\s]")); //8
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+(seed([ \\t\\x0B\\f]+|_))?species)[#\\s]")); //9
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+(seed([ \\t\\x0B\\f]+|_))?species)[#\\s]")); //10
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+observables)[#\\s]")); //11
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+observables)[#\\s]")); //12
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+reaction([ \\t\\x0B\\f]+|_)rules)[#\\s]")); //13
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+reaction([ \\t\\x0B\\f]+|_)rules)[#\\s]")); //14
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+actions)[#\\s]")); //15
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+actions)[#\\s]")); //16
		
		// the 17th one, comment, the index number cannot be changed
		getKeywords().add(Pattern.compile("(#.*)[\n\r]")); //17
		
		getKeywords().add(Pattern.compile("\\s+(generate_network)[\\(\\s]")); //18
		getKeywords().add(Pattern.compile("\\s+(writeSBML)[\\(\\s]")); //19
		getKeywords().add(Pattern.compile("\\s+(writeMfile)[\\(\\s]")); //20
		getKeywords().add(Pattern.compile("\\s+(writeSSC)[\\;\\s]")); //21
		getKeywords().add(Pattern.compile("\\s+(writeSSCcfg)[\\;\\s]")); //22
		getKeywords().add(Pattern.compile("\\s+(simulate_ssa)[\\(\\s]")); //23
		getKeywords().add(Pattern.compile("\\s+(simulate_ode)[\\(\\s]")); //24
		
		// the 25th one, molecule name, the index number cannot be changed
		getKeywords().add(Pattern.compile("[,\"\\s+\\.\\>]([_\\d\\w]*)\\([%_,!\\?\\s\\+\\~\\d\\w\\*]*\\)[@,\"\\<\\-\\.\\+\\s]+")); //25
		
		getKeywords().add(Pattern.compile("\\s+(setConcentration)[\\(\\s]")); //26
		getKeywords().add(Pattern.compile("\\s+(saveConcentrations)[\\(\\s]")); //27
		getKeywords().add(Pattern.compile("\\s+(resetConcentrations)[\\(\\s]")); //28	
		getKeywords().add(Pattern.compile("\\s+(setOption)[\\(\\s]")); //29
		getKeywords().add(Pattern.compile("\\s+(begin[ \\t\\x0B\\f]+model)[#\\s]")); //30
		getKeywords().add(Pattern.compile("\\s+(end[ \\t\\x0B\\f]+model)[#\\s]")); //31
		getKeywords().add(Pattern.compile("[\\s+\\)\\d\\w](->)[\\s+\\(\\d\\w]")); //32
		getKeywords().add(Pattern.compile("[\\s+\\)\\d\\w](<->)[\\s+\\(\\d\\w]")); //33
		getKeywords().add(Pattern.compile("[\\s+\\)](\\+)\\s+")); //34
		
		getKeywords().add(Pattern.compile("\\s+(writeXML)[\\(\\s]")); //35	
		
		getKeywords().add(Pattern.compile("\\s+(Molecules)[\\s]")); //36
		getKeywords().add(Pattern.compile("\\s+(Species)[\\s]")); //37
		
		
		getKeywords().add(Pattern.compile("[{,\\s](overwrite)(=>)")); //38
		getKeywords().add(Pattern.compile("[{,\\s](suffix)(=>)")); //39
		getKeywords().add(Pattern.compile("[{,\\s](prefix)(=>)")); //40
		getKeywords().add(Pattern.compile("[{,\\s](t_end)(=>)")); //41
		getKeywords().add(Pattern.compile("[{,\\s](n_steps)(=>)")); //42
		getKeywords().add(Pattern.compile("[{,\\s](atol)(=>)")); //43
		getKeywords().add(Pattern.compile("[{,\\s](rtol)(=>)")); //44
		getKeywords().add(Pattern.compile("[{,\\s](sparse)(=>)")); //45
		getKeywords().add(Pattern.compile("[{,\\s](steady_state)(=>)")); //46
		getKeywords().add(Pattern.compile("[{,\\s](check_iso)(=>)")); //47
		getKeywords().add(Pattern.compile("[{,\\s](max_iter)(=>)")); //48
		getKeywords().add(Pattern.compile("[{,\\s](max_stoich)(=>)")); //49
		getKeywords().add(Pattern.compile("[{,\\s](t_start)(=>)")); //50
		getKeywords().add(Pattern.compile("[{,\\s](TextReaction)(=>)")); //51
		
		setBlocknum(8);
	}
	
	/** 
	 * Create the toolbar.
	 */
	private void createToolBar() 
	{
		toolBar = new ToolBar(mainEditorShell, SWT.NONE);
		
		ArrayList<ActionInterface> toolBarActions = ToolBarActionListCreator.getActionItems();
		
		for(final ActionInterface ai : toolBarActions)
		{
			ToolItem button = new ToolItem(toolBar, SWT.PUSH);
			button.setText(ai.getShortName());
			button.addSelectionListener(new SelectionListener() {
				
				public void widgetSelected(SelectionEvent arg0) 
				{
					if(ai.hasComposite())
						ActionFrameFactory.constructAndShowActionFrame(ai);
					else
						ai.executeAction();
				}
				
				public void widgetDefaultSelected(SelectionEvent arg0) 
				{
					// TODO Auto-generated method stub
					
				}
			});
		}
		
		//toolBar.setFont(globalfont);
		
		/*
		 * The Actions Button
		 */
		/*
		final ToolItem actionButton = new ToolItem(toolBar, SWT.PUSH);
		actionButton.setText("Actions");
		actionButton.setEnabled(true);
		actionButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				
				System.out.println("Action Hot Dog Go");
				
				Menu actionMenu = new Menu(getMainEditorShell(), SWT.DROP_DOWN);
				MenuItem test = new MenuItem(actionMenu, SWT.PUSH);
				test.setText("test");
				//ActionWindowView.getWindow().setVisible(true);
				
				
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {}
		});
		*/
		/*
		 * The Visualize Button
		 */
		/*
		ToolItem toolItem4 = new ToolItem(toolBar, SWT.PUSH);
		toolItem4.setText("Visualize");
		toolItem4.setEnabled(true);
		toolItem4.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				
				//BNGEditor.getEditor().getInputFiles().get(BNGEditor.getEditor().getFileSelection()).produceNewModels();
				//VisualizationViewer.getViewer().fileSelectionChange(BNGEditor.getEditor().getInputFiles().get(BNGEditor.getEditor().getFileSelection()));
				//VisualizationViewer.getViewer().setVisible(true);
				
				visViewController.openVisualizationViewerFrame();//inputfiles.get(fileselection));
				
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {}
		});
		*/
		
		
		
	}

	private void setupEditorContextMenu()
	{
		// Add things to a menu
		setPopmenu(new Menu(mainEditorShell,SWT.POP_UP));
		MenuItem undoMenuItem = new MenuItem(getPopmenu(),SWT.PUSH);
		undoMenuItem.setText("Undo");
		undoMenuItem.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).getTextarea().Undo();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator11 = new MenuItem(getPopmenu(), SWT.SEPARATOR);
		MenuItem push = new MenuItem(getPopmenu(), SWT.PUSH);
		push.setText("Cut");
		push.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).getTextarea().cut();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mp3 = new MenuItem(getPopmenu(), SWT.PUSH);
		mp3.setText("Copy");
		mp3.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).getTextarea().copy();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem mp4 = new MenuItem(getPopmenu(), SWT.PUSH);
		mp4.setText("Paste");
		mp4.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).getTextarea().paste();
			}
			public void widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent e) {
			}
		});
		MenuItem separator12 = new MenuItem(getPopmenu(), SWT.SEPARATOR);
		MenuItem mp5 = new MenuItem(getPopmenu(), SWT.PUSH);
		mp5.setText("Select All");
		mp5.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				if(textfolder.getSelection() == null)
					return;
				getInputfiles().get(getFileselection()).getTextarea().selectall();
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
		composite1 = new Composite(mainEditorShell, SWT.NONE);
		composite1.setFont(getGlobalfont());
		composite1.setLayout(new FillLayout());
		createSashForm();
		composite1.setLayoutData(sashdata);	
	}
	
	private void createSashForm() 
	{
		sashForm = new SashForm(composite1, SWT.VERTICAL);	
		sashForm.setFont(getGlobalfont());
		createTextfolder();
		createConsole();
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
				CurrentFile tempfile = new CurrentFile(null,filename,ConfigurationManager.getConfigurationManager().getOSType(),true, -1);
				emptyFileCount++;
				emptyFileCount = emptyFileCount % 100;
				
				// pass the resViewer to current file
				tempfile.setResViewer(resViewer);
				
				getInputfiles().add(tempfile);
				setFileselection(getInputfiles().size()-1);
				
				textfolder.setSelection(getFileselection());			
			}

			public void mouseDown(MouseEvent arg0) {
				
			}

			public void mouseUp(MouseEvent arg0) {
				
			}
		});
	}
	
	/*****
	 * Setup the console
	 *****/
	private void createConsole() 
	{
		// Create the group.
		consoleGroup = new Group(sashForm, SWT.BORDER);
		consoleGroup.setBounds(new Rectangle(0, 227, 644, 224));
		consoleGroup.setLayout(new FillLayout());
		consoleGroup.setText("Console");
	
		console = new Text(consoleGroup, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		console.setEditable(false);
		console.setBackground(new Color(Display.getCurrent(), 255, 255, 255));
	}

	private void createComposite2() {
		GridData tempdata = new GridData();
		tempdata.widthHint = GridData.FILL_BOTH;
		tempdata.grabExcessHorizontalSpace = true;
		//tempdata.grabExcessVerticalSpace = true;
		composite2 = new Composite(mainEditorShell, SWT.NONE);
		composite2.setFont(getGlobalfont());
		composite2.setLayoutData(tempdata);
		composite2.setLayout(new FillLayout());
		setShowfilepath(new Text(composite2, SWT.BORDER));
		getShowfilepath().setEditable(false);
	}

	/**
	 * Main method.  Creates the display, sets the font, instantiates the editor,
	 * creates a shell, checks for updates, waits for input.
	 * 
	 * @param args
	 */
	public static void main(String[] args) 
	{	
		
		Display display = Display.getDefault();
		FontData fontdata = display.getSystemFont().getFontData()[0];
		fontdata.setHeight(9);
		setGlobalfont(new Font(display,fontdata));
		
		BNGEditor thisClass = BNGEditor.getEditor();
		thisClass.createSShell();
		
		// initializes the config options and asks for workspace before the
		// shell opens.
		ConfigurationManager.getConfigurationManager().initConfigOptions();
		
		thisClass.mainEditorShell.open();
		init();
		
		//DEBUG
		//BNGEditor.displayOutput("User Dir = " + System.getProperty("user.dir")+BNGEditor.getConsoleLineDelimeter());
		
		//initializeActionWindow();
		
		// DEBUG
		//System.out.println("About to call update");
		// call the concurrent version
		new Thread(new VersionCheckerThread()).start();
		//System.out.println("Update thread started");
		
		while (!thisClass.mainEditorShell.isDisposed()) {
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		display.dispose();
	}
	
	/**
	 * TODO This is where the actions will be added to the action window.
	 */
	/*  The list is used now, but this is what to do if we want the window.
	private static void initializeActionWindow() 
	{
		
		ActionWindowView.getWindow().addAction(new RunSimulationActionView());
		ActionWindowView.getWindow().addAction(new ParameterScanActionView());
		
	}
*/
	
	
	
	void closeallfiles()
	{
		if(textfolder.getSelection() == null)
			return;
		while(getFileselection()!=-1)
		{
		
			getInputfiles().get(getFileselection()).close();
			getInputfiles().remove(getFileselection());	
			textfolder.getItem(getFileselection()).dispose();
			for(int i=0;i<getInputfiles().size();i++)
				if(getInputfiles().get(i).tabitem.equals(textfolder.getSelection()))
				{
					setFileselection(i);
					break;
				}	
			if(getFileselection()>=getInputfiles().size())
			{
				setFileselection(-1);
				getShowfilepath().setText("");
				return;
			}
			if(getInputfiles().get(getFileselection()).getFilepath()==null)
				getShowfilepath().setText("");
			else
				if(ConfigurationManager.getConfigurationManager().getOSType() == 1)
					getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"\\"+getInputfiles().get(getFileselection()).getFilename());
				else
					getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"/"+getInputfiles().get(getFileselection()).getFilename());
		}
	}
	
	static void init()
	{
		
		visViewController = VisualizationViewerController.loadVisualizationViewController();
		
		textfolder.setFocus();
		textfolder.addSelectionListener(new SelectionAdapter()
		{
			public void widgetSelected(SelectionEvent arg0) {
				// Get the newly selected file
				setFileselection(textfolder.getSelectionIndex());
				
				// If it is null then don't show anything in the path box.
				if(getInputfiles().get(getFileselection()).getFilepath()==null)
				{
					getShowfilepath().setText("");
				}
				
				// If it is not null then show the path.
				else
				{
					if(ConfigurationManager.getConfigurationManager().getOSType() == 1)
						getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"\\"+getInputfiles().get(getFileselection()).getFilename());
					else
						getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"/"+getInputfiles().get(getFileselection()).getFilename());
				}
				
				// Tell the viewer controller that a file has changed focus.
				visViewController.fileBecomesFocus(getInputfiles().get(getFileselection()));
				
				//visViewController.informOfNewCMapModel();
				
				// Change the parameterscan info
				ParameterScanController psController = ParameterScanController.getParameterScanController();
				psController.fileSelectionChange(getInputfiles().get(getFileselection()));
			}
		});
		
		textfolder.addCTabFolder2Listener(new CTabFolder2Adapter()
		{
			public void close(CTabFolderEvent arg0) {
				for(int i=0;i<getInputfiles().size();i++)
					if(getInputfiles().get(i).tabitem.equals(arg0.item))
					{
						getInputfiles().get(i).close();
						getInputfiles().remove(i);
						break;
					}
				for(int i=0;i<getInputfiles().size();i++)
					if(getInputfiles().get(i).tabitem.equals(textfolder.getSelection()))
					{
						setFileselection(i);
						visViewController.fileBecomesFocus(getInputfiles().get(getFileselection()));
						
						break;
					}	
				if(getFileselection() == -1 || getFileselection()>=getInputfiles().size())
				{
					setFileselection(-1);
					getShowfilepath().setText("");
					visViewController.fileBecomesFocus(null);
					return;
				}
				if(getInputfiles().get(getFileselection()).getFilepath()==null)
					getShowfilepath().setText("");
				else
					if(ConfigurationManager.getConfigurationManager().getOSType() == 1)
						getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"\\"+getInputfiles().get(getFileselection()).getFilename());
					else
						getShowfilepath().setText(getInputfiles().get(getFileselection()).getFilepath()+"/"+getInputfiles().get(getFileselection()).getFilename());
			}
		});
		
		getShowfilepath().addFocusListener(new FocusAdapter()
		{
			public void focusGained(FocusEvent e)
			{
				if(getFileselection()!=-1)
					getInputfiles().get(getFileselection()).getTextarea().textarea.setFocus();
				else
					textfolder.setFocus();
			}
		});
	}
	
	
	
	/*
	 * Set the root directory for the result viewer, and create a result viewer.
	 */
	public static void setResultViewerPath() {
		/*
		// slash for different operating system
		String tmpSlash = "";
		if(ConfigurationManager.getConfigurationManager().getOSType() == 1) {
			tmpSlash = "\\";
		} else {
			tmpSlash = "/";
		}

		// parentDir
		String parentDir = System.getProperty("user.dir");
		String resultsFolderPath = parentDir + tmpSlash + "BNGResults";
		
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
		
		String resultsFolderPath = ConfigurationManager.getConfigurationManager().getWorkspacePath()
								   + ConfigurationManager.getConfigurationManager().getSlash() + "BNGResults";
		System.out.println("ResultsFolderPath: " + resultsFolderPath);
		// resultsFolder
		File resultsFolder = new File(resultsFolderPath);
		if (!resultsFolder.isDirectory()) {
			resultsFolder.mkdir();
		}

		// create a result viewer
		Rectangle bounds = new Rectangle(0, 0, 600, 600);

		if (resViewer == null || resViewer.getShell() == null) {
			resViewer = new ResultViewer(resultsFolderPath, "", "", bounds);
			
			for (int i = 0; i < getInputfiles().size(); i++) {
				getInputfiles().get(i).setResViewer(resViewer);
			}

			// open
			// resViewer.setBlockOnOpen(true);
			resViewer.open();

			if (resViewer != null && resViewer.getGraphFrame() != null
					&& resViewer.getGraphFrame().isShowing()) {
				resViewer.getGraphFrame().dispose(); // close the graph
			}
			
			for (int i = 0; i < getInputfiles().size(); i++) {
				getInputfiles().get(i).setResViewer(resViewer);
			}
		}

	}

	public static ResultViewer getResViewer() {
		return resViewer;
	}

	public static void setResViewer(ResultViewer resViewer) {
		BNGEditor.resViewer = resViewer;
		for (int i = 0; i < getInputfiles().size(); i++) {
			getInputfiles().get(i).setResViewer(resViewer);
		}
	}

	/*
	 * After "save as", close the original one, open the new saved one
	 */
	public static void replaceInputfiles(CurrentFile oldF, CurrentFile newF) {
		int oldIndex = getInputfiles().indexOf(oldF);
		textfolder.getItem(oldIndex).dispose();
		getInputfiles().set(oldIndex, newF);
		textfolder.setSelection(oldIndex);
		if (ConfigurationManager.getConfigurationManager().getOSType() == 1) {
			getShowfilepath().setText(newF.getFilepath() + "\\" + newF.getFilename());
		}
		else {
			getShowfilepath().setText(newF.getFilepath() + "/" + newF.getFilename());
		}
	}
	
	public static Shell getMainEditorShell()
	{
		return mainEditorShell;
	}

	public int getEmptyFileCount() 
	{
		return emptyFileCount;
	}

	public void incrementEmptyFileCount() 
	{
		emptyFileCount++;
		emptyFileCount = emptyFileCount % 100;
	}

	public void addToInputFiles(CurrentFile tempfile) 
	{
		getInputfiles().add(tempfile);
	}

	public ArrayList<CurrentFile> getInputFiles()
	{
		return getInputfiles();
	}

	public int getFileSelection()
	{
		return fileselection;
	}
	public void setFileSelection(int filenum) 
	{
		System.out.println("**setFileSelectionCalled");
		visViewController.fileBecomesFocus(inputfiles.get(filenum));
		setFileselection(filenum);
	}
	

	public static ActionHub getActionHub()
	{
		return actionHub;
	}

	public void focusConsole() 
	{
	//	consoleGroup.setSelection(console);
		
	}
	
	public static CTabFolder getTextFolder()
	{
		return textfolder;
	}

	public static void setInputfiles(ArrayList<CurrentFile> inputfiles) {
		BNGEditor.inputfiles = inputfiles;
	}

	public static ArrayList<CurrentFile> getInputfiles() {
		return inputfiles;
	}

	public static void setFileselection(int fileselection) {
		BNGEditor.fileselection = fileselection;
	}

	public static int getFileselection() {
		return fileselection;
	}
	
	public static void displayOutput(String s)
	{
		console.append(s);
		console.update();
	}

	public static void setBlocknum(int blocknum) {
		BNGEditor.blocknum = blocknum;
	}

	public static int getBlocknum() {
		return blocknum;
	}

	public static void setKeywords(ArrayList<Pattern> keywords) {
		BNGEditor.keywords = keywords;
	}

	public static ArrayList<Pattern> getKeywords() {
		return keywords;
	}

	public static void setPopmenu(Menu popmenu) {
		BNGEditor.popmenu = popmenu;
	}

	public static Menu getPopmenu() {
		return popmenu;
	}

	public static void setShowfilepath(Text showfilepath) {
		BNGEditor.showfilepath = showfilepath;
	}

	public static Text getShowfilepath() {
		return showfilepath;
	}

	public static String getConsoleLineDelimeter() 
	{
		return console.getLineDelimiter();
		
	}

	public static String getConsoleText() 
	{
		return console.getText();
	}

	public static void setGlobalfont(Font globalfont) {
		BNGEditor.globalfont = globalfont;
	}

	public static Font getGlobalfont() {
		return globalfont;
	}
}