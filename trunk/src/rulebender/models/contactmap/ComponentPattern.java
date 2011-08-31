package rulebender.models.contactmap;

public class ComponentPattern 
{
	private int compindex;
	private int stateindex;
	private int wildcards; // -1: None 0: ? 1: +
	public ComponentPattern()
	{
		setStateindex(-1);
		setWildcards(-1);
	}
	public void setCompindex(int compindex) {
		this.compindex = compindex;
	}
	public int getCompindex() {
		return compindex;
	}
	public void setStateindex(int stateindex) {
		this.stateindex = stateindex;
	}
	public int getStateindex() {
		return stateindex;
	}
	public void setWildcards(int wildcards) {
		this.wildcards = wildcards;
	}
	public int getWildcards() {
		return wildcards;
	}
}
