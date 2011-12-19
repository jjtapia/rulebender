package rulebender.results.view;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeViewerListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeExpansionEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.part.ViewPart;
import org.jfree.chart.JFreeChart;

import rulebender.editors.net.NETConfiguration;
import rulebender.editors.net.NETDocumentProvider;
import rulebender.navigator.model.DATFileData;
import rulebender.navigator.model.FileNode;
import rulebender.navigator.model.LogFileData;
import rulebender.navigator.model.NETFileData;
import rulebender.navigator.model.NETItemNode;
import rulebender.navigator.model.ObservableNode;
import rulebender.navigator.model.SpeciesNode;
import rulebender.navigator.model.TreeContentProvider;
import rulebender.navigator.model.TreeLabelProvider;
import rulebender.navigator.model.TreeNode;

public class ResultsView extends ViewPart implements ISelectionProvider 
{
	private static Color color_DarkGoldenrod1 = new Color(null, 255, 185, 15);
	
	private SashForm split;
	
	private CustomizedChartComposite curChartPanel; // chart panel for CDAT, GDAT, SCAN files
	
	private CTabFolder textFolder, outlineFolder;
	private FileNode curFileNode;
	
	private String chartType = "line";

	// control options
	private String xAxisType = "linear";
	private String yAxisType = "linear";

	
	// syntax highlighting configure for NET and BNGL files
	private NETConfiguration netConfig;
	private NETDocumentProvider netProvider;
	
	private ArrayList<FileNode> curFileList;
	
	private Composite outlineCmp_cdat, outlineCmp_gdat, outlineCmp_compare;
	private CTabItem elementOutline;
	
	private CheckboxTreeViewer element_tv_cdat;
	private CheckboxTreeViewer element_tv_gdat;
	private TreeViewer element_tv_net;
	private ArrayList<CheckboxTreeViewer> element_tv_compare;
	private ArrayList<String> chartTypeList; // used for comparing multiple files
	
	private ListenerList listeners = new ListenerList();
	private String selection="";
	
	public ResultsView() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		split = new SashForm(parent, SWT.VERTICAL | SWT.NULL); 
				
		netConfig = new NETConfiguration();
		netProvider = new NETDocumentProvider();
	
		chartTypeList = new ArrayList<String>();
		
		curFileList = new ArrayList<FileNode>();
		// Create the CTabFolder that will hold the results.
		initTextFolder(split);
		initOutline(split);
		
		getSite().setSelectionProvider(this);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	/**
	 * Sloppily called from the results file navigator.
	 * @param node
	 */
	public void openFile(FileNode node)
	{
		System.out.println("Results path being opened: " + node.getPath());
		openFileItem(node);
		
	}

	/*
	 * Initialize text tab folder
	 */
	private void initTextFolder(Composite parent) 
	{
		textFolder = new CTabFolder(parent, SWT.BORDER);
		textFolder.setSimple(true);
		textFolder.setBackground(new Color(Display.getCurrent(), 240, 240, 240));

	}
	
	private void initOutline(Composite parent)
	{
		outlineFolder = new CTabFolder(parent, SWT.BORDER);
		
		outlineFolder.setSimple(true);
		elementOutline = new CTabItem(outlineFolder, SWT.NONE);
		elementOutline.setText("Outline");

		// set default selection on elementOutline tab item
		outlineFolder.setSelection(elementOutline);

		createNETOutline();
		createCDATOutline();
		createGDATOutline();
		createCompareOutline();


	}
	
	/*
	 * Create the outline for NET files
	 */
	private void createNETOutline() 
	{
	
		// create the tree viewer
		element_tv_net = new TreeViewer(outlineFolder, SWT.FULL_SELECTION);
		element_tv_net.getTree()
				.setLayoutData(new GridData(GridData.FILL_BOTH));
		element_tv_net.setContentProvider(new TreeContentProvider());
		element_tv_net.setLabelProvider(new TreeLabelProvider());

		// nothing in the tree at the beginning
		element_tv_net.setInput(null);

		element_tv_net.addSelectionChangedListener(new ISelectionChangedListener() 
				{
					public void selectionChanged(SelectionChangedEvent event) {

						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();

						// empty selection
						if (selection.isEmpty())
							return;

						NETItemNode node = (NETItemNode) selection
								.getFirstElement();
						NETFileData data = (NETFileData) curFileNode
								.getFileData();

						// reset selection and reveal range
						int offset = node.getOffset();
						int length = node.getLength();
						data.getSourceViewer().revealRange(offset, length);

						// store the selected item
						data.setSelectedItem(node.getName());

						//setStatus("Link to the part of \"" + node.getName()
							//	+ "\"");
					}

				});
	}

	/*
	 * Create outline for CDAT files
	 */
	private void createCDATOutline() {

		outlineCmp_cdat = new Composite(outlineFolder, SWT.NONE);
		outlineCmp_cdat.setLayout(new GridLayout(2, false));

		// element tree viewer
		element_tv_cdat = new CheckboxTreeViewer(outlineCmp_cdat);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		element_tv_cdat.getTree().setLayoutData(gridData);
		element_tv_cdat.setContentProvider(new TreeContentProvider());
		element_tv_cdat.setLabelProvider(new TreeLabelProvider());

		// nothing in the tree at the beginning
		element_tv_cdat.setInput(null);

		// select all button

		final Button selectAllBnt = new Button(outlineCmp_cdat, SWT.CHECK);
		selectAllBnt.setText("Check/Uncheck All");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		selectAllBnt.setLayoutData(gridData);

		chartType = "line";

		// chart type
		new Label(outlineCmp_cdat, SWT.NONE).setText("Chart");

		final Combo chartTypeBnt = new Combo(outlineCmp_cdat, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		chartTypeBnt.add("line");
		chartTypeBnt.add("point");
		chartTypeBnt.add("line & point");
		chartTypeBnt.setText(chartType);

		// add listener
		chartTypeBnt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chartType = chartTypeBnt.getText();

				DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
						yAxisType, chartType));

