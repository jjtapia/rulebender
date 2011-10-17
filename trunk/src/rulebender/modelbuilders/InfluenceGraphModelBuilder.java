package rulebender.modelbuilders;

import rulebender.modelbuilders.ruledata.RuleData;
import rulebender.models.contactmap.CMapModel;
import rulebender.models.contactmap.Molecule;
import rulebender.models.influencegraph.IGraphModel;

public class InfluenceGraphModelBuilder implements ModelBuilderInterface 
{
	
	private IGraphModel model;

	
	public InfluenceGraphModelBuilder()
	{
		//model = new IGraphModel();
	}
	
	public void parameterFound(String id, String type, String value) 
	{
		// don't care
	}

	public void foundCompartment(String id, String volume, String outside) 
	{
		// don't care
	}

	public void foundMoleculeInSeedSpecies(Molecule molecule) 
	{
		// TODO Auto-generated method stub
	}

	public void foundBondInSeedSpecies(String moleName1, String compName1,
			int compID1, String state1, String moleName2, String compName2,
			int compID2, String state2) {
		// TODO Auto-generated method stub

	}

	public void foundMoleculeType(Molecule molecule) {
		// TODO Auto-generated method stub

	}

	public void foundRule(RuleData ruleData) {
		// TODO Auto-generated method stub

	}

	public void foundObservable(String observableID, String observableName,
			String observableType) {
		// TODO Auto-generated method stub

	}

	public void foundObservablePattern(String observableID, String patternID) {
		// TODO Auto-generated method stub

	}

	public void foundObservablePatternMolecule(String observableID,
			String patternID, String moleculeID, String moleculeName) {
		// TODO Auto-generated method stub

	}

	public void foundObservablePatternMoleculeComponent(String observableID,
			String patternID, String moleculeID, String componentID,
			String componentName) {
		// TODO Auto-generated method stub

	}

	public void foundObservablePatternMoleculeComponentState(
			String observableID, String patternID, String moleculeID,
			String componentID, String componentState) {
		// TODO Auto-generated method stub

	}

	public IGraphModel getIGraphModel() 
	{
		// TODO Auto-generated method stub
		return model;
	}

}
