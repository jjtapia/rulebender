package rulebender.editors.dat;

import java.io.File;
import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.part.FileEditorInput;
import org.jfree.chart.JFreeChart;

import rulebender.editors.common.PathEditorInput;
import rulebender.navigator.model.ObservableNode;
import rulebender.navigator.model.SpeciesNode;
import rulebender.navigator.model.TreeContentProvider;
import rulebender.navigator.model.TreeLabelProvider;
import rulebender.navigator.model.TreeNode;
import rulebender.results.data.DATFileData;
import rulebender.results.view.CustomizedChartComposite;
import rulebender.results.view.DATChart;

public class ChartComposite extends Composite implements ISelectionProvider 
{	
	private SashForm split;
	
	private CustomizedChartComposite curChartPanel; // chart panel for CDAT, GDAT, SCAN files
	
	private CTabFolder textFolder, outlineFolder;
	//private FileNode curFileNode;
	private DATFileData m_datFileData;
	
	private String chartType = "line";

	// control options
	private String xAxisType = "linear";
	private String yAxisType = "linear";

	private Composite outlineCmp_cdat, outlineCmp_gdat; //, outlineCmp_compare;
	private CTabItem elementOutline;
	
	private CheckboxTreeViewer element_tv_cdat;
	private CheckboxTreeViewer element_tv_gdat;
	//private ArrayList<CheckboxTreeViewer> element_tv_compare;
	//private ArrayList<String> chartTypeList; // used for comparing multiple files
	
	private ListenerList listeners = new ListenerList();
	private String selection="";

	public ChartComposite(Composite composite, IEditorInput editorInput, IWorkbenchPartSite iWorkbenchPartSite) 
	{
		super(composite, SWT.None);
		
		this.setLayout(new FillLayout());
			
		split = new SashForm(this, SWT.VERTICAL | SWT.NULL); 
	
		//chartTypeList = new ArrayList<String>();
		
		IFile iFile = ((FileEditorInput) ((IEditorInput) editorInput)).getFile();
		m_datFileData = new DATFileData(new File(iFile.getLocation().toOSString()));
		
		// Create the CTabFolder that will hold the results.
		initTextFolder(split);
		initOutline(split);
		
		createChartItem(editorInput);
		
		iWorkbenchPartSite.setSelectionProvider(this);
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
		
		createCDATOutline();
		createGDATOutline();
		// TODO  Figure out how to do comparisons
		//createCompareOutline();
	}
	

