package rulebender.test.parser.astreader;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Test;

import rulebender.contactmap.models.Component;
import rulebender.contactmap.models.Molecule;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.editors.bngl.model.BNGLModelBuilderInterface;
import rulebender.editors.bngl.model.ruledata.BondActionData;
import rulebender.editors.bngl.model.ruledata.MoleculePatternData;
import rulebender.editors.bngl.model.ruledata.RuleData;
import rulebender.editors.bngl.model.ruledata.RulePatternData;
import bngparser.grammars.BNGGrammar.prog_return;

/**
 * This class tests the BNGASTReader. It should test that the reader is
 * returning the correct information with respect to the TestModel.bngl file.
 * 
 * @author mr_smith22586
 * 
 */
public class TestBNGASTReader {
	prog_return ast;

	@Before
	public void setup() {
		// Get the ast
		try {
			// ast =
			// BNGParserCommands.getASTForFileName("testFiles/xml/TestMoleculeTypeStates.xml");
			ast = BNGParserCommands
			    .getASTForFileName("/Users/mr_smith22586/Documents/workspace/rulebender/testFiles/bngl/TestModel.bngl");
			// System.out.println(ast.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RecognitionException e) {
			e.printStackTrace();
		}

		// Make sure the ast is good.
		assertTrue(ast != null);
	}

	@Test
	public void testMoleculeType() {
		// Create the builder for the cmap
		BNGLModelBuilderInterface mockBuilder = new BNGLModelBuilderInterface() {

			@Override
			public void parameterFound(String id, String type, String value) {
			}

			@Override
			public void foundCompartment(String id, String volume, String outside) {
			}

			@Override
			public void foundMoleculeInSeedSpecies(Molecule molecule) {
			}

			@Override
			public void foundBondInSeedSpecies(String moleName1, String compName1,
			    int compID1, String state1, String moleName2, String compName2,
			    int compID2, String state2) {
			}

			/**
			 * Implementation checks for presence of (some of the) molecules specific
			 * to this model.
			 */
			@Override
			public void foundMoleculeType(Molecule molecule) {
				if (molecule.getName().equals("Syk")) {
					assertTrue(molecule.getComponents().size() == 3);

					for (Component comp : molecule.getComponents()) {
						if (comp.getName().equals("tSH2")) {
							assertTrue(comp.getStates().size() == 0);
						}

						else if (comp.getName().equals("l")) {
							assertTrue(comp.getStates().size() == 1);

							assertTrue(comp.getStates().get(0).getName().equals("Y"));
						}

						else if (comp.getName().equals("a")) {
							assertTrue(comp.getStates().size() == 1);

							assertTrue(comp.getStates().get(0).getName().equals("Y"));
						}
					}
				} else if (molecule.getName().equals("Rec")) {

					assertTrue(molecule.getComponents().size() == 3);

					for (Component comp : molecule.getComponents()) {
						if (comp.getName().equals("a")) {
							assertTrue(comp.getStates().size() == 0);
						}

						else if (comp.getName().equals("b")) {
							assertTrue(comp.getStates().size() == 1);

							assertTrue(comp.getStates().get(0).getName().equals("Y"));
						}

						else if (comp.getName().equals("g")) {
							assertTrue(comp.getStates().size() == 2);

							assertTrue(comp.getStates().get(0).getName().equals("Y"));
							assertTrue(comp.getStates().get(1).getName().equals("pY"));
						}
					}
				}
			}

			@Override
			public void foundRule(RuleData ruleData) {
			}

			@Override
			public void foundObservable(String observableID, String observableName,
			    String observableType) {
			}

			@Override
			public void foundObservablePattern(String observableID, String patternID) {
			}

			@Override
			public void foundObservablePatternMolecule(String observableID,
			    String patternID, String moleculeID, String moleculeName) {
			}

			@Override
			public void foundObservablePatternMoleculeComponent(String observableID,
			    String patternID, String moleculeID, String componentID,
			    String componentName) {
			}

			@Override
			public void foundObservablePatternMoleculeComponentState(
			    String observableID, String patternID, String moleculeID,
			    String componentID, String componentState) {
			}

		};
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(mockBuilder);
		// Use the reader to construct the model for the given ast.
		astReader.buildModel(ast);
	}

