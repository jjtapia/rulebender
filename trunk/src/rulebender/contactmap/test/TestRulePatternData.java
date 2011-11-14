package rulebender.contactmap.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import rulebender.editors.bngl.model.ruledata.ComponentData;
import rulebender.editors.bngl.model.ruledata.RulePatternData;

public class TestRulePatternData 
{
	@Test
	public void TestChangeExistingComponentState()
	{
		// Create the component data
		ComponentData cd = new ComponentData("component");
		
		// create RulePatternData
		RulePatternData rpd = new RulePatternData();
		
		// add a molecule to the rulepattern data
		rpd.addMolecule("molecule");
		
		// Add the component to the molecule.
		rpd.addComponentToMolecule(cd, "molecule");
		
		rpd.setStateForComponentInMolecule("state", "component", "molecule");
		
		assertTrue(cd.getState().equals("state"));
	}
	
	//start here
}
