package rulebender.models.contactmap;

import java.util.ArrayList;

public class PotentialBond 
{

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
