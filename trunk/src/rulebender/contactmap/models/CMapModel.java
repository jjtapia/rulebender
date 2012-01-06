package rulebender.contactmap.models;
/**
 * CMap.java
 * 
 * This file defines the CMap class.  The ContactMap is a visualization
 * of molecules and their potential interactions.
 * 
 * @author Yao Sun - Original code.
 * @author Adam M. Smith - Additions and documentation marked with '-ams <date>' 
 */

import java.util.ArrayList;

import rulebender.contactmap.models.Bond;
import rulebender.contactmap.models.Compartment;
import rulebender.contactmap.models.CompartmentTable;
import rulebender.contactmap.models.Molecule;
import rulebender.contactmap.models.Rule;




/******************************************
 * 
 * 1. Some bugs found with duplicate components models, ex: A(a,a)->A(a!1, a!1)
 * 2. Duplicate component propagation in bonds
 *
 ******************************************/

public class CMapModel
{
	// ArrayLists to hold the necessary CMap data.
	ArrayList<Molecule> molecules;
	ArrayList<Bond> bonds;
	ArrayList<Rule> rules;
	
	//private ArrayList<PotentialBond> pbonds;
	private CompartmentTable cmptTable;
	
	// Default does nothing.
	public CMapModel() 
	{	
		cmptTable = new CompartmentTable();
		molecules = new ArrayList<Molecule>();
		bonds = new ArrayList<Bond>();
		rules = new ArrayList<Rule>();
	}
	
	public ArrayList<Molecule> getMolecules()
	{
		return molecules;
	}
	
	public ArrayList<Bond> getBonds()
	{
		return bonds;
	}
	
	public ArrayList<Rule> getRules()
	{
		return rules;
	}
	
	public CompartmentTable getCompartments() 
	{
		return cmptTable;
	}	
	
	public int addMolecule(Molecule m)
	{
		molecules.add(m);
		
		return (molecules.size() - 1);
	}

	public int addBond(Bond bond) 
	{	
		bonds.add(bond);
		
		return (bonds.size() - 1);
	}

	public Rule getRuleWithName(String substring) 
	{
		for(Rule rule : rules)
		{
			if(rule.getName().equals(substring))
			{
				return rule;
			}
		}
		
		return null;
	}
	
	public void addCompartment(Compartment c)
	{
		cmptTable.addCompartment(c);
	}

	public void addRule(Rule rule) 
	{
		rules.add(rule);
	}
}

