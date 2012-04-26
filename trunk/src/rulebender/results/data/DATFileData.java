package rulebender.results.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import rulebender.navigator.model.FileData;
import rulebender.navigator.model.ObservableFolderNode;
import rulebender.navigator.model.ObservableNode;
import rulebender.navigator.model.SpeciesFolderNode;
import rulebender.navigator.model.SpeciesNode;

/**
 * 
 * Store all the information for CDAT, GDAT and SCAN files
 * 
 */
public class DATFileData extends FileData {
	// CDAT & GDAT & SCAN

	private String xAxisName; // name of x axis
	private ArrayList<String> varName; // list of name of variables

	// data for plotting
	private XYSeriesCollection seriesCollection;
	private ArrayList<XYSeries> seriesList;
	
	private boolean allValueLargerThanZero_X = true;
	private boolean allValueLargerThanZero_Y = true;
	private double minX = Double.MAX_VALUE;
	private double minY = Double.MAX_VALUE;

	private SpeciesFolderNode speciesFolder;
	private String selectedSpeciesName;
	private Hashtable<Integer, SpeciesNode> checkedSpecies;

	private ObservableFolderNode observableFolder;
	private String selectedObservableName;
	private Hashtable<String, ObservableNode> checkedObservable;

	// whether the "check/uncheck all" button is checked
	private boolean isAllChecked;

	private Object[] expandedElements;
	private Object[] grayedElements;

	private String chartType; // type of chart, for plotting

	public DATFileData(File file) {
		varName = new ArrayList<String>(); // species or observable
		seriesList = new ArrayList<XYSeries>();
		seriesCollection = new XYSeriesCollection();
		checkedSpecies = new Hashtable<Integer, SpeciesNode>();
		checkedObservable = new Hashtable<String, ObservableNode>();

		this.file = file;
		this.fileName = file.getName();
		this.readData();
	}

	/**
	 * 
	 * @return name of x axis
	 */
	public String getXAxisName() {
		return this.xAxisName;
	}

	/**
	 * 
	 * @return the data set use to plot a chart.
	 */
	public XYSeriesCollection getSeriesCollection() {

		// clear data
		seriesCollection.removeAllSeries();

		// create a SeriesCollection object from the seriesList
		if (this.fileName.endsWith(".cdat")) {
			// CDAT
			for (int i = 0; i < seriesList.size(); i++) {
				String id = (String) seriesList.get(i).getKey();
				// add checked species series only
				
				// The S prefix to the column number was introduced with BNG 2.2.0
				if(id.substring(0,1).equals("S"))
				{
					id = id.substring(1);
				}
				
				if (checkedSpecies.containsKey(Integer.parseInt(id)) == true) 
				{
					seriesCollection.addSeries(seriesList.get(i));
				}
			}

		} else if (this.fileName.endsWith(".gdat")
				|| this.fileName.endsWith(".scan")) {
			// GDAT & SCAN
			for (int i = 0; i < seriesList.size(); i++) {
				String name = (String) seriesList.get(i).getKey();
				// add checked observable series only
				if (checkedObservable.containsKey(name) == true) {
					seriesCollection.addSeries(seriesList.get(i));
				}
			}
		}
		return this.seriesCollection;

	}

