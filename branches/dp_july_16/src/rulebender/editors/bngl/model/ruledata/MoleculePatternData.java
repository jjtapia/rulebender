package rulebender.editors.bngl.model.ruledata;

import java.util.ArrayList;

public class MoleculePatternData 
{
	private String name;
	private ArrayList<ComponentData> components;
	
	public MoleculePatternData(String nameIn)
	{
		setName(nameIn);
		components = new ArrayList<ComponentData>();
	}
	
	public void addComponent(ComponentData cd)
	{	
		int cCount = 0;
		
		for (ComponentData c : components)
		{
			if(c.getComponent().equals(cd.getComponent()))
			{
				cCount++;
			}
		}
		
		cd.setUniqueID(cCount);
		components.add(cd);
	}
	
	public void setName(String nameIn)
	{
		name = nameIn;
	}
	
	public String getName()
	{
		return name;
	}

	public void setStateForComponent(String state, String component) 
	{
		for(ComponentData cd : components)
		{
			if(cd.getComponent().equals(component))
			{
				cd.setState(state);
			}
		}
		
	}

	public ArrayList<ComponentData> getComponentPatterns() 
	{
		return components;
	}
}
