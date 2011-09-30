package rulebender.prefuse.networkviewer.influencegraph;
import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.BorderFactory;

import rulebender.prefuse.networkviewer.PrefuseTooltip;

public class RuleTooltip extends PrefuseTooltip {
	protected javax.swing.JLabel nameLabel;
	protected javax.swing.JLabel moleLabel;
	protected javax.swing.JPanel contentsPanel;
	protected String gender;
	
	public RuleTooltip(javax.swing.JComponent owner, String name) {
			super(owner, 0 , 1000);
			
			contentsPanel = new javax.swing.JPanel();
			contentsPanel.setLayout(new BorderLayout());
			
			nameLabel = new javax.swing.JLabel();
			moleLabel = new javax.swing.JLabel();
			
			nameLabel.setText(name);
			
			contentsPanel.add(nameLabel, BorderLayout.PAGE_START);
			contentsPanel.add(moleLabel, BorderLayout.PAGE_END);
			

			contentsPanel.setBorder(BorderFactory.createLineBorder(Color.gray));
			contentsPanel.setBackground(new Color(255, 255, 255));
		}
		
		public java.awt.Component getContents() {
			return contentsPanel;
		}
	}