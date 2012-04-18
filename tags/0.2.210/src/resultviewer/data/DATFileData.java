package resultviewer.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Scanner;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import resultviewer.tree.ObservableFolderNode;
import resultviewer.tree.ObservableNode;
import resultviewer.tree.SpeciesFolderNode;
import resultviewer.tree.SpeciesNode;

/**
 * 
 * Store all the information for CDAT, GDAT and SCAN files
 *
 */
public class DATFileData extends FileData {
	// CDAT & GDAT & SCAN

	private String xAxisName;
	private ArrayList<String> varName;
	private XYSeriesCollection seriesCollection;
	private ArrayList<XYSeries> seriesList;

	private SpeciesFolderNode speciesFolder;
	private String selectedSpeciesName;
	private Hashtable<Integer, SpeciesNode> checkedSpecies;

	private ObservableFolderNode observableFolder;
	private String selectedObservableName;
	private Hashtable<String, ObservableNode> checkedObservable;
	private boolean isAllChecked; // whether the "check/uncheck all" button is checked
	private Object[] expandedElements;
	private Object[] grayedElements;
	
	private String chartType;

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

	public String getXAxisName() {
		return this.xAxisName;
	}

	/*
	 * Return the data set use to draw a chart.
	 */
	public XYSeriesCollection getSeriesCollection() {

		seriesCollection.removeAllSeries();
		 // create a SeriesCollection object from the seriesList

		if (this.fileName.endsWith(".cdat")) {
			// CDAT
			for (int i = 0; i < seriesList.size(); i++) {
				String id = (String) seriesList.get(i).getKey();
				if (checkedSpecies.containsKey(Integer.parseInt(id)) == true) {
					seriesCollection.addSeries(seriesList.get(i));
				}
			}

		} else if (this.fileName.endsWith(".gdat")
				|| this.fileName.endsWith(".scan")) {
			// GDAT & SCAN
			for (int i = 0; i < seriesList.size(); i++) {
				String name = (String) seriesList.get(i).getKey();
				if (checkedObservable.containsKey(name) == true) {
					seriesCollection.addSeries(seriesList.get(i));
				}
			}
		}
		return this.seriesCollection;

	}

	@Override
	protected void readData() {

		/* Read numbers from DAT file */
		try {
			Scanner in = new Scanner(file);

			/*
			 * variables # 1 2 3 ...
			 */
			String firstLine = "";
			while (firstLine.equals("") && in.hasNextLine()) {
				firstLine = in.nextLine();
			}
			
			if (firstLine.equals("")) {
				return;
			}
			
			firstLine = firstLine.substring(firstLine.indexOf('#') + 1,
					firstLine.length()).trim();
			xAxisName = firstLine.substring(0, firstLine.indexOf(' '));// name
																		// for x
																		// axis
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
			// TODO
			System.out.println("File: " + file.getName() + " not found!");
			e.printStackTrace();
		}

		/* read the species from NET file */
		if (readSpeciesFromNETFile(file) == false) {
			speciesFolder = new SpeciesFolderNode("Species", varName);
		}

		if (file.getName().endsWith(".gdat")
				|| file.getName().endsWith(".scan")) {
			/* read the observables from NET file */
			if (readObservableFromNETFile(file) == false) {
				ArrayList<String>[] componentsList = new ArrayList[varName
						.size()];
				observableFolder = new ObservableFolderNode("Observables",
						varName, componentsList);
			}
		}

	}

	/*
	 * Process exponential float number.
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

	/*
	 * Get the expression of species from netFile.
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
					/* build array of species */
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
			System.out.println("File not found!");
			e.printStackTrace();
			return false;
		}
	}

	/*
	 * Get the expression and composition of observable from netFile.
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
					/* build array of observable info */
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
			System.out.println("File not found!");
			e.printStackTrace();
			return false;
		}
	}

	/* speciesFolder */
	public SpeciesFolderNode getSpeciesFolder() {
		return this.speciesFolder;
	}

	public String getSelectedSpeciesName() {
		return selectedSpeciesName;
	}

	public void setSelectedSpeciesName(String selectedSpeciesName) {
		this.selectedSpeciesName = selectedSpeciesName;
	}

	/*
	 * Add a species node if not exists.
	 */
	public void addCheckedSpecies(SpeciesNode node) {
		if (!checkedSpecies.containsKey(node.getId())) {
			checkedSpecies.put(node.getId(), node);
		}
	}

	/*
	 * Remove a species node if exists.
	 */
	public void removeCheckedSpecies(SpeciesNode node) {
		checkedSpecies.remove(node.getId());
	}

	/*
	 * return the checked species nodes in the element tree
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

	/* observableFolder */
	public ObservableFolderNode getObservableFolder() {
		return this.observableFolder;
	}

	public String getSelectedObservableName() {
		return selectedObservableName;
	}

	public void setSelectedObservableName(String selectedObservableName) {
		this.selectedObservableName = selectedObservableName;
	}

	/*
	 * Add a species node if not exists.
	 */
	public void addCheckedObservable(ObservableNode node) {
		if (!checkedObservable.containsKey(node.getName())) {
			checkedObservable.put(node.getName(), node);
		}
	}

	/*
	 * Remove a species node if exists.
	 */
	public void removeCheckedObservable(ObservableNode node) {
		checkedObservable.remove(node.getName());
	}

	/*
	 * return the checked species nodes in the element tree
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

	public boolean isAllChecked() {
		return isAllChecked;
	}

	public void setAllChecked(boolean isAllChecked) {
		this.isAllChecked = isAllChecked;
	}

	public Object[] getExpandedElements() {
		return expandedElements;
	}

	public void setExpandedElements(Object[] expandedElements) {
		this.expandedElements = expandedElements;
	}

	public Object[] getGrayedElements() {
		return grayedElements;
	}

	public void setGrayedElements(Object[] grayedElements) {
		this.grayedElements = grayedElements;
	}

	public String getChartType() {
		return chartType;
	}

	public void setChartType(String chartType) {
		this.chartType = chartType;
	}
	
	public Object clone() {
		DATFileData object = null;
		try {
			object = (DATFileData)super.clone();
		} catch (CloneNotSupportedException e) {
			System.out.println("CloneNotSupportedException");
			e.printStackTrace();
		}
		return object;
	}
	
}
