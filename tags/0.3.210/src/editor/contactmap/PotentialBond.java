package editor.contactmap;

import java.util.ArrayList;

public class PotentialBond 
{

	String name;
	ArrayList<Site> sites = new ArrayList<Site>();
	PotentialBond(String in1, Site in2)
	{
		name = in1;
		sites.add(in2);
	}
}
