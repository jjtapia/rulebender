package rulebender.contactmap.models;

/**
 * The BondAction class defines whether a bond is being created or being destroyed.
 */
public class BondAction 
{
	// The index of the bond in the Bond list for the model
	private int bondIndex;
	
	// negative means delete, positive means add
	private int action;
	
	public BondAction(int indexIn, int actionIn) 
	{
		setAction(actionIn);
		setBondIndex(indexIn);
	}
	
	public void setAction(int action) 
	{
		this.action = action;
	}
	
	public int getAction() 
	{
		return action;
	}
	
	public void setBondIndex(int bondindex) 
	{
		this.bondIndex = bondindex;
	}
	
	public int getBondIndex() 
	{
		return bondIndex;
	}
}
