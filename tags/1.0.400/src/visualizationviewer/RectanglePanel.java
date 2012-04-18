package visualizationviewer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class RectanglePanel extends JComponent {
	
	private int width;
	private int height;
	
	private static Color color_light = new Color(217, 95, 14, 60);
	private static Color color_dark = new Color(217, 95, 14, 255);
	
	public RectanglePanel(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	
	// This is Swing, so override paint*Component* - not paint
    protected void paintComponent(Graphics g) {
        // call super.paintComponent to get default Swing 
        // painting behavior (opaque honored, etc.)
        super.paintComponent(g);
        g.setColor(color_dark);
        g.drawRect(0, 0, width-1, height-1);
        g.setColor(color_light);
        g.fillRect(0, 0, width, height);
    }


	public void setWidth(int width_in) 
	{
		width = width_in;
	}
	
	public void setHeight(int height_in) 
	{
		height = height_in;
	}

}
