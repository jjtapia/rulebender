package visualizationviewer;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import prefuse.Display;

public class SelectBoxControl implements MouseListener, MouseMotionListener{
	
	private int m_down_x, m_down_y; // mouse down x and y
	private int upperleft_x, upperleft_y; // upper left corner x and y of select box
	private int width, height; // width and height of select box
	private RectanglePanel selectbox; // select box component
	private Display dis; // big focus display needs to be moved
	private double ratio; // scale of overview over focus
	
	/**
	 * 
	 * @param x upper left corner x of select box
	 * @param y upper left corner y of select box
	 * @param width width of select box
	 * @param height height of select box
	 * @param selectbox select box component
	 * @param dis big focus display needs to be moved
	 * @param ratio scale of overview over focus
	 */
	public SelectBoxControl(int x, int y, int width, int height, RectanglePanel selectbox, Display dis, double ratio) {
		m_down_x = -1;
		m_down_y = -1;
		upperleft_x = x;
		upperleft_y = y;
		this.width = width;
		this.height = height;
		this.selectbox = selectbox;
		this.dis = dis;
		this.ratio = ratio;
	}

	/**
	 * Compute drag distance based on mouse down position,
	 * and reset bounds for select box;
	 * Then update focus display by panning.
	 */
	public void mouseDragged(MouseEvent e) {
		// compute drag distance
		int distance_x = e.getX() - m_down_x;
		int distance_y = e.getY() - m_down_y;
		// reset bounds
		upperleft_x += distance_x;
		upperleft_y += distance_y;
		selectbox.setBounds(upperleft_x, upperleft_y, width, height);
		
		// compute pan_x and pan_y for focus display
		int pan_x = (int)(-distance_x/ratio);
		int pan_y = (int)(-distance_y/ratio);
		dis.pan(pan_x, pan_y);
		// repaint
		dis.repaint();
	}
	
	public void updateInfo(int x, int y, int width, int height, double ratio) {
		upperleft_x = x;
		upperleft_y = y;
		this.width = width;
		this.height = height;
		this.ratio = ratio;
		
		selectbox.setBounds(x, y, width, height);
		selectbox.setWidth(this.width);
		selectbox.setHeight(this.height);
	}

	public void mouseMoved(MouseEvent arg0) {
		
	}

	public void mouseClicked(MouseEvent arg0) {
		
	}

	public void mouseEntered(MouseEvent arg0) {
		
	}

	public void mouseExited(MouseEvent arg0) {
		
	}

	/**
	 * Store the mouse press position
	 */
	public void mousePressed(MouseEvent e) {
		m_down_x = e.getX();
		m_down_y = e.getY();	
	}

	public void mouseReleased(MouseEvent arg0) {
		
	}

}
