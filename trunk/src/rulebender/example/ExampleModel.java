package rulebender.example;

import java.util.ArrayList;

public class ExampleModel 
{
	private ArrayList<String> m_moleculeList;
	
	public ExampleModel()
	{
		m_moleculeList = new ArrayList<String>();
	}
	
	public void add(String mol)
	{
		m_moleculeList.add(mol);
	}
	
	public String toString()
	{
		String toReturn = "Molecules:\n";
				
		for(String s : m_moleculeList)
			toReturn += "\t" + s + "\n";
				
		return toReturn;
	}
}
