package rulebender.contactmap.models;

import java.util.ArrayList;

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
