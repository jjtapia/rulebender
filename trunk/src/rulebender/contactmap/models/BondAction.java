package rulebender.contactmap.models;

public class BondAction 
{
	private int bondIndex;//start from 0
	private int action;// negative means delete, positive means add
	
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
