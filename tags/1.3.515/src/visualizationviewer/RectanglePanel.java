package visualizationviewer;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

public class RectanglePanel extends JComponent
{

  private float width;
  private float height;

  private static Color color_light = new Color(217, 95, 14, 60);
  private static Color color_dark = new Color(217, 95, 14, 255);


  public RectanglePanel(float width, float height)
  {
    this.width = width;
    this.height = height;
  }


  // This is Swing, so override paint*Component* - not paint
  protected void paintComponent(Graphics g)
  {
    // call super.paintComponent to get default Swing
    // painting behavior (opaque honored, etc.)
    super.paintComponent(g);

    g.setColor(color_dark);
    g.drawRect(0, 0, (int) width - 1, (int) height - 1);
    g.setColor(color_light);
    g.fillRect(0, 0, (int) width, (int) height);

    // DEBUG. Random space to see when it changes.
    // system.out.println((System.currentTimeMillis()%2 == 0 ? "Painted:" :
    // " Painted:"));
    // system.out.println("\tx: " + this.getBounds().x +
    // "\n\ty: " + this.getBounds().y +
    // "\n\twidth: " + width +
    // "\n\theight: " + height);
  }


  public void setWidth(float width_in)
  {
    width = width_in;
  }


  public void setHeight(float height_in)
  {
    height = height_in;
  }

}
