package visualizationviewer.annotation;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

public class AnnotationTableModel implements TableModel {

	private String[] columnNames;
	private Object[][] data;

	/**
	 * Set columnNames and data
	 * 
	 * @param columnNames
	 * @param data
	 */
	public AnnotationTableModel(String[] newcolumnNames, Object[][] newdata) {

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

	public void addTableModelListener(TableModelListener I) {

	}

	public Class<?> getColumnClass(int c) {
		if (getValueAt(0, c) == null)
			return String.class;

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

	/**
	 * Return true to make things editable
	 * 
	 * @see javax.swing.table.TableModel#isCellEditable(int, int)
	 */
	public boolean isCellEditable(int arg0, int arg1) {
		return false;
	}

	public void removeTableModelListener(TableModelListener arg0) {

	}

	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
	}

	public String toString() {
		String res = "";

		/*
		 * // column names for (int i = 0; i < columnNames.length; i++) { res +=
		 * columnNames[i] + "\t"; } res += "\n";
		 */

		// data
		for (int i = 0; i < data.length; i++) {
			for (int j = 0; j < data[i].length; j++) {
				if (data[i][j] != null) {
					res += data[i][j] + "\t";
				} else {
					res += "" + "\t";
				}
			}
			res += "\n";
		}
		return res;
	}

}
