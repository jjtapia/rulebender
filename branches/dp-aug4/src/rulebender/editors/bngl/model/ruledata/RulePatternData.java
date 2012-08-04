package rulebender.editors.bngl.model.ruledata;

import java.util.ArrayList;

public class RulePatternData 
{
	private ArrayList<MoleculePatternData> molePatterns;
	private ArrayList<BondData> bonds;
	
	public RulePatternData()
	{
		molePatterns = new ArrayList<MoleculePatternData>();
		bonds = new ArrayList<BondData>();
	}

	public void addMolecule(String name) 
	{
		molePatterns.add(new MoleculePatternData(name));
	}
	
	public void addComponentToMolecule(ComponentData componentName, String moleculeName)
	{
		for(MoleculePatternData mpd : molePatterns)
		{
			if(mpd.getName().equals(moleculeName))
			{
				mpd.addComponent(componentName);
			}
		}
	}
	
	public void setStateForComponentInMolecule(String state, String component, String molecule)
	{
		for(MoleculePatternData mpd : molePatterns)
		{
			if(mpd.getName().equals(molecule))
			{
				mpd.setStateForComponent(state, component);
			}
		}
	}

	public void addbond(String sourceMolIn, String sourceCompIn, int compID1, String sourceStateIn, 
						String targetMolIn, String targetCompIn, int compID2, String targetStateIn)
	{
		bonds.add(new BondData(sourceMolIn, sourceCompIn, compID1, sourceStateIn, 
							   targetMolIn, targetCompIn, compID2, targetStateIn));
	}

	public ArrayList<MoleculePatternData> getMoleculePatterns() 
	{
		
		return molePatterns;
	}
	
}


