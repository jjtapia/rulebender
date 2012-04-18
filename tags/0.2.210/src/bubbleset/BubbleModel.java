

// Why is my energy matrix so big and why does it get out of bounds errors for some values of pixelGroupSize?	


/**
	 * BubbleModel.java
	 * 
	 * This class defines a model for a single bubble set.   
	 * 
	 * Whole BubbleSets algorithm:
	 * 
	 *  given items in set S with known positions
	 *  for all sets s in S do
	 *		find centroid c of s
	 *      
	 *      // compute energy
	 *		for all items i in s, order ascending by distance to c do
	 *			find optimal neighbor j in s
	 *			find best route from i to j	
	 *			for all cells (pixel or pixel group) within R1 of i do
	 *				add potential due to i
	 *				add potential due to nearest virtual edge i -> j
	 *				subtract potential due to nearby non-set members k 62 s
	 *			end for
 	 *		end for
 	 *
 	 *		repeat
	 *			perform marching squares to discover isopotential contour ø s
	 *			reduce threshold 
	 *		until forall i in s, isocontour s contains(i)
	 *		draw cardinal splines using every Nth point on the contour
	 *	end for
	 *
	 *
	 * @author Adam M. Smith
	 */

package bubbleset;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BubbleModel<T extends BubbleSetNodeInterface> 
{
	private static int bubbleEdgeBuffer = 7;
	
	private float energy[][];
	private int pixelGroupSize = 1;	
	
	private ArrayList<T> in;
	private ArrayList<T> out;
	
	private ArrayList<T> processed;
	
	// The size of the energy matrix 
	private int energyWidth;
	private int energyHeight;
	
	// The translation from world space to energy space
	private int insideXShift;
	private int insideYShift;
	
	private int outsideXShift;
	private int outsideYShift;
	
	// The center point of the "in" set. 
	int centroidX = 0;
	int centroidY = 0;
	
	private ArrayList<Pair> isoContourPoints;

	/**
	 * Constructor for the BubbleSetModel.  Accepts two float arrays
	 * to create ArrayLists of virtualNodes that are sent to the 
	 * other constructor. 
	 * 
	 * The float arrays are formatted as 
	 * [top_left_first_x, top_left_first_y, top_right_first_x, top_right_first_y, 
	 * bottom_right_first_x, bottom_right_first_y, bottom_left_first_x, 
	 * bottom_left_first_y, top_left_second_x, top_left_second_y, ...]
	 * 
	 * @param m_pts The set of elements that are included in the bubble.
	 * @param out_ The set of elements that are explicitly excluded from the bubble.
	 */
	
	public BubbleModel(double[] m_pts, int inLimit, double[] out_, int outLimit)
	{
		in = new ArrayList<T>();
		out = new ArrayList<T>();
		
		
		for(int i = 0; i < inLimit; i+=(4*2))
			in.add((T) new VirtualNode(m_pts[i],m_pts[i+1], m_pts[i+2]-m_pts[i],m_pts[i+5]-m_pts[i+1]));	

		for(int i = 0; i < outLimit; i+=(4*2))
			out.add((T) new VirtualNode(out_[i],out_[i+1], out_[i+2]-out_[i],out_[i+1]-out_[i+5]));	
		
		processed = new ArrayList<T>();
		 
		initialize();
		
		energy = new float[(int)Math.ceil((float)energyHeight/(float)pixelGroupSize)][(int)Math.ceil((float)energyWidth/(float)pixelGroupSize)];

		computeEnergy();
		
		isoContourPoints = MarchingSquares.returnIsoContour(energy);
	}

	/**
	 * Constructor for the BubbleSetModel.  Accepts two ArrayLists of objects 
	 * that implement the BubbleSetNodeInterface.
	 * 
	 * @param in_ The set of elements that are included in the bubble.
	 * @param out_ The set of elements that are explicitly excluded from the bubble.
	 */
	
	public BubbleModel(ArrayList<T> in_, ArrayList<T> out_)
	{
		// Set the in and out list references
		in = in_;
		out = out_;
	
		processed = new ArrayList<T>();
		
		// 
		initialize();
		
		energy = new float[(int)Math.ceil((float)energyHeight/(float)pixelGroupSize)][(int)Math.ceil((float)energyWidth/(float)pixelGroupSize)];
		
		//System.out.println("energyHeight: " + energyHeight +"\nenergyWidth: "+energyWidth);
		//System.out.println("width = "+energy[0].length+", height = "+energy.length);
		
		computeEnergy();
		
		isoContourPoints = MarchingSquares.returnIsoContour(energy);
	}
	
	
	/**
	 * Perform various preparatory actions.
	 * 1. Find the centroid and set the values centroidX and centroidY.
	 * 2. Find the upper left point and set the values upperLeftEnergyPosX
	 *    and upperLeftEnergyPosY.
	 * 3. Set the width and height values.
	 * 4. Order the in ArrayList by increasing distance from centroid.
	 */
	private void initialize()
	{
			
		T minXNode = in.get(0), minYNode = in.get(0), maxXNode = in.get(0), maxYNode = in.get(0);
		
		// search for max x and y points in the 'in' set
		for(T inNode : in)
		{
			// Simple search
			if(inNode.getX() > maxXNode.getX())
				maxXNode = inNode;
			else if(inNode.getX() < minXNode.getX())
				minXNode = inNode;
			if(inNode.getY() > maxYNode.getY())
				maxYNode = inNode;
			else if(inNode.getY() < minYNode.getY())
				minYNode = inNode;
		}
		
		// set centroid
		centroidX = (int) ((maxXNode.getX() + minXNode.getX())/2);
		centroidY = (int) ((maxYNode.getY() + minYNode.getY())/2);
		
		processed.add((T) new VirtualNode(centroidX, centroidY, 0,0));
		
		// Set the orientation point for the energy matrix.
		// This point will be where the energy matrix lines up on the frame.
		//TODO For now I am ignoring the possibility that I may draw off of the edge of the screen.
		// Drawing at negative coordinates _should_ not throw an error.  
		insideXShift = (int) (-minXNode.getX() + minXNode.getWidth()/2 + bubbleEdgeBuffer);
		insideYShift = (int) (-minYNode.getY() + minYNode.getHeight()/2 + bubbleEdgeBuffer);
		
		outsideXShift = (int) (-minXNode.getX() + minXNode.getWidth()/2 + bubbleEdgeBuffer);
		outsideYShift = (int) (-minYNode.getY() + minYNode.getHeight()/2 + bubbleEdgeBuffer);
		
		
		// Set the width and height
		energyWidth = (int) (((maxXNode.getX()-minXNode.getX()) + maxXNode.getWidth()/2+minXNode.getWidth()/2)
		+ (bubbleEdgeBuffer*2));
		
		energyHeight = (int) (((maxYNode.getY()-minYNode.getY()) + maxYNode.getHeight()/2+minXNode.getHeight()/2)
		+ (bubbleEdgeBuffer*2));
		
		// Order the in Arraylist by increasing distance from centroid
		quickSortByDistanceFromPoint(in, centroidX, centroidY, 0, in.size()-1);
	}
	
	/**
	 * Computes the energy function for the bubble set.
	 *   
	 *		for all items i in set s, do
	 *			find optimal neighbor j in s
	 *			find best route from i to j	
	 *			for all cells (pixel or pixel group) within R1 of i do
	 *				add potential due to i
	 *				add potential due to nearest virtual edge i -> j
	 *				subtract potential due to nearby non-set members k 62 s
	 *			end for
 	 *		end for
 	 **/
	private void computeEnergy()
	{
		BubbleSetNodeInterface neighbor = null;
		ArrayList<BubbleSetNodeInterface> edge = null;
		
		for(T item : in)
		{	
			// Add energy to the pixels around the node, scaled by the 
			// scale factor
			for(int i = (int) (((item.getX()+insideXShift) - item.getWidth()/2 - bubbleEdgeBuffer)/pixelGroupSize); 
			i < ((item.getX()+insideXShift) + item.getWidth()/2 + bubbleEdgeBuffer)/pixelGroupSize;
			i++)
			{
			
				for(int j = (int) (((item.getY()+insideYShift) - item.getHeight()/2 - bubbleEdgeBuffer)/pixelGroupSize); 
				j < ((item.getY()+insideYShift) + item.getHeight()/2 + bubbleEdgeBuffer)/pixelGroupSize;
				j++)
				{
				//	System.out.println("(x="+item.getX()+",y="+item.getY()+", w="+item.getWidth()+", h="+item.getHeight()+")");
					//System.out.println("(i="+i+",j="+j+")\n");
					if(i >= 0 && j >= 0 && i < energy[0].length && j < energy.length)
						energy[j][i] += 1;
				}
				
			}
			
			
			if(processed.size() > 1)
			{
				// find the optimal neighbor
				neighbor = getOptimalNeighbour(item);
				
				// find the best route from item to neighbor
				edge = getRoute(neighbor, item);
				
				BubbleSetNodeInterface startNode = edge.get(0);
				BubbleSetNodeInterface endNode = null;
				for(int i = 1; i < edge.size(); i++)
				{
					endNode = edge.get(i);
					
					lineBresenham((int) startNode.getX(), (int) startNode.getY(),
							(int) endNode.getX(), (int) endNode.getY());
					
					startNode = endNode;
				}
			}
			
			processed.add((T) item);
			
		}
			
		// subtract energy around items from out set.
		for(BubbleSetNodeInterface item : out)
		{
			if(!(item.getX() < (energy[0].length -5 - bubbleEdgeBuffer) && 
					item.getY() < (energy.length - 5 - bubbleEdgeBuffer) ))
				continue;
		
			// remove energy from the pixels around the node, scaled by the 
			// scale factor
			for(int i = (int) (((item.getX()+insideXShift) - item.getWidth()/2 - bubbleEdgeBuffer)/pixelGroupSize); 
			i <= ((item.getX()+insideXShift) + item.getWidth()/2 + bubbleEdgeBuffer)/pixelGroupSize;
			i++)
			{
			
				for(int j = (int) (((item.getY()+insideYShift) - item.getHeight()/2 - bubbleEdgeBuffer)/pixelGroupSize); 
				j <= ((item.getY()+insideYShift) + item.getHeight()/2 + bubbleEdgeBuffer)/pixelGroupSize;
				j++)
				{
					//System.out.println("(x="+item.getX()+",y="+item.getY()+", w="+item.getWidth()+", h="+item.getHeight()+")");
					//System.out.println("(i="+i+",j="+j+")\n");
					
					energy[j][i] -= 1;
				}
				
			}
		}
		
		//DEBUG
		/*
		System.out.print("|");
		for(int i = 0; i < energy.length; i++)
			{
				for(int j = 0; j < energy[0].length; j++)
				{
					if(energy[i][j] > 0)
						System.out.print(energy[i][j] +" ");
					else if(energy[i][j] < 0)
						System.out.print(" -  ");
					else
						System.out.print(" .  ");
					
				}
				System.out.print("|\n|");
			}
		*/
	}
	
	/**
	 * Altered version of the code from 
	 * http://www.cs.unc.edu/~mcmillan/comp136/Lecture6/Lines.html
	 * 
	 * The Bresenham line algorithm computes the set of points 
	 * on a line between two points that best approximates a 
	 * straight line path.
	 * 
	 * I needed this originally because we had no real concept of an edge.
	 * If we somehow can get information about the edges, then I will not
	 * need to compute straight line paths.
	 * 
	 * This may be possible with prefuse, but I would rather not depend on it...
	 * Prefuse has 'VisualItems' for the edges, so if I could get values from those,
	 * then I could use them to add energy where the edges lay.  Maybe I'll add
	 * a way to optionally use prefuse.
	 * 
	 * @param x0
	 * @param y0
	 * @param x1
	 * @param y1
	 * @param color
	 */
	
	private void lineBresenham(int x0, int y0, int x1, int y1)
    {
        x0 = (x0 + insideXShift)/pixelGroupSize;
        y0 = (y0 + insideYShift)/pixelGroupSize;
        x1 = (x1 + insideXShift)/pixelGroupSize;
        y1 = (y1 + insideYShift)/pixelGroupSize;
		
		int dy = y1 - y0;
        int dx = x1 - x0;
        int stepx, stepy;

        if (dy < 0) { dy = -dy;  stepy = -1; } else { stepy = 1; }
        if (dx < 0) { dx = -dx;  stepx = -1; } else { stepx = 1; }
        dy <<= 1;                                                  // dy is now 2*dy
        dx <<= 1;                                                  // dx is now 2*dx

        //System.out.println("Writing at energy["+y0+"]["+x0+"]");
        if(x0 >= 0 && x0 < energy[0].length && y0 >= 0 && y0 < energy.length)
        	energy[y0][x0] += .8;
        
        if (dx > dy) {
            int fraction = dy - (dx >> 1);                         // same as 2*dy - dx
            while (x0 != x1) 
            {
                if (fraction >= 0) {
                    y0 += stepy;
                    fraction -= dx;                                // same as fraction -= 2*dx
                }
                x0 += stepx;
                fraction += dy;                                    // same as fraction -= 2*dy
                for(int i = (int) (x0-Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize))); i < x0+Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize));i++)
                {
                	for(int j = (int) (y0-Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize))); j < y0+Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize));j++)
                	{
                		if(i >= 0 && i <energy[0].length && j >= 0 && j < energy.length)
                			energy[j][i] += .8;
                	}
                }
            }
        } else {
            int fraction = dx - (dy >> 1);
            while (y0 != y1) 
            {
            	if (fraction >= 0) {
                    x0 += stepx;
                    fraction -= dy;
                }
                y0 += stepy;
                fraction += dx;
                for(int i = (int) (y0-Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize))); i < y0+Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize));i++)
                {
                	for(int j = (int) (x0-Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize))); j < x0+Math.ceil(((double)bubbleEdgeBuffer)/((double)pixelGroupSize));j++)
                	{
                		if(i >= 0 && i <energy.length && j >= 0 && j <energy[0].length)
                			energy[i][j] += .8;
                	}
                }
            }
        }
    }	
	
	/**
	 * This returns a list of points that define the edge between two nodes.  
	 * 
	 * As of now, this does not account for structural edges that already 
	 * exist and will be altered later.
	 * 
	 *  This ArrayList representation, however, does allow for virtual edges.
	 */
	// TODO Allow structural edges.  I need to figure out the best way to 
	// encode edges.  Not all bubble sets will have them.  I also want to 
	// require as few changes to Yao's code as possible.  
	// Right now his edges are defined between components as Bond objects.
	// I could make an interface that requires getNode1 and getNode2 
	// methods, however, for the BNG example we have bonds between 
	// components that are also representing bonds between molecules. 
	// So, when trying to bond molecules it will look like there is no bond
	// if I search a list of Bond objects.  This may be ok....but for now
	// I'll just use the straight line.
	private ArrayList<BubbleSetNodeInterface> getRoute(BubbleSetNodeInterface item, BubbleSetNodeInterface neighbor)
	{
		ArrayList<BubbleSetNodeInterface> toReturn = new ArrayList<BubbleSetNodeInterface>();
		toReturn.add(item);
		toReturn.add(neighbor);
		return toReturn;
	}
	
	/**
	 * This method finds the optimal neighbor for a BubbleSetNodeInterface
	 * object.
	 *
	 * This is where I can do the virtual edge routing if I want. 
	 * 
	 * from the paper:
	 * minimize the function cost(j) = distance(i, j) * obstacles(i,j)
	 * where obstacles(i, j) is the count of non-set members on the direct 
	 * path between i and j
	 *
	 * @param item The item for which to find a neighbor
	 */
	private BubbleSetNodeInterface getOptimalNeighbour(BubbleSetNodeInterface item)
	{
		BubbleSetNodeInterface toReturn = null;
		
		//For now, just return the closest based on a O(n) search.
		int minDistance = Integer.MAX_VALUE;
		for(BubbleSetNodeInterface possibleNeighbor : processed)
			if(distance(item.getX(), item.getY(), possibleNeighbor.getX(), possibleNeighbor.getY()) < minDistance)
				toReturn = possibleNeighbor;
		
		return toReturn;
	}
	/**
	 * A shortcut function for finding point distance.  
	 * 
	 * @param d The x value of the first point.
	 * @param e The y value of the first point.
	 * @param f The x value of the second point.
	 * @param g The y value of the second point.
	 */
	private static double distance(double d, double e, double f, double g)
	{
		return Math.sqrt( Math.pow((d-f),2) + Math.pow((e-g),2) );
	}
	
	/** 
	 * Quicksort algorithm that sorts in ascending distance from the passed in x,y.
	 * 
	 * I could probably have gotten this for free if I would make a comparator, but
	 * for now, this will do. 
	 * 
	 * @param in2 
	 * @param x
	 * @param y
	 * @param left
	 * @param right
	 */
	public static <T extends BubbleSetNodeInterface> void quickSortByDistanceFromPoint (ArrayList<T> in2, int x, int y, int left, int right)
	{
		 if(right > left)
		 {
	         int pivotIndex = left; 
			 int pivotNewIndex = partition(in2, x, y, left, right, pivotIndex);
	         quickSortByDistanceFromPoint(in2, x, y, left, pivotNewIndex - 1);
	         quickSortByDistanceFromPoint(in2, x, y, pivotNewIndex + 1, right);
		 }
	}
	
	/**
	 * A partition function.
	 * 
	 * @param in2 The list to sort.
	 * @param x The x value of the centroid from which to find the distance.
	 * @param y The x value of the centroid from which to find the distance.
	 * @param left The index of the left point in the array (for recursion).
	 * @param right The index of the right point in the array (for recursion).
	 * @param pivotIndex The index of the pivot.  It is assumed that it is 
	 *        always on the left for now.
	 * 
	 * @return
	 */
	private static <T extends BubbleSetNodeInterface> int partition(ArrayList<T> in2, int x, int y, int left, int right, int pivotIndex)
	{
		int b = left+1;
		int c = right;
		pivotIndex = left;

		// Partition
		while(b <= c)
		{
			while(b < c && distance(in2.get(b).getX(),in2.get(b).getY(), x, y) <= distance(in2.get(pivotIndex).getX(), in2.get(pivotIndex).getY(), x, y))
				b++;
			while( b < c && distance(in2.get(c).getX(),in2.get(c).getY(), x, y) >= distance(in2.get(pivotIndex).getX(), in2.get(pivotIndex).getY(), x, y))
				c--;
		
			T temp = in2.get(b);
			in2.set(b, in2.get(c));
			in2.set(c, temp);
			
			b++;
			c--;
		}
		
		// Swap the pivot
		T temp = in2.get(0);
		in2.set(0, in2.get(b-1));
		in2.set(b-1, temp);
	
		return b-1;
	}
	
	/**/
	//TODO random place for this thing I have just noticed.  
	// the edges of the energy function may not be defined only by x and y values.  
	// it will depend on the height and width of the nodes as well
	/**/
	
	/**
	 * 
	 */
	public void drawEnergyOntoBufferedImage(BufferedImage img, int color)
	{	
		// To translate from world to energy we shift by these values, 
		// so to translate back out, we translate by the negative values.
		// Also, we don't want to try and draw outside of the image, 
		// so we take start at 0, or the start of the energy function.
		int start_i = Math.max(-insideXShift, 0);
		int start_j = Math.max(-insideYShift, 0);
		
		for(int i = start_i; i < Math.min(img.getWidth(), start_i+energyWidth);i++)
		{
			for(int j = start_j; j < Math.min(img.getHeight(), start_j+energyHeight);j++)
			{
				//System.out.println("i = "+i+" of "+Math.min(img.getWidth(), start_i+energyWidth)+
					//	", j = " + j + " of "+Math.min(img.getHeight(), start_j+energyHeight));
				if(energy[(j+insideYShift)/pixelGroupSize][(i+insideXShift)/pixelGroupSize] > 0)
					img.setRGB(i, j, color);
			}
		}
	}
	
	public void drawIsocontourOntoBufferedImage(BufferedImage img, int color)
	{
		Graphics graphics = img.getGraphics();
		ArrayList<Pair> points = MarchingSquares.returnIsoContour(energy);
		
		int[] xPoints = new int[points.size()+1];
		int[] yPoints = new int[points.size()+1];
		
		for(int i = 0; i < xPoints.length-1; i++)
		{
			xPoints[i] = points.get(i).col*pixelGroupSize-insideXShift;
			yPoints[i] = points.get(i).row*pixelGroupSize-insideYShift;
		}
		
		xPoints[xPoints.length-1] = xPoints[0];
		yPoints[xPoints.length-1] = yPoints[0];
		
		int a = (color >> 24) & (0xFF);
		int r = (color & (0x00FF0000)) >> 16;
		int g = (color & (0x0000FF00)) >> 8;
		int b = color & (0x000000FF);
		
		//System.out.println(r + " " + g + " "+ b + " " + a );
		
		graphics.setColor(new Color( r, g, b, a));
		
		graphics.drawPolygon(xPoints, yPoints, xPoints.length);
	}
	/*
	 * Accessors and Mutators
	 */

	public int getCentroidX()
	{
		return centroidX;
	}
	
	public int getCentroidY()
	{
		return centroidY;
	}

	public float[][] getEnergy()
	{
		return energy;
	}
	
	public int getEnergyWidth()
	{
		return energyWidth;
	}
	
	public int getEnergyHeight()
	{
		return energyHeight;
	}
	
	public int getPixelGroupSize()
	{
		return pixelGroupSize;
	}

	public int getUpperLeftEnergyPosX() 
	{
		return insideXShift;
	}

	public int getUpperLeftEnergyPosY() 
	{
		return insideYShift;
	}
	
	public ArrayList<Pair> getIsoContourPoints()
	{
		return isoContourPoints;
	}
	
	public double[] getIsoContourPointsDoubleArray()
	{
		double[] points = new double[isoContourPoints.size()*2];
		
		for(int i = 0; i < isoContourPoints.size(); i++)
		{
			points[2*i] = isoContourPoints.get(i).col-insideXShift + bubbleEdgeBuffer + 10 ;
			points[2*i+1] = isoContourPoints.get(i).row-insideYShift + bubbleEdgeBuffer + 5;
		}
		
		return points;
	}
}