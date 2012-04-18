/**
 * @author Adam M. Smith 12/2009
 */

package bubbleset;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import junit.framework.TestSuite;

public class TestBubbleSetModel extends TestSuite
{
	private static int width = 15;
	private static int height = 15;

	public void setup()
	{
		
	}
	
	public static void testSort()
	{
		ArrayList<VirtualNode> inMols = new ArrayList<VirtualNode>(); 
		
		 VirtualNode mol1 = new VirtualNode(5, 5, width, height);
		 VirtualNode mol2 = new VirtualNode(90, 54, width, height);
		 VirtualNode mol3 = new VirtualNode(35, 26, width, height);
		 VirtualNode mol4 = new VirtualNode(57, 5, width, height);
		 VirtualNode mol5 = new VirtualNode(11, 80, width, height);	
		
		 inMols.add(mol1);
		 inMols.add(mol3);
		 inMols.add(mol4);
		 inMols.add(mol2);
		 inMols.add(mol5);
		
		 mol1.setName("1");
		 mol2.setName("2");
		 mol3.setName("3");
		 mol4.setName("4");
		 mol5.setName("5");

		ArrayList<VirtualNode> outMols = new ArrayList<VirtualNode>();
		 
		 VirtualNode mol6 = new VirtualNode(55, 35, width, height);
		 VirtualNode mol7 = new VirtualNode(30, 7, width, height);
		 VirtualNode mol8 = new VirtualNode(28, 26, width, height);
		 VirtualNode mol9 = new VirtualNode(9, 5, width, height);
		 VirtualNode mol10 = new VirtualNode(100, 40, width, height);
		 
		 outMols.add(mol6);
		 outMols.add(mol7);
		 outMols.add(mol8);
		 outMols.add(mol9);
		 outMols.add(mol10);
		
		 mol6.setName("6");
		 mol7.setName("7");
		 mol8.setName("8");
		 mol9.setName("9");
		 mol10.setName("10");
		
		BubbleModel model = new BubbleModel(inMols, outMols);
		//BubbleModel reverseModel = new BubbleModel(outMols, inMols);
		
		//printList(inMols);
	}
	
	private static void printList(ArrayList<VirtualNode> list)
	{
		System.out.print("[");
		for(int i = 0; i < list.size()-1; i++)
		{
			System.out.print((list.get(i)).getName() +", ");
		}
		System.out.println(list.get(list.size()-1).getName() + "]");
	}
	