	@Test
	public void testSpeciesBlock() {
		// Create the builder for the cmap
		BNGLModelBuilderInterface mockBuilder = new BNGLModelBuilderInterface() {

			@Override
			public void parameterFound(String id, String type, String value) {
			}

			@Override
			public void foundCompartment(String id, String volume, String outside) {
			}

			@Override
			public void foundMoleculeInSeedSpecies(Molecule molecule) {
				if (molecule.getName().equals("Syk(tSH2,l~Y,a~Y")) {
					assertTrue(molecule.getComponents().size() == 3);

					for (Component comp : molecule.getComponents()) {
						if (comp.getName().equals("tSH2")) {
							assertTrue(comp.getStates().size() == 0);
						}

						if (comp.getName().equals("l")) {
							assertTrue(comp.getStates().size() == 1);

							assertTrue(comp.getStates().get(0).getName().equals("Y"));
						}

						if (comp.getName().equals("a")) {
							assertTrue(comp.getStates().size() == 1);

							assertTrue(comp.getStates().get(0).getName().equals("Y"));
						}
					}
				}
			}

			@Override
			public void foundBondInSeedSpecies(String moleName1, String compName1,
			    int compID1, String state1, String moleName2, String compName2,
			    int compID2, String state2) {
			}

			@Override
			public void foundMoleculeType(Molecule molecule) {
			}

			@Override
			public void foundRule(RuleData ruleData) {
			}

			@Override
			public void foundObservable(String observableID, String observableName,
			    String observableType) {
			}

			@Override
			public void foundObservablePattern(String observableID, String patternID) {
			}

			@Override
			public void foundObservablePatternMolecule(String observableID,
			    String patternID, String moleculeID, String moleculeName) {
			}

			@Override
			public void foundObservablePatternMoleculeComponent(String observableID,
			    String patternID, String moleculeID, String componentID,
			    String componentName) {
			}

			@Override
			public void foundObservablePatternMoleculeComponentState(
			    String observableID, String patternID, String moleculeID,
			    String componentID, String componentState) {
			}

		};
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(mockBuilder);
		// Use the reader to construct the model for the given ast.
		astReader.buildModel(ast);
	}

