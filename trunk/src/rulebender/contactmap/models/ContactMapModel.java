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

/*------------------------------
 * TODO
 * 1. bugs found with duplicate components models, ex: A(a,a)->A(a!1, a!1)?
 * 2. Duplicate component propagation in bonds
 *
 ******************************************/

/**
 * This is the main model for the ContactMap. It has a list of Molecules, Bonds,
 * Rules, and a CompartmentTable for the compartments. It is built using the
 * CMapModelBuilder class which uses the builder pattern with the BNGASTReader.
 */
public class ContactMapModel {
	// This is the path for the file that is the source of the model.
	private String m_sourcePath;

	// ArrayLists to hold the necessary CMap data.
	private final ArrayList<Molecule> m_molecules;
	private final ArrayList<Bond> m_bonds;
	private final ArrayList<Rule> m_rules;

	// private ArrayList<PotentialBond> pbonds;
	private final CompartmentTable m_cmptTable;

	/**
	 * Default constructor. Instantiate the data ArrayLists.
	 */
	public ContactMapModel() {
		m_cmptTable = new CompartmentTable();
		m_molecules = new ArrayList<Molecule>();
		m_bonds = new ArrayList<Bond>();
		m_rules = new ArrayList<Rule>();
	}

	public ArrayList<Molecule> getMolecules() {
		return m_molecules;
	}

	public ArrayList<Bond> getBonds() {
		return m_bonds;
	}

	public ArrayList<Rule> getRules() {
		return m_rules;
	}

	public CompartmentTable getCompartments() {
		return m_cmptTable;
	}

	public int addMolecule(Molecule m) {
		m_molecules.add(m);

		return (m_molecules.size() - 1);
	}

	public int addBond(Bond bond) {
		// Make sure that the bond is not already here.

		// Iterate through existing
		for (int i = 0; i < m_bonds.size(); i++) {
			// A match to the current?
			if (m_bonds.get(i).equals(bond)) {
				return i;
			}
		}

		m_bonds.add(bond);
		return (m_bonds.size() - 1);
	}

	public Rule getRuleWithLabel(String ruleLabel) {
		for (Rule rule : m_rules) {
			if (rule.getLabel().equals(ruleLabel)) {
				return rule;
			}
		}

		return null;
	}

	public void addCompartment(Compartment c) {
		m_cmptTable.addCompartment(c);
	}

	public void addRule(Rule rule) {
		m_rules.add(rule);
	}

	public void setSourcePath(String sourcePath) {
		m_sourcePath = sourcePath;
	}

	public String getSourcePath() {
		return m_sourcePath;

	}
}