				data.setChartType(chartType);
			}
		});

		// add selection listener for clear button
		selectAllBnt.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent arg0) {
				boolean selected = selectAllBnt.getSelection();
				if (selected == true) {
					// select all

					element_tv_cdat.setAllChecked(true);

					Object[] elements = element_tv_cdat.getCheckedElements();

					// GDAT & SCAN
					DATFileData data = (DATFileData) curFileNode.getFileData();

					// check all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
							data.addCheckedSpecies((SpeciesNode) elements[i]);
						}
					}
					// set allChecked be true
					data.setAllChecked(true);
					// set some nodes be checked
					element_tv_cdat.setCheckedElements(data.getCheckedSpecies());
					// plot the chart
					curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
							yAxisType, chartType));

				} else {
					// remove all

					Object[] elements = element_tv_cdat.getCheckedElements();

					// GDAT & SCAN
					DATFileData data = (DATFileData) curFileNode.getFileData();

					// uncheck all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
							data.removeCheckedSpecies((SpeciesNode) elements[i]);
						}
					}
					// set allChecked be false
					data.setAllChecked(false);
					// set some nodes be checked
					element_tv_cdat.setCheckedElements(data.getCheckedSpecies());
					curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
							yAxisType, chartType));

					element_tv_cdat.setAllChecked(false);

					data.setSelectedSpeciesName(null);
					element_tv_cdat.setSelection(null);
				}
			}

		});

		// set selection be empty when user click on empty area
		element_tv_cdat.getTree().addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent arg0) {

			}

			public void mouseDown(MouseEvent event) {

				Point p = Display.getCurrent().getCursorLocation();
				p = element_tv_cdat.getTree().toControl(p);
				// get the item corresponding the click point
				TreeItem item = element_tv_cdat.getTree().getItem(p);
				if (item == null) {
					element_tv_cdat.setSelection(null);
					DATChart.resetChart(curChartPanel.getChart()); // reset chart
				}
			}

			public void mouseUp(MouseEvent arg0) {

			}

		});

		// add check listener for element_tv_cdat
		element_tv_cdat.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				// get data
				DATFileData data = (DATFileData) curFileNode.getFileData();
				// SpeciesNode
				TreeNode node = (TreeNode) event.getElement();
				
				if (event.getChecked() == true) {
					data.addCheckedSpecies((SpeciesNode) event.getElement());
					//setStatus("Checked:" + node.getName());
				}
				// not check
				else if (event.getChecked() == false) {
					data.removeCheckedSpecies((SpeciesNode) event.getElement());
					//setStatus("Unchecked:" + node.getName());
				}
				
				// update chart
				curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
						yAxisType, chartType));

				// mark line
				String labelName = data.getSelectedSpeciesName();
				if (labelName != null) {
					String key = labelName.split(" ")[0]; // index
					DATChart.markSelectedLine(curChartPanel.getChart(), key);
					
					element_tv_cdat.setSelection(new StructuredSelection(
							findSpeciesNode(element_tv_cdat, labelName)),
							true);
				}
			}
		});

		// add selection listener for element_tv_cdat
		element_tv_cdat
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {

						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();

						DATFileData data = ((DATFileData) curFileNode
								.getFileData());

						// empty selection
						if (selection.isEmpty()) {
							return;
						}

						String labelName = selection.getFirstElement()
								.toString();

						boolean drawGraph = true;
						if (labelName.equals(data.getSelectedSpeciesName())) {
							// selected species not changed
							drawGraph = false;
						}

						String key = labelName.split(" ")[0]; // index

						// store the name of the selected species
						data.setSelectedSpeciesName(labelName);
						// mark selected line
						DATChart.markSelectedLine(curChartPanel.getChart(), key);

						if (drawGraph && labelName.contains(" ")) {
							// show the species graph in a JFrame
							ArrayList<String> itemList = new ArrayList<String>();
							String item = "species\t" + labelName;
							itemList.add(item);
							showGraph(itemList);
							//setStatus("Selected: " + labelName);
						}
					}
				});
	}

	private void createGDATOutline() {
		outlineCmp_gdat = new Composite(outlineFolder, SWT.NONE);
		outlineCmp_gdat.setLayout(new GridLayout(2, false));

		// element tree viewer
		element_tv_gdat = new CheckboxTreeViewer(outlineCmp_gdat);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		element_tv_gdat.getTree().setLayoutData(gridData);
		element_tv_gdat.setContentProvider(new TreeContentProvider());
		element_tv_gdat.setLabelProvider(new TreeLabelProvider());

		// nothing in the tree at the beginning
		element_tv_gdat.setInput(null);

		// select all button
		final Button selectAllBnt = new Button(outlineCmp_gdat, SWT.CHECK);
		selectAllBnt.setText("Check/Uncheck All");
		gridData = new GridData();

		selectAllBnt.setLayoutData(gridData);
		gridData.horizontalSpan = 2;
		chartType = "line";

		// chart type
		new Label(outlineCmp_gdat, SWT.NONE).setText("Chart");

		final Combo chartTypeBnt = new Combo(outlineCmp_gdat, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		chartTypeBnt.add("line");
		chartTypeBnt.add("point");
		chartTypeBnt.add("line & point");
		chartTypeBnt.setText(chartType);

		// add listener
		chartTypeBnt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				chartType = chartTypeBnt.getText();

				DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
						yAxisType, chartType));

				data.setChartType(chartType);
			}
		});

		// add listener for selectAllBnt
		selectAllBnt.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent arg0) {
				boolean selected = selectAllBnt.getSelection();
				if (selected == true) {
					// select all

					element_tv_gdat.setAllChecked(true);

					Object[] elements = element_tv_gdat.getCheckedElements();

					// GDAT & SCAN
					DATFileData data = (DATFileData) curFileNode.getFileData();

					// check all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							data.addCheckedObservable((ObservableNode) elements[i]);
						}
					}
					// set allChecked be true
					data.setAllChecked(true);
					// set some nodes be checked
					element_tv_gdat.setCheckedElements(data
							.getCheckedObservable());
					// plot the chart
					curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
							yAxisType, chartType));

				} else {
					// remove all

					Object[] elements = element_tv_gdat.getCheckedElements();

					// GDAT & SCAN
					DATFileData data = (DATFileData) curFileNode.getFileData();

					// uncheck all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							data.removeCheckedObservable((ObservableNode) elements[i]);
						}
					}
					// set allChecked be false
					data.setAllChecked(false);
					// set some nodes be checked
					element_tv_gdat.setCheckedElements(data
							.getCheckedObservable());
					curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
							yAxisType, chartType));

					element_tv_gdat.setAllChecked(false);

					data.setSelectedObservableName(null);
					data.setSelectedSpeciesName(null);
					element_tv_gdat.setSelection(null);
				}
			}

		});

		// set selection be empty when user click on empty area
		element_tv_gdat.getTree().addMouseListener(new MouseListener() {

			public void mouseDoubleClick(MouseEvent arg0) {

			}

			public void mouseDown(MouseEvent event) {

				Point p = Display.getCurrent().getCursorLocation();
				p = element_tv_gdat.getTree().toControl(p);
				// get the item corresponding the click point
				TreeItem item = element_tv_gdat.getTree().getItem(p);	
				
				if (item == null) {
					element_tv_gdat.setSelection(null);
					DATChart.resetChart(curChartPanel.getChart()); // reset chart
				}
			}

			public void mouseUp(MouseEvent arg0) {

			}

		});

		// add tree listener to disable some checkboxes

		element_tv_gdat.addTreeListener(new ITreeViewerListener() {

			public void treeCollapsed(TreeExpansionEvent arg0) {

			}

			public void treeExpanded(TreeExpansionEvent event) {
				TreeNode node = (TreeNode) event.getElement();
				if (node.getNodeType().equalsIgnoreCase("ObservableNode")
						|| node.getNodeType().equalsIgnoreCase(
								"SpeciesFolderNode")) {
					Object[] children = node.getChildrenArray();
					for (int i = 0; i < children.length; i++) {
						element_tv_gdat.setGrayChecked(children[i], true);
					}
				}

			}

		});

		element_tv_gdat.addCheckStateListener(new ICheckStateListener() {

			public void checkStateChanged(CheckStateChangedEvent event) {
				// get node
				TreeNode node = (TreeNode) event.getElement();
				// get data
				DATFileData data = (DATFileData) curFileNode
				.getFileData();
				
				if (event.getChecked() == true) {
					if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
						// SpeciesNode
						element_tv_gdat.setGrayChecked(node, true);
					} else if (node.getNodeType().equalsIgnoreCase(
							"ObservableNode")) {
						// ObservableNode
						data.addCheckedObservable((ObservableNode) event
								.getElement());
						//setStatus("Checked:" + node.getName());
					} else if (node.getNodeType().equalsIgnoreCase(
							"SpeciesFolderNode")) {
						// SpeciesFolderNode
						element_tv_gdat.setGrayChecked(node, true);
					}

				}
				// not check
				else if (event.getChecked() == false) {
					if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
						// SpeciesNode
						element_tv_gdat.setGrayChecked(node, true);
					} else if (node.getNodeType().equalsIgnoreCase(
							"ObservableNode")) {
						// ObservableNode
						data.removeCheckedObservable((ObservableNode) event
								.getElement());
						//setStatus("Unchecked:" + node.getName());
					} else if (node.getNodeType().equalsIgnoreCase(
							"SpeciesFolderNode")) {
						// SpeciesFolderNode
						element_tv_gdat.setGrayChecked(node, true);
					}
				}
				
				//plot
				curChartPanel.setChart(DATChart.plotChart(data,
						xAxisType, yAxisType, chartType));

				// mark line
				String labelName = data.getSelectedObservableName();
				if (labelName != null) {
					DATChart.markSelectedLine(curChartPanel.getChart(),
							labelName);
					
					// set selection
					element_tv_gdat.setSelection(new StructuredSelection(
							findObservableNode(element_tv_gdat, labelName)),
							true);
				}
			}
		});

		// add selection changed listener

		element_tv_gdat
				.addSelectionChangedListener(new ISelectionChangedListener() {

					public void selectionChanged(SelectionChangedEvent event) {

						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();

						DATFileData data = ((DATFileData) curFileNode
								.getFileData());

						TreeNode node = (TreeNode) selection.getFirstElement();

						// empty selection
						if (selection.isEmpty()) {
							return;
						}

						String labelName = node.getName();

						if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							// if selected observable changed
							if (!labelName.equals(data
									.getSelectedObservableName())) {
								// store the name of the selected species
								data.setSelectedObservableName(labelName);
							}

							// mark selected line
							DATChart.markSelectedLine(curChartPanel.getChart(),
									labelName);
							//setStatus("Selected: " + labelName);
						} else {
							// SpeciesFolderNode or SpeciesNode
							String str = "species\t";
							if (node.getNodeType().equalsIgnoreCase(
									"SpeciesFolderNode")) {
								str = "pattern\t-1 ";
							}

							if (labelName.equals(data.getSelectedSpeciesName())) {
								// selected species not change
								return;
							}

							// store the name of the selected species
							data.setSelectedSpeciesName(labelName);

							// show the species graph in a JFrame
							ArrayList<String> itemList = new ArrayList<String>();
							String item = str + labelName;
							itemList.add(item);

							showGraph(itemList);
							//setStatus("Selected: " + labelName);
						}
					}

				});

	}

	private void createCompareOutline() {
		outlineCmp_compare = new Composite(outlineFolder, SWT.NONE);
		outlineCmp_compare.setLayout(new GridLayout(2, false));

		element_tv_compare = new ArrayList<CheckboxTreeViewer>();

		for (int i = 0; i < 2; i++) {
			
			Text filePath = new Text(outlineCmp_compare, SWT.READ_ONLY | SWT.MULTI);
			filePath.setText("");
			GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
			gridData.horizontalSpan = 2;
			filePath.setLayoutData(gridData);

			// element tree viewer
			final CheckboxTreeViewer cur_compare = new CheckboxTreeViewer(
					outlineCmp_compare);
			gridData = new GridData(GridData.FILL_BOTH);
			gridData.horizontalSpan = 2;
			cur_compare.getTree().setLayoutData(gridData);
			cur_compare.setContentProvider(new TreeContentProvider());
			cur_compare.setLabelProvider(new TreeLabelProvider());

			// nothing in the tree at the beginning
			cur_compare.setInput(null);

			// select all button
			final Button selectAllBnt = new Button(outlineCmp_compare,
					SWT.CHECK);
			selectAllBnt.setText("Check/Uncheck All");
			gridData = new GridData();

			selectAllBnt.setLayoutData(gridData);
			gridData.horizontalSpan = 2;
			chartTypeList.add("line");

			// chart type
			new Label(outlineCmp_compare, SWT.NONE).setText("Chart");

			final Combo chartTypeBnt = new Combo(outlineCmp_compare,
					SWT.DROP_DOWN | SWT.READ_ONLY);
			chartTypeBnt.add("line");
			chartTypeBnt.add("point");
			chartTypeBnt.add("line & point");
			chartTypeBnt.setText(chartTypeList.get(i));
			chartTypeBnt.setData("index", i);

			// set index for cur_compare
			cur_compare.setData("index", i);

			element_tv_compare.add(cur_compare);

			// add listener
			chartTypeBnt.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					chartTypeList.set((Integer) chartTypeBnt.getData("index"),
							chartTypeBnt.getText());

					DATComparisonData data = (DATComparisonData) curFileNode
							.getFileData(); // plot
					curChartPanel.setChart(DATComparisonChart.plotChart(data,
							chartTypeList));
					curChartPanel.setChart(DATComparisonChart.plotChart(curChartPanel.getChart(), xAxisType, yAxisType));
				}
			});

			// add listener for selectAllBnt
			selectAllBnt.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent arg0) {
					boolean selected = selectAllBnt.getSelection();
					if (selected == true) {
						// select all

						cur_compare.setAllChecked(true);

						Object[] elements = cur_compare.getCheckedElements();

						// Compare
						DATComparisonData data = (DATComparisonData) curFileNode
								.getFileData();

						// check all the ObservableNode
						for (int i = 0; i < elements.length; i++) {
							TreeNode node = (TreeNode) elements[i];
							if (node.getNodeType().equalsIgnoreCase(
									"ObservableNode")) {
								data.addCheckedObservable(
										(Integer) cur_compare.getData("index"),
										(ObservableNode) elements[i]);
							}
							if (node.getNodeType().equalsIgnoreCase(
									"SpeciesNode")) {
								data.addCheckedSpecies(
										(Integer) cur_compare.getData("index"),
										(SpeciesNode) elements[i]);
							}
						}
						// set allChecked be true
						data.setAllChecked(
								(Integer) cur_compare.getData("index"), true);
						// set some nodes be checked
						cur_compare.setCheckedElements(data
								.getCheckedElements((Integer) cur_compare
										.getData("index")));
						// plot the chart
						curChartPanel.setChart(DATComparisonChart.plotChart(
								data, chartTypeList));
						curChartPanel.setChart(DATComparisonChart.plotChart(
								curChartPanel.getChart(), xAxisType, yAxisType));

					} else {
						// remove all

						Object[] elements = cur_compare.getCheckedElements();

						// Compare
						DATComparisonData data = (DATComparisonData) curFileNode
								.getFileData();

						// uncheck all the ObservableNode
						for (int i = 0; i < elements.length; i++) {
							TreeNode node = (TreeNode) elements[i];
							if (node.getNodeType().equalsIgnoreCase(
									"ObservableNode")) {
								data.removeCheckedObservable(
										(Integer) cur_compare.getData("index"),
										(ObservableNode) elements[i]);
							}
							if (node.getNodeType().equalsIgnoreCase(
									"SpeciesNode")) {
								data.removeCheckedSpecies(
										(Integer) cur_compare.getData("index"),
										(SpeciesNode) elements[i]);
							}
						}

						// set allChecked be false
						data.setAllChecked(
								(Integer) cur_compare.getData("index"), false);
						// set some nodes be checked
						cur_compare.setCheckedElements(data
								.getCheckedElements((Integer) cur_compare
										.getData("index")));
						// plot the chart
						curChartPanel.setChart(DATComparisonChart.plotChart(
								data, chartTypeList));
						curChartPanel.setChart(DATComparisonChart.plotChart(
								curChartPanel.getChart(), xAxisType, yAxisType));

						cur_compare.setAllChecked(false);
						cur_compare.setSelection(null);
					}
				}

			});

			// set selection be empty when user click on empty area
			cur_compare.getTree().addMouseListener(new MouseListener() {

				public void mouseDoubleClick(MouseEvent arg0) {

				}

				public void mouseDown(MouseEvent event) {

					Point p = Display.getCurrent().getCursorLocation();
					p = cur_compare.getTree().toControl(p);
					// get the item corresponding the click point
					TreeItem item = cur_compare.getTree().getItem(p);
					if (item == null) {
						int cur_index = (Integer) cur_compare.getData("index");

						cur_compare.setSelection(null);
						DATComparisonChart.resetChart(curChartPanel.getChart(),
								cur_index); // reset chart
					}
				}

				public void mouseUp(MouseEvent arg0) {

				}

			});

			// add tree listener to disable some checkboxes

			cur_compare.addTreeListener(new ITreeViewerListener() {

				public void treeCollapsed(TreeExpansionEvent arg0) {

				}

				public void treeExpanded(TreeExpansionEvent event) {
					TreeNode node = (TreeNode) event.getElement();
					if (node.getNodeType().equalsIgnoreCase("ObservableNode")
							|| node.getNodeType().equalsIgnoreCase(
									"SpeciesFolderNode")) {
						Object[] children = node.getChildrenArray();
						for (int i = 0; i < children.length; i++) {
							cur_compare.setGrayChecked(children[i], true);
						}
					}
				}

			});

			cur_compare.addCheckStateListener(new ICheckStateListener() {

				public void checkStateChanged(CheckStateChangedEvent event) {

					//index for current compare tree viewer
					int cur_index = (Integer) cur_compare.getData("index");
					//get node
					TreeNode node = (TreeNode) event.getElement();
					//get data
					DATComparisonData data = (DATComparisonData) curFileNode
					.getFileData();

					if (event.getChecked() == true) {
						if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
							// SpeciesNode
							if (((TreeNode) node.getParent()).getParent() != null) {
								// GDAT
								cur_compare.setGrayChecked(node, true);

							} else {
								// CDAT
								data.addCheckedSpecies(cur_index,
										(SpeciesNode) event.getElement());
								//setStatus("Checked:" + node.getName());
							}

						} else if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							// ObservableNode
							data.addCheckedObservable(cur_index,
									(ObservableNode) event.getElement());
							//setStatus("Checked:" + node.getName());
						} else if (node.getNodeType().equalsIgnoreCase(
								"SpeciesFolderNode")) {
							// SpeciesFolderNode
							cur_compare.setGrayChecked(node, true);
						}

					}
					// not check
					else if (event.getChecked() == false) {
						if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
							// SpeciesNode
							if (((TreeNode) node.getParent()).getParent() != null) {
								// GDAT
								cur_compare.setGrayChecked(node, true);

							} else {
								// CDAT
								data.removeCheckedSpecies(cur_index,
										(SpeciesNode) event.getElement());
							//	setStatus("Unchecked:" + node.getName());
							}

						} else if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							// ObservableNode
							data.removeCheckedObservable(cur_index,
									(ObservableNode) event.getElement());

							//setStatus("Unchecked:" + node.getName());
						} else if (node.getNodeType().equalsIgnoreCase(
								"SpeciesFolderNode")) {
							// SpeciesFolderNode
							cur_compare.setGrayChecked(node, true);

						}
					}
					curChartPanel.setChart(DATComparisonChart
							.plotChart(data, chartTypeList));
					curChartPanel.setChart(DATComparisonChart.plotChart(
							curChartPanel.getChart(), xAxisType, yAxisType));

					// mark line
					if (data.getSelectedSpeciesName(cur_index) != null) {
						String labelName = data.getSelectedSpeciesName(cur_index);
						String key = labelName.split(" ")[0];
						if (key != null) {
							DATComparisonChart.markSelectedLine(
									curChartPanel.getChart(),
									cur_index, key);
							
							cur_compare.setSelection(new StructuredSelection(
									findSpeciesNode(cur_compare, labelName)),
									true);
							
						}
					}
				}
			});

			// add selection changed listener

			cur_compare.addSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(SelectionChangedEvent event) 
						{
							IStructuredSelection selection = (IStructuredSelection) event.getSelection();

							DATComparisonData data = ((DATComparisonData) curFileNode.getFileData());

							TreeNode node = (TreeNode) selection.getFirstElement();

							// empty selection
							if (selection.isEmpty()) 
							{
								return;
							}

							String labelName = node.getName();
							
							int cur_index = (Integer) cur_compare.getData("index");

							if (node.getNodeType().equalsIgnoreCase("ObservableNode")) 
							{
								// if selected observable changed
								if (!labelName.equals(data.getSelectedObservableName(cur_index))) 
								{
									// store the name of the selected species
									data.setSelectedObservableName(cur_index, labelName);
								}

								// mark selected line
								DATComparisonChart.markSelectedLine(curChartPanel.getChart(), cur_index, labelName);
								//setStatus("Selected: "
							//			+ data.getFileName(cur_index) + " "
							//			+ labelName);
							} 
							
							else 
							{
								// SpeciesFolderNode or SpeciesNode
								String str = "species\t";
								if (node.getNodeType().equalsIgnoreCase(
										"SpeciesFolderNode")) {
									str = "pattern\t-1 ";
								}

								if (labelName.equals(data
										.getSelectedSpeciesName(cur_index))) {
									// selected species not change
									return;
								}

								// store the name of the selected species
								data.setSelectedSpeciesName(cur_index,
										labelName);

								if (data.getFileName(cur_index).endsWith(
										".cdat")) {
									// CDAT

									String key = labelName.split(" ")[0]; // index

									// mark selected line
									DATComparisonChart.markSelectedLine(
											curChartPanel.getChart(),
											cur_index, key);
								}

								if (labelName.contains(" ")) 
								{
									// show the species graph in a JFrame
									ArrayList<String> itemList = new ArrayList<String>();
									String item = str + labelName;
									itemList.add(item);
	
									showGraph(itemList);
									////setStatus("Selected: "
									//		+ data.getFileName(cur_index) + " "
									//		+ labelName);
								}
							}
						}

					});
		}

	}


	private void openFileItem(FileNode node) {
		
		// convert TreeNode to FileNode
		curFileNode = (FileNode) node;

		// add the curFileNode to curFileList
		int nodeIndex = curFileList.indexOf(curFileNode);
		
		if (nodeIndex == -1) 
		{
			curFileList.add(curFileNode);
		} else {
			curFileNode = curFileList.get(nodeIndex);
		}

		// If the file has not opened, create corresponding chart or
		// text, and then open it.

		if (curFileNode.getName().endsWith(".net")
				|| curFileNode.getName().endsWith(".bngl")
				|| curFileNode.getName().endsWith(".log")
				|| curFileNode.getName().endsWith(".pl")
				|| curFileNode.getName().endsWith(".m")
				|| curFileNode.getName().endsWith(".xml")
				|| curFileNode.getName().endsWith(".rxn")
				|| curFileNode.getName().endsWith(".cfg")) {
			// NET
			// check if the file has already opened
			boolean opened = false;
			for (int i = 0; i < textFolder.getItemCount(); i++) {
				CTabItem item = textFolder.getItem(i);
				String path = (String) item.getData("path");
				if (path.equals(curFileNode.getPath())) {
					// set selection on this matched item
					textFolder.setSelection(item);

					updateOutline();
					opened = true;
					break;
				}
			}
			if (opened == false) {

				if (curFileNode.getName().endsWith(".log")
						|| curFileNode.getName().endsWith(".pl")
						|| curFileNode.getName().endsWith(".m")
						|| curFileNode.getName().endsWith(".xml")
						|| curFileNode.getName().endsWith(".rxn")
						|| curFileNode.getName().endsWith(".cfg")) {
					// create a new LOGItem
					createLOGItem(curFileNode);
				} else {
					// create a new netItem
					createNetItem(curFileNode);
				}

				updateOutline();
			}

		} else if (curFileNode.getName().endsWith(".cdat")
				|| curFileNode.getName().endsWith(".gdat")
				|| curFileNode.getName().endsWith(".scan")) {
			// CDAT & GDAT & SCAN

			// check if the file has already opened
			boolean opened = false;
			for (int i = 0; i < textFolder.getItemCount(); i++) {
				CTabItem item = textFolder.getItem(i);
				String path = (String) item.getData("path");
				if (path.equals(curFileNode.getPath())) {
					// set selection on this matched item
					textFolder.setSelection(item);
					int chartIndex = ((Composite) item.getControl())
							.getChildren().length - 1;
					curChartPanel = (CustomizedChartComposite) ((Composite) item
							.getControl()).getChildren()[chartIndex];
					DATChart.resetChart(curChartPanel.getChart()); // reset

					updateOutline();
					opened = true;
					break;
				}
			}
			if (opened == false) {
				// create a new chartItem
				createChartItem(curFileNode);
			}
		}
	}
	
	private void createLOGItem(FileNode fNode) {
		// open the text with TextViewer
		CTabItem LOGItem = new CTabItem(textFolder, SWT.CLOSE);
		LOGItem.setText(fNode.getName());

		// set tool tip
		String tooltip = fNode.getPath();
		
		LOGItem.setToolTipText(tooltip);

		LOGItem.setData("path", fNode.getPath());

		// use SourceViewer to control the NET file
		final SourceViewer sv = new SourceViewer(textFolder, null,
				SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);

		final LogFileData data = (LogFileData) fNode.getFileData();
		Document document = new Document(data.getFileContent());
		sv.setDocument(document); // connect sourceViewer with document

		LOGItem.setControl(sv.getControl());
		textFolder.setSelection(LOGItem);

		// add close listener for tabItem
		LOGItem.addDisposeListener(new DisposeListener() 
		{
			public void widgetDisposed(DisposeEvent event) 
			{
				CTabItem item = (CTabItem) event.getSource();
				String filePath = (String) item.getData("path");

				// remove the fileNode from curFileList
				removeFileNode(filePath);

				if (curFileList.size() == 0) 
				{
					curFileNode = null;
					if (elementOutline != null
							&& elementOutline.isDisposed() == false) 
					{
						elementOutline.setControl(null);
						// maximize explorerFolder
					//	sash_form_top.setMaximizedControl(sash_form_top
						//		.getChildren()[0]);
					}
				}
			}

		});
	}
	
	/*
	 * create a netItem on textFolder when the corresponding file was double
	 * clicked
	 */
	private void createNetItem(FileNode fNode) 
	{
		// open the text with TextViewer
		CTabItem netItem = new CTabItem(textFolder, SWT.CLOSE);
		netItem.setText(fNode.getName());

		// set tool tip
		String tooltip = fNode.getPath();
		netItem.setToolTipText(tooltip);

		netItem.setData("path", fNode.getPath());

		// use SourceViewer to control the NET file
		final SourceViewer sv = new SourceViewer(textFolder, null,
				SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);

		if (netConfig == null)
			netConfig = new NETConfiguration();

		final NETFileData data = (NETFileData) fNode.getFileData();
		Document document = null;
		try {
			document = (Document) netProvider.createDocument(data.getFileContent());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		sv.setDocument(document); // connect sourceViewer with document
		sv.configure(netConfig); // set configuration

		// get the style ranges
		final StyleRange[] styleRanges = sv.getTextWidget().getStyleRanges();

		data.setSourceViewer(sv); // store the sourceViewer in NETFileData

		netItem.setControl(sv.getControl());
		textFolder.setSelection(netItem);

		if (fNode.getName().endsWith(".net")) 
		{
			// TODO set pop up menu
			//sv.getControl().setMenu(textMenu);
		}

		data.setSelectedItem(null);

		// add post selection listener
		sv.addPostSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {

				// reset all style ranges
				sv.getTextWidget().setStyleRanges(styleRanges);

				// set background color for all the text which is the same as
				// the selected one
				String fileContent = data.getFileContent();
				Point p = sv.getSelectedRange();
				if (p.y == 0) {
					int lineNum = sv.getTextWidget().getLineAtOffset(p.x) + 1;
					//setStatus("Line: " + Integer.toString(lineNum));
					return;
				}

				String selected = "";

				// get the selected string
				for (int i = p.x; i < p.x + p.y; i++) {
					selected += fileContent.charAt(i);
				}

				// whitespace
				if (selected.equals(" ")) {
					return;
				}

				// multiple line
				if (selected.contains("\n")) {
					return;
				}

				// set status line
			//	setStatus("Select Text: " + selected);

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
				String filePath = (String) item.getData("path");

				// remove the fileNode from curFileList
				removeFileNode(filePath);

				if (curFileList.size() == 0) 
				{
					curFileNode = null;
					if (elementOutline != null
							&& elementOutline.isDisposed() == false) {
						elementOutline.setControl(null);
						// maximize explorerFolder
					//	sash_form_top.setMaximizedControl(sash_form_top
						//		.getChildren()[0]);
					}
				}
			}

		});
	}

	
	/*
	 * create a chartItem on textFolder when the corresponding file was double
	 * clicked
	 */
	private void createChartItem(FileNode fNode) 
	{
		CTabItem chartItem = new CTabItem(textFolder, SWT.CLOSE);
		chartItem.setText(fNode.getName());

		// set tool tip
		String tooltip = fNode.getPath();
		chartItem.setToolTipText(tooltip);

		chartItem.setData("path", fNode.getPath());

		Composite chartCmp = new Composite(textFolder, SWT.NONE);
		chartCmp.setLayout(new GridLayout(6, false));

		
		// x axis scale
		new Label(chartCmp, SWT.NONE).setText("\tX Axis");

		final Combo xAxisBnt = new Combo(chartCmp, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		xAxisBnt.add("linear");
		xAxisBnt.add("log");
		xAxisBnt.setText(xAxisType);

		// y axis scale
		new Label(chartCmp, SWT.NONE).setText("\tY Axis");

		final Combo yAxisBnt = new Combo(chartCmp, SWT.DROP_DOWN
				| SWT.READ_ONLY);
		yAxisBnt.add("linear");
		yAxisBnt.add("log");
		yAxisBnt.setText(yAxisType);

		xAxisBnt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (xAxisType.equals(xAxisBnt.getText())) {
					return ;
				}
				xAxisType = xAxisBnt.getText();

				DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
						yAxisType, chartType));
				curChartPanel.redraw();
				updateOutline();
			}
		});
		yAxisBnt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (yAxisType.equals(yAxisBnt.getText())) {
					return;
				}
				
				yAxisType = yAxisBnt.getText();

				DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				curChartPanel.setChart(DATChart.plotChart(data, xAxisType,
						yAxisType, chartType));
				//curChartPanel.redraw();
				updateOutline();
			}
		});

		// create the chart using JFreeChart
		JFreeChart chart = DATChart.plotChart(
				(DATFileData) fNode.getFileData(), xAxisType, yAxisType,
				chartType);
		CustomizedChartComposite chartPanel = new CustomizedChartComposite(chartCmp, SWT.NONE,
				chart, true);
		GridData griddata = new GridData(GridData.FILL_BOTH);
		griddata.horizontalSpan = 6;
		chartPanel.setLayoutData(griddata);
		curChartPanel = chartPanel;

		// set control
		chartItem.setControl(chartCmp);
		textFolder.setSelection(chartItem);

		DATFileData data = (DATFileData) fNode.getFileData();
		data.setSelectedObservableName(null);
		data.setSelectedSpeciesName(null);
		data.setExpandedElements(null);

		// reset chart type
		chartType = "line";
		data.setChartType("line");

		updateOutline();

		/* This code selects all of the observables from the start. 
		 * This incurs high overhead and is not always necessary, so if the
		 * user wants to load all of the data points they can easily manually select it. 
		if (fNode.getName().endsWith(".gdat")
				|| fNode.getName().endsWith(".scan")) 
		{

			// update the "check/uncheck all" button
			data.setAllChecked(true);
			
			((Button) outlineCmp_gdat.getChildren()[1]).setSelection(true);

			// update checkbox in the tree
			element_tv_gdat.setAllChecked(true);

			Object[] elements = element_tv_gdat.getCheckedElements();

			for (int i = 0; i < elements.length; i++) {
				TreeNode node = (TreeNode) elements[i];
				if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
					// SpeciesNode
					data.addCheckedSpecies((SpeciesNode) elements[i]);
				} else if (node.getNodeType()
						.equalsIgnoreCase("ObservableNode")) {
					// ObservableNode
					data.addCheckedObservable((ObservableNode) elements[i]);
				} else {
				}
			}
			
		} else if (fNode.getName().endsWith(".cdat")) {
			// update the "check/uncheck all" button
			data.setAllChecked(true);
			
			((Button) outlineCmp_cdat.getChildren()[1]).setSelection(true);

			// update checkbox in the tree
			element_tv_cdat.setAllChecked(true);

			Object[] elements = element_tv_cdat.getCheckedElements();

			for (int i = 0; i < elements.length; i++) {
				TreeNode node = (TreeNode) elements[i];
				if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
					// SpeciesNode
					data.addCheckedSpecies((SpeciesNode) elements[i]);
				} else {
				}
			}   
		}
		 */
		// plot chart
		curChartPanel.setChart(DATChart.plotChart(data, xAxisType, yAxisType,
				chartType));
		

		// add close listener for tabItem
		chartItem.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent event) {
				CTabItem item = (CTabItem) event.getSource();

				String filePath = (String) item.getData("path");

				// remove the fileNode from curFileList
				removeFileNode(filePath);

				if (curFileList.size() == 0) 
				{
					curFileNode = null;
					if (elementOutline != null&& elementOutline.isDisposed() == false) 
					{
						elementOutline.setControl(null);
					}
				}
			}

		});

	}

	/*
	 * Return speciesNode based on index
	 */
	private SpeciesNode findSpeciesNode(CheckboxTreeViewer tv, String speciesName) {
		Tree tree = tv.getTree();
		for (int i = 0; i < tree.getItemCount(); i++) {
			if (tree.getItem(i).getText().equalsIgnoreCase(speciesName)) {
				return (SpeciesNode) tree.getItem(i).getData();
			}
		}
		return null;
	}
	

	/*
	 * Show the graph for itemList
	 */
	// TODO Switch this to using the NetworkViewer
	private boolean showGraph(ArrayList<String> itemList) 
	{
		// add items to graph
		String curItem = itemList.get(0);
		setSelection(new StructuredSelection(curItem));
		return true;

	}
	
	/* update element_tv be the treeViewer of the current selected file */
	private void updateOutline() {

		if (curFileNode == null) {
			elementOutline.setControl(null);
			return;
		}

		if (curFileNode.getName().contains("Compare")) {
			// Compare
			elementOutline.setControl(outlineCmp_compare);

			DATComparisonData data = (DATComparisonData) curFileNode
					.getFileData();
			for (int i = 0; i < element_tv_compare.size(); i++) {
				CheckboxTreeViewer cur_compare = element_tv_compare.get(i);

				// set input
				cur_compare.setInput(data.getFolderNode(i));

				// set selection on element_tv
				if (data.getFileName(i).endsWith(".cdat")) {
					String selectedSpeciesName = data.getSelectedSpeciesName(i);
					if (selectedSpeciesName != null) {
						cur_compare.setSelection(new StructuredSelection(
								findSpeciesNode(cur_compare, selectedSpeciesName)), true);
					} else {
						cur_compare.setSelection(null);
					}
				} else {
					String selectedObservableName = data
							.getSelectedObservableName(i);
					if (selectedObservableName != null) {
						cur_compare.setSelection(new StructuredSelection(
								findObservableNode(cur_compare, selectedObservableName)),
								true);
					} else {
						cur_compare.setSelection(null);
					}
				}

				// set some nodes be checked
				cur_compare.setCheckedElements(data.getCheckedElements(i));
			}
			return;
		}

		// create the tree in the outline
		if (curFileNode.getName().endsWith(".cdat")) {

			elementOutline.setControl(outlineCmp_cdat);
			// CDAT

			DATFileData data = (DATFileData) curFileNode.getFileData();
			element_tv_cdat.setInput(data.getSpeciesFolder());

			// set selection on element_tv
			String selectedSpeciesName = data.getSelectedSpeciesName();
			if (selectedSpeciesName != null) {
				element_tv_cdat.setSelection(new StructuredSelection(
						findSpeciesNode(element_tv_cdat, selectedSpeciesName)), true);
			} else {
				element_tv_cdat.setSelection(null);
			}

			// set some nodes be checked
			element_tv_cdat.setCheckedElements(data.getCheckedSpecies());

			// set chart type
			int index = outlineCmp_cdat.getChildren().length - 1;
			((Combo) outlineCmp_cdat.getChildren()[index]).setText(data
					.getChartType());

		} else if (curFileNode.getName().endsWith(".gdat")
				|| curFileNode.getName().endsWith(".scan")) {
			// GDAT & SCAN
			elementOutline.setControl(outlineCmp_gdat);

			DATFileData data = (DATFileData) curFileNode.getFileData();
			element_tv_gdat.setInput(data.getObservableFolder());

			// set selection on element_tv
			String selectedObservableName = data.getSelectedObservableName();
			if (selectedObservableName != null) {
				element_tv_gdat.setSelection(new StructuredSelection(
						findObservableNode(element_tv_gdat, selectedObservableName)), true);
			} else {
				element_tv_gdat.setSelection(null);
			}

			// set some nodes be checked
			element_tv_gdat.setCheckedElements(data.getCheckedObservable());

			((Button) outlineCmp_gdat.getChildren()[1]).setSelection(data
					.isAllChecked());

			// expand nodes
			if (data.getExpandedElements() != null) {
				element_tv_gdat.setExpandedElements(data.getExpandedElements());
			}

			// set grayed nodes
			Object[] toSetGray = data.getGrayedElements();
			if (toSetGray != null) {
				for (int i = 0; i < toSetGray.length; i++) {
					element_tv_gdat.setGrayChecked(toSetGray[i], true);
				}
			}

			// set chart type
			int index = outlineCmp_gdat.getChildren().length - 1;
			((Combo) outlineCmp_gdat.getChildren()[index]).setText(data
					.getChartType());

		} else if (curFileNode.getName().endsWith(".net")
				|| curFileNode.getName().endsWith(".bngl")) {
			// NET

			NETFileData data = (NETFileData) curFileNode.getFileData();
			element_tv_net.setInput(data.getNETFolderNode());
			elementOutline.setControl(element_tv_net.getControl());

			// set selection on selected item
			String selectedItem = data.getSelectedItem();
			if (selectedItem != null) {
				element_tv_net.setSelection(new StructuredSelection(
						findNETItemNode(selectedItem)), true);
			} else {
				element_tv_net.setSelection(null);
			}

		} else if (curFileNode.getName().endsWith(".log")
				|| curFileNode.getName().endsWith(".pl")
				|| curFileNode.getName().endsWith(".m")
				|| curFileNode.getName().endsWith(".xml")
				|| curFileNode.getName().endsWith(".rxn")
				|| curFileNode.getName().endsWith(".cfg")) {
			elementOutline.setControl(null);
		}

	}
	
	/*
	 * Return NETItemNode based on name
	 */
	private NETItemNode findNETItemNode(String itemName) {
		Tree tree = element_tv_net.getTree();
		for (int i = 0; i < tree.getItemCount(); i++) {
			if (tree.getItem(i).getText().equals(itemName)) {
				return (NETItemNode) tree.getItem(i).getData();
			}
		}
		return null;
	}
	
	/*
	 * Return ObservableNode based on index
	 */
	private ObservableNode findObservableNode(CheckboxTreeViewer tv, String observableName) {
		Tree tree = tv.getTree();
		for (int i = 0; i < tree.getItemCount(); i++) {
			if (tree.getItem(i).getText().equalsIgnoreCase(observableName)) {
				return (ObservableNode) tree.getItem(i).getData();
			}
		}
		return null;
	}
	
	/*
	 * Remove file node from curFileList by name.
	 */
	private void removeFileNode(String text) {
		for (int i = 0; i < curFileList.size(); i++) {
			if (curFileList.get(i).getPath().equals(text)) {
				curFileList.remove(i);
				return;
			}
		}
	}

	public void addSelectionChangedListener(ISelectionChangedListener listener) 
	{
		listeners.add(listener);
		
	}

	public ISelection getSelection() 
	{
		return new StructuredSelection(selection);
	}

	public void removeSelectionChangedListener(
			ISelectionChangedListener listener) 
	{
		listeners.remove(listener);
		
	}

	public void setSelection(ISelection select) 
	{		
		Object[] list = listeners.getListeners();  
		for (int i = 0; i < list.length; i++) 
		{  
			((ISelectionChangedListener) list[i]).selectionChanged(new SelectionChangedEvent(this, select));  
		 }  
	}
}
