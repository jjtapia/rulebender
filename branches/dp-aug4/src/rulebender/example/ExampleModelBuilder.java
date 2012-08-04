package rulebender.example;

import rulebender.contactmap.models.Molecule;
import rulebender.editors.bngl.model.BNGLModelBuilderInterface;
import rulebender.editors.bngl.model.ruledata.RuleData;

public class ExampleModelBuilder implements BNGLModelBuilderInterface 
{
	
	ExampleModel m_exampleModel;
	
	public ExampleModelBuilder()
	{
		m_exampleModel = new ExampleModel();
	}
	
	@Override
	public void foundMoleculeInSeedSpecies(Molecule molecule) 
	{
		m_exampleModel.add(molecule.getName());
	}
	
	public ExampleModel getModel()
	{
		return m_exampleModel;
	}
	
	@Override
	public void foundMoleculeType(Molecule molecule) 
	{
		m_exampleModel.add(molecule.getName());
	}


	@Override
	public void parameterFound(String id, String type, String value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundCompartment(String id, String volume, String outside) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundBondInSeedSpecies(String moleName1, String compName1,
			int compID1, String state1, String moleName2, String compName2,
			int compID2, String state2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundRule(RuleData ruleData) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundObservable(String observableID, String observableName,
			String observableType) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundObservablePattern(String observableID, String patternID) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundObservablePatternMolecule(String observableID,
			String patternID, String moleculeID, String moleculeName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundObservablePatternMoleculeComponent(String observableID,
			String patternID, String moleculeID, String componentID,
			String componentName) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundObservablePatternMoleculeComponentState(
			String observableID, String patternID, String moleculeID,
			String componentID, String componentState) {
		// TODO Auto-generated method stub
		
	}

}
