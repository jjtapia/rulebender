package rulebender.editors.dat.view;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYSeriesCollection;

import rulebender.editors.dat.model.FileData;
import rulebender.editors.dat.model.ObservableFolderNode;
import rulebender.editors.dat.model.ObservableNode;
import rulebender.editors.dat.model.SpeciesFolderNode;
import rulebender.editors.dat.model.SpeciesNode;
import rulebender.editors.dat.model.TreeNode;
import rulebender.results.data.DATFileData;

public class DATComparisonData extends FileData 
{

	private List<String> fileTypeList; // list of string of file types
	private List<DATFileData> fileDataList; // list of DATFileData objects

	private String xAxisName; // name of x axis
	private XYSeriesCollection seriesCollection; // data for plotting
	
	private boolean allValueLargerThanZero_X = true;
	private boolean allValueLargerThanZero_Y = true;
	private double minX = Double.MAX_VALUE;
	private double minY = Double.MAX_VALUE;

	/**
	 * 
	 * @param type1
	 * @param data1
	 * @param type2
	 * @param data2
	 */
	public DATComparisonData(String type1, DATFileData data1, String type2,
			DATFileData data2) {
		seriesCollection = new XYSeriesCollection();

		fileTypeList = new ArrayList<String>();
		fileDataList = new ArrayList<DATFileData>();

		fileTypeList.add(type1);
		fileDataList.add((DATFileData) data1.clone());

		fileTypeList.add(type2);
		fileDataList.add((DATFileData) data2.clone());

		// clear the record of selection
		for (int i = 0; i < fileDataList.size(); i++) {
			fileDataList.get(i).setSelectedObservableName(null);
			fileDataList.get(i).setSelectedSpeciesName(null);
		}
		
		if (data1.isAllValueLargerThanZero_X() == false 
				|| data2.isAllValueLargerThanZero_X() == false ) {
			this.allValueLargerThanZero_X = false;
		}
		
		if (data1.isAllValueLargerThanZero_Y() == false 
				|| data2.isAllValueLargerThanZero_Y() == false ) {
			this.allValueLargerThanZero_Y = false;
		}
		
		this.minX = data1.getMinX();
		if (data2.getMinX() < this.minX) {
			this.minX = data2.getMinX();
		}
		
		this.minY = data1.getMinY();
		if (data2.getMinY() < this.minY) {
			this.minY = data2.getMinY();
		}

		readData();
	}

	/**
	 * No need to read data from file.
	 */
	protected void readData() {

	}

	/**
	 * @return whether these two DATFileData are comparable
	 */
	public boolean isComparable() {
		String filename = fileDataList.get(0).getFileName();
		// SCAN file can only compared with SCAN file
		if (filename.endsWith(".scan")) {
			xAxisName = "Concentration";
			for (int i = 1; i < fileDataList.size(); i++) {
				if (!fileDataList.get(i).getFileName().endsWith(".scan")) {
					return false;
				}
			}
		}
		// CDAT and GDAT file can not compare with SCAN file
		else {
			xAxisName = "time";
			for (int i = 1; i < fileDataList.size(); i++) {
				if (fileDataList.get(i).getFileName().endsWith(".scan")) {
					return false;
				}
			}
		}

		return true;

	}

	/**
	 * 
	 * @return name of x axis
	 */
	public String getXAxisName() {
		return this.xAxisName;
	}

	/**
	 * Get SpeciesFolderNode object by file index
	 * 
	 * @param index
	 * @return
	 */
	public SpeciesFolderNode getSpeciesFolder(int index) {
		return fileDataList.get(index).getSpeciesFolder();
	}

	/**
	 * Get ObservableFolderNode object by file index
	 * 
	 * @param index
	 * @return
	 */
	public ObservableFolderNode getObservableFolder(int index) {
		return fileDataList.get(index).getObservableFolder();
	}

