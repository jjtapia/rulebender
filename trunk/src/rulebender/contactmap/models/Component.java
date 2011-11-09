package rulebender.contactmap.models;

import java.awt.Color;
import java.util.ArrayList;

public class Component 
{
	private String name;
	private ArrayList<State> states;
	private int uniqueID;
	
	public Component(String name)
	{
		setName(name);
		
		states = new ArrayList<State>();
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addState(State stateIn) 
	{	
		for(State state : states )
		{
			if(stateIn.getName().equals(state.getName()))
					return;
		}
		
		states.add(stateIn);
	}
	
	public void mergeStates(Component c)
	{
		ArrayList<State> incomingStates = c.getStates();
		
		if(incomingStates == null)
			return;
				
		for(State s : incomingStates)
		{
			if(!stateExists(s.getName()))
			{
				addState(s);
			}
		}
	}
	
	public boolean stateExists(String name)
	{
		for(State s : states)
		{
			if(s.getName() == name)
			{
				return true;
			}
		}
		
		return false;
	}
	
	public ArrayList<State> getStates() 
	{
		return states;
	}
	
	public void setUniqueID(int id)
	{
		uniqueID = id;
	}
	
	public int getUniqueID()
	{
		return uniqueID;
	}

	public int getStateIndex(String state1) 
	{
		// Return -1 if there is no state information
		if(state1 == null)
			return -1;
		
		// I'm ignoring the case for now where there is no state arrayslist
		// If that happens, then I have bigger problems.
		
		// Get the index if there is state information
		for(int i = 0; i < states.size(); i++)
		{
			if(states.get(i).getName().equals(state1))
				return i;
		}
		
		return -2;
	}
}
