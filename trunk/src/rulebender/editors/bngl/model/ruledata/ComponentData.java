package rulebender.editors.bngl.model.ruledata;

public class ComponentData 
{
	private String component;
	private String state = null;
	private int uniqueID;
	
	public ComponentData(String componentNameIn) 
	{
		setComponent(componentNameIn);
	}

	public String getComponent() {
		return component;
	}

	public void setComponent(String component) {
		this.component = component;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
	
	public void setUniqueID(int id)
	{
		uniqueID = id;
	}
	
	public int getUniqueID()
	{
		return uniqueID;
	}
}
