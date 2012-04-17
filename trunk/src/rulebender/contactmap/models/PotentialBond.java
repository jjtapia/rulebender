package rulebender.contactmap.models;

import java.util.ArrayList;

/**
 * This class is apparently not used anymore and I cannot find any 
 * old references to it.  It looks like it was a temporary structure
 * for creating bonds?   It may be for the InfluenceGraph 
 * (not included in 2.0) so I will leave it here.
 *
 */
public class PotentialBond 
{

	// The name of the PotentialBond
	private String name;
	 
	private ArrayList<Site> sites;
	
	public PotentialBond(String in1, Site in2)
	{
		setName(in1);
		getSites().add(in2);
		
		sites = new ArrayList<Site>();
	}
	public void setSites(ArrayList<Site> sites) {
		this.sites = sites;
	}
	public ArrayList<Site> getSites() {
		return sites;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
}
