package rulebender.test.parser.astreader;

import static org.junit.Assert.*;

import java.io.IOException;

import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Test;

import rulebender.modelbuilders.BNGASTReader;
import rulebender.modelbuilders.ModelBuilderInterface;
import rulebender.modelbuilders.ruledata.RuleData;
import rulebender.models.contactmap.CMapModel;
import rulebender.models.contactmap.Component;
import rulebender.models.contactmap.Molecule;
import rulebender.utility.BNGParserCommands;
import bngparser.grammars.BNGGrammar.prog_return;

public class TestBNGASTReader 
{
	prog_return ast;
	
	@Before
	public void setup()
	{
		// Get the ast
		try 
		{
			//ast = BNGParserCommands.getASTForFileName("testFiles/xml/TestMoleculeTypeStates.xml");
			ast = BNGParserCommands.getASTForFileName("/Users/mr_smith22586/Documents/workspace/CMapTest/testModels/fceri_ji.bngl");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RecognitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Make sure the ast is good.
		assertTrue(ast != null);
	}
	
	@Test
	public void testMoleculeType()
	{	
		// Create the builder for the cmap
		ModelBuilderInterface mockBuilder = new ModelBuilderInterface()
		{

			public void parameterFound(String id, String type, String value) 
			{
				
			}

			public void foundCompartment(String id, String volume,	String outside) 
			{
				
			}

			public void foundMoleculeInSeedSpecies(Molecule molecule) 
			{
				// Do nothing	
			}

			public void foundBondInSeedSpecies(String moleName1,
					String compName1, int compID1, String state1,
					String moleName2, String compName2, int compID2,
					String state2)
			{
				// Do nothing
			}

			public void foundMoleculeType(Molecule molecule) 
			{
				if(molecule.getName().equals("Syk"))
				{
					assertTrue(molecule.getComponents().size() == 3);
					
					for(Component comp : molecule.getComponents())
					{
						if(comp.getName().equals("tSH2"))
						{
							assertTrue(comp.getStates().size() == 0);
						}
						
						if(comp.getName().equals("l"))
						{
							assertTrue(comp.getStates().size() == 1);
							
							assertTrue(comp.getStates().get(0).getName().equals("Y"));
						}
						
						if(comp.getName().equals("a"))
						{
							assertTrue(comp.getStates().size() == 1);
							
							assertTrue(comp.getStates().get(0).getName().equals("Y"));
						}
					}
				}
			}

			public void foundRule(RuleData ruleData) 
			{
				// TODO Auto-generated method stub
				
			}

			public void foundObservable(String observableID,
					String observableName, String observableType) {
				// TODO Auto-generated method stub
				
			}

			public void foundObservablePattern(String observableID,
					String patternID) {
				// TODO Auto-generated method stub
				
			}

			public void foundObservablePatternMolecule(String observableID,
					String patternID, String moleculeID, String moleculeName) {
				// TODO Auto-generated method stub
				
			}

			public void foundObservablePatternMoleculeComponent(
					String observableID, String patternID, String moleculeID,
					String componentID, String componentName) {
				// TODO Auto-generated method stub
				
			}

			public void foundObservablePatternMoleculeComponentState(
					String observableID, String patternID, String moleculeID,
					String componentID, String componentState) {
				// TODO Auto-generated method stub
				
			}
			
		};
		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(mockBuilder);
		// Use the reader to construct the model for the given ast.
		astReader.buildWithAST(ast);
	}
	
	@Test
	public void testSpeciesBlock()
	{
	// Create the builder for the cmap
			ModelBuilderInterface mockBuilder = new ModelBuilderInterface()
			{

				public void parameterFound(String id, String type, String value) 
				{
					
				}

				public void foundCompartment(String id, String volume,	String outside) 
				{
					
				}

				public void foundMoleculeInSeedSpecies(Molecule molecule) 
				{
					System.out.println(molecule.getName());
					if(molecule.getName().equals("Syk(tSH2,l~Y,a~Y"))
					{
						assertTrue(molecule.getComponents().size() == 3);
						
						for(Component comp : molecule.getComponents())
						{
							if(comp.getName().equals("tSH2"))
							{
								assertTrue(comp.getStates().size() == 0);
							}
							
							if(comp.getName().equals("l"))
							{
								assertTrue(comp.getStates().size() == 1);
								
								assertTrue(comp.getStates().get(0).getName().equals("Y"));
							}
							
							if(comp.getName().equals("a"))
							{
								assertTrue(comp.getStates().size() == 1);
								
								assertTrue(comp.getStates().get(0).getName().equals("Y"));
							}
						}
					}
				}

				public void foundBondInSeedSpecies(String moleName1,
						String compName1, int compID1, String state1,
						String moleName2, String compName2, int compID2,
						String state2)
				{
					// Do nothing
				}

				public void foundMoleculeType(Molecule molecule) 
				{
					
				}

				public void foundRule(RuleData ruleData) 
				{
					// TODO Auto-generated method stub
					
				}

				public void foundObservable(String observableID,
						String observableName, String observableType) {
					// TODO Auto-generated method stub
					
				}

				public void foundObservablePattern(String observableID,
						String patternID) {
					// TODO Auto-generated method stub
					
				}

				public void foundObservablePatternMolecule(String observableID,
						String patternID, String moleculeID, String moleculeName) {
					// TODO Auto-generated method stub
					
				}

				public void foundObservablePatternMoleculeComponent(
						String observableID, String patternID, String moleculeID,
						String componentID, String componentName) {
					// TODO Auto-generated method stub
					
				}

				public void foundObservablePatternMoleculeComponentState(
						String observableID, String patternID, String moleculeID,
						String componentID, String componentState) {
					// TODO Auto-generated method stub
					
				}
				
			};
			// Create the astReader and register the cmapModelBuilder
			BNGASTReader astReader = new BNGASTReader(mockBuilder);
			// Use the reader to construct the model for the given ast.
			astReader.buildWithAST(ast);
		}
	}
