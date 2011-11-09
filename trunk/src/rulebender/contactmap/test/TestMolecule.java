package rulebender.contactmap.test;

import static org.junit.Assert.assertTrue;
import rulebender.contactmap.models.Component;
import rulebender.contactmap.models.Molecule;

import org.junit.Test;


public class TestMolecule 
{
	
	@Test
	public void TestMergeSame()
	{
		// Build 1
		Molecule mol1 = new Molecule("mol1");
		
		mol1.addCompartment("compartment1");
		mol1.addCompartment("compartment2");
		
		mol1.addComponent(new Component("A"));
		mol1.addComponent(new Component("B"));
		
		mol1.addStateToComponent("Y", "A");
		mol1.addStateToComponent("N", "A");
		
		//Build 2
		Molecule mol2 = new Molecule("mol2");
		
		mol2.addCompartment("compartment1");
		mol2.addCompartment("compartment2");
		
		mol2.addComponent(new Component("A"));
		mol2.addComponent(new Component("B"));
		
		mol2.addStateToComponent("Y", "A");
		mol2.addStateToComponent("N", "A");
		
		// Merge
		mol1.mergeData(mol2);
		
		// Check
		assertTrue(mol1.getComponents().size() == 2);
		
		for(Component comp : mol1.getComponents())
		{
			if(comp.getName().equals("A"))
			{
				assertTrue(comp.getStates().size() == 2);
				assertTrue(comp.getStates().get(0).getName().equals("Y"));
				assertTrue(comp.getStates().get(1).getName().equals("N"));
			}
			
			if(comp.getName().equals("B"))
			{
				assertTrue(comp.getStates().size() == 0);	
			}
		}
		
		assertTrue(mol1.getCompartments().size() == 2);
		assertTrue(mol1.getCompartments().get(0).equals("compartment1"));
		assertTrue(mol1.getCompartments().get(1).equals("compartment2"));		
		
	}

	@Test
	public void TestMergeNewCompartments()
	{
		// Build 1
		Molecule mol1 = new Molecule("mol1");
		
		//Build 2
		Molecule mol2 = new Molecule("mol2");
		
		mol2.addCompartment("compartment1");
		mol2.addCompartment("compartment2");
		
		// Merge
		mol1.mergeData(mol2);
		
		assertTrue(mol1.getCompartments().size() == 2);
		assertTrue(mol1.getCompartments().get(0).equals("compartment1"));
		assertTrue(mol1.getCompartments().get(1).equals("compartment2"));
	}
	
	@Test
	public void TestMergeNewComponents()
	{
		// Build 1
		Molecule mol1 = new Molecule("mol1");
				
		//Build 2
		Molecule mol2 = new Molecule("mol2");
		mol2.addComponent(new Component("A"));
		mol2.addComponent(new Component("B"));
		
		// Merge
		mol1.mergeData(mol2);
		
		// Check
		assertTrue(mol1.getComponents().size() == 2);
		assertTrue(mol1.getComponents().get(0).getName().equals("A"));
		assertTrue(mol1.getComponents().get(1).getName().equals("B"));
	}
	
	@Test
	public void TestMergeNewStates()
	{
		// Build 1
		Molecule mol1 = new Molecule("mol1");
		mol1.addComponent(new Component("A"));
		mol1.addComponent(new Component("B"));
				
		//Build 2
		Molecule mol2 = new Molecule("mol2");
		mol2.addComponent(new Component("A"));
		mol2.addComponent(new Component("B"));
		
		mol2.addStateToComponent("Y", "A");
		
		// Merge
		mol1.mergeData(mol2);
		
		// Check
		assertTrue(mol1.getComponents().get(0).getStates().get(0).getName().equals("Y"));
	}
	
	@Test
	public void TestAddStateToComponent()
	{
		Molecule mol = new Molecule("mol");
		
		mol.addComponent(new Component("component"));
		mol.addStateToComponent("Y", "component");
		
		assertTrue(mol.getComponents().size() == 1);
		assertTrue(mol.getComponents().get(0).getStates().size() == 1);
		assertTrue(mol.getComponents().get(0).getStates().get(0).getName().equals("Y"));
	}
	
	@Test
	public void TestComponentIDAdd3()
	{
		Molecule mol = new Molecule("mol");
		mol.addComponent(new Component("A"));
		mol.addComponent(new Component("A"));
		mol.addComponent(new Component("A"));
		
		assertTrue(mol.getComponents().get(0).getUniqueID() == 0);
		assertTrue(mol.getComponents().get(1).getUniqueID() == 1);
		assertTrue(mol.getComponents().get(2).getUniqueID() == 2);
	}
	
	
}
