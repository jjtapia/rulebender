package rulebender.modelbuilders;

import java.util.HashMap;

import rulebender.modelbuilders.ruledata.BondActionData;
import rulebender.modelbuilders.ruledata.ComponentData;
import rulebender.modelbuilders.ruledata.MoleculePatternData;
import rulebender.modelbuilders.ruledata.RuleData;
import rulebender.modelbuilders.ruledata.RulePatternData;
import rulebender.models.contactmap.CMapModel;
import rulebender.models.contactmap.Bond;
import rulebender.models.contactmap.BondAction;
import rulebender.models.contactmap.Compartment;
import rulebender.models.contactmap.Molecule;
import rulebender.models.contactmap.MoleculePattern;
import rulebender.models.contactmap.Rule;
import rulebender.models.contactmap.RulePattern;


/**
 * This class needs to construct and return a CMapModel object
 * based on the method calls that it receives from the BNGASTReader.
 * 
 *  Need to produce:
  
    ArrayList<Molecule>:
   		String expression;
		String name;
		ArrayList<Component>;
			String name;
			ArrayList<State> states;
				String name;
		ArrayList<String>; // for compartments
	
	ArrayList<Bond>
		int molecule1;  // int id is index of molecule in molecule list
		int component1;	// int id is index of component in component list for molecule
		int state1;		// int id is index of state in component list 
		int molecule2;
		int component2;
		int state2;
		boolean CanGenerate;
		
	ArrayList<Rule>
		String label;  // label of the rule, could be empty
		String name;   // expression of rule, including rates
		boolean bidirection;
		String rate1;
		String rate2;
		ArrayList<RulePattern> reactantpatterns;
			ArrayList<MoleculePattern> molepatterns;
				private int moleindex; 	// int id into the molecules arraylist
				ArrayList<ComponentPattern> comppatterns;
					int compindex; // int id into component arraylist for molecule
					int stateindex; // int id into state arraylist for component
					int wildcards; // -1: None 0: ? 1: + // I have no idea
			ArrayList<Integer> bonds; // integer id into the main bond arraylist
		ArrayList<RulePattern> productpatterns;
			ArrayList<MoleculePattern> molepatterns;
				private int moleindex; 	// int id into the molecules arraylist
				ArrayList<ComponentPattern> comppatterns;
					int compindex; // int id into component arraylist for molecule
					int stateindex; // int id into state arraylist for component
					int wildcards; // -1: None 0: ? 1: + // I have no idea
		ArrayList<BondAction> bondactions;
			int bondindex; // index into bonds arraylist
			int action;// negative means delete, positive means add
   
 * @author mr_smith22586
 *
 */
public class CMapModelBuilder implements ModelBuilderInterface 
{
	CMapModel model;
	
	HashMap<String, Integer> moleculeIDForName;
	
	public CMapModelBuilder()
	{
		model = new CMapModel(); 
		moleculeIDForName = new HashMap<String, Integer>();
	}

	public void parameterFound(String id, String type, String value)
	{
		//DEBUG
		System.out.println("Parameter:\n\tid: " + id + "\n\ttype: " + "\n\tvalue: " + value);
		
		// Ignored for contact map.
	}
	
	/**
	 * outside can be null
	 */
	public void foundCompartment(String id, String volume, String outside) 
	{	
		// Try and get the parent
		Compartment parent = model.getCompartments().getCompartment(outside);
		
		// Using the parent (which may be null), add the compartment to the table.
		model.addCompartment(new Compartment(id, parent));
	}
	

	/**
	 * This method is called from the reader when a molecule is found in the 
	 * molecule types block.  The object is ready to be added to the model as is.
	 */
	public void foundMoleculeType(Molecule molecule) 
	{
		System.out.println("Found molecule in molecule types: " + molecule.getName());
		int index = model.addMolecule(molecule);
		moleculeIDForName.put(molecule.getName(), index);
	}


	/**
	 * This method is called from the the reader when a molecule is ofund
	 * in the seed species block.  These molecules _should_ already exist,
	 * but may not.  So we need to check to see if we have it in the model, 
	 * and add it if we don't.  
	 * 
	 * If it was already there, then we need to merge the two.
	 */
	public void foundMoleculeInSeedSpecies(Molecule molecule) 
	{
		System.out.println("Found molecule in seed species: " + molecule.getName());
		
		
		Molecule existingMolecule = null;
		
		if(moleculeIDForName.get(molecule.getName()) != null)
		{
			existingMolecule = model.getMolecules().get(moleculeIDForName.get(molecule.getName()));
		}
		
		// If we have it.
		if(existingMolecule != null)
		{
			existingMolecule.mergeData(molecule);
		}
		// If we do not have it.
		else
		{
			// Add it to the model and to the lookup table
			int index = model.addMolecule(molecule);
			moleculeIDForName.put(molecule.getName(), index);
		}
	}


