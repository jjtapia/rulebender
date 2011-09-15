package rulebender.models.contactmap;

public class State 
{
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