	/*
	 * Create outline for CDAT files
	 */
	private void createCDATOutline() 
	{
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

				//DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
						yAxisType, chartType));

				m_datFileData.setChartType(chartType);
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
					//DATFileData data = (DATFileData) curFileNode.getFileData();

					// check all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
							m_datFileData.addCheckedSpecies((SpeciesNode) elements[i]);
						}
					}
					// set allChecked be true
					m_datFileData.setAllChecked(true);
					// set some nodes be checked
					element_tv_cdat.setCheckedElements(m_datFileData.getCheckedSpecies());
					// plot the chart
					curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
							yAxisType, chartType));

				} else {
					// remove all

					Object[] elements = element_tv_cdat.getCheckedElements();

					// GDAT & SCAN
					//DATFileData data = (DATFileData) curFileNode.getFileData();

					// uncheck all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
							m_datFileData.removeCheckedSpecies((SpeciesNode) elements[i]);
						}
					}
					// set allChecked be false
					m_datFileData.setAllChecked(false);
					// set some nodes be checked
					element_tv_cdat.setCheckedElements(m_datFileData.getCheckedSpecies());
					curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
							yAxisType, chartType));

					element_tv_cdat.setAllChecked(false);

					m_datFileData.setSelectedSpeciesName(null);
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
				//DATFileData data = (DATFileData) curFileNode.getFileData();
				// SpeciesNode
				TreeNode node = (TreeNode) event.getElement();
				
				if (event.getChecked() == true) 
				{
					m_datFileData.addCheckedSpecies((SpeciesNode) event.getElement());
					//setStatus("Checked:" + node.getName());
				}
				
				// not check
				else if (event.getChecked() == false) 
				{
					m_datFileData.removeCheckedSpecies((SpeciesNode) event.getElement());
					//setStatus("Unchecked:" + node.getName());
				}
				
				// update chart
				curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
						yAxisType, chartType));

				// mark line
				String labelName = m_datFileData.getSelectedSpeciesName();
				if (labelName != null) {
					String key = labelName.split(" ")[0]; // index
					DATChart.markSelectedLine(curChartPanel.getChart(), key);
					
					element_tv_cdat.setSelection(new StructuredSelection(findSpeciesNode(element_tv_cdat, labelName)),
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

						//DATFileData data = ((DATFileData) curFileNode.getFileData());

						// empty selection
						if (selection.isEmpty()) {
							return;
						}

						String labelName = selection.getFirstElement()
								.toString();

						boolean drawGraph = true;
						if (labelName.equals(m_datFileData.getSelectedSpeciesName())) {
							// selected species not changed
							drawGraph = false;
						}

						String key = labelName.split(" ")[0]; // index

						// store the name of the selected species
						m_datFileData.setSelectedSpeciesName(labelName);
						
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

	private void createGDATOutline() 
	{
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

				//DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				
				curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
						yAxisType, chartType));

				m_datFileData.setChartType(chartType);
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
					//DATFileData data = (DATFileData) curFileNode.getFileData();

					// check all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							m_datFileData.addCheckedObservable((ObservableNode) elements[i]);
						}
					}
					// set allChecked be true
					m_datFileData.setAllChecked(true);
					// set some nodes be checked
					element_tv_gdat.setCheckedElements(m_datFileData.getCheckedObservable());
					// plot the chart
					curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
							yAxisType, chartType));

				} else {
					// remove all

					Object[] elements = element_tv_gdat.getCheckedElements();

					// GDAT & SCAN
					//DATFileData data = (DATFileData) curFileNode.getFileData();

					// uncheck all the ObservableNode
					for (int i = 0; i < elements.length; i++) {
						TreeNode node = (TreeNode) elements[i];
						if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							m_datFileData.removeCheckedObservable((ObservableNode) elements[i]);
						}
					}
					// set allChecked be false
					m_datFileData.setAllChecked(false);
					// set some nodes be checked
					element_tv_gdat.setCheckedElements(m_datFileData.getCheckedObservable());
					curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
							yAxisType, chartType));

					element_tv_gdat.setAllChecked(false);

					m_datFileData.setSelectedObservableName(null);
					m_datFileData.setSelectedSpeciesName(null);
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
				//DATFileData data = (DATFileData) curFileNode.getFileData();
				
				if (event.getChecked() == true) 
				{
					if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) 
					{
						// SpeciesNode
						element_tv_gdat.setGrayChecked(node, true);
					} else if (node.getNodeType().equalsIgnoreCase(
							"ObservableNode")) {
						// ObservableNode
						m_datFileData.addCheckedObservable((ObservableNode) event
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
						m_datFileData.removeCheckedObservable((ObservableNode) event
								.getElement());
						//setStatus("Unchecked:" + node.getName());
					} else if (node.getNodeType().equalsIgnoreCase(
							"SpeciesFolderNode")) {
						// SpeciesFolderNode
						element_tv_gdat.setGrayChecked(node, true);
					}
				}
				
				//plot
				curChartPanel.setChart(DATChart.plotChart(m_datFileData,
						xAxisType, yAxisType, chartType));

				// mark line
				String labelName = m_datFileData.getSelectedObservableName();
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

						//DATFileData data = ((DATFileData) curFileNode.getFileData());

						TreeNode node = (TreeNode) selection.getFirstElement();

						// empty selection
						if (selection.isEmpty()) {
							return;
						}

						String labelName = node.getName();

						if (node.getNodeType().equalsIgnoreCase(
								"ObservableNode")) {
							// if selected observable changed
							if (!labelName.equals(m_datFileData
									.getSelectedObservableName())) {
								// store the name of the selected species
								m_datFileData.setSelectedObservableName(labelName);
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

							if (labelName.equals(m_datFileData.getSelectedSpeciesName())) {
								// selected species not change
								return;
							}

							// store the name of the selected species
							m_datFileData.setSelectedSpeciesName(labelName);

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

	/*
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
*/
	
	/*
	 * create a chartItem on textFolder when the corresponding file was double
	 * clicked
	 */
	private void createChartItem(IEditorInput editorInput ) 
	{
		//Get the  reference for this editor input.
		IFile iFile = ((FileEditorInput) ((IEditorInput) editorInput)).getFile();
		
		CTabItem chartItem = new CTabItem(textFolder, SWT.CLOSE);
		chartItem.setText(editorInput.getName());

		chartItem.setData("path", iFile.getLocation().toOSString());

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

				//DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
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

				//DATFileData data = (DATFileData) curFileNode.getFileData(); // plot
				curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType,
						yAxisType, chartType));
				//curChartPanel.redraw();
				updateOutline();
			}
		});

		//create the DatFileData
		//DATFileData datData = new DATFileData(new File(iFile.getLocation().toOSString()));
		
		// create the chart using JFreeChart
		JFreeChart chart = DATChart.plotChart(m_datFileData, xAxisType, yAxisType,chartType);
		
		CustomizedChartComposite chartPanel = new CustomizedChartComposite(chartCmp, SWT.NONE,
				chart, true);
		GridData griddata = new GridData(GridData.FILL_BOTH);
		griddata.horizontalSpan = 6;
		chartPanel.setLayoutData(griddata);
		curChartPanel = chartPanel;

		// set control
		chartItem.setControl(chartCmp);
		textFolder.setSelection(chartItem);

		m_datFileData.setSelectedObservableName(null);
		m_datFileData.setSelectedSpeciesName(null);
		m_datFileData.setExpandedElements(null);

		// reset chart type
		chartType = "line";
		m_datFileData.setChartType("line");

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
		curChartPanel.setChart(DATChart.plotChart(m_datFileData, xAxisType, yAxisType, chartType));
		
		// add close listener for tabItem
		chartItem.addDisposeListener(new DisposeListener() 
		{
			public void widgetDisposed(DisposeEvent event) 
			{
				CTabItem item = (CTabItem) event.getSource();

				String filePath = (String) item.getData("path");

				// remove the fileNode from curFileList
				/*removeFileNode(filePath);

				//if (curFileList.size() == 0) 
				//{
					curFileNode = null;
					if (elementOutline != null&& elementOutline.isDisposed() == false) 
					{
						elementOutline.setControl(null);
					}
				}
				*/
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
	private void updateOutline()
	{
		/*
		if (curFileNode == null) 
		{
			elementOutline.setControl(null);
			return;
		}
		// */
		
		/*
		if (curFileNode.getName().contains("Compare")) 
		{
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

		// */
		
		// create the tree in the outline
		//if (curFileNode.getName().endsWith(".cdat"))
		if (m_datFileData.getFileName().endsWith(".cdat"))
		{
			elementOutline.setControl(outlineCmp_cdat);
			// CDAT

			//DATFileData data = (DATFileData) curFileNode.getFileData();
			
			element_tv_cdat.setInput(m_datFileData.getSpeciesFolder());

			// set selection on element_tv
			String selectedSpeciesName = m_datFileData.getSelectedSpeciesName();
			if (selectedSpeciesName != null) {
				element_tv_cdat.setSelection(new StructuredSelection(
						findSpeciesNode(element_tv_cdat, selectedSpeciesName)), true);
			} else {
				element_tv_cdat.setSelection(null);
			}

			// set some nodes be checked
			element_tv_cdat.setCheckedElements(m_datFileData.getCheckedSpecies());

			// set chart type
			int index = outlineCmp_cdat.getChildren().length - 1;
			((Combo) outlineCmp_cdat.getChildren()[index]).setText(m_datFileData
					.getChartType());

		}
		//else if (curFileNode.getName().endsWith(".gdat")
		//		|| curFileNode.getName().endsWith(".scan")) 
		else if (m_datFileData.getFileName().endsWith(".gdat") || 
				 m_datFileData.getFileName().endsWith(".scan"))
			
		{
			// GDAT & SCAN
			elementOutline.setControl(outlineCmp_gdat);

			//DATFileData data = (DATFileData) curFileNode.getFileData();
			element_tv_gdat.setInput(m_datFileData.getObservableFolder());

			// set selection on element_tv
			String selectedObservableName = m_datFileData.getSelectedObservableName();
			if (selectedObservableName != null) {
				element_tv_gdat.setSelection(new StructuredSelection(
						findObservableNode(element_tv_gdat, selectedObservableName)), true);
			} else {
				element_tv_gdat.setSelection(null);
			}

			// set some nodes be checked
			element_tv_gdat.setCheckedElements(m_datFileData.getCheckedObservable());

			((Button) outlineCmp_gdat.getChildren()[1]).setSelection(m_datFileData
					.isAllChecked());

			// expand nodes
			if (m_datFileData.getExpandedElements() != null) {
				element_tv_gdat.setExpandedElements(m_datFileData.getExpandedElements());
			}

			// set grayed nodes
			Object[] toSetGray = m_datFileData.getGrayedElements();
			if (toSetGray != null) {
				for (int i = 0; i < toSetGray.length; i++) {
					element_tv_gdat.setGrayChecked(toSetGray[i], true);
				}
			}

			// set chart type
			int index = outlineCmp_gdat.getChildren().length - 1;
			((Combo) outlineCmp_gdat.getChildren()[index]).setText(m_datFileData.getChartType());
		} 

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
	
	/**
	 * Create the IEditorInput object from a File.
	 * 
	 * @param file The file for which we need an IEditorInput.
	 * @return The IEditorInput object.
	 */
	private IEditorInput createEditorInput(File file) 
	{
		// Get the IPath for the file.
		IPath location= new Path(file.getAbsolutePath());
		
		// Get an instance of PathEditorInput based on the IPath 
		PathEditorInput input= new PathEditorInput(location);
		
		// Return it.  (PathEditorInput implements IEditorInput)
		return input;
	}

	public boolean isComparable() 
	{
		/*
		if(curFileNode == null)
		{
			return false;
		}
		// */
		
		String curFileName = m_datFileData.getFileName();
		
		if(curFileName.endsWith(".cdat") || 
		   curFileName.endsWith(".gdat") || 
		   curFileName.endsWith(".scan")) 
		{
			return true;
		}
			
		return false;
	}
	/* TODO  Figure out how to do comparisons when the charts are in multipage editors.
	public void compareWith(FileNode node)
	{
		
		String curFileName = m_datFileData.getFileName(); //curFileNode.getName();

		// get file type
		String type1 = curFileName.substring(
				curFileName.indexOf(".") + 1);
		String type2 = node.getName().substring(
				node.getName().indexOf(".") + 1);

		// get DATFileData
		DATFileData data1 = (DATFileData) curFileNode.getFileData();
		DATFileData data2 = (DATFileData) ((FileNode) node)
				.getFileData();

		// create a compare data object
		DATComparisonData compData = new DATComparisonData(type1,
				data1, type2, data2);

		if (compData.isComparable() == false) {
			MessageBox msgBox = new MessageBox(this.getShell(),
					SWT.ICON_ERROR | SWT.OK);
			msgBox.setText("Error");
			msgBox.setMessage("Different X axis");
			msgBox.open();
			return;
		}

		// create a new compare tab item
		createComparisonItem(curFileNode, (FileNode) node, compData);
	}
	*/
	
	/**
	 * Creates a comparison chart.
	 * 
	 * @auther Wen
	 * 
	 * @param fNode1
	 * @param fNode2
	 * @param data
	 */
	/*
	private void createComparisonItem(FileNode fNode1, FileNode fNode2,
			DATComparisonData data) {
		curFileNode = new FileNode(null, null);
		curFileNode.setfData(data);

		// tab item
		CTabItem compItem = new CTabItem(textFolder, SWT.CLOSE);
		String fileName1 = fNode1.getName().substring(
				fNode1.getName().indexOf(":") + 2);
		String fileName2 = fNode2.getName().substring(
				fNode2.getName().indexOf(":") + 2);
		String fileName = "Compare " + fileName1 + " with " + fileName2;
		compItem.setText(fileName);
		curFileNode.setName(fileName);
		data.setFileName(fileName);

		// set tool tip
		String filePath1 = fNode1.getPath();
		String filePath2 = fNode2.getPath();
		String filePath = "Compare " + filePath1 + " with " + filePath2;
		compItem.setToolTipText(filePath);
		curFileNode.setPath(filePath);

		compItem.setData("path", filePath);

		Composite chartCmp = new Composite(textFolder, SWT.NONE);
		chartCmp.setLayout(new GridLayout(6, false));

		// control options
		xAxisType = "linear";
		yAxisType = "linear";

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
				xAxisType = xAxisBnt.getText();

				DATComparisonData data = (DATComparisonData) curFileNode
						.getFileData(); // plot
				curChartPanel.setChart(DATComparisonChart.plotChart(data,
						chartTypeList));
				curChartPanel.setChart(DATComparisonChart.plotChart(
						curChartPanel.getChart(), xAxisType, yAxisType));
				updateOutline();
			}
		});
		yAxisBnt.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {

				yAxisType = yAxisBnt.getText();

				DATComparisonData data = (DATComparisonData) curFileNode
						.getFileData(); // plot
				curChartPanel.setChart(DATComparisonChart.plotChart(data,
						chartTypeList));
				curChartPanel.setChart(DATComparisonChart.plotChart(
						curChartPanel.getChart(), xAxisType, yAxisType));
				updateOutline();
			}
		});

		// create the chart using JFreeChart
		JFreeChart chart = DATComparisonChart.plotChart(data, chartTypeList);
		CustomizedChartComposite chartPanel = new CustomizedChartComposite(chartCmp, SWT.NONE,
				chart, true);
		GridData griddata = new GridData(GridData.FILL_BOTH);
		griddata.horizontalSpan = 6;
		chartPanel.setLayoutData(griddata);
		curChartPanel = chartPanel;

		// set control
		compItem.setControl(chartCmp);
		textFolder.setSelection(compItem);

		updateOutline();

		for (int i = 0; i < data.getDATDataCount(); i++) {
			// update the "check/uncheck all" button
			data.setAllChecked(i, true);
			((Button) outlineCmp_compare.getChildren()[2 + 5 * i])
					.setSelection(true);

			// update checkbox in the tree
			element_tv_compare.get(i).setAllChecked(true);

			Object[] elements = element_tv_compare.get(i).getCheckedElements();

			for (int j = 0; j < elements.length; j++) {
				TreeNode node = (TreeNode) elements[j];
				if (node.getNodeType().equalsIgnoreCase("SpeciesNode")) {
					// SpeciesNode
					data.addCheckedSpecies(i, (SpeciesNode) elements[j]);
				} else if (node.getNodeType()
						.equalsIgnoreCase("ObservableNode")) {
					// ObservableNode
					data.addCheckedObservable(i, (ObservableNode) elements[j]);
				} else {
				}
			}
		}
		
		// set file path for each tree
		((Text) outlineCmp_compare.getChildren()[0]).setText(fileName1);
		((Text) outlineCmp_compare.getChildren()[0]).setToolTipText(filePath1);
		((Text) outlineCmp_compare.getChildren()[5]).setText(fileName2);
		((Text) outlineCmp_compare.getChildren()[5]).setToolTipText(filePath2);

		// plot chart
		curChartPanel.setChart(DATComparisonChart
				.plotChart(data, chartTypeList));
		curChartPanel.setChart(DATComparisonChart.plotChart(curChartPanel.getChart(), xAxisType, yAxisType));

		// add close listener for tabItem
		compItem.addDisposeListener(new DisposeListener() {

			public void widgetDisposed(DisposeEvent event) {
				CTabItem item = (CTabItem) event.getSource();

				String filePath = (String) item.getData("path");

				// remove the fileNode from curFileList
			//	removeFileNode(filePath);

				//if (curFileList.size() == 0) 
				//{
					//curFileNode = null;
					
				//}
			}
		});
	}
*/
	
}