	/** 
	 * This method is called when a bond is found in the seed species block.  
	 * 
	 *  It needs to add a bond object to the model.
	 * 
	 */
	public void foundBondInSeedSpecies(String moleName1, String compName1, int compID1,
			String state1, String moleName2, String compName2, int compID2, String state2) 
	{
		System.out.println("Found Bond:\n\tmole1: " + moleName1 + "\n\tcomp1: " + compName1 + "\n\tcompID1: "
							+ compID1 + "\n\tmole2: " + moleName2 + "\n\tcomp2: " + compName2 + "\n\tcompID2" + 
							compID2);
		
		addBondToModel( moleName1,  compName1,  compID1,
				 state1,  moleName2,  compName2,  compID2, state2);
	}
	
	private int addBondToModel(String moleName1, String compName1, int compID1,
			String state1, String moleName2, String compName2, int compID2, String state2)
	{
		//Need ints for all of these strings
		int moleIndex1 = moleculeIDForName.get(moleName1);
		System.out.println("\tmolecule1 index: " + moleIndex1);
		int compIndex1 = model.getMolecules().get(moleIndex1).getComponentIndex(compName1, compID1);
		System.out.println("\tcomponent index: " + compIndex1);
		int stateIndex1 = (model.getMolecules().get(moleIndex1).getComponents().get(compIndex1)).getStateIndex(state1);
		
		//Need ints for all of these strings
		int moleIndex2 = moleculeIDForName.get(moleName2);
		int compIndex2 = model.getMolecules().get(moleIndex2).getComponentIndex(compName2, compID2);
		int stateIndex2 = (model.getMolecules().get(moleIndex2).getComponents().get(compIndex2)).getStateIndex(state2);
		
		return model.addBond(new Bond(moleIndex1, compIndex1, stateIndex1, 
					           moleIndex2, compIndex2, stateIndex2));
		
	}
	
