package rulebender.contactmap.models;

import java.util.ArrayList;

/**
 * MoleculePatterns are used to hold information about Molecules
 * that are in Rules.  
 *
 */
public class MoleculePattern 
{
	// The index to the molecule in the ContactMapModel
	private int moleIndex;
	
	// A list of ComponentPattern objects associated with this MoleculePattern.
	private ArrayList<ComponentPattern> compPatterns;
	
	/**
	 * Constructor produces the object given the index of the Molecule 
	 * in the ContactMapModel.
	 * @param moleculeIndexIn
	 */
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
