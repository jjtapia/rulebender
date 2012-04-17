package rulebender.contactmap.models;

/**
 * ComponentPatterns are for storing rule information.  Existing Components
 * that are found in a rule are represented as a ComponentPattern.
 *
 */
public class ComponentPattern 
{
	// The index into the containing Molecule's arraylist for this component.
	private int componentIndex;
	
	// The index of the state that is present in this pattern.
	private int stateIndex;
	
	// Whether or not there are wild cards.
	private int wildCards; // -1: None 0: ? 1: +
	
	/**
	 * Constructor takes the indices for the component and state, and also 
	 * whether there are wild cards.
	 * 
	 * @param componentIndexIn
	 * @param stateIndexIn
	 * @param wildCardIn
	 */
	public ComponentPattern(int componentIndexIn, int stateIndexIn, int wildCardIn)
	{
		setComponentIndex(componentIndexIn);
		setStateIndex(stateIndexIn);
		setWildCards(wildCardIn);
	}
	public void setComponentIndex(int compindex) {
		this.componentIndex = compindex;
	}
	public int getCompIndex() {
		return componentIndex;
	}
	public void setStateIndex(int stateindex) {
		this.stateIndex = stateindex;
	}
	public int getStateindex() {
		return stateIndex;
	}
	public void setWildCards(int wildcards) {
		this.wildCards = wildcards;
	}
	public int getWildcards() {
		return wildCards;
	}
}
