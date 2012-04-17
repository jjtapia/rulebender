package rulebender.contactmap.models;

/**
 * This class represents a State object in a CMapModel.  
 */
public class State 
{
	// The name of the state.
	private String name;
	
	public State(String name_in)
	{
		setName(name_in);
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	public String getName() 
	{
		return name;
	}
}
