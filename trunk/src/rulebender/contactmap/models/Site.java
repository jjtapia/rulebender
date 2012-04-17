package rulebender.contactmap.models;

/**
 * This class is apparently not used anymore and I cannot find any 
 * old references to it. It may be for the InfluenceGraph 
 * (not included in 2.0) so I will leave it here.
 *
 */

public class Site 
{
	private int molecule;
	private int component;
	private int state;
	
	public Site(int in1, int in2, int in3)
	{
		setMolecule(in1);
		setComponent(in2);
		setState(in3);
	}

	public void setMolecule(int molecule) {
		this.molecule = molecule;
	}

	public int getMolecule() {
		return molecule;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getState() {
		return state;
	}

	public void setComponent(int component) {
		this.component = component;
	}

	public int getComponent() {
		return component;
	}

}