	/*  // I changed the interaction so this won't work.
	private static void visEnergy()
	{

		ArrayList<VirtualNode> inMols = new ArrayList<VirtualNode>(); 
		 //VirtualNode mol1 = new VirtualNode(5, 5, width, height);
		 //VirtualNode mol2 = new VirtualNode(90, 54, width, height);
		 //VirtualNode mol3 = new VirtualNode(35, 26, width, height);
		 //VirtualNode mol4 = new VirtualNode(57, 5, width, height);
		// VirtualNode mol5 = new VirtualNode(11, 80, width, height);	
		
		VirtualNode mol1 = new VirtualNode(25, 25, width, height);
		 VirtualNode mol2 = new VirtualNode(450, 254, width, height);
		 VirtualNode mol3 = new VirtualNode(155, 106, width, height);
		 VirtualNode mol4 = new VirtualNode(257, 25, width, height);
		 VirtualNode mol5 = new VirtualNode(51, 400, width, height);	
		
		 inMols.add(mol1);
		 inMols.add(mol3);
		 inMols.add(mol4);
		 inMols.add(mol2);
		 inMols.add(mol5);
		
		 mol1.setName("1");
		 mol2.setName("2");
		 mol3.setName("3");
		 mol4.setName("4");
		 mol5.setName("5");

		ArrayList<VirtualNode> outMols = new ArrayList<VirtualNode>();
		 
		 //VirtualNode mol6 = new VirtualNode(55, 35, width, height);
		 //VirtualNode mol7 = new VirtualNode(30, 7, width, height);
		 //VirtualNode mol8 = new VirtualNode(28, 26, width, height);
		 //VirtualNode mol9 = new VirtualNode(9, 5, width, height);
		 //VirtualNode mol10 = new VirtualNode(100, 40, width, height);
		
		VirtualNode mol6 = new VirtualNode(255, 155, width, height);
		 VirtualNode mol7 = new VirtualNode(150, 35, width, height);
		 VirtualNode mol8 = new VirtualNode(108, 106, width, height);
		 VirtualNode mol9 = new VirtualNode(45, 25, width, height);
		 VirtualNode mol10 = new VirtualNode(500, 200, width, height);
		 
		 outMols.add(mol6);
		 outMols.add(mol7);
		 outMols.add(mol8);
		 outMols.add(mol9);
		 outMols.add(mol10);
		
		 mol6.setName("6");
		 mol7.setName("7");
		 mol8.setName("8");
		 mol9.setName("9");
		 mol10.setName("10");
		
		BubbleModel model = new BubbleModel(inMols, outMols);

		JFrame frame = new JFrame("Visualizing Energy");
		frame.setSize(new Dimension(500,500));

		BufferedImage image = new BufferedImage(490, 490, BufferedImage.TYPE_INT_RGB);
		
		
		float[][] energy = model.getEnergy();
		
		System.out.println("energy rows/height: "+energy.length+"\nenergy cols/width: "+energy[0].length);
		System.out.println("image rows/height: "+image.getHeight()+"\nimage cols/width: "+image.getWidth());
		
		for(int i = 0; i < image.getWidth();i++)
		{
			for(int j = 0; j < image.getHeight();j++)
			{
				//System.out.println("i="+i +"of"+ image.getWidth()+"\n"+"j="+j+"of"+image.getHeight());
				if(energy[j/model.getPixelGroupSize()][i/model.getPixelGroupSize()] > 0)
					image.setRGB(i, j, 500);
			}
		}
		
		for(int i = 0; i < energy[0].length;i++)
	//	{
		//	for(int j = 0; j < energy.length;j++)
		//	{
		//		System.out.println("i="+i +"of"+ image.getWidth()+"\n"+"j="+j+"of"+image.getHeight());
	//			if(energy[j][i] > 0)
			//		image.setRGB(i*model.getPixelGroupSize(), j*model.getPixelGroupSize(), 500);
		//	}
	//	}
		// 
		
		for(BubbleSetNodeInterface item : inMols)
		{
			for(int i = (item.getX() - item.getWidth()/2)-model.getUpperLeftEnergyPosX(); 
			i <= (item.getX() + item.getWidth()/2)-model.getUpperLeftEnergyPosX(); i++)
			{
			
				for(int j = (item.getY() - item.getHeight()/2)-model.getUpperLeftEnergyPosY();
				j <= (item.getY() + item.getHeight()/2)-model.getUpperLeftEnergyPosY(); j++)
				{
					//System.out.println("(x="+item.getX()+",y="+item.getY()+", w="+item.getWidth()+", h="+item.getHeight()+")");
					//System.out.println("(i="+i+",j="+j+")\n");
					
					if(i<image.getWidth() && j < image.getHeight() && i>=0 && j>=0 )
						image.setRGB(i, j, 3000000);
				}
				
			}
		}
		
		for(BubbleSetNodeInterface item : outMols)
		{
			for(int i = (item.getX() - item.getWidth()/2)-model.getUpperLeftEnergyPosX(); 
			i <= (item.getX() + item.getWidth()/2)-model.getUpperLeftEnergyPosX(); i++)
			{
			
				for(int j = (item.getY() - item.getHeight()/2)-model.getUpperLeftEnergyPosY();
				j <= (item.getY() + item.getHeight()/2)-model.getUpperLeftEnergyPosY(); j++)
				{
					//System.out.println("(x="+item.getX()+",y="+item.getY()+", w="+item.getWidth()+", h="+item.getHeight()+")");
					//System.out.println("(i="+i+",j="+j+")\n");
					
					if(i<image.getWidth() && j < image.getHeight() && i>=0 && j>=0 )
						image.setRGB(i, j, 20000);
				}
				
			}
		}
		
		
		ImageIcon icon = new ImageIcon(image);
        frame.setContentPane(new JLabel(icon));
	
		frame.pack();
		frame.setVisible(true);
	}
*/
	private static void drawEnergy()
	{
		
		// Create the list to be the 'in set'.
		ArrayList<VirtualNode> inMols = new ArrayList<VirtualNode>(); 
		
		 // Create the nodes. 
		 VirtualNode mol1 = new VirtualNode(25, 25, width, height);
		 VirtualNode mol2 = new VirtualNode(450, 254, width, height);
		 VirtualNode mol3 = new VirtualNode(155, 106, width, height);
		 VirtualNode mol4 = new VirtualNode(257, 25, width, height);
		 VirtualNode mol5 = new VirtualNode(51, 400, width, height);	
		
		 // Set the name of each node.
		 mol1.setName("1");
		 mol2.setName("2");
		 mol3.setName("3");
		 mol4.setName("4");
		 mol5.setName("5");

		 // Add each node to the set
		 inMols.add(mol1);
		 inMols.add(mol3);
		 inMols.add(mol4);
		 inMols.add(mol2);
		 inMols.add(mol5);
		 
		// Create the list to be the 'out set'. 
		ArrayList<VirtualNode> outMols = new ArrayList<VirtualNode>();
		 
		 // Create the nodes.
		 VirtualNode mol6 = new VirtualNode(255, 155, width, height);
		 VirtualNode mol7 = new VirtualNode(150, 35, width, height);
		 VirtualNode mol8 = new VirtualNode(108, 106, width, height);
		 VirtualNode mol9 = new VirtualNode(45, 25, width, height);
		 VirtualNode mol10 = new VirtualNode(500, 200, width, height);
		 
		 // Set the name of each node.
		 mol6.setName("6");
		 mol7.setName("7");
		 mol8.setName("8");
		 mol9.setName("9");
		 mol10.setName("10");
		
		 // Add each node to the out set.
		 outMols.add(mol6);
		 outMols.add(mol7);
		 outMols.add(mol8);
		 outMols.add(mol9);
		 outMols.add(mol10);
		
		// Create the Bubble model object with the in-set and the out-set.
		BubbleModel model = new BubbleModel(inMols, outMols);

		// Create a JFrame object to show the bubble.
		JFrame frame = new JFrame("Visualizing Energy");

		// Set the size of the JFrame.
		frame.setSize(new Dimension(500,500));

		// Create a blank image to fill most of the window.
		BufferedImage image = new BufferedImage(490, 490, BufferedImage.TYPE_INT_ARGB);
		
		// Draw the bubble on the blank image.
		// The hex numbers are for color.  It is a 32 bit color variable, 
		// 8 bits (two hex chars) for r, g, b, and alpha channels.
		
		// This draws the energy function onto the image (the fill).
		model.drawEnergyOntoBufferedImage(image, (0xFF89FFFF));
		
		// This draws the isocontour onto the image (the line).
		model.drawIsocontourOntoBufferedImage(image, 0xFFFF0000);
		
		// Draw the items of the in-set on top as simple rectangles by turning 
		// on the pixels around the shape.
		for(BubbleSetNodeInterface item : inMols)
		{
			for(int i = (int) (item.getX() - item.getWidth()/2); 
			i <= (item.getX() + item.getWidth()/2); i++)
			{
			
				for(int j = (int) (item.getY() - item.getHeight()/2);
				j <= (item.getY() + item.getHeight()/2); j++)
				{
					//System.out.println("(x="+item.getX()+",y="+item.getY()+", w="+item.getWidth()+", h="+item.getHeight()+")");
					//System.out.println("(i="+i+",j="+j+")\n");
					
					if(i<image.getWidth() && j < image.getHeight() && i>=0 && j>=0 )
						image.setRGB(i, j, 0xFF00FF00);
				}
				
			}
		}
		
		// Draw the items of the out-set.
		for(BubbleSetNodeInterface item : outMols)
		{
			for(int i = (int) (item.getX() - item.getWidth()/2); 
			i <= (item.getX() + item.getWidth()/2); i++)
			{
			
				for(int j = (int) (item.getY() - item.getHeight()/2);
				j <= (item.getY() + item.getHeight()/2); j++)
				{
					//System.out.println("(x="+item.getX()+",y="+item.getY()+", w="+item.getWidth()+", h="+item.getHeight()+")");
					//System.out.println("(i="+i+",j="+j+")\n");
					
					if(i<image.getWidth() && j < image.getHeight() && i>=0 && j>=0 )
						image.setRGB(i, j, 0xFF00FFFF);
				}
				
			}
		}
		
		// Create an ImageIcon object out of the BufferedImage.
		ImageIcon icon = new ImageIcon(image);
		
		// Set the ContentPane of the JFrame to be a JLabel containing the 
		// ImageIcon of our BufferedImage.
        frame.setContentPane(new JLabel(icon));
	
        // Pack the frame (get it ready to be displayed)
		frame.pack();
		
		// Display the frame.
		frame.setVisible(true);
	}
	public static void main(String[] args)
	{
		//testSort();
	
		// Displays a test case.
		drawEnergy();
	}
}
