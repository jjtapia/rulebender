package resultviewer.data;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import resultviewer.tree.ObservableFolderNode;
import resultviewer.tree.ObservableNode;
import resultviewer.tree.SpeciesFolderNode;
import resultviewer.tree.SpeciesNode;
import resultviewer.tree.TreeNode;

public class DATComparisonData extends FileData{
	
	private List<String> fileTypeList;
	private List<DATFileData> fileDataList;
	
	
	private String xAxisName;
	private XYSeriesCollection seriesCollection;
	
	
	public DATComparisonData(String type1, DATFileData data1, String type2, DATFileData data2) {
		seriesCollection = new XYSeriesCollection();
		
		fileTypeList = new ArrayList<String>();
		fileDataList = new ArrayList<DATFileData>();
		
		fileTypeList.add(type1);
		fileDataList.add((DATFileData)data1.clone());
		
		fileTypeList.add(type2);
		fileDataList.add((DATFileData)data2.clone());
		
		// clear the record of selection
		for(int i = 0; i < fileDataList.size(); i++) {
			fileDataList.get(i).setSelectedObservableName(null);
			fileDataList.get(i).setSelectedSpeciesName(null);
		}
		
		readData();	
	}
	
	protected void readData() {
		
	}
	
	/*
	 * Check if these two DATFileData are comparable
	 */
	public boolean isComparable() {
		xAxisName = fileDataList.get(0).getXAxisName();
		for (int i = 1; i < fileDataList.size(); i++) {
			if (!fileDataList.get(i).getXAxisName().equals(xAxisName)) {
				return false;
			}
		}
		return true;
	}
	
	public String getXAxisName() {
		return this.xAxisName;
	}
	
	/*
	 * Get SpeciesFolderNode by index
	 */
	public SpeciesFolderNode getSpeciesFolder(int index) {
		return fileDataList.get(index).getSpeciesFolder();
	}
	
	/*
	 * Get ObservableFolderNode by index
	 */
	public ObservableFolderNode getObservableFolder(int index) {
		return fileDataList.get(index).getObservableFolder();
	}
	

	/*
	 * Return the whole data set
	 */
	public XYSeriesCollection getSeriesCollection() {
		seriesCollection.removeAllSeries();
		
		for (int i = 0; i < fileDataList.size(); i++) {
			XYSeriesCollection curSeriesCollection = fileDataList.get(i).getSeriesCollection();
			
			for (int j = 0; j < curSeriesCollection.getSeriesCount(); j++) {
				seriesCollection.addSeries(curSeriesCollection.getSeries(j));
			}
		}
		
		return seriesCollection;
	}
	
	/*
	 * Return the data set by index
	 */
	public XYSeriesCollection getSeriesCollection(int index) {
		return fileDataList.get(index).getSeriesCollection();
	}
	
	public int getSeriesCount(int index) {
		return fileDataList.get(index).getSeriesCollection().getSeriesCount();
	}
	
	public int getDATDataCount() {
		return fileDataList.size();
	}
	
	public TreeNode getFolderNode(int index) {
		// read folderNodeList
		String fileType = fileTypeList.get(index);
		if (fileType.equalsIgnoreCase("cdat")) {
			return fileDataList.get(index).getSpeciesFolder();
		} else if (fileType.equalsIgnoreCase("gdat")
				|| fileType.equalsIgnoreCase("scan")) {
			return fileDataList.get(index).getObservableFolder();
		}
		else {
			return null;
		}
	}
	
	public void addCheckedObservable(Integer index, ObservableNode node) {
		fileDataList.get(index).addCheckedObservable(node);
	}
	
	public void removeCheckedObservable(Integer index, ObservableNode node) {
		fileDataList.get(index).removeCheckedObservable(node);
	}
	
	public void addCheckedSpecies(Integer index, SpeciesNode node) {
		fileDataList.get(index).addCheckedSpecies(node);
	}
	
	public void removeCheckedSpecies(Integer index, SpeciesNode node) {
		fileDataList.get(index).removeCheckedSpecies(node);
	}
	
	public void setAllChecked(Integer index, boolean isAllChecked) {
		fileDataList.get(index).setAllChecked(isAllChecked);
	}
	
	public TreeNode[] getCheckedElements(Integer index) {
		String fileType = fileTypeList.get(index);
		if (fileType.equalsIgnoreCase("cdat")) {
			return fileDataList.get(index).getCheckedSpecies();
		} else if (fileType.equalsIgnoreCase("gdat")
				|| fileType.equalsIgnoreCase("scan")) {
			return fileDataList.get(index).getCheckedObservable();
		}
		else {
			return null;
		}
		
	}
	
	public String getSelectedObservableName(int index) {
		return fileDataList.get(index).getSelectedObservableName();
	}

	public void setSelectedObservableName(int index, String selectedObservableName) {
		fileDataList.get(index).setSelectedObservableName(selectedObservableName);
	}
	
	public String getSelectedSpeciesName(int index) {
		return fileDataList.get(index).getSelectedSpeciesName();
	}

	public void setSelectedSpeciesName(int index, String selectedObservableName) {
		fileDataList.get(index).setSelectedSpeciesName(selectedObservableName);
	}
	
	public String getFileName(int index) {
		return fileDataList.get(index).getFileName();
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
}
