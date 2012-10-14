package visualizationviewer;

/**
 * This class manages the size and location of the selectbox in the overview 
 * pane and the visible area in the main window when there is a selectbox drag.
 * 
 */

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

import prefuse.Display;

public class SelectBoxControl implements MouseListener, MouseMotionListener
{

  private float m_down_x, m_down_y; // mouse down x and y
  private float upperleft_x, upperleft_y; // upper left corner x and y of
  // select box
  private float width, height; // width and height of select box
  private RectanglePanel selectbox; // select box component
  private Display dis; // big focus display needs to be moved
  private double ratio; // scale of overview over focus


  /**
   * 
   * @param x
   *          upper left corner x of select box
   * @param y
   *          upper left corner y of select box
   * @param width
   *          width of select box
   * @param height
   *          height of select box
   * @param selectbox
   *          select box component
   * @param dis
   *          big focus display needs to be moved
   * @param ratio
   *          scale of overview over focus
   */
  public SelectBoxControl(int x, int y, int width, int height,
      RectanglePanel selectbox, Display dis, double ratio)
  {
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
   * Compute drag distance based on mouse down position, and reset bounds for
   * select box; Then update focus display by panning.
   */
  public void mouseDragged(MouseEvent e)
  {
    // compute drag distance
    float distance_x = e.getX() - m_down_x;
    float distance_y = e.getY() - m_down_y;
    // reset bounds
    upperleft_x += distance_x;
    upperleft_y += distance_y;
    selectbox.setBounds((int) upperleft_x, (int) upperleft_y, (int) width,
        (int) height);

    // compute pan_x and pan_y for focus display
    int pan_x = (int) (-distance_x / ratio);
    int pan_y = (int) (-distance_y / ratio);
    dis.pan(pan_x, pan_y);
    // repaint
    dis.repaint();
    // //system.out.println("drag");
  }


  /**
   * This can be called from any other class to change the position of the
   * select box. Right now, it is how the select box gets updated when the
   * prefuse visualization is zoomed.
   * 
   * For some reason, the zoom information is always 1 zoom late...When the
   * update is called, the painted values are always the same, so the problem is
   * in the passed values.
   * 
   * @param x
   * @param y
   * @param width
   * @param height
   * @param result
   */
  public void updateInfo(int x, int y, int width, int height, double result)
  {

    // normal
    // if(System.currentTimeMillis()%2 == 0)
    // {
    upperleft_x = x;
    upperleft_y = y;
    this.width = width;
    this.height = height;
    this.ratio = result;
    /*
     * } else { upperleft_x = 10; upperleft_y = 10; this.width = 50; this.height
     * = 50; this.ratio = result; }
     */
    selectbox.setBounds((int) upperleft_x, (int) upperleft_y, (int) this.width,
        (int) this.height);
    selectbox.setWidth(this.width);
    selectbox.setHeight(this.height);
    // selectbox.repaint();

    // system.out.println("Updated: \n\tx: "+ upperleft_x +
    // "\n\ty: " + upperleft_y +
    // "\n\twidth: " + this.width +
    // "\n\theight: " + this.height);

  }


  public void mouseMoved(MouseEvent arg0)
  {

  }


  public void mouseClicked(MouseEvent arg0)
  {

  }


  public void mouseEntered(MouseEvent arg0)
  {

  }


  public void mouseExited(MouseEvent arg0)
  {

  }


  /**
   * Store the mouse press position
   */
  public void mousePressed(MouseEvent e)
  {
    m_down_x = e.getX();
    m_down_y = e.getY();
  }


  public void mouseReleased(MouseEvent arg0)
  {

  }

}