	/**
	 * Read data from file.
	 */
	protected void readData() {

		// Read numbers from DAT file
		try {
			Scanner in = new Scanner(file);

			String firstLine = "";

			// ignore empty lines
			while (firstLine.equals("") && in.hasNextLine()) {
				firstLine = in.nextLine();
			}

			// empty file
			if (firstLine.equals("")) {
				return;
			}

			/*
			 * format: variables # 1 2 3 ...
			 */
			firstLine = firstLine.substring(firstLine.indexOf('#') + 1,
					firstLine.length()).trim();
			// name for x axis
			xAxisName = firstLine.substring(0, firstLine.indexOf(' '));

			// delete #
			firstLine = firstLine.substring(firstLine.indexOf(' '),
					firstLine.length()).trim();

			// get variable name
			while (!firstLine.equals("")) {
				String name = null;
				if (firstLine.indexOf(' ') != -1)
					name = firstLine.substring(0, firstLine.indexOf(' '));
				else
					name = firstLine; // last variable, no space

				varName.add(name);
				XYSeries series = new XYSeries(name); // value of legend
				seriesList.add(series);

				// delete the first name
				if (firstLine.indexOf(' ') != -1)
					firstLine = firstLine.substring(firstLine.indexOf(' '),
							firstLine.length()).trim();
				else
					firstLine = ""; // finished
			}

			/*
			 * values space number number ...
			 */

			while (in.hasNext()) {
				String line = in.nextLine();
				line = line.substring(line.indexOf(' '), line.length()).trim();
				double time = processNum(line.substring(0, line.indexOf(' '))); // time
				
				if (time <= 0) {
					allValueLargerThanZero_X = false;
				}
				else if (time < minX) {
					minX = time;
				}
				
				// delete time
				line = line.substring(line.indexOf(' '), line.length()).trim();

				// concentrations
				int count = 0;
				while (!line.equals("")) {
					String number = null;
					if (line.indexOf(' ') != -1)
						number = line.substring(0, line.indexOf(' '));
					else
						number = line; // last number, no space

					double concentration = processNum(number); // concentration
					
					if (concentration <= 0) {
						allValueLargerThanZero_Y = false;
					}
					else if (concentration < minY) {
						minY = concentration;
					}

					// linear scale
					XYDataItem item = new XYDataItem(time, concentration);
					seriesList.get(count).add(item);
					count++;

					// delete the first number
					if (line.indexOf(' ') != -1)
						line = line.substring(line.indexOf(' '), line.length())
								.trim();
					else
						line = ""; // finished
				}
			}
			in.close();

		} catch (FileNotFoundException e) {
			System.out.println("File: " + file.getName() + " not found!");
			e.printStackTrace();
		}

		// read the species from NET file
		if (readSpeciesFromNETFile(file) == false) {
			speciesFolder = new SpeciesFolderNode("Species", varName);
		}

		if (file.getName().endsWith(".gdat")
				|| file.getName().endsWith(".scan")) {
			// read the observables from NET file
			if (readObservableFromNETFile(file) == false) {
				ArrayList<String>[] componentsList = new ArrayList[varName.size()];
				observableFolder = new ObservableFolderNode("Observables",
						varName, componentsList);
			}
		}

	}

	/**
	 * Process exponential float number.
	 * 
	 * @param in
	 *            string of float number, with E
	 * @return real double number
	 */
	private double processNum(String in) {
		Double base = Double.parseDouble(in.substring(0, in.indexOf('e'))
				.trim());
		int pow = Integer.parseInt(in.substring(in.indexOf('e') + 2,
				in.length()).trim());
		char chr = in.charAt(in.indexOf('e') + 1);
		Double temp;
		if (chr == '+')
			temp = base * Math.pow(10, pow);
		else
			temp = base * Math.pow(10, 0 - pow);
		return Double.valueOf(temp.toString());
	}

	/**
	 * Get the expression of species from netFile.
	 * 
	 * @param file
	 *            NET file
	 * @return whether successful
	 */
	private boolean readSpeciesFromNETFile(File file) {
		String filePath = file.getAbsolutePath();
		String fileName = file.getName();
		String fileType = fileName.substring(fileName.lastIndexOf("."),
				fileName.length());
		String netFilePath = filePath.substring(0, filePath.indexOf(fileType))
				+ ".net";
		;

		// the NET file path for SCAN file is different from CDAT & GDAT files
		String slash = "";
		String stemp = System.getProperty("os.name");
		if (stemp.contains("Windows") || stemp.contains("WINDOWS")
				|| stemp.contains("windows"))
			slash = "\\";
		else
			slash = "/";

		if (fileType.equals(".scan")) {
			String prefixName = fileName
					.substring(0, fileName.lastIndexOf('.'));
			netFilePath = file.getParent() + slash + prefixName + slash
					+ prefixName + ".net";
		}

		File netFile = new File(netFilePath);

		try {
			Scanner in = new Scanner(netFile);
			String line;
			// list of species' name
			ArrayList<String> speciesList = new ArrayList<String>();

			while (in.hasNext()) {
				line = in.nextLine().trim();
				if (line.equalsIgnoreCase("begin species")) {
					// build array of species
					do {
						line = in.nextLine().trim();
						if (line.lastIndexOf(")") != -1) {
							// name of species
							String s = line.substring(0,
									line.lastIndexOf(")") + 1);
							speciesList.add(s);
						}
					} while (!line.equalsIgnoreCase("end species"));
					break;
				}
			}
			speciesFolder = new SpeciesFolderNode("Species", speciesList);

			in.close();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("NET File not found!");
			return false;
		}
	}

