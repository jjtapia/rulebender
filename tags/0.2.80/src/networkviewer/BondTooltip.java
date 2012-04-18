package networkviewer;
import java.awt.Color;

import javax.swing.BorderFactory;

public class BondTooltip extends PrefuseTooltip {
	protected javax.swing.JLabel nameLabel;
	protected javax.swing.JPanel contentsPanel;
	protected String gender;
	
	public BondTooltip(javax.swing.JComponent owner) {
		super(owner);
		
		contentsPanel = new javax.swing.JPanel();
		nameLabel = new javax.swing.JLabel();
		
		nameLabel.setText("Bond");
		
		contentsPanel.add(nameLabel);

		contentsPanel.add(new javax.swing.JButton(new javax.swing.AbstractAction("Context Menu") {
            public void actionPerformed (java.awt.event.ActionEvent e) {
            	System.out.println("Bubblesets action go!");
            }
		}));

		contentsPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
		contentsPanel.setBackground(new Color(255, 250, 205));
	}
	
	public java.awt.Component getContents() {
		return contentsPanel;
	}
}