package networkviewer;
import java.awt.Color;

import javax.swing.BorderFactory;

public class ComponentTooltip extends PrefuseTooltip {
	protected javax.swing.JLabel nameLabel;
	protected javax.swing.JLabel moleLabel;
	protected javax.swing.JPanel contentsPanel;
	protected String gender;
	
	public ComponentTooltip(javax.swing.JComponent owner, String name, String molecule) {
			super(owner);
			
			contentsPanel = new javax.swing.JPanel();
			nameLabel = new javax.swing.JLabel();
			moleLabel = new javax.swing.JLabel();
			
			nameLabel.setText(name);
			moleLabel.setText(molecule);
			
			contentsPanel.add(nameLabel);
			contentsPanel.add(moleLabel);
			contentsPanel.add(new javax.swing.JButton(new javax.swing.AbstractAction("Context Menu") {
	            public void actionPerformed (java.awt.event.ActionEvent e) {
	            	System.out.println("Show a rule or something!");
	            }
			}));

			contentsPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
			contentsPanel.setBackground(new Color(255, 250, 205));
		}
		
		public java.awt.Component getContents() {
			return contentsPanel;
		}
	}