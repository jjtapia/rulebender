package resultviewer.graph;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class SpeciesGraphTableModel implements TableModel {

	private String[] columnNames;
	private Object[][] data;

	/**
	 * Set columnNames and data
	 * 
	 * @param columnNames
	 * @param data
	 */
	public SpeciesGraphTableModel(String[] newcolumnNames, Object[][] newdata) {

		// initialize columnNames
		this.columnNames = new String[newcolumnNames.length];
		for (int i = 0; i < newcolumnNames.length; i++) {
			// set value
			this.columnNames[i] = newcolumnNames[i];
		}

		// initialize data
		this.data = new Object[newdata.length][];
		for (int i = 0; i < newdata.length; i++) {
			// initialize items in data
			this.data[i] = new Object[newdata[i].length];
			for (int j = 0; j < newdata[i].length; j++) {
				// set value
				this.data[i][j] = newdata[i][j];
			}
		}

	}

	public void addTableModelListener(TableModelListener arg0) {

	}

	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public int getRowCount() {
		return data.length;
	}

	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	public void removeTableModelListener(TableModelListener arg0) {

	}

	public void setValueAt(Object arg0, int arg1, int arg2) {

	}

}
