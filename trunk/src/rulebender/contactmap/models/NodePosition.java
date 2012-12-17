package rulebender.contactmap.models;

public class NodePosition {
	private String molecule;
	private String component;
	private String id;
	private double x;
	private double y;
	
	public NodePosition(String molecule, String component, String id, double newX, double newY) {
		setMolecule(molecule);
		setComponent(component);
		setID(id);
		setX(newX);
		setY(newY);
	} //Position (constructor)
	
	public String getMolecule() {
		return molecule;
	} //getMolecule
	
	public String getComponent() {
		return component;
	} //getComponent
	
	public String getID() {
		return id;
	} //getID
	
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
	
	public void setID(String newID) {
		id = newID;
	} //setID
	
	public void setX(double newX) {
		x = newX;
	} //setX
	
	public void setY(double newY) {
		y = newY;
	} //setY
	
} //position