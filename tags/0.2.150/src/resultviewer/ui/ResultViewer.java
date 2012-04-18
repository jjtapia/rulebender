package resultviewer.ui;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;

import javax.swing.JFrame;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.util.IOpenEventListener;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.window.*;
import org.eclipse.swt.*;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jfree.chart.JFreeChart;
import org.jfree.experimental.chart.swt.ChartComposite;

import resultviewer.tree.*;
import resultviewer.tree.TreeNode;

import resultviewer.data.*;
import resultviewer.graph.*;
import resultviewer.text.*;

/*
 * Pop up a new JFrame to show the graph
 */

/*
 * 1. The program cannot deal with opening files with same name at the same time
 * 2. XML file
 */

public class ResultViewer extends ApplicationWindow {

	// root path
	private String rootDirPath;

	// the name of model need to expand
	private String expandModel;

	private static Color color_DarkGoldenrod1 = new Color(null, 255, 185, 15);

	// composites
	private SashForm sash_form_bottom, sash_form_top;
	private CTabFolder textFolder, outlineFolder, explorerFolder;
	private Composite outlineCmp;

	// components
	private TreeViewer file_tv;
	private TreeNode rootNode;
	private CheckboxTreeViewer element_tv;
	private Button selectAllBnt;
	private ChartComposite curChartPanel;
	private String dataType = "linear"; // linear or log
	private FileNode curFileNode;
	private ArrayList<FileNode> curFileList;
	private NETDocumentProvider netProvider;

	private JFrame jf; // show the graph generated by Prefuse

	private NETConfiguration netConfig;

	// menus
	private Menu textFolderMenu; // control items on textFolder
	private Menu textMenu; // control text for NET file

	// actions
	private ExitAction exit_action;

	public ResultViewer(String rootDirPath, String expandModel) {
		super(null);

		this.rootDirPath = rootDirPath;

		this.expandModel = expandModel;

		// create a NETDocumentProvider object
		netProvider = new NETDocumentProvider();

		// menu bar and tool bar
		exit_action = new ExitAction(this);

		// create a list to store current fileNodes
		curFileList = new ArrayList<FileNode>();

		addStatusLine(); // status line
		addMenuBar(); // menu bar
		addToolBar(SWT.FLAT | SWT.WRAP); // tool bar
	}

	/*
	 * Create the contents
	 * 
	 * @see
	 * org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets
	 * .Composite)
	 */
	protected Control createContents(Composite parent) {
		// window title
		getShell().setText("Viewer"); // window title

		sash_form_bottom = new SashForm(parent, SWT.HORIZONTAL | SWT.NULL);

		sash_form_top = new SashForm(sash_form_bottom, SWT.VERTICAL | SWT.NULL);

		explorerFolder = new CTabFolder(sash_form_top, SWT.BORDER);
		initExplorerFolder();

		outlineFolder = new CTabFolder(sash_form_top, SWT.BORDER);
		initOutlineFolder();

		sash_form_top.setWeights(new int[] { 1, 1 });

		textFolder = new CTabFolder(sash_form_bottom, SWT.BORDER);
		initTextFolder();

		sash_form_bottom.setWeights(new int[] { 1, 3 });

		parent.setBounds(0, 0, 680, 768);
		return parent;
	}

	/*
	 * Create the menu bar
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow#createMenuManager()
	 */
	protected MenuManager createMenuManager() {
		MenuManager bar_menu = new MenuManager("");

		MenuManager file_menu = new MenuManager("&File");
		MenuManager edit_menu = new MenuManager("&Edit");
		MenuManager view_menu = new MenuManager("&View");

		bar_menu.add(file_menu);
		bar_menu.add(edit_menu);
		bar_menu.add(view_menu);

		file_menu.add(exit_action);

		return bar_menu;
	}

	/*
	 * Create the tool bar
	 * 
	 * @see org.eclipse.jface.window.ApplicationWindow#createToolBarManager(int)
	 */
	protected ToolBarManager createToolBarManager(int style) {

		ToolBarManager tool_bar_manager = new ToolBarManager(style);

		tool_bar_manager.add(exit_action);

		return tool_bar_manager;
	}

