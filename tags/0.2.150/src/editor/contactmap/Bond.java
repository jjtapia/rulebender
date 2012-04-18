package editor.contactmap;

import java.awt.Color;
import java.util.ArrayList;

public class Bond 
{
	int molecule1;
	int component1;
	int state1;
	int molecule2;
	int component2;
	int state2;
	boolean CanGenerate;
	
	Color color = null;
	ArrayList<Point> position = new ArrayList<Point>();
	
	Bond()
	{
		molecule1 = -1;
		component1 = -1;
		state1 = -1;
		molecule2 = -1;
		component2 = -1;
		state2 = -1;
		CanGenerate = false;
	}
}
