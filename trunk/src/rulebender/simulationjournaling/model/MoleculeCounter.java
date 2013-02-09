package rulebender.simulationjournaling.model;

public class MoleculeCounter {
	
	private String molecule;
	private String component;
	private int count;
	
	public MoleculeCounter() {
		setMolecule("");
		setComponent("");
		setCount(0);
	} //MoleculeCounter (constructor)
	
	public MoleculeCounter(String newMolecule, String newComponent, int newCount) {
		setMolecule(newMolecule);
		setComponent(newComponent);
		setCount(newCount);
	} //MoleculeCounter (constructor)
	
	public String getMolecule() {
		return molecule;
	} //getMolecule
	
	public String getComponent() {
		return component;
	} //getComponent
	
	public int getCount() {
		return count;
	} //getCount
	
	public void setMolecule(String newMolecule) {
		molecule = newMolecule;
	} ///setMolecule
	
	public void setComponent(String newComponent) {
		component = newComponent;
	} //setComponent
	
	public void setCount(int newCount) {
		count = newCount;
	} //setCount
	
} //MoleculeCounter (class)