	/*
	 * Initialize explorer tab folder
	 */
	private void initExplorerFolder() {
		CTabItem modelExplorer = new CTabItem(explorerFolder, SWT.CLOSE);
		modelExplorer.setText("Explorer");
		file_tv = new TreeViewer(explorerFolder, SWT.NONE);
		file_tv.setContentProvider(new TreeContentProvider());
		file_tv.setLabelProvider(new TreeLabelProvider());
		rootNode = new FolderNode(new File(rootDirPath));
		file_tv.setInput(rootNode);
		modelExplorer.setControl(file_tv.getControl());

		// set default selection on modelExplorer tab item
		explorerFolder.setSelection(modelExplorer);

		// expand nodes
		TreeNode expandFolder = this.findFileFolderNode(expandModel);
		file_tv.expandToLevel(expandFolder, 1);

		// add selection listener
		file_tv.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();

				if (selection.isEmpty()) {
					return;
				}
				// set message on status line
				setStatus("Selected: "
						+ ((TreeNode) selection.getFirstElement()).getName());
			}
		});

		// add double click listener
		file_tv.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				IStructuredSelection selection = (IStructuredSelection) e
						.getSelection();

				if (selection.isEmpty()) {
					return;
				}

				TreeNode node = (TreeNode) selection.getFirstElement();

				if (node.getNodeType().equalsIgnoreCase("FolderNode")) {
					return;
				}

				setStatus("Opened file " + node.getName());
				// convert TreeNode to FileNode
				curFileNode = (FileNode) node;

				// add the curFileNode to curFileList
				curFileList.add(curFileNode);

				/*
				 * If the file has not opened, create corresponding chart or
				 * text, and then open it.
				 */

				if (curFileNode.getName().endsWith(".net")) {
					// NET
					// check if the file has already opened
					boolean opened = false;
					for (int i = 0; i < textFolder.getItemCount(); i++) {
						CTabItem item = textFolder.getItem(i);
						if (item.getText().equals(curFileNode.getName())) {
							// set selection on this matched item
							textFolder.setSelection(item);

							updateOutline();
							opened = true;
						}
					}
					if (opened == false) {
						// create a new netItem
						createNetItem(curFileNode);
					}

				} else if (curFileNode.getName().endsWith(".cdat")
						|| curFileNode.getName().endsWith(".gdat")
						|| curFileNode.getName().endsWith(".scan")) {
					// CDAT & GDAT & SCAN

					// clear the name of the selected species
					((DATFileData) curFileNode.getFileData())
							.setSelectedSpeciesName(null);

					// check if the file has already opened
					boolean opened = false;
					for (int i = 0; i < textFolder.getItemCount(); i++) {
						CTabItem item = textFolder.getItem(i);
						if (item.getText().equals(curFileNode.getName())) {
							// set selection on this matched item
							textFolder.setSelection(item);
							curChartPanel = (ChartComposite) ((Composite) item
									.getControl()).getChildren()[2];
							DATChart.resetChart(curChartPanel.getChart()); // reset
							updateOutline();

							opened = true;
						}
					}
					if (opened == false) {
						// create a new chartItem
						createChartItem(curFileNode);
					}
				}

			}
		});
	}

	/*
	 * Initialize text tab folder
	 */
	private void initTextFolder() {
		textFolder.setSimple(true);
		textFolder
				.setBackground(new Color(Display.getCurrent(), 240, 240, 240));

		// add selection listener for textFolder
		textFolder.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent event) {
				CTabItem item = (CTabItem) event.item;
				String fileName = item.getText();

				setStatus("File: " + fileName);

				// set the curFieNode be the current selection
				curFileNode = getFileNode(fileName);
				if (fileName.endsWith(".cdat") || fileName.endsWith(".gdat")
						|| fileName.endsWith(".scan")) {
					// CDAT & GDAT & SCAN
					curChartPanel = (ChartComposite) ((Composite) item
							.getControl()).getChildren()[2];

				} else if (fileName.endsWith(".net")) {
					// NET

				}
				updateOutline();
			}
		});

		// initialize textFolderMenu
		initTextFolderMenu();

		// initialize textMenu;
		initTextMenu();
	}

	/*
	 * Initialize outline tab folder
	 */
	private void initOutlineFolder() {
		outlineFolder.setSimple(true);
		CTabItem elementOutline = new CTabItem(outlineFolder, SWT.CLOSE);
		elementOutline.setText("Outline");

		outlineCmp = new Composite(outlineFolder, SWT.NONE);
		outlineCmp.setLayout(new GridLayout(1, false));

		// select all button
		selectAllBnt = new Button(outlineCmp, SWT.CHECK);
		selectAllBnt.setText("Check/Uncheck All");

		// element tree viewer
		element_tv = new CheckboxTreeViewer(outlineCmp);
		element_tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		element_tv.setContentProvider(new TreeContentProvider());
		element_tv.setLabelProvider(new TreeLabelProvider());

		// nothing in the tree at the beginning
		element_tv.setInput(null);
		elementOutline.setControl(outlineCmp);

		// set default selection on elementOutline tab item
		outlineFolder.setSelection(elementOutline);

		// add listener for selectAllBnt
		selectAllBnt.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				boolean selected = selectAllBnt.getSelection();
				if (selected == true) {
					// select all

					element_tv.setAllChecked(true);

					Object[] elements = element_tv.getCheckedElements();

					if (curFileNode.getName().endsWith(".cdat")) {
						// CDAT
						DATFileData data = (DATFileData) curFileNode
								.getFileData();

						for (int i = 0; i < elements.length; i++) {
							data.addCheckedSpecies((SpeciesNode) elements[i]);
						}
						// set some nodes be checked
						element_tv.setCheckedElements(data.getCheckedSpecies());
						// plot the chart
						curChartPanel.setChart(DATChart.plotXYLineChart(data,
								dataType));

					} else if (curFileNode.getName().endsWith(".gdat")
							|| curFileNode.getName().endsWith(".scan")) {
						// GDAT & SCAN
						DATFileData data = (DATFileData) curFileNode
								.getFileData();

						for (int i = 0; i < elements.length; i++) {
							TreeNode node = (TreeNode) elements[i];
							if (node.getNodeType().equalsIgnoreCase(
									"ObservableNode")) {
								data.addCheckedObservable((ObservableNode) elements[i]);
							}
						}
						// set some nodes be checked
						element_tv.setCheckedElements(data
								.getCheckedObservable());
						// plot the chart
						curChartPanel.setChart(DATChart.plotXYLineChart(data,
								dataType));

					} else if (curFileNode.getName().endsWith(".net")) {
						// NET
						// TODO
					}

				} else {
					// remove all

					Object[] elements = element_tv.getCheckedElements();

					if (curFileNode.getName().endsWith(".cdat")) {
						// CDAT
						DATFileData data = (DATFileData) curFileNode
								.getFileData();

						for (int i = 0; i < elements.length; i++) {
							data.removeCheckedSpecies((SpeciesNode) elements[i]);
						}
						// set some nodes be checked
						element_tv.setCheckedElements(data.getCheckedSpecies());
						curChartPanel.setChart(DATChart.plotXYLineChart(data,
								dataType));

					} else if (curFileNode.getName().endsWith(".gdat")
							|| curFileNode.getName().endsWith(".scan")) {
						// GDAT & SCAN
						DATFileData data = (DATFileData) curFileNode
								.getFileData();

						for (int i = 0; i < elements.length; i++) {
							TreeNode node = (TreeNode) elements[i];
							if (node.getNodeType().equalsIgnoreCase(
									"ObservableNode")) {
								data.removeCheckedObservable((ObservableNode) elements[i]);
							}
						}
						// set some nodes be checked
						element_tv.setCheckedElements(data
								.getCheckedObservable());
						curChartPanel.setChart(DATChart.plotXYLineChart(data,
								dataType));
					} else if (curFileNode.getName().endsWith(".net")) {
						// NET
						// TODO
					}

					element_tv.setAllChecked(false);
				}
			}

		});

		element_tv.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {

				if (event.getChecked() == true) {

					TreeNode node = (TreeNode) event.getElement();

					if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
						// SpeciesNode

						if (curFileNode.getName().endsWith(".cdat")) {
							DATFileData data = (DATFileData) curFileNode
									.getFileData();
							data.addCheckedSpecies((SpeciesNode) event
									.getElement());
							curChartPanel.setChart(DATChart.plotXYLineChart(
									data, dataType));
							setStatus("Checked:" + node.getName());
						} else {
							element_tv.setChecked(node, false);
						}
					} else if (node.getNodeType().equalsIgnoreCase(
							"ObservableNode")) {
						// ObservableNode
						DATFileData data = (DATFileData) curFileNode
								.getFileData();
						data.addCheckedObservable((ObservableNode) event
								.getElement());
						curChartPanel.setChart(DATChart.plotXYLineChart(data,
								dataType));
						setStatus("Checked:" + node.getName());
					} else {
						element_tv.setChecked(node, false);
					}

				}
				// not check
				else if (event.getChecked() == false) {

					TreeNode node = (TreeNode) event.getElement();

					if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
						// SpeciesNode
						if (curFileNode.getName().endsWith(".cdat")) {
							DATFileData data = (DATFileData) curFileNode
									.getFileData();
							data.removeCheckedSpecies((SpeciesNode) event
									.getElement());
							curChartPanel.setChart(DATChart.plotXYLineChart(
									data, dataType));
							setStatus("Unchecked:" + node.getName());
						} else {

						}
					} else if (node.getNodeType().equalsIgnoreCase(
							"ObservableNode")) {
						// ObservableNode
						DATFileData data = (DATFileData) curFileNode
								.getFileData();
						data.removeCheckedObservable((ObservableNode) event
								.getElement());
						curChartPanel.setChart(DATChart.plotXYLineChart(data,
								dataType));
						setStatus("Unchecked:" + node.getName());
					} else {

					}
				}
			}
		});

		/*
		 * add selection listener for outline tree change the line to RED when
		 * the species is selected in the outline tree
		 */

		element_tv.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();

				if (selection.isEmpty())
					return;

				if (curFileNode.getName().endsWith(".cdat")) {
					// CDAT

					String labelName = selection.getFirstElement().toString();
					setStatus("Selected: " + labelName);
					String key = labelName.split(" ")[0]; // index

					// store the name of the selected species
					DATFileData data = ((DATFileData) curFileNode.getFileData());
					data.setSelectedSpeciesName(labelName);
					// mark selected line
					DATChart.markSelectedLine(curChartPanel.getChart(), key);

					/* draw the species graph */

					String[] tmp = labelName.split(" "); // <id speciesExp>
					int id = Integer.parseInt(tmp[0]); // get species id
					if (tmp.length > 1) {
						String speciesExp = tmp[1]; // get species expression

						// get the display
						prefuse.Display dis = data.getDisplay();
						if (dis == null || !dis.getName().equals(speciesExp)) {
							dis = new SpeciesGraph(id, speciesExp).getDisplay();
							dis.setName(speciesExp);
							data.setDisplay(dis);

							// TODO adjust the location of graph
						}

						// show the graph in a JFrame
						showGraph(data, id, speciesExp);
					}
				}

				else if (curFileNode.getName().endsWith(".gdat")
						|| curFileNode.getName().endsWith(".scan")) {
					// GDAT & SCAN

					DATFileData data = ((DATFileData) curFileNode.getFileData());
					Iterator it = selection.iterator();

					while (it.hasNext()) {
						TreeNode node = (TreeNode) it.next();
						String labelName = node.getName();
						setStatus("Selected: " + labelName);

						if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							// store the name of the selected species
							data.setSelectedObservableName(labelName);
							// mark selected line
							DATChart.markSelectedLine(curChartPanel.getChart(),
									labelName);
						} else if (node.getNodeType().equalsIgnoreCase(
								"SpeciesNode")) {
							/* draw the species graph */

							String[] tmp = labelName.split(" "); // <id
																	// speciesExp>

							int id = Integer.parseInt(tmp[0]); // get species id
							String speciesExp = tmp[1]; // get species

							// show the graph in a JFrame
							showGraph(data, id, speciesExp);
						} else if (node.getNodeType().equalsIgnoreCase(
								"SpeciesFolderNode")) {
							showGraph(data, 0, labelName);
						}
					}

				} else if (curFileNode.getName().endsWith(".net")) {
					// NET
					NETItemNode node = (NETItemNode) selection
							.getFirstElement();
					NETFileData data = (NETFileData) curFileNode.getFileData();

					// reset selection and reveal range
					int offset = node.getOffset();
					int length = node.getLength();
					data.getSourceViewer().revealRange(offset, length);
					setStatus("Link to the part of \"" + node.getName() + "\"");
				}
			}

		});
	}

	/*
	 * Create a menu to control tab items on textFolder. - Close All
	 */

	private void initTextFolderMenu() {

		textFolderMenu = new Menu(getShell(), SWT.POP_UP);
		final MenuItem closeAllMenuItem = new MenuItem(textFolderMenu, SWT.PUSH);
		closeAllMenuItem.setText("Close All");

		// add listener for textFolderMenu
		textFolder.setMenu(textFolderMenu);
		textFolderMenu.addMenuListener(new MenuAdapter() {
			public void menuShown(MenuEvent e) {
				Point p = Display.getCurrent().getCursorLocation();
				p = textFolder.toControl(p);
				final CTabItem item = textFolder.getItem(p);
				if (item == null) {
					textFolderMenu.setVisible(false);
					return;
				}
			}
		});

		// add listener for closeAllMenuItem
		closeAllMenuItem.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				while (textFolder.getItemCount() > 0) {
					textFolder.getItem(0).dispose();
				}
			}

		});
	}

	/*
	 * Create a menu to control text for NET file. Show graph based on selected
	 * Text. - Species ID - Species Expression
	 */

	private void initTextMenu() {
		textMenu = new Menu(getShell(), SWT.POP_UP);

		// draw graph based on species ID
		final MenuItem speciesIDItem = new MenuItem(textMenu, SWT.PUSH);
		speciesIDItem.setText("Species ID");

		// draw graph based on species Expression
		final MenuItem speciesExpItem = new MenuItem(textMenu, SWT.PUSH);
		speciesExpItem.setText("Species Expression");

		// add listener for speciesIDItem
		speciesIDItem.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				NETFileData data = (NETFileData) curFileNode.getFileData();
				Point p = data.getSourceViewer().getSelectedRange();
				String idStr = "";

				// get the selected text
				for (int i = p.x; i < p.x + p.y; i++) {
					idStr += Character
							.toString(data.getFileContent().charAt(i));
				}

				try {
					int id = Integer.parseInt(idStr);
					String exp = data.getSpeciesExpByID(id);

					if (exp == null) {
						setStatus("\"" + idStr + "\""
								+ " has no correspoding species.");
						return;
					}
					// show the graph
					showGraph(data, id, exp);
					setStatus("Species " + id + ": " + exp);
				} catch (NumberFormatException exception) {
					setStatus("\"" + idStr + "\""
							+ " is not an Integer number.");
				}

			}

		});

		// add listener for speciesExpItem
		speciesExpItem.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				NETFileData data = (NETFileData) curFileNode.getFileData();
				Point p = data.getSourceViewer().getSelectedRange();
				String expression = "";

				// get the selected text
				for (int i = p.x; i < p.x + p.y; i++) {
					expression += Character.toString(data.getFileContent()
							.charAt(i));
				}

				if (expression.charAt(expression.length() - 1) != ')') {
					setStatus("\"" + expression + "\""
							+ " is not a complete species expression");
					return;
				}
				Integer id = data.getSpeciesIDByExp(expression);
				if (id == null) {
					setStatus("\"" + expression + "\""
							+ " is not a species expression");
					return;
				}

				// show the graph
				setStatus("Species " + id + ": " + expression);
				showGraph(data, id, expression);
			}

		});
	}

	/*
	 * create a chartItem on textFolder when the corresponding file was double
	 * clicked
	 */
	private void createChartItem(FileNode fNode) {
		
		CTabItem chartItem = new CTabItem(textFolder, SWT.CLOSE);
		chartItem.setText(fNode.getName());

		Composite chartCmp = new Composite(textFolder, SWT.NONE);
		chartCmp.setLayout(new GridLayout(2, false));

		// radio buttons
		Button linearBnt = new Button(chartCmp, SWT.RADIO);
		linearBnt.setText("linear");
		dataType = "linear";
		linearBnt.setSelection(true);

		linearBnt.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				dataType = "linear";

				// get data
				DATFileData data = (DATFileData) curFileNode.getFileData();
				// plot chart
				curChartPanel.setChart(DATChart.plotXYLineChart(data, dataType));

				updateOutline();
			}

		});

		Button logBnt = new Button(chartCmp, SWT.RADIO);
		logBnt.setText("logarithmic");

		logBnt.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent arg0) {

			}

			public void widgetSelected(SelectionEvent arg0) {
				dataType = "log";

				// get data
				DATFileData data = (DATFileData) curFileNode.getFileData();
				// plot chart
				curChartPanel.setChart(DATChart.plotXYLineChart(data, dataType));

				updateOutline();

			}

		});

		// create the chart using JFreeChart
		JFreeChart chart = DATChart.plotXYLineChart(
				(DATFileData) fNode.getFileData(), dataType);
		ChartComposite chartPanel = new ChartComposite(chartCmp, SWT.NONE,
				chart, true);
		GridData griddata = new GridData(GridData.FILL_BOTH);
		griddata.horizontalSpan = 2;
		chartPanel.setLayoutData(griddata);
		curChartPanel = chartPanel;

		// set control
		chartItem.setControl(chartCmp);
		textFolder.setSelection(chartItem);

		updateOutline();

		DATFileData data = (DATFileData) fNode.getFileData();

		/*
		 * Set initial status be selecting the first node
		 */
		/*
		 * selectAllBnt.setSelection(true); element_tv.setAllChecked(true);
		 * 
		 * 
		 * Object[] elements = element_tv.getCheckedElements();
		 * 
		 * for (int i = 0; i < elements.length; i++) { TreeNode node =
		 * (TreeNode) elements[i]; if
		 * (node.getNodeType().equalsIgnoreCase("SpeciesNode")) { // SpeciesNode
		 * data.addCheckedSpecies((SpeciesNode) elements[i]); } else if
		 * (node.getNodeType().equalsIgnoreCase("ObservableNode")) { //
		 * ObservableNode data.addCheckedObservable((ObservableNode)
		 * elements[i]); } else { } }
		 */

		// plot chart
		curChartPanel.setChart(DATChart.plotXYLineChart(data, dataType));

		// add close listener for tabItem
		chartItem.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent event) {
				CTabItem item = (CTabItem) event.getSource();
				String fileName = item.getText();

				// remove the fileNode from curFileList
				removeFileNode(fileName);

				if (curFileList.size() == 0) {
					curFileNode = null;
					if (!outlineFolder.isDisposed())
						outlineFolder.setVisible(false);
				}
			}

		});

	}

	/*
	 * create a netItem on textFolder when the corresponding file was double
	 * clicked
	 */
	private void createNetItem(FileNode fNode) {

		// open the text with TextViewer
		CTabItem netItem = new CTabItem(textFolder, SWT.CLOSE);
		netItem.setText(fNode.getName());

		// use SourceViewer to control the NET file
		final SourceViewer sv = new SourceViewer(textFolder, null,
				SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);

		if (netConfig == null)
			netConfig = new NETConfiguration();

		final NETFileData data = (NETFileData) fNode.getFileData();
		Document document = netProvider.createDocument(data.getFileContent());
		sv.setDocument(document); // connect sourceViewer with document
		sv.configure(netConfig); // set configuration

		// get the style ranges
		final StyleRange[] styleRanges = sv.getTextWidget().getStyleRanges();

		data.setSourceViewer(sv); // store the sourceViewer in NETFileData

		netItem.setControl(sv.getControl());
		textFolder.setSelection(netItem);

		// set pop up menu
		sv.getControl().setMenu(textMenu);

		// add post selection listener
		sv.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				// reset all style ranges
				sv.getTextWidget().setStyleRanges(styleRanges);

				// set background color for all the text which is the same as
				// the selected one
				String fileContent = data.getFileContent();
				Point p = sv.getSelectedRange();
				if (p.y == 0)
					return;

				String selected = "";

				// get the selected string
				for (int i = p.x; i < p.x + p.y; i++) {
					selected += fileContent.charAt(i);
				}

				if (selected.equals(" ")) {
					return;
				}

				// set status line
				setStatus("Select Text: " + selected);

				int selectOffset = 0;
				int subStringOffset = 0;
				while (selectOffset != -1) {
					// find the new text position
					selectOffset = fileContent.substring(subStringOffset)
							.indexOf(selected);

					if (selectOffset != -1) {
						// create a style range
						final StyleRange sr = new StyleRange();
						sr.background = color_DarkGoldenrod1;
						sr.start = subStringOffset + selectOffset;
						sr.length = p.y;
						sv.getTextWidget().setStyleRange(sr);
					}

					// update the substring offset
					subStringOffset += selectOffset + p.y;
				}

			}

		});

		updateOutline();

		// add close listener for tabItem
		netItem.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent event) {
				CTabItem item = (CTabItem) event.getSource();
				String fileName = item.getText();

				// remove the fileNode from curFileList
				removeFileNode(fileName);

				if (curFileList.size() == 0) {
					curFileNode = null;
					if (!outlineFolder.isDisposed())
						outlineFolder.setVisible(false);
				}
			}

		});

	}

	/* update element_tv be the treeViewer of the current selected file */
	private void updateOutline() {
		outlineFolder.setVisible(true);

		if (curFileNode == null) {
			element_tv.setInput(null);
			return;
		}

		// create the tree in the outline
		if (curFileNode.getName().endsWith(".cdat")) {
			// CDAT
			selectAllBnt.setEnabled(true);

			DATFileData data = (DATFileData) curFileNode.getFileData();
			element_tv.setInput(data.getSpeciesFolder());

			// set selection on element_tv
			String selectedSpeciesName = data.getSelectedSpeciesName();
			if (selectedSpeciesName != null) {
				element_tv.setSelection(new StructuredSelection(
						findSpeciesNode(selectedSpeciesName)), true);
			}

			// set some nodes be checked
			element_tv.setCheckedElements(data.getCheckedSpecies());

		} else if (curFileNode.getName().endsWith(".gdat")
				|| curFileNode.getName().endsWith(".scan")) {
			// GDAT & SCAN
			selectAllBnt.setEnabled(true);

			DATFileData data = (DATFileData) curFileNode.getFileData();
			element_tv.setInput(data.getObservableFolder());

			// set selection on element_tv
			String selectedObservableName = data.getSelectedObservableName();
			if (selectedObservableName != null) {
				element_tv.setSelection(new StructuredSelection(
						findObservableNode(selectedObservableName)), true);
			}

			// set some nodes be checked
			element_tv.setCheckedElements(data.getCheckedObservable());

		} else if (curFileNode.getName().endsWith(".net")) {
			// NET
			selectAllBnt.setEnabled(false);

			NETFileData data = (NETFileData) curFileNode.getFileData();
			element_tv.setInput(data.getNETFolderNode());

		}

	}

	/*
	 * Return speciesNode based on index
	 */
	private SpeciesNode findSpeciesNode(String speciesName) {
		Tree tree = element_tv.getTree();
		for (int i = 0; i < tree.getItemCount(); i++) {
			if (tree.getItem(i).getText().equalsIgnoreCase(speciesName)) {
				return (SpeciesNode) tree.getItem(i).getData();
			}
		}
		return null;
	}

	/*
	 * Return ObservableNode based on index
	 */
	private ObservableNode findObservableNode(String observableName) {
		Tree tree = element_tv.getTree();
		for (int i = 0; i < tree.getItemCount(); i++) {
			if (tree.getItem(i).getText().equalsIgnoreCase(observableName)) {
				return (ObservableNode) tree.getItem(i).getData();
			}
		}
		return null;
	}

	/*
	 * Return fileFolderNode based on name
	 */
	private TreeNode findFileFolderNode(String expand) {
		if (expand.indexOf(" ") == -1)
			return null;

		String[] tmp = expand.split(" ");
		String modelName = tmp[0];
		String dateTime = tmp[1];

		// first level children
		Object[] firstLevel = rootNode.getChildrenArray();

		for (int i = 0; i < firstLevel.length; i++) {
			TreeNode curFirst = (TreeNode) firstLevel[i];
			// match the model name
			if (curFirst.getName().equalsIgnoreCase(modelName)) {
				Object[] secondLevel = ((TreeNode) curFirst).getChildrenArray();

				if (secondLevel == null) {
					return curFirst;
				}

				// TODO can't find second level children
				for (int j = 0; j < secondLevel.length; j++) {
					TreeNode curSecond = (TreeNode) secondLevel[j];
					// match the date and time
					if (curSecond.getName().equalsIgnoreCase(dateTime)) {
						return curSecond;
					}
				}
			}
		}
		return null;
	}

	/*
	 * Return file node from curFileList by name.
	 */
	private FileNode getFileNode(String text) {
		for (int i = 0; i < curFileList.size(); i++) {
			if (curFileList.get(i).getName().equals(text))
				return curFileList.get(i);
		}
		return null;
	}

	/*
	 * Remove file node from curFileList by name.
	 */
	private void removeFileNode(String text) {
		for (int i = 0; i < curFileList.size(); i++) {
			if (curFileList.get(i).getName().equals(text)) {
				curFileList.remove(i);
				return;
			}
		}
	}

	/*
	 * Show a graph using JFrame
	 */

	private void showGraph(FileData data, int id, String speciesExp) {
		// get the display
		prefuse.Display dis = data.getDisplay();

		if (dis == null || !dis.getName().equals(speciesExp)) {
			dis = new SpeciesGraph(id, speciesExp).getDisplay();
			dis.setName(speciesExp);
			data.setDisplay(dis);

			// TODO adjust the location of graph
		}

		if (jf == null) {
			jf = new JFrame();
			jf.setBackground(java.awt.Color.WHITE);
			jf.setTitle("Graph");
			jf.setVisible(true);

			// TODO adjust the size of graph
			jf.addWindowStateListener(new WindowStateListener() {

				public void windowStateChanged(WindowEvent e) {
					if (e.getOldState() != e.getNewState()) {
						switch (e.getNewState()) {
						case Frame.MAXIMIZED_BOTH:
							System.out.println("MAXIMIZED_BOTH");
							break;
						case Frame.ICONIFIED:
							System.out.println("ICONIFIED");
							break;
						case Frame.NORMAL:
							System.out.println("NORMAL");
							break;
						default:
							break;
						}
					}

				}

			});

		} else {
			if (jf.isShowing() == false) {
				jf.setVisible(true);
			}
			jf.getContentPane().removeAll();
		}

		// set size and position of the JFrame
		Rectangle displayRec = Display.getCurrent().getBounds();
		Rectangle shellRec = getShell().getBounds();
		int graphWidth = displayRec.width - shellRec.width - shellRec.x;
		if (graphWidth > shellRec.width)
			graphWidth = shellRec.width;

		jf.setLocation(shellRec.x + shellRec.width, shellRec.y);
		jf.setPreferredSize(new Dimension(graphWidth, shellRec.height));

		// add the Display to the JFrame
		jf.getContentPane().add(dis);
		jf.pack();
		getShell().setFocus();
	}

	/*
	 * Return the graph frame.
	 */

	public JFrame getGraphFrame() {
		return this.jf;
	}

	/*
	 * public static void main(String[] args) { ResultViewer w = new
	 * ResultViewer(); w.setBlockOnOpen(true); w.open();
	 * 
	 * if (w.jf != null && w.jf.isShowing()) { w.jf.dispose(); // close the
	 * graph } Display.getCurrent().dispose(); }
	 */
}
