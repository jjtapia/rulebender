package rulebender.models.contactmap;

public class Bond 
{
	private int molecule1;
	private int component1;
	private int state1;
	private int molecule2;
	private int component2;
	private int state2;
	private boolean CanGenerate;
	
	public Bond(int moleIndex1, int compIndex1, int stateIndex1, int moleIndex2, int compIndex2, int stateIndex2)
	{
		setMolecule1(moleIndex1);
		setComponent1(compIndex1);
		setState1(stateIndex1);
		setMolecule2(moleIndex2);
		setComponent2(compIndex2);
		setState2(stateIndex2);
		setCanGenerate(false);
	}

	public void setCanGenerate(boolean canGenerate) {
		CanGenerate = canGenerate;
	}

	public boolean isCanGenerate() {
		return CanGenerate;
	}

	public void setMolecule1(int molecule1) {
		this.molecule1 = molecule1;
	}

	public int getMolecule1() {
		return molecule1;
	}

	public void setComponent1(int component1) {
		this.component1 = component1;
	}

	public int getComponent1() {
		return component1;
	}

	public void setMolecule2(int molecule2) {
		this.molecule2 = molecule2;
	}

	public int getMolecule2() {
		return molecule2;
	}

	public void setState1(int state1)
	{
		this.state1 = state1;	
	}

	public int getState1() {
		return state1;
	}

	public void setState2(int state2) {
		this.state2 = state2;
	}

	public int getState2() {
		return state2;
	}

	public void setComponent2(int component2) {
		this.component2 = component2;
	}

	public int getComponent2() {
		return component2;
	}
}
