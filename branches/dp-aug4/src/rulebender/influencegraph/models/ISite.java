package rulebender.influencegraph.models;

public class ISite
{
	private int molecule;
	private int component;
	private int state;
	
	public ISite(int in1, int in2, int in3)
	{
		setMolecule(in1);
		setComponent(in2);
		setState(in3);
	}

	public void setComponent(int component) {
		this.component = component;
	}

	public int getComponent() {
		return component;
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
}