	/**
	 * Get the expression and composition of observable from netFile.
	 * 
	 * @param file
	 *            NET file
	 * @return whether successful
	 */
	private boolean readObservableFromNETFile(File file) {
		String filePath = file.getAbsolutePath();
		String fileName = file.getName();
		String fileType = fileName.substring(fileName.lastIndexOf("."),
				fileName.length());
		String netFilePath = filePath.substring(0, filePath.indexOf(fileType))
				+ ".net";

		// the NET file path for SCAN file is different from CDAT & GDAT files
		String slash = "";
		String stemp = System.getProperty("os.name");
		if (stemp.contains("Windows") || stemp.contains("WINDOWS")
				|| stemp.contains("windows"))
			slash = "\\";
		else
			slash = "/";

		if (fileType.equals(".scan")) {
			String prefixName = fileName
					.substring(0, fileName.lastIndexOf('.'));
			netFilePath = file.getParent() + slash + prefixName + slash
					+ prefixName + ".net";
		}

		File netFile = new File(netFilePath);

		try {
			Scanner in = new Scanner(netFile);
			String line;

			// list of observale nodes
			ArrayList<String> info = new ArrayList<String>();
			ArrayList<String>[] componentsList = null;
			ArrayList<String> speciesList = speciesFolder.getComponents();

			while (in.hasNext()) {
				line = in.nextLine().trim();
				if (line.equalsIgnoreCase("begin observables")) {
					// build array of observable info
					line = in.nextLine().trim();
					while (!line.equalsIgnoreCase("end observables")) {
						info.add(line);
						line = in.nextLine().trim();
					}
				}

				else if (line.equalsIgnoreCase("begin groups")) {
					// allocate space for observable components
					componentsList = new ArrayList[info.size()];
					int count = 0;

					line = in.nextLine().trim();

					while (!line.equalsIgnoreCase("end groups")) {

						// format: id name listOfComponent
						String[] tmp = line.split(" ");

						// the observable has no species list
						if (tmp.length == 2) {
							ArrayList<String> components = new ArrayList<String>();
							componentsList[count++] = components;
							line = in.nextLine().trim();
							continue;
						}

						String listOfComponent = tmp[tmp.length - 1];

						// format: number*speciesID,number*speciesID,...
						String[] tmp2 = null;
						if (listOfComponent.contains(",")) {
							tmp2 = listOfComponent.split(",");
						} else {
							// only one species
							tmp2 = new String[1];
							tmp2[0] = listOfComponent;
						}

						ArrayList<String> components = new ArrayList<String>();
						// add speciesID to componentsID
						for (int i = 0; i < tmp2.length; i++) {
							String speciesID = tmp2[i];

							// number*speciesID, remove number, only leave
							// speciesID
							if (speciesID.contains("*")) {
								speciesID = speciesID.substring(
										speciesID.indexOf("*") + 1,
										speciesID.length());
							}

							// add speciesID
							components.add(speciesList.get(Integer
									.parseInt(speciesID) - 1));
						}
						componentsList[count++] = components;
						line = in.nextLine().trim();
					}
					break;
				}

			}

			observableFolder = new ObservableFolderNode("Observables", info,
					componentsList);
			in.close();
			return true;
		} catch (FileNotFoundException e) {
			System.out.println("NET File not found!");
			return false;
		}
	}

	/**
	 * 
	 * @return SpeciesFolderNode object
	 */
	public SpeciesFolderNode getSpeciesFolder() {
		return this.speciesFolder;
	}

	/**
	 * 
	 * @return name of selected species
	 */
	public String getSelectedSpeciesName() {
		return selectedSpeciesName;
	}

	/**
	 * 
	 * @param selectedSpeciesName
	 */
	public void setSelectedSpeciesName(String selectedSpeciesName) {
		this.selectedSpeciesName = selectedSpeciesName;
	}

