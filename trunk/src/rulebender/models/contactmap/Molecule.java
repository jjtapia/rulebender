package rulebender.models.contactmap;

import java.util.ArrayList;

public class Molecule
{
	private String expression;
	private boolean expComplete = false;
	private String name;
	private ArrayList<Component> components;
	private ArrayList<String> compartments;
	
	public Molecule(String name_in)
	{
		setName(name_in);
		
		components = new ArrayList<Component>();
		compartments = new ArrayList<String>();
	}
	
	/**
	 * The expression set at the very beginning may be not complete.
	 * But it should be complete before VisualItem tries to get it.
	 * So reset it when the first time get called.
	 * @return complete expression of molecule
	 */
	public String getExpression() 
	{
		if (expComplete == false) {
			expression = "";
			expression += name + "(";
			
			// no component
			if (components.size() == 0) {
				expression += ")";
			}
			
			// each component
			for (int i = 0; i < components.size(); i++) {
				Component curC = components.get(i);
				// component name
				expression += curC.getName();
				
				ArrayList<State> states = curC.getStates();
				// each state
				for (int j = 0; j < states.size(); j++) {
					State curS = states.get(j);
					// state name
					expression += "~" + curS.getName();
				}
				
				if (i == components.size() - 1) {
					expression += ")";
				}
				else {
					expression += ",";
				}
			}
			expComplete = true;
		}
		return expression;
	}
	
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	
	public int addComponent(Component comp) 
	{	
		int cCount = 0;
		
		for(Component c : components)
		{
			if(c.getName().equals(comp.getName()))
			{
				cCount++;
			}
		}
		
		comp.setUniqueID(cCount);
		
		components.add(comp);
			
		return cCount;
	}
	
	/**
	 * Returns a component object for a name string.
	 * @param name
	 */
	public Component getComponent(String name, int id)
	{
		
		for(Component c : components)
		{
			if(c.getName().equals(name) && c.getUniqueID() == id)
				return c;
		}
		
		return null;
	}
	
	/**
	 * Given a state and a component string, add the state to the component.
	 * @param state
	 * @param component
	 */
	public void addStateToComponent(String state, String component)
	{
		for(Component c : components)
		{
			if(c.getName().equals(component))
			{ 
				c.addState(new State(state));
			}
		}
	}
		
	public ArrayList<Component> getComponents() 
	{
		return components;
	}
	
	public void addCompartment(String compartment) 
	{
		compartments.add(compartment);
	}
	
	public ArrayList<String> getCompartments() 
	{
		return compartments;
	}
	
	public String getFirstCompartment() 
	{
		if (compartments.size() >= 1) 
		{
			return compartments.get(0);
		}
		
		return null;
	}

	public void mergeData(Molecule molecule) 
	{
		// For each new component
		for(Component component : molecule.getComponents())
		{
			Component existingComponent = getComponent(component.getName(), component.getUniqueID());
					
			// If the component is not already in the molecule
			if(existingComponent == null)
			{
				// Just add it
				addComponent(component);
			}
			// If it is already in the molecule, then we have to check the states
			else
			{
				existingComponent.mergeStates(component);
			}	
		}
		
		// For each compartment
		for(String compartment  : molecule.getCompartments())
		{
			if(!compartments.contains(compartment))
			{
				compartments.add(compartment);
			}
		}
	}

	public int getComponentIndex(String compName1, int compID1) 
	{
		for(int i = 0; i < components.size(); i++)
		{
			if(components.get(i).getName().equals(compName1) && components.get(i).getUniqueID() == compID1)
			{
				return i;
			}
		}
		
		return -1;
	}
}
