/**
 * ContactMapLayeredPane.java
 * @author Adam M. Smith
 * April 29, 2011
 * 
 * This class contains the main contact map visualization and its
 * context view.  The size of the context view is static, but the 
 * overall size of the window is set in the constructor and can
 * be changed with a method call after instantiation. 
 */
package rulebender.views.contactmap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.geom.Rectangle2D;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.visual.VisualItem;
import rulebender.prefuse.overview.Overview;

public class LayeredPane extends JLayeredPane 
{
	// The overview window size
	private final float OVERVIEW_WIDTH = 0.2f;
	private final float OVERVIEW_HEIGHT = 0.2f;
	
	// The width of the borders in pixels
	private final int BORDER_WIDTH = 1;

	// The size of the main visualization window.
	private Dimension currentSize;
	
	// The JPanels that hold the visualization and the overview
	private JPanel mainJPanel;
	private JPanel overviewJPanel;
	
	// The border object that the two JPanels share.
	private Border border;
	
	/**
	 * Constructor
	 * 
	 * @param size - The Dimension object describing the size (in pixels)
	 * 				 of the main visualization.
	 */
	public LayeredPane(Dimension size)
	{
		// Set the value of the local size variable.
		currentSize = size;
		
		// Instantiate the border object.
		border = new LineBorder(Color.GRAY, BORDER_WIDTH);
		
		// Instantiate the JPanel for the overview and set its border
		overviewJPanel = new JPanel(new BorderLayout()); 
	    overviewJPanel.setBorder(border);
	    
	    overviewJPanel.setBackground(Color.RED);
	    
	    // Instantiate the JPanel for the main visualization and set its border.
	    mainJPanel = new JPanel(new BorderLayout());
		mainJPanel.setBorder(border);
		mainJPanel.setBackground(Color.BLACK);
		
		// Add the JPanels to the JLayeredPane (this object)
		this.add(mainJPanel, new Integer(0));		
		this.add(overviewJPanel, new Integer(1));
		
		// Update the sizes of the JPanels and Displays
		myResize(size);
	}
	
	/**
	 * Use this to set the Display object to use.
	 * 
	 * @param d - The prefuse.Display object for the visualization. 
	 */
	public void setCMap(Display d)
	{
		if(mainJPanel.getComponentCount() > 0)
		{
			mainJPanel.removeAll();
		}
		
		if(d != null)
		{
			mainJPanel.add(d, BorderLayout.CENTER);
		}
			
		//system.out.println("------------------Setting Overview----------------");
		
		if(overviewJPanel.getComponentCount() > 0)
		{
			overviewJPanel.removeAll();
		}	
		
		if(mainJPanel.getComponentCount() == 1)
		{
			// absolute layout
			overviewJPanel.setLayout(null);
			
			Display dis_focus = (Display) mainJPanel.getComponent(0);
			
	     	// add overview display to panel
			overviewJPanel.add(new Overview(d));
		}
	
		overviewJPanel.revalidate();
		
		myResize();
	}

	public void myResize()
	{
		myResize(currentSize);
	}
	
	public void myResize(Dimension size) 
	{	
		currentSize = size;
		
		int overviewWidth = (int) (currentSize.getWidth() * OVERVIEW_WIDTH);
		int overviewHeight = (int) (currentSize.getHeight() * OVERVIEW_HEIGHT);
		
		
		if(mainJPanel != null && overviewJPanel != null)
		{
			mainJPanel.setBounds(0, 0, currentSize.width, currentSize.height);
			
			overviewJPanel.setBounds(0, size.height-overviewHeight, overviewWidth-BORDER_WIDTH, overviewHeight-BORDER_WIDTH);
			
			//cMapOverviewJPanel.getComponent(0).setSize(new Dimension(overviewWidth, overviewHeight));
			
			if(mainJPanel.getComponentCount() == 1 && overviewJPanel.getComponentCount() == 1)
			{
				((Display) mainJPanel.getComponent(0)).setSize(new Dimension(currentSize.width-BORDER_WIDTH*2, currentSize.height-BORDER_WIDTH*2));
				((Display) overviewJPanel.getComponent(0)).setBounds(BORDER_WIDTH, BORDER_WIDTH, overviewWidth-BORDER_WIDTH*3, overviewHeight-BORDER_WIDTH*2);
				((Display) overviewJPanel.getComponent(0)).setSize(new Dimension(overviewWidth-BORDER_WIDTH*3, overviewHeight-BORDER_WIDTH*2));
				
			}
		}
		
	}
}
