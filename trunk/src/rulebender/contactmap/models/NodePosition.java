package rulebender.contactmap.models;

/**
 * Class to store a node's position and label information for layout stabilization
 * 
 * @author John
 */
public class NodePosition {
	private String molecule;
	private String component;
	private String id;
	private double x;
	private double y;
	
	/**
	 * Constructor - sets all of the instance variables
	 * 
	 * @param molecule - The name of the molecule
	 * @param component - The name of the component
	 * @param id - The sequence number (number of times this molecule has been seen to this point)
	 * @param newX - X-position of the node
	 * @param newY - Y-position of the node
	 */
	public NodePosition(String molecule, String component, String id, double newX, double newY) {
		setMolecule(molecule);
		setComponent(component);
		setID(id);
		setX(newX);
		setY(newY);
	} //Position (constructor)
	
	/**
	 * Returns the name of the molecule
	 * 
	 * @return - The name of the molecule
	 */
	public String getMolecule() {
		return molecule;
	} //getMolecule
	
	/**
	 * Returns the name of the component
	 * 
	 * @return - The name of the component
	 */
	public String getComponent() {
		return component;
	} //getComponent
	
	/**
	 * Returns the ID - the number of times we've seen this node to this point
	 * 
	 * @return - The ID for the node
	 */
	public String getID() {
		return id;
	} //getID
	
	/**
	 * Returns the x-coordinate of the node
	 * 
	 * @return - The x-coordinate of the node
	 */
	public double getX() {
		return x;
	} //getX
	
	/**
	 * Returns the y-coordinate of the node
	 * 
	 * @return - The y-coordinate of the node
	 */
	public double getY() {
		return y;
	} //getY
	
	/**
	 * Sets the name of the molecule
	 * 
	 * @param newMolecule - The name of the molecule
	 */
	public void setMolecule(String newMolecule) {
		molecule = newMolecule;
	} //setMolecule
	
	/**
	 * Sets the name of the component
	 * 
	 * @param newComponent - The name of the component
	 */
	public void setComponent(String newComponent) {
		component = newComponent; 
	} //setComponent
	
	/**
	 * Sets the ID of the node
	 * 
	 * @param newID - The ID of the node
	 */
	public void setID(String newID) {
		id = newID;
	} //setID
	
	/**
	 * Sets the x-coordinate of the node
	 * 
	 * @param newX - The x-coordinate of the node
	 */
	public void setX(double newX) {
		x = newX;
	} //setX
	
	/**
	 * Sets the y-coordinate of the node
	 * 
	 * @param newY - The y-coordinate of the node
	 */
	public void setY(double newY) {
		y = newY;
	} //setY
	
} //position