	/**
	 * Called when a rule is found.  The RuleData object contains all of 
	 * the information that can be gleaned from the model.  The rest must
	 * be looked up from the existing data structures.
	 */
	public void foundRule(RuleData ruleData) 
	{
		// If it is a reverse rule
		if(ruleData.getName().contains("(Reverse)"))
		{
			// Get the rule, set it to bidirectional, and set the 2nd rate.
			Rule existingRule = model.getRuleWithName(ruleData.getName().substring(0, ruleData.getName().indexOf("(")));// Get the existing rule.
			existingRule.setBidirection(true);
			existingRule.setRate2(ruleData.getRate());
		
			//Done
			return;
		}
		
		// Create a new rule.
		Rule rule = new Rule();
		
		// Set the label of the rule.
		rule.setLabel(ruleData.getName());
		
		// Set the expression of the rule. TODO
		//rule.setName(ruleData.getExpression());
		rule.setName("EXPRESSION FOR " + ruleData.getName());
		
		// Set the rate
		rule.setRate1(ruleData.getRate());
		
		// For each RulePatternDataObject
		for(RulePatternData rpd : ruleData.getReactantPatternData())
		{
			// Create the RulePattern Object
			RulePattern rulePattern = new RulePattern();
			rule.addReactantPattern(rulePattern);
			
			// For each MoleculePatternData object
			for(MoleculePatternData mpd : rpd.getMoleculePatterns())
			{
				// Create the MoleculePattern object based on the id
				// of the molecule.
				int moleID = moleculeIDForName.get(mpd.getName());
				
				// Get a reference to the molecule itself for later use.
				Molecule mole = model.getMolecules().get(moleID);
				
				// Create a new MoleculePattern.
				MoleculePattern moleculePattern = new MoleculePattern(moleID);
				rulePattern.addMoleculePattern(moleculePattern);
				
				// For each ComponentData object.
				for(ComponentData cd : mpd.getComponentPatterns())
				{
					moleculePattern.addComponentPattern(mole.getComponentIndex(cd.getComponent(),cd.getUniqueID()),
													 mole.getComponent(cd.getComponent(), cd.getUniqueID()).getStateIndex(cd.getState()),
													 0);
													//cd.getWildCards); TODO
													
				} // done adding components to moleculepatterns
			} // done with the molecule patterns
		} // done with reactant patterns
		
		// For each RulePatternDataObject
		for(RulePatternData rpd : ruleData.getProductPatternData())
		{
			// Create the RulePattern Object
			RulePattern rulePattern = new RulePattern();
			rule.addProductPattern(rulePattern);
			
			// For each MoleculePatternData object
			for(MoleculePatternData mpd : rpd.getMoleculePatterns())
			{
				// Create the MoleculePattern object based on the id
				// of the molecule.
				int moleID = moleculeIDForName.get(mpd.getName());
				
				// Get a reference to the molecule itself for later use.
				Molecule mole = model.getMolecules().get(moleID);
				
				// Create a new MoleculePattern.
				MoleculePattern moleculePattern = new MoleculePattern(moleID);
				rulePattern.addMoleculePattern(moleculePattern);
				
				// For each ComponentData object.
				for(ComponentData cd : mpd.getComponentPatterns())
				{
					moleculePattern.addComponentPattern(mole.getComponentIndex(cd.getComponent(),cd.getUniqueID()),
													 mole.getComponent(cd.getComponent(), cd.getUniqueID()).getStateIndex(cd.getState()),
													 0);
													//cd.getWildCards); TODO
													
				} // done adding components to moleculepatterns
			} // done with the molecule patterns
		} // Done with product patterns

		
		// Bond Actions
		// The bonds that we see here will not have been seen before. 
		// All other bonds are in the seed species. 
		
		// So, we need to add the bonds, get the index, and then set the
		// index value in the rule object.
		
		//DEBUG
		System.out.println("ruleData.getBondActions is null?: " + (ruleData.getBondActions() == null));
		
		// For each bond
		for(BondActionData bad : ruleData.getBondActions())
		{
			// Add it to the model and get the index.
			int index = addBondToModel(bad.getBondData().getSourceMol(), bad.getBondData().getSourceComp(), 
									   bad.getBondData().getSourceID(), bad.getBondData().getSourceState(),
									   bad.getBondData().getTargetMol(), bad.getBondData().getTargetComp(),
									   bad.getBondData().getTargetID(), bad.getBondData().getTargetState());
		
			// Add the BondAction to the rule.
			rule.addBondAction(new BondAction(index, bad.getAction()));
		}
		
		model.addRule(rule);
	}

	public void foundObservable(String observableID, String observableName,
			String observableType) 
	{
		System.out.println("Observable:\n\tid: " + observableID + 
						   "\n\tname: " + observableName + "\n\ttype: " + 
						   observableType);
	}

	
	public void foundObservablePattern(String observableID, String patternID) 
	{
		System.out.println("ObservablePattern:\n\tobservable id: " + observableID +
						   "\n\tpattern id: " + patternID);
		
	}

	
	public void foundObservablePatternMolecule(String observableID,
			String patternID, String moleculeID, String moleculeName) 
	{
		System.out.println("ObservablePatternMolecule:\n\tobservable ID: " + observableID 
						   + "\n\tpattern id: " + patternID + "\n\tmolecule id: " +
						   moleculeID + "\n\tmolecule name: " + moleculeName);
	}

	
	public void foundObservablePatternMoleculeComponent(String observableID,
			String patternID, String moleculeID, String componentID,
			String componentName) 
	{
		System.out.println("ObservablePatternMolecule:\n\tobservable ID: " + observableID 
				   + "\n\tpattern id: " + patternID + "\n\tmolecule id: " +
				   moleculeID + "\n\tcomponent id: " + componentID + 
				   "\n\tcomponent name: " + componentName);
		
	}

	
	public void foundObservablePatternMoleculeComponentState(
			String observableID, String patternID, String moleculeID,
			String componentID, String componentState) 
	{
		System.out.println("ObservablePatternMolecule:\n\tobservable ID: " + observableID 
				   + "\n\tpattern id: " + patternID + "\n\tmolecule id: " +
				   moleculeID + "\n\tcomponent id: " + componentID + 
				   "\n\tcomponent state: " + componentState);
	
		
	}
	
	public CMapModel getCMapModel() 
	{
		return model;
	}
}
