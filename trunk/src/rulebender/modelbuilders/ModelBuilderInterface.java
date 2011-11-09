package rulebender.modelbuilders;

import rulebender.contactmap.models.Molecule;
import rulebender.modelbuilders.ruledata.RuleData;

import org.jdom.Attribute;


/**
 * This interface is for objects that want to be used for building 
 * models based on the periodic updates from the AST Reader.
 * 
 * @author mr_smith22586
 *
 */
public interface ModelBuilderInterface {

	/**
	 * Called when a parameter is encountered while reading the parameters block.
	 * 
	 * @param id The id that the parser uses for this parameter
	 * @param type Either Constant or ??? TODO
	 * @param value the float value of the parameter
	 */
	void parameterFound(String id, String type, String value);

	
	void foundCompartment(String id, String volume,
			String outside);

	void foundMoleculeInSeedSpecies(Molecule molecule);

	void foundBondInSeedSpecies(String moleName1, String compName1,
			int compID1, String state1, String moleName2, String compName2, int compID2, String state2);


	void foundMoleculeType(Molecule molecule);

	void foundRule(RuleData ruleData);
	
	/**
	 * Called when an observable is found.
	 *  
	 * @param observableID
	 * @param observableName
	 * @param observableType can be Molecule, ...?
	 */
	void foundObservable(String observableID, String observableName,
			String observableType);

	/**
	 * Called when an observable pattern is found.
	 * @param observableID
	 * @param patternID
	 */
	void foundObservablePattern(String observableID, String patternID);
	 
	/**
	 * Called when an observable pattern molecule is found.
	 * @param observableID
	 * @param patternID
	 * @param moleculeID
	 * @param moleculeName
	 */
	void foundObservablePatternMolecule(String observableID,
			String patternID, String moleculeID,
			String moleculeName);

	
	void foundObservablePatternMoleculeComponent(String observableID,
			String patternID, String moleculeID,
			String componentID, String componentName);

	void foundObservablePatternMoleculeComponentState(String observableID,
			String patternID, String moleculeID,
			String componentID, String componentState);
}
