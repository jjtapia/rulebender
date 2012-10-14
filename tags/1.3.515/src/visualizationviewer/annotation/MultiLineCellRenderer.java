package visualizationviewer.annotation;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;

import java.awt.*;
import java.util.ArrayList;

/**
 * @version 1.0 11/09/98
 */

public class MultiLineCellRenderer extends JTextArea implements
		TableCellRenderer {

	private static Color color_SlateGray2 = new Color(185, 211, 238);
	private static Color color_SlateGray3 = new Color(159, 182, 205);
	private static Color color_RoyalBlue = new Color(65, 105, 225);
	private static Color color_grey = new Color(190, 190, 190);
	private static Color color_White = new Color(255, 255, 255);

	public MultiLineCellRenderer() {
		setLineWrap(true);
		setWrapStyleWord(true);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		// select
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(color_SlateGray2);
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}

		// focus
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			if (table.isCellEditable(row, column)) {
				setForeground(UIManager.getColor("Table.focusCellForeground"));
				setBackground(color_White);
				setBorder(new MatteBorder(3, 3, 3, 3, color_SlateGray3));
			}
		} else {
			// border
			setBorder(new MatteBorder(1, 1, 1, 1, color_grey));
		}

		// foreground color
		ArrayList<String> database = new ArrayList<String>();
		database.add("UniProt");
		database.add("PathwayCommons");
		database.add("HPRD");
		database.add("Reactome");
		database.add("UCSD-Nature");
		database.add("InterPro");
		database.add("PROSITE");
		database.add("KEGG");
		database.add("ChEBI");
		database.add("PubChem");
		String columnContent = (String) table.getModel()
				.getValueAt(row, column);
		if (database.contains(columnContent)) {
			setForeground(color_RoyalBlue);
		} else {
			setForeground(table.getForeground());
		}
		// font
		setFont(table.getFont());
		// text
		setText((value == null) ? "" : value.toString());
		// size
		setSize(table.getColumnModel()
				.getColumn(table.convertColumnIndexToModel(column)).getWidth(),
				10);
		// To set the table row height dynamically
		int height = (int) getPreferredSize().getHeight();
		if (height > table.getRowHeight(row))
			table.setRowHeight(row, height + 10);

		return this;
	}
}
