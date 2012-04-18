/**
 * @author Adam M. Smith 12/2009
 */
package bubbleset;

/**
 * This class will be used as both the virtual edges and for testing purposes. 
 * 
 */
public class VirtualNode implements BubbleSetNodeInterface {

	double x;

	double y;

	double width;

	private double height;
	
	private String name;
	
	public VirtualNode(double m_pts, double m_pts2, double d, double e)
	{
		x = m_pts;
		y = m_pts2;
		width = d;
		height = e;
	}
	
	public void setName(String name_)
	{
		name = name_;
	}
	public String getName()
	{
		return name;
	}
	
	public double getHeight() {
		return height;
	}

	public double getWidth() {

		return width;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

}
