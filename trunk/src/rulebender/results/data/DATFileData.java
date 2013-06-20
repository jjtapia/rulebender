package rulebender.results.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.jfree.data.xy.XYDataItem;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import rulebender.editors.dat.model.FileData;
import rulebender.editors.dat.model.ObservableFolderNode;
import rulebender.editors.dat.model.ObservableNode;
import rulebender.editors.dat.model.SpeciesFolderNode;
import rulebender.editors.dat.model.SpeciesNode;

/**
 * 
 * Store all the information for CDAT, GDAT and SCAN files
 * 
 */
public class DATFileData extends FileData {
	// CDAT & GDAT & SCAN

	private String xAxisName; // name of x axis
	private final ArrayList<String> varName; // list of name of variables

	// data for plotting
	private final XYSeriesCollection seriesCollection;
	private final ArrayList<XYSeries> seriesList;

	private boolean allValueLargerThanZero_X = true;
	private boolean allValueLargerThanZero_Y = true;
	private double minX = Double.MAX_VALUE;
	private double minY = Double.MAX_VALUE;

	private SpeciesFolderNode speciesFolder;
	private String selectedSpeciesName;
	private final Map<String, String> speciesNameTrans;
	private final Hashtable<String, SpeciesNode> checkedSpecies;

	private ObservableFolderNode observableFolder;
	private String selectedObservableName;
	private final Hashtable<String, ObservableNode> checkedObservable;

	// whether the "check/uncheck all" button is checked
	private boolean isAllChecked;

	private Object[] expandedElements;
	private Object[] grayedElements;

	private String chartType; // type of chart, for plotting

	public DATFileData(File file) {
		varName = new ArrayList<String>(); // species or observable
		seriesList = new ArrayList<XYSeries>();
		seriesCollection = new XYSeriesCollection();
		speciesNameTrans = new HashMap<>();
		checkedSpecies = new Hashtable<String, SpeciesNode>();
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
				// if (id.substring(0, 1).equals("S")) {
				// id = id.substring(1);
				// }

				if (checkedSpecies.containsKey(id)) {
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
	@Override
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
				in.close();
				return;
			}

			/*
			 * format: variables # 1 2 3 ...
			 */
			firstLine = firstLine.substring(firstLine.indexOf('#') + 1,
			    firstLine.length()).trim();

			// name for x axis - Have to have this (hack) check because NFSim
			// does not produce data for observables.
			int indexForSpace = firstLine.indexOf(' ');

			if (indexForSpace < 0) {
				// FIXME Need to rework results in general, but for now should at least
				// log this.
				System.out.println("LOG: No observable data found. Cannot display "
				    + "results");
				in.close();
				return;
			}

			xAxisName = firstLine.substring(0, indexForSpace);

			// delete #
			firstLine = firstLine.substring(firstLine.indexOf(' '),
			    firstLine.length()).trim();

			processVariableNames(firstLine);

			/*
			 * values space number number ...
			 */

			while (in.hasNext()) {
				String line = in.nextLine();
				line = line.substring(line.indexOf(' '), line.length()).trim();
				double time = Double.valueOf(line.substring(0, line.indexOf(' '))); // time

				if (time <= 0) {
					allValueLargerThanZero_X = false;
				} else if (time < minX) {
					minX = time;
				}

				// delete time
				line = line.substring(line.indexOf(' '), line.length()).trim();

				// concentrations
				processConcentrations(line, time);
			}
			in.close();

		} catch (FileNotFoundException e) {
			System.out.println("File: " + file.getName() + " not found!");
			e.printStackTrace();
		}

		// read the species from NET file
		if (file.getName().endsWith(".cdat")) {
			List<String> speciesNames = readSpeciesFromNETFile(file);
			if (speciesNames != varName) {
				speciesFolder = new SpeciesFolderNode("Species", speciesNames);
				speciesFolder.setComponentsID(varName);
				for (int i = 0; i < speciesNames.size(); i++) {
					speciesNameTrans.put(speciesNames.get(i), varName.get(i));
				}
			} else {
				speciesFolder = new SpeciesFolderNode("Species", varName);
			}
		} else {
			speciesFolder = new SpeciesFolderNode("Species",
			    readSpeciesFromNETFile(file));
		}

		List<String> observableNames = varName;
		if (!file.getName().endsWith(".gdat") && !file.getName().endsWith(".scan")) {
			File existingFile = new File(file.getParentFile(), file.getName()
			    .substring(0, file.getName().lastIndexOf(".")) + ".gdat");
			if (existingFile.exists()) {
				observableNames = readObservableNamesFromFile(existingFile);
			} else {
				existingFile = new File(file.getParentFile(), file.getName().substring(
				    0, file.getName().lastIndexOf("."))
				    + ".scan");
				observableNames = readObservableNamesFromFile(existingFile);
			}
		}

