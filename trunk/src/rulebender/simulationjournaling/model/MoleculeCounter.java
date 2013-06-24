package rulebender.simulationjournaling.model;

/**
 * Class to keep track of the number of times we've seen each molecule/component pair in the current model
 * 
 * @author John
 */
public class MoleculeCounter {
	
	private String molecule;
	private String component;
	private String state;
	
	private int count;
	
	/** 
	 * Default constructor - initializes names to blank and count to 0
	 */
	public MoleculeCounter() {
		setMolecule("");
		setComponent("");
		setState("");
		setCount(0);
	} //MoleculeCounter (constructor)
	
	/**
	 * Constructor - Initializes instance variables
	 * 
	 * @param newMolecule - The name of the molecule 
	 * @param newComponent - The name of the component
	 * @param newCount - The number of times we've seen this molecule/component pair
	 */
	public MoleculeCounter(String newMolecule, String newComponent, int newCount) {
		setMolecule(newMolecule);
		setComponent(newComponent);
		setState("");
		setCount(newCount);
	} //MoleculeCounter (constructor)

	/**
	 * Bigger constructor to also initialize the state information
	 * 
	 * @param newMolecule - The name of the molecule 
	 * @param newComponent - The name of the component
	 * @param newState - The name of the state
	 * @param newCount - The number of times we've seen this molecule/component pair
	 */
	public MoleculeCounter(String newMolecule, String newComponent, String newState, int newCount) {
		setMolecule(newMolecule);
		setComponent(newComponent);
		setState(newState);
		setCount(newCount);
	} //MoleculeCounter (constructor)	
	
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
	 * Returns the name of the state
	 * 
	 * @return - The name of the state
	 */
	public String getState() {
		return state;
	} //getState
	
	/**
	 * Returns the number of times we've seen this molecule/component pair to this point
	 * 
	 * @return - The number of times we've seen this molecule/component pair
	 */
	public int getCount() {
		return count;
	} //getCount
	
	/**
	 * Sets the name of the molecule
	 * 
	 * @param newMolecule - The name of the molecule
	 */
	public void setMolecule(String newMolecule) {
		molecule = newMolecule;
	} ///setMolecule
	
	/**
	 * Sets the name of the component
	 * 
	 * @param newComponent - The name of the component
	 */
	public void setComponent(String newComponent) {
		component = newComponent;
	} //setComponent
	
	/**
	 * Sets the name of the state
	 * 
	 * @param newState - The name of the state
	 */
	public void setState(String newState) {
		state = newState;
	} //setState
	
	/**
	 * Sets the number of times we've seen this molecule/component pair to this point
	 * 
	 * @param newCount - The number of times we've seen this molecule/component pair
	 */
	public void setCount(int newCount) {
		count = newCount;
	} //setCount
	
} //MoleculeCounter (class)