package bubbleset;
import java.util.ArrayList;

public class Tester
{
	public static void main(String[] Args)
	{
		float test[][] = 
		{
			{0, 0, 0, 0, 0},
			{0, 0, 0, 0, 0},
			{0, 0, 1, 1, 0},
			{0, 0, 1, 1, 0},
			{0, 0, 0, 1, 0},
			{0, 0, 0, 0, 0}
		};
		ArrayList<Pair> result = MarchingSquares.returnIsoContour(test);
		for(int i=0; i<result.size(); i++)
			System.out.print("{"+result.get(i).row+","+result.get(i).col+"} ");
	}
}