		File bnglFile = new File(file.getParent(), file.getName().substring(0,
		    file.getName().lastIndexOf("."))
		    + ".bngl");
		List<List<String>> patterns = readPatternsFromBnglFile(observableNames,
		    bnglFile);
		List<List<String>> components = readComponentsFromNetFile(observableNames);
		observableFolder = new ObservableFolderNode("Observables", observableNames,
		    patterns, components);
		// readObservableFromNETFile(file);
	}

	/**
	 * Process variable names.
	 * 
	 * @param line
	 *          the line
	 */
	private void processVariableNames(String line) {
		// get variable name
		while (!line.equals("")) {
			String name = null;
			if (line.indexOf(' ') != -1) {
				name = line.substring(0, line.indexOf(' '));
			} else {
				name = line; // last variable, no space
			}

			varName.add(name);
			XYSeries series = new XYSeries(name); // value of legend
			seriesList.add(series);

			// delete the first name
			if (line.indexOf(' ') != -1) {
				line = line.substring(line.indexOf(' '), line.length()).trim();
			} else {
				line = ""; // finished
			}
		}

	}

	/**
	 * Process concentrations.
	 * 
	 * @param line
	 *          the line
	 * @param time
	 *          the time
	 */
	private void processConcentrations(String line, double time) {
		int count = 0;
		while (!line.equals("")) {
			String number = null;
			if (line.indexOf(' ') != -1) {
				number = line.substring(0, line.indexOf(' '));
			} else {
				number = line; // last number, no space
			}

			double concentration = Double.valueOf(number); // concentration

			if (concentration <= 0) {
				allValueLargerThanZero_Y = false;
			} else if (concentration < minY) {
				minY = concentration;
			}

			// linear scale
			XYDataItem item = new XYDataItem(time, concentration);
			seriesList.get(count).add(item);
			count++;

			// delete the first number
			if (line.indexOf(' ') != -1) {
				line = line.substring(line.indexOf(' '), line.length()).trim();
			} else {
				line = ""; // finished
			}
		}
	}

	/**
	 * Read observable names from the file. The file has to be a ".gdat" or
	 * ".scan" file.
	 * 
	 * @return the list
	 */
	private List<String> readObservableNamesFromFile(File file) {
		List<String> observables = new ArrayList<>();
		if (!file.getName().endsWith(".gdat") && !file.getName().endsWith(".scan")) {
			System.err.println("Couldn't read file names from file.");
			return varName;
		}
		Scanner in = null;
		try {
			in = new Scanner(file);
		} catch (FileNotFoundException e) {
			System.err.println("The specified file does not exist. File: "
			    + file.toString());
			return varName;
		}
		// read first real line
		String line;
		do {
			line = in.nextLine();
		} while (line != null && line.trim().isEmpty());
		in.close();

		if (line != null && !line.trim().isEmpty()) {
			// remove whitespaces
			String[] split = line.split("\\s+");
			// add all observables
			for (String s : split) {
				if (s.isEmpty() || s.equals("#") || s.equals("time")) {
					continue;
				}
				observables.add(s);
			}
		}
		return observables;
	}

	private List<List<String>> readPatternsFromBnglFile(List<String> observables,
	    File bnglFile) {
		ArrayList<List<String>> patterns = new ArrayList<List<String>>();
		if (bnglFile == null || !bnglFile.exists()) {
			// bnglFile doesnt exists
			return generateEmptyPatterns();
		}
		Scanner in = null;
		try {
			in = new Scanner(bnglFile);
		} catch (FileNotFoundException e) {
			return generateEmptyPatterns();
		}

		String line;
		boolean observe = false;
		Iterator<String> it = varName.iterator();
		while ((line = in.nextLine()) != null) {
			if (line.contains("#")) {
				line = line.substring(0, line.indexOf("#"));
			}
			if (line.trim().equalsIgnoreCase("begin observables")) {
				observe = true;
			} else if (observe) {
				if (line.trim().equals("end observables")) {
					break;
				}
				if (line.trim().isEmpty()) {
					continue;
				}
				patterns.add(parsePatterns(it.next(), line));
			}
		}
		in.close();
		return patterns;
	}

	private List<List<String>> generateEmptyPatterns() {
		List<List<String>> patterns = new ArrayList<>();
		for (int i = 0; i < varName.size(); i++) {
			List<String> name = new ArrayList<String>();
			name.add(varName.get(i));
			patterns.add(name);
		}
		return patterns;
	}

	private List<String> parsePatterns(String observableName,
	    String observableLine) {
		List<String> patterns = new ArrayList<>();
		String line = observableLine.trim().replaceAll("\\s+", " ");
		int start, bracCount;
		start = bracCount = 0;
		start = line.indexOf(observableName) + observableName.length();

		for (int i = start; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == ',') {
				if (bracCount == 0) {
					patterns.add(line.substring(start, i));
					start = i + 1;
				}
			} else if (c == ' ') {
				// check whether whitespaces are allowed in pattern
				// whitespace not allowed in a pattern
				start++;
			} else if (c == '(') {
				bracCount++;
			} else if (c == ')') {
				bracCount--;
			}
		}
		if (start < line.length()) {
			patterns.add(line.substring(start));
		}
		return patterns;
	}

	/**
	 * Get the expression of species from netFile.
	 * 
	 * @param file
	 *          NET file
	 * @return whether successful
	 */
	private List<String> readSpeciesFromNETFile(File file) {
		String filePath = file.getAbsolutePath();
		String fileName = file.getName();
		String fileType = fileName.substring(fileName.lastIndexOf("."),
		    fileName.length());
		String netFilePath = filePath.substring(0, filePath.indexOf(fileType))
		    + ".net";
		;

		// the NET file path for SCAN file is different from CDAT & GDAT files
		String slash = "/";
		if (fileType.equals(".scan")) {
			String prefixName = fileName.substring(0, fileName.lastIndexOf('.'));
			netFilePath = file.getParent() + slash + prefixName + slash + prefixName
			    + ".net";
		}

		File netFile = new File(netFilePath);

		try {
			Scanner in = new Scanner(netFile);
			String line;
			// list of species' name
			List<String> speciesList = new ArrayList<String>();

			while (in.hasNext()) {
				line = in.nextLine().trim();
				if (line.equalsIgnoreCase("begin species")) {
					// build array of species
					do {
						line = in.nextLine().trim();
						if (line.lastIndexOf(")") != -1) {
							// name of species
							String s = line.substring(line.indexOf(" ") + 1,
							    line.lastIndexOf(")") + 1);
							speciesList.add(s);
						}
					} while (!line.equalsIgnoreCase("end species"));
					break;
				}
			}

			in.close();
			return speciesList;
		} catch (FileNotFoundException e) {
			System.out.println("NET File not found!");
			return varName;
		}
	}

	/**
	 * Get the expression and composition of observable from netFile.
	 * 
	 * @param file
	 *          NET file
	 * @return whether successful
	 */
	private List<List<String>> readComponentsFromNetFile(List<String> observables) {
		String filePath = file.getAbsolutePath();
		String fileName = file.getName();
		String fileType = fileName.substring(fileName.lastIndexOf("."),
		    fileName.length());
		String netFilePath = filePath.substring(0, filePath.indexOf(fileType))
		    + ".net";

		// the NET file path for SCAN file is different from CDAT & GDAT files
		String slash = "/";

		if (fileType.equals(".scan")) {
			String prefixName = fileName.substring(0, fileName.lastIndexOf('.'));
			netFilePath = file.getParent() + slash + prefixName + slash + prefixName
			    + ".net";
		}

		File netFile = new File(netFilePath);

		// list of observale nodes
		List<List<String>> componentsList = new ArrayList<>(observables.size());
		List<String> speciesList = speciesFolder.getComponents();
		for (int i = 0; i < observables.size(); i++) {
			componentsList.add(new ArrayList<String>());
		}
		try {
			Scanner in = new Scanner(netFile);
			String line;
			Iterator<List<String>> itComp = componentsList.iterator();

			while (in.hasNext()) {
				line = in.nextLine().trim();
				if (line.equalsIgnoreCase("begin groups")) {

					line = in.nextLine().trim();

					while (!line.equalsIgnoreCase("end groups")) {

						// format: id name listOfComponent
						String[] tmp = line.split(" ");
						List<String> components = itComp.next();

						// the observable has no species list
						if (tmp.length == 2) {
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

						// add speciesID to componentsID
						for (String element : tmp2) {
							String speciesID = element;

							// number*speciesID, remove number, only leave
							// speciesID
							if (speciesID.contains("*")) {
								speciesID = speciesID.substring(speciesID.indexOf("*") + 1,
								    speciesID.length());
							}

							// add speciesID
							components.add(speciesList.get(Integer.parseInt(speciesID) - 1));
						}
						line = in.nextLine().trim();
					}
					break;
				}

			}

			in.close();
			return componentsList;
		} catch (FileNotFoundException e) {
			System.out.println("NET File not found!");
			return componentsList;
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

	public String getSpeciesID(String speciesName) {
		if (speciesNameTrans.containsKey(speciesName)) {
			return speciesNameTrans.get(speciesName);
		}
		return speciesName;
	}

	/**
	 * 
	 * @param selectedSpeciesName
	 */
	public void setSelectedSpeciesName(String selectedSpeciesName) {
		if (!speciesNameTrans.isEmpty()
		    && speciesNameTrans.containsKey(selectedSpeciesName)) {
			this.selectedSpeciesName = speciesNameTrans.get(selectedSpeciesName);
		} else {
			this.selectedSpeciesName = selectedSpeciesName;
		}
	}

	/**
	 * Add a species node if not exists.
	 * 
	 * @param node
	 *          SpeciesNode object
	 */
	public void addCheckedSpecies(SpeciesNode node) {
		if (!checkedSpecies.containsKey(node.getId())) {
			checkedSpecies.put(node.getId(), node);
		}
		// if (!checkedSpecies.containsKey(node.getName())) {
		// checkedSpecies.put(node.getName(), node);
		// }
	}

	/**
	 * Remove a species node if exists.
	 * 
	 * @param node
	 *          SpeciesNode object
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
	 *          ObservableFolderNode object
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
	 *          ObservableFolderNode object
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
	 *          array of expanded tree elements
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
	 *          array of grayed tree elements
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
	 *          type of chart
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
	@Override
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