	/**
	 * 
	 * @return whole data set for plotting
	 */
	public XYSeriesCollection getSeriesCollection() {
		// clear data
		seriesCollection.removeAllSeries();

		for (int i = 0; i < fileDataList.size(); i++) {
			XYSeriesCollection curSeriesCollection = fileDataList.get(i)
					.getSeriesCollection();

			for (int j = 0; j < curSeriesCollection.getSeriesCount(); j++) {
				seriesCollection.addSeries(curSeriesCollection.getSeries(j));
			}
		}

		return seriesCollection;
	}

	/**
	 * 
	 * @param index
	 * @return sub data set based on file index
	 */
	public XYSeriesCollection getSeriesCollection(int index) {
		return fileDataList.get(index).getSeriesCollection();
	}

	/**
	 * 
	 * @param index
	 * @return number of series based on file index
	 */
	public int getSeriesCount(int index) {
		return fileDataList.get(index).getSeriesCollection().getSeriesCount();
	}

	/**
	 * 
	 * @return number of file data
	 */
	public int getDATDataCount() {
		return fileDataList.size();
	}

	/**
	 * 
	 * @param index
	 * @return SpeciesFolderNode object or ObservableFolderNode object based on
	 *         file type
	 */
	public TreeNode getFolderNode(int index) {
		// read folderNodeList
		String fileType = fileTypeList.get(index);
		if (fileType.equalsIgnoreCase("cdat")) {
			// CDAT
			return fileDataList.get(index).getSpeciesFolder();
		} else if (fileType.equalsIgnoreCase("gdat")
				|| fileType.equalsIgnoreCase("scan")) {
			// GDAT & SCAN
			return fileDataList.get(index).getObservableFolder();
		} else {
			return null;
		}
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @param node
	 *            ObservableNode object
	 */
	public void addCheckedObservable(Integer index, ObservableNode node) {
		fileDataList.get(index).addCheckedObservable(node);
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @param node
	 *            ObservableNode object
	 */
	public void removeCheckedObservable(Integer index, ObservableNode node) {
		fileDataList.get(index).removeCheckedObservable(node);
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @param node
	 *            SpeciesNode object
	 */
	public void addCheckedSpecies(Integer index, SpeciesNode node) {
		fileDataList.get(index).addCheckedSpecies(node);
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @param node
	 *            SpeciesNode object
	 */
	public void removeCheckedSpecies(Integer index, SpeciesNode node) {
		fileDataList.get(index).removeCheckedSpecies(node);
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @param isAllChecked
	 */
	public void setAllChecked(Integer index, boolean isAllChecked) {
		fileDataList.get(index).setAllChecked(isAllChecked);
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @return array of checked elements
	 */
	public TreeNode[] getCheckedElements(Integer index) {
		String fileType = fileTypeList.get(index);
		if (fileType.equalsIgnoreCase("cdat")) {
			return fileDataList.get(index).getCheckedSpecies();
		} else if (fileType.equalsIgnoreCase("gdat")
				|| fileType.equalsIgnoreCase("scan")) {
			return fileDataList.get(index).getCheckedObservable();
		} else {
			return null;
		}

	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @return name of selected observable
	 */
	public String getSelectedObservableName(int index) {
		return fileDataList.get(index).getSelectedObservableName();
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @param selectedObservableName
	 *            name of selected observable
	 */
	public void setSelectedObservableName(int index,
			String selectedObservableName) {
		fileDataList.get(index).setSelectedObservableName(
				selectedObservableName);
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @return name of selected species
	 */
	public String getSelectedSpeciesName(int index) {
		return fileDataList.get(index).getSelectedSpeciesName();
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @param selectedObservableName
	 *            name of selected species
	 */
	public void setSelectedSpeciesName(int index, String selectedObservableName) {
		fileDataList.get(index).setSelectedSpeciesName(selectedObservableName);
	}

	/**
	 * 
	 * @param index
	 *            file index
	 * @return file name
	 */
	public String getFileName(int index) {
		return fileDataList.get(index).getFileName();
	}

	/**
	 * 
	 * @param fileName
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
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

}