	/**
	 * Add a species node if not exists.
	 * 
	 * @param node
	 *            SpeciesNode object
	 */
	public void addCheckedSpecies(SpeciesNode node) {
		if (!checkedSpecies.containsKey(node.getId())) {
			checkedSpecies.put(node.getId(), node);
		}
	}

	/**
	 * Remove a species node if exists.
	 * 
	 * @param node
	 *            SpeciesNode object
	 */
	public void removeCheckedSpecies(SpeciesNode node) {
		checkedSpecies.remove(node.getId());
	}

	/**
	 * 
	 * @return the checked species nodes in the element tree
	 */
	public SpeciesNode[] getCheckedSpecies() {
		int n = checkedSpecies.size();
		SpeciesNode[] res = new SpeciesNode[n];
		Iterator<SpeciesNode> it = checkedSpecies.values().iterator();
		int i = 0;
		while (it.hasNext()) {
			res[i++] = it.next();
		}
		return res;
	}

	/**
	 * 
	 * @return ObservableFolderNode object
	 */
	public ObservableFolderNode getObservableFolder() {
		return this.observableFolder;
	}

	/**
	 * 
	 * @return name of selected observable
	 */
	public String getSelectedObservableName() {
		return selectedObservableName;
	}

	/**
	 * 
	 * @param selectedObservableName
	 */
	public void setSelectedObservableName(String selectedObservableName) {
		this.selectedObservableName = selectedObservableName;
	}

	/**
	 * Add an observable node if not exists.
	 * 
	 * @param node
	 *            ObservableFolderNode object
	 */
	public void addCheckedObservable(ObservableNode node) {
		if (!checkedObservable.containsKey(node.getName())) {
			checkedObservable.put(node.getName(), node);
		}
	}

	/**
	 * Remove an observable node if exists.
	 * 
	 * @param node
	 *            ObservableFolderNode object
	 */
	public void removeCheckedObservable(ObservableNode node) {
		checkedObservable.remove(node.getName());
	}

	/**
	 * 
	 * @return the checked observable nodes in the element tree
	 */
	public ObservableNode[] getCheckedObservable() {
		int n = checkedObservable.size();
		ObservableNode[] res = new ObservableNode[n];
		Iterator<ObservableNode> it = checkedObservable.values().iterator();
		int i = 0;
		while (it.hasNext()) {
			res[i++] = it.next();
		}

		return res;
	}

	/**
	 * 
	 * @return whether the "check/uncheck all" is checked
	 */
	public boolean isAllChecked() {
		return isAllChecked;
	}

	/**
	 * 
	 * @param isAllChecked
	 */
	public void setAllChecked(boolean isAllChecked) {
		this.isAllChecked = isAllChecked;
	}

	/**
	 * 
	 * @return array of expanded objects
	 */
	public Object[] getExpandedElements() {
		return expandedElements;
	}

	/**
	 * 
	 * @param expandedElements
	 *            array of expanded tree elements
	 */
	public void setExpandedElements(Object[] expandedElements) {
		this.expandedElements = expandedElements;
	}

	/**
	 * 
	 * @return array of grayed objects
	 */
	public Object[] getGrayedElements() {
		return grayedElements;
	}

	/**
	 * 
	 * @param grayedElements
	 *            array of grayed tree elements
	 */
	public void setGrayedElements(Object[] grayedElements) {
		this.grayedElements = grayedElements;
	}

	/**
	 * 
	 * @return type of chart
	 */
	public String getChartType() {
		return chartType;
	}

	/**
	 * 
	 * @param chartType
	 *            type of chart
	 */
	public void setChartType(String chartType) {
		this.chartType = chartType;
	}	
	
	public boolean isAllValueLargerThanZero_X() {
		return allValueLargerThanZero_X;
	}

	public boolean isAllValueLargerThanZero_Y() {
		return allValueLargerThanZero_Y;
	}

	public double getMinX() {
		return minX;
	}


	public double getMinY() {
		return minY;
	}

	/**
	 * Clone DATFileData object.
	 */
	public Object clone() {
		DATFileData object = null;
		try {
			object = (DATFileData) super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("CloneNotSupportedException");
			e.printStackTrace();
		}
		return object;
	}

}
