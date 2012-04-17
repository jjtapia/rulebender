package rulebender.contactmap.models;

import java.util.ArrayList;

/**
 * This class represents the patterns of molecules and bonds that appear in a rule.
 * It is basically just a shortcut holder for the indices of the actual model
 * elements that are involved in a rule.  
 */
public class RulePattern 
{
	private ArrayList<MoleculePattern> molepatterns;
	private ArrayList<Integer> bonds;
	
	/**
	 *  constructor
	 */
	public RulePattern()
	{
		molepatterns = new ArrayList<MoleculePattern>();
		bonds = new ArrayList<Integer>();
	}
	
	/**
	 * Returns the bond arraylist.
	 * 
	 * REQUIRED for the CMapModel
	 *
	 * @return
	 */
	public ArrayList<Integer> getBonds() 
	{
		return bonds;
	}
	
	/**
	 * Returns the molepatterns arraylist
	 * 
	 * REQUIRED for the CMapModel
	 * @return
	 */
	public ArrayList<MoleculePattern> getMolepatterns() 
	{
		return molepatterns;
	}
	
	/**
	 * Adds a MoleculePattern object to the arraylist
	 * @param moleculePattern
	 */
	public void addMoleculePattern(MoleculePattern moleculePattern) 
	{
		molepatterns.add(moleculePattern);
	}
	
	public void addBondIndex(int index)
	{
		bonds.add(index);
	}
}
