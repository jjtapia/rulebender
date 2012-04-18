package visualizationviewer;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @version 1.0 11/09/98
 */

public class MultiLineCellRenderer extends JTextArea implements
		TableCellRenderer{

	public MultiLineCellRenderer() {
		setLineWrap(true);
		setWrapStyleWord(true);
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		
		/*
		// select
		if (isSelected) {
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		} else {
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}
		*/
		
		/*
		// focus
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
			if (table.isCellEditable(row, column)) {
				setForeground(UIManager.getColor("Table.focusCellForeground"));
				setBackground(UIManager.getColor("Table.focusCellBackground"));
			}
		} else {
			setBorder(new EmptyBorder(1, 2, 1, 2));
		}
		*/
		
		// foreground color
		ArrayList<String> database = new ArrayList<String>();
		database.add("UniProt");
		database.add("HPRD");
		database.add("Reactome");
		database.add("UCSD-Nature");
		database.add("InterPro");
		database.add("PROSITE");
		database.add("KEGG");
		database.add("ChEBI");
		database.add("PubChem");
		String columnName = table.getColumnName(column);
		if (database.contains(columnName)) {
			setForeground(Color.BLUE);
		}
		else {
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
		
		// border
		if (row %2 == 1) {
			setBorder(new MatteBorder(1, 0, 0, 0, Color.GRAY));
		}
		
		return this;
	}
}
