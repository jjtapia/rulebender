/**
 * @author Yao Sun
 */

package bubbleset;

import java.util.ArrayList;

public class MarchingSquares
{
	private static ArrayList<Pair> contour = new ArrayList<Pair>();
	
	private static int[][] energymatrix;
	private static int rowlimit;
	private static int collimit;
	
	public static ArrayList<Pair> returnIsoContour(float[][] in)
	{
		contour.clear();
		if(in.length == 0)
			return contour;
		generateEnergyMatrix(in);
		Grid tempgrid = null;
		boolean startfound = false;
		for(int i=0; i<rowlimit; i++)
		{
			for(int j=0; j<collimit; j++)
			{
				tempgrid = new Grid(new Pair(i,j),0);
				if(tempgrid.value != 0)
				{
					contour.add(tempgrid.position);
					startfound = true;
					break;
				}
			}
			if(startfound)
				break;
		}
		if(contour.size() == 0)
			return contour;
		
		tempgrid = new Grid(tempgrid.returnNextPair(), tempgrid.returnNextDir());
		
		while(!tempgrid.position.equals(contour.get(0)))
		{
			contour.add(tempgrid.position);
			tempgrid = new Grid(tempgrid.returnNextPair(), tempgrid.returnNextDir());
		}
		contour.add(contour.get(0));
		return contour;
	}
	
	public static void generateEnergyMatrix(float[][] in)
	{
		energymatrix = new int[in.length][in[0].length];
		rowlimit = in.length;
		collimit = in[0].length;
		for(int i=0;i<in.length;i++)
			for(int j=0;j<in[0].length;j++)
				if(in[i][j]>0)
					energymatrix[i][j] = 1;
				else
					energymatrix[i][j] = 0;
	}
	
	private static class Grid
	{
		int value;
		Pair position;
		Pair nextposition;
		int nextdir;//0:u 1:d 2:l 3:r
		Grid(Pair in, int indir)
		{
			position = in;
			value = 0;
			if(valid(position.row-1,position.col-1) && energymatrix[position.row-1][position.col-1] == 1)
				value = value + 8;
			if(valid(position.row-1,position.col) && energymatrix[position.row-1][position.col] == 1)
				value = value + 4;
			if(valid(position.row,position.col-1) && energymatrix[position.row][position.col-1] == 1)
				value = value + 2;
			if(valid(position.row,position.col) && energymatrix[position.row][position.col] == 1)
				value = value + 1;
			switch(value)
			{
			case 0://Error
				break;
			case 1:
				nextposition = new Pair(position.row+1,position.col);
				nextdir = 1;
				break;
			case 2:
				nextposition = new Pair(position.row,position.col-1);
				nextdir = 2;
				break;
			case 3:
				nextposition = new Pair(position.row,position.col-1);
				nextdir = 2;
				break;
			case 4:
				nextposition = new Pair(position.row,position.col+1);
				nextdir = 3;
				break;
			case 5:
				nextposition = new Pair(position.row+1,position.col);
				nextdir = 1;
				break;
			case 6://?
				if(indir == 1)
				{
					nextposition = new Pair(position.row,position.col-1);
					nextdir = 2;
				}
				else if(indir == 0)
				{
					nextposition = new Pair(position.row,position.col+1);
					nextdir = 3;
				}
				else
					;//Error
				break;
			case 7:
				nextposition = new Pair(position.row,position.col-1);
				nextdir = 2;
				break;
			case 8:
				nextposition = new Pair(position.row-1,position.col);
				nextdir = 0;
				break;
			case 9://?
				if(indir == 3)
				{
					nextposition = new Pair(position.row+1,position.col);
					nextdir = 1;
				}
				else if(indir == 2)
				{
					nextposition = new Pair(position.row-1,position.col);
					nextdir = 0;
				}
				else
					;//Error
				break;
			case 10:
				nextposition = new Pair(position.row-1,position.col);
				nextdir = 0;
				break;
			case 11:
				nextposition = new Pair(position.row-1,position.col);
				nextdir = 0;
				break;
			case 12:
				nextposition = new Pair(position.row,position.col+1);
				nextdir = 3;
				break;
			case 13:
				nextposition = new Pair(position.row+1,position.col);
				nextdir = 1;
				break;
			case 14:
				nextposition = new Pair(position.row,position.col+1);
				nextdir = 3;
				break;
			case 15://Error
				break;
			}
		}
		
		boolean valid(int in1, int in2)
		{
			if(in1 < 0 || in1 >= rowlimit )
				return false;
			if(in2 < 0 || in2 >= collimit)
				return false;
			return true;
		}
		
		Pair returnNextPair()
		{
			return nextposition;
		}
		
		int returnNextDir()
		{
			return nextdir;
		}
	}
}

class Pair
{
	int row;
	int col;
	Pair(int in1, int in2)
	{
		row = in1;
		col = in2;
	}
	
	boolean equals(Pair in)
	{
		if(in.row == row && in.col == col)
			return true;
		return false;
	}
}