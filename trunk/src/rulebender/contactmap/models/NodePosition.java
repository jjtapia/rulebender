package rulebender.contactmap.models;

public class NodePosition {
	private String molecule;
	private String component;
	private double x;
	private double y;
	
	public NodePosition(String molecule, String component, double newX, double newY) {
		setMolecule(molecule);
		setComponent(component);
		setX(newX);
		setY(newY);
	} //Position (constructor)
	
	public String getMolecule() {
		return molecule;
	} //getMolecule
	
	public String getComponent() {
		return component;
	} //getComponent
	
	public double getX() {
		return x;
	} //getX
	
	public double getY() {
		return y;
	} //getY
	
	public void setMolecule(String newMolecule) {
		molecule = newMolecule;
	} //setMolecule
	
	public void setComponent(String newComponent) {
		component = newComponent; 
	} //setComponent
	
	public void setX(double newX) {
		x = newX;
	} //setX
	
	public void setY(double newY) {
		y = newY;
	} //setY
	
} //position