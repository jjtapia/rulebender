package rulebender.core.prefuse.overview;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import prefuse.Display;
import prefuse.controls.ControlAdapter;
import prefuse.util.ColorLib;
import prefuse.util.display.PaintListener;
import prefuse.util.ui.UILib;

public class OverviewControl extends ControlAdapter implements PaintListener {

    private Point pointClicked;
    private int m_button = LEFT_MOUSE_BUTTON;
    private boolean buttonPressed = false;
    private Display display,  overview;
    private Insets insets;

    public OverviewControl(Display display, Display overview) {
        this(display, overview, LEFT_MOUSE_BUTTON);
    }

    public OverviewControl(Display display, Display overview, int button) {
        super();

        this.display = display;
        this.overview = overview;
        this.insets = overview.getInsets();
        m_button = button;

        pointClicked = new Point();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (UILib.isButtonPressed(e, m_button)) {
            buttonPressed = true;

            pointClicked = new Point(e.getX(), e.getY());

            panDisplayTo(pointClicked);
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == m_button) {
            buttonPressed = false;

            pointClicked = new Point(e.getX(), e.getY());

            panDisplayTo(pointClicked);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (buttonPressed) {
            pointClicked = new Point(e.getX(), e.getY());

            panDisplayTo(pointClicked);
        }
    }

    private void panDisplayTo(Point point) {
        Point to = overview2display(point);

        display.panTo(to);
        display.repaint();
    }

    private Point overview2display(Point point) {
        try {
            AffineTransform displayT = display.getTransform();
            AffineTransform overviewT = overview.getTransform();

            Point tPoint = new Point();
            Point displayPoint = new Point();

            overviewT.inverseTransform(point, tPoint);   // Overview -> 1:1
            displayT.transform(tPoint, displayPoint);   // 1:1 -> Display

            return displayPoint;
        } catch (NoninvertibleTransformException nitex) {
        }
        return null;
    }

    private Point display2overview(Point point) {
        Point overviewPoint = null;
        try {
            AffineTransform displayT = display.getTransform();
            AffineTransform overviewT = overview.getTransform();

            Point tPoint = new Point();
            overviewPoint = new Point();

            displayT.inverseTransform(point, tPoint);   // Display -> 1:1
            overviewT.transform(tPoint, overviewPoint);   // 1:1 -> Overview
        } catch (NoninvertibleTransformException nitex) {
        }
        return overviewPoint;
    }
    /*
    private void fixBounds(Point point) {
    Point boundsStart = new Point(insets.left, insets.top);
    Point boundsEnd = new Point(overview.getWidth() - insets.right, overview.getHeight() - insets.bottom);
    
    // Gets rectangle coordinates.
    Point rectStart = new Point();
    Point rectEnd = new Point();
    getRectangleCoordinates(display, rectStart, rectEnd);
    
    int dx = (rectEnd.x - rectStart.x) / 2;
    int dy = (rectEnd.y - rectStart.y) / 2;
    
    // Check bounds.
    int fixedX = point.x;
    int fixedY = point.y;
    
    if (rectStart.x < boundsStart.x) {
    fixedX = boundsStart.x + dx;
    }
    if (rectStart.y < boundsStart.y) {
    fixedY = boundsStart.y + dy;
    }
    if (rectEnd.x > boundsEnd.x) {
    fixedX = boundsEnd.x - dx;
    }
    if (rectEnd.y > boundsEnd.y) {
    fixedY = boundsEnd.y - dy;
    }
    
    //        System.out.println(" >> (" + point.x + "," + point.y + ") ->> (" + fixedX + "," + fixedY + ")");
    
    point.setLocation(fixedX, fixedY);
    }*/

    public void postPaint(Display d, Graphics2D g) {
        drawClearRectangle(g);
    }

    public void prePaint(Display d, Graphics2D g) {
        overview.repaint();
    }

    private void getRectangleCoordinates(Display display, Point rectangleStart, Point rectangleEnd) {
        // Gets visible rect...
        Point tmpStart = display.getVisibleRect().getLocation();

        Point tmpEnd = display.getVisibleRect().getLocation();
        tmpEnd.translate(display.getVisibleRect().width, display.getVisibleRect().height);

        // ... converts it to the overview coordinates system...
        rectangleStart.setLocation(display2overview(tmpStart));
        rectangleEnd.setLocation(display2overview(tmpEnd));
    }

    private void drawClearRectangle(Graphics2D g) {
        // Updates rectangle coordinates...
        Point rectangleStart = new Point();
        Point rectangleEnd = new Point();
        getRectangleCoordinates(display, rectangleStart, rectangleEnd);

        // Prepares drawing area.
        Color before = g.getColor();
        Shape clip = g.getClip();

        g.setClip(insets.left, insets.top,
                overview.getWidth() - insets.left - insets.right,
                overview.getHeight() - insets.bottom - insets.top);
        g.setColor(new Color(ColorLib.hex("#4682B47A"), true));

        // ...and draws it:
        // Top.
        g.fillRect(insets.left, insets.top,
                overview.getWidth() - insets.left - insets.right,
                rectangleStart.y - insets.top);

        // Bottom.
        g.fillRect(insets.left, rectangleEnd.y,
                overview.getWidth() - insets.left - insets.right,
                overview.getHeight() - insets.bottom - rectangleEnd.y);

        // Left.
        g.fillRect(insets.left, rectangleStart.y,
                rectangleStart.x - insets.left,
                rectangleEnd.y - rectangleStart.y);

        // Right.
        g.fillRect(rectangleEnd.x, rectangleStart.y,
                overview.getWidth() - rectangleEnd.x - insets.right,
                rectangleEnd.y - rectangleStart.y);

        // Draws the rectangle.
        g.setColor(new Color(ColorLib.hex("#A6A8FA"), true));
        g.drawRect(rectangleStart.x, rectangleStart.y,
                rectangleEnd.x - rectangleStart.x,
                rectangleEnd.y - rectangleStart.y);

        // ... and a cross.
        //drawCross(g, rectangleStart, rectangleEnd);

        // Restore drawing area.
        g.setColor(before);
        g.setClip(clip);
    }
/*
    private void drawCross(Graphics g, Point start, Point end) {
        Color anterior = g.getColor();
        g.setColor(Color.RED);

        g.drawLine(start.x, start.y, end.x, end.y);
        g.drawLine(start.x, end.y, end.x, start.y);

        g.setColor(anterior);
    }*/
}