	@Test
	public void testRuleStates() {
		// Create the builder for the cmap
		BNGLModelBuilderInterface mockBuilder = new BNGLModelBuilderInterface() {

			@Override
			public void parameterFound(String id, String type, String value) {
			}

			@Override
			public void foundCompartment(String id, String volume, String outside) {
			}

			@Override
			public void foundMoleculeInSeedSpecies(Molecule molecule) {
			}

			@Override
			public void foundBondInSeedSpecies(String moleName1, String compName1,
			    int compID1, String state1, String moleName2, String compName2,
			    int compID2, String state2) {
			}

			@Override
			public void foundMoleculeType(Molecule molecule) {
			}

			@Override
			public void foundRule(RuleData ruleData) {
				// For the rule with the name "SuperR1"
				if (ruleData.getLabel().equals("SuperR1")) {
					/* ----- Reactant Patterns ----- */

					// There is a Rec and Lig Molecule and they are not bound.
					// So, they are each a pattern.
					assertTrue(ruleData.getReactantPatternData().size() == 2);

					// For each of the patterns.
					for (RulePatternData rpd : ruleData.getReactantPatternData()) {
						// Should be two patterns, each having 1 molecule.
						assertTrue(rpd.getMoleculePatterns().size() == 1);

						// For each of the molecules in the pattern (just 1)
						for (MoleculePatternData mpd : rpd.getMoleculePatterns()) {
							// If it's the Rec pattern, then the molecule is Rec
							if (mpd.getName().equals("Rec")) {
								// It has 1 component
								assertTrue(mpd.getComponentPatterns().size() == 1);

								// The component is "a" and has no state information
								assertTrue(mpd.getComponentPatterns().get(0).getComponent()
								    .equals("a"));
								assertTrue(mpd.getComponentPatterns().get(0).getState() == null);
							}

							// If it's the Lig pattern, then the molecule is Lig
							else if (mpd.getName().equals("Lig")) {
								// It has 2 components
								assertTrue(mpd.getComponentPatterns().size() == 2);

								// They are both "l" and have no state information
								assertTrue(mpd.getComponentPatterns().get(0).getComponent()
								    .equals("l"));
								assertTrue(mpd.getComponentPatterns().get(0).getState() == null);

								assertTrue(mpd.getComponentPatterns().get(0).getComponent()
								    .equals("l"));
								assertTrue(mpd.getComponentPatterns().get(1).getState() == null);
							}
						}

						/* ----- Reactant Bonds ----- */
					}

					/*---Product Patterns---*/

					// just 1 product pattern since there is a bond.
					assertTrue(ruleData.getProductPatternData().size() == 1);
					assertTrue(ruleData.getProductPatternData().get(0)
					    .getMoleculePatterns().size() == 2);

					// For each produce molecule pattern
					for (MoleculePatternData mpd : ruleData.getProductPatternData()
					    .get(0).getMoleculePatterns()) {
						// If it's the Rec pattern, then the molecule is Rec
						if (mpd.getName().equals("Rec")) {
							// It has 1 component
							assertTrue(mpd.getComponentPatterns().size() == 1);

							// The component is "a" and has no state information
							assertTrue(mpd.getComponentPatterns().get(0).getComponent()
							    .equals("a"));
							assertTrue(mpd.getComponentPatterns().get(0).getState() == null);
						}

						// If it's the Lig pattern, then the molecule is Lig
						else if (mpd.getName().equals("Lig")) {
							// It has 2 components
							assertTrue(mpd.getComponentPatterns().size() == 2);

							// They are both "l" and have no state information
							assertTrue(mpd.getComponentPatterns().get(0).getComponent()
							    .equals("l"));
							assertTrue(mpd.getComponentPatterns().get(0).getState() == null);

							assertTrue(mpd.getComponentPatterns().get(0).getComponent()
							    .equals("l"));
							assertTrue(mpd.getComponentPatterns().get(1).getState() == null);
						}
					}

					/* ----- Product bonds ----- */

					/*----- Bond Actions -----*/
					assertTrue(ruleData.getBondActions().size() == 1);

					BondActionData bad = ruleData.getBondActions().get(0);

					// Should add a bond.
					assertTrue(bad.getAction() == "AddBond");

					assertTrue(bad.getBondData().getSourceMol().equals("Lig"));
					assertTrue(bad.getBondData().getSourceComp().equals("l"));
					assertTrue(bad.getBondData().getSourceState() == null);

					assertTrue(bad.getBondData().getTargetMol().equals("Rec"));
					assertTrue(bad.getBondData().getTargetComp().equals("a"));
					assertTrue(bad.getBondData().getTargetState() == null);
				}

				// For the rule with the name "R3"
				if (ruleData.getLabel().equals("R3")) {
					/* ----- Reactant Patterns ----- */

					// There is a Rec and Lig Molecule and they are not bound.
					// So, they are each a pattern.
					assertTrue(ruleData.getReactantPatternData().size() == 2);

					// For each of the reactant patterns.
					for (int i = 0; i < ruleData.getReactantPatternData().size(); i++) {
						RulePatternData rpd = ruleData.getReactantPatternData().get(i);

						if (i == 0) {
							// The first is the pattern for rec
							assertTrue(rpd.getMoleculePatterns().size() == 1);

							MoleculePatternData mpd = rpd.getMoleculePatterns().get(0);

							assertTrue(mpd.getName().equals("Rec"));

							// It has 1 component
							assertTrue(mpd.getComponentPatterns().size() == 1);

							// The component is "b" and is in the "Y" state
							assertTrue(mpd.getComponentPatterns().get(0).getComponent()
							    .equals("b"));
							assertTrue(mpd.getComponentPatterns().get(0).getState()
							    .equals("Y"));

							/* ----- Reactant Bonds ----- */
						}
						if (i == 1) {
							// The second is the pattern for Lyn
							assertTrue(rpd.getMoleculePatterns().size() == 1);

							MoleculePatternData mpd = rpd.getMoleculePatterns().get(0);

							assertTrue(mpd.getName().equals("Lyn"));

							// It has 2 components
							assertTrue(mpd.getComponentPatterns().size() == 2);

							// The component is "b" and is in the "Y" state
							assertTrue(mpd.getComponentPatterns().get(0).getComponent()
							    .equals("U"));
							assertTrue(mpd.getComponentPatterns().get(0).getState() == null);
							assertTrue(mpd.getComponentPatterns().get(1).getComponent()
							    .equals("SH2"));
							assertTrue(mpd.getComponentPatterns().get(1).getState() == null);
						}
					}

					/* ----- Product Pattern ----- */

					// just 1 product pattern since there is a bond.
					assertTrue(ruleData.getProductPatternData().size() == 1);
					assertTrue(ruleData.getProductPatternData().get(0)
					    .getMoleculePatterns().size() == 2);

					// For each product molecule pattern
					for (MoleculePatternData mpd : ruleData.getProductPatternData()
					    .get(0).getMoleculePatterns()) {
						// If it's the Rec pattern, then the molecule is Rec
						if (mpd.getName().equals("Rec")) {
							// It has 1 component
							assertTrue(mpd.getComponentPatterns().size() == 1);

							// The component is "a" and has no state information
							assertTrue(mpd.getComponentPatterns().get(0).getComponent()
							    .equals("b"));
							assertTrue(mpd.getComponentPatterns().get(0).getState()
							    .equals("Y"));
						}

						// If it's the Lyn pattern, then the molecule is Lyn
						else if (mpd.getName().equals("Lyn")) {
							// It has 2 components
							assertTrue(mpd.getComponentPatterns().size() == 2);

							// They are both "l" and have no state information
							assertTrue(mpd.getComponentPatterns().get(0).getComponent()
							    .equals("U"));
							assertTrue(mpd.getComponentPatterns().get(0).getState() == null);

							assertTrue(mpd.getComponentPatterns().get(1).getComponent()
							    .equals("SH2"));
							assertTrue(mpd.getComponentPatterns().get(1).getState() == null);
						}
					}

					/* ----- Product bonds ----- */

					/*----- Bond Actions -----*/
					assertTrue(ruleData.getBondActions().size() == 1);

					BondActionData bad = ruleData.getBondActions().get(0);

					// Should add a bond.
					assertTrue(bad.getAction() == "AddBond");

					assertTrue(bad.getBondData().getSourceMol().equals("Lyn"));
					assertTrue(bad.getBondData().getSourceComp().equals("U"));
					assertTrue(bad.getBondData().getSourceState() == null);

					assertTrue(bad.getBondData().getTargetMol().equals("Rec"));
					assertTrue(bad.getBondData().getTargetComp().equals("b"));
					assertTrue(bad.getBondData().getTargetState().equals("Y"));
				}

			}

			@Override
			public void foundObservable(String observableID, String observableName,
			    String observableType) {
			}

			@Override
			public void foundObservablePattern(String observableID, String patternID) {
			}

			@Override
			public void foundObservablePatternMolecule(String observableID,
			    String patternID, String moleculeID, String moleculeName) {
			}

			@Override
			public void foundObservablePatternMoleculeComponent(String observableID,
			    String patternID, String moleculeID, String componentID,
			    String componentName) {
			}

			@Override
			public void foundObservablePatternMoleculeComponentState(
			    String observableID, String patternID, String moleculeID,
			    String componentID, String componentState) {
			}

		};
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(mockBuilder);
		// Use the reader to construct the model for the given ast.
		astReader.buildModel(ast);
	}
}
