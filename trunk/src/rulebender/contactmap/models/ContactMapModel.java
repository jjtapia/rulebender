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

public class ContactMapModel
{
	// ArrayLists to hold the necessary CMap data.
	ArrayList<Molecule> m_molecules;
	ArrayList<Bond> m_bonds;
	ArrayList<Rule> m_rules;
	
	//private ArrayList<PotentialBond> pbonds;
	private CompartmentTable m_cmptTable;
	
	/**
	 * Default constructor.  Instantiate the data ArrayLists.
	 */
	public ContactMapModel() 
	{	
		m_cmptTable = new CompartmentTable();
		m_molecules = new ArrayList<Molecule>();
		m_bonds = new ArrayList<Bond>();
		m_rules = new ArrayList<Rule>();
	}
	
	public ArrayList<Molecule> getMolecules()
	{
		return m_molecules;
	}
	
	public ArrayList<Bond> getBonds()
	{
		return m_bonds;
	}
	
	public ArrayList<Rule> getRules()
	{
		return m_rules;
	}
	
	public CompartmentTable getCompartments() 
	{
		return m_cmptTable;
	}	
	
	public int addMolecule(Molecule m)
	{
		m_molecules.add(m);
		
		return (m_molecules.size() - 1);
	}

	public int addBond(Bond bond) 
	{	
		// Make sute that the bond is not already here.
		
		// Iterate through existing
		for(int i=0;i<m_bonds.size();i++)
		{
			// A match to the current? 
			if(m_bonds.get(i).equals(bond))
			{
				return i;
			}
		}
		
		m_bonds.add(bond);
		return (m_bonds.size() - 1);
	}

	public Rule getRuleWithExpression(String ruleExpression) 
	{
		for(Rule rule : m_rules)
		{
			if(rule.getExpression().equals(ruleExpression))
			{
				return rule;
			}
		}
		
		return null;
	}
	
	public void addCompartment(Compartment c)
	{
		m_cmptTable.addCompartment(c);
	}

	public void addRule(Rule rule) 
	{
		m_rules.add(rule);
	}
}

