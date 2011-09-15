package rulebender.models.contactmap;

public class ComponentPattern 
{
	private int componentIndex;
	private int stateIndex;
	private int wildCards; // -1: None 0: ? 1: +
	
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
