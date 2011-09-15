package rulebender.models.contactmap;

import java.util.ArrayList;

public class MoleculePattern 
{

	private int moleIndex;
	
	private ArrayList<ComponentPattern> compPatterns;
	
	public MoleculePattern(int moleculeIndexIn)
	{
		setMoleIndex(moleculeIndexIn);
		
		compPatterns = new ArrayList<ComponentPattern>();
	}
	
	public int getMoleIndex() 
	{
		return moleIndex;
	}
	
	public void setMoleIndex(int index)
	{
		moleIndex = index;
	}
	
	public ArrayList<ComponentPattern> getComppatterns() 
	{
		return compPatterns;
	}

	public void addComponentPattern(int componentIndex, int stateIndex, int wildCard) 
	{	
		compPatterns.add(new ComponentPattern(componentIndex, stateIndex, wildCard));	
	}
}
