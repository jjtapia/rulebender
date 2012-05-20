package editor.contactmap.cdata;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CompartmentTable {
	private Compartment root;
	private Hashtable compartments;
	private List cmptList;
	
	public CompartmentTable() {
		root = null;
		compartments = new Hashtable<String, Compartment>();
	}
	
	public Compartment getRoot() {
		return root;
	}
	public void setRoot(Compartment root) {
		this.root = root;
	}
	
	public void addCompartment(Compartment compartment) {		
		compartments.put(compartment.getName(), compartment);
		if (compartment.getParent() == null) {
			this.root = compartment;
		}
	}
	
	public Compartment getCompartment(String name) {
		return (Compartment)compartments.get(name);
	}
	
	public List getCompartmentsList(boolean update) {
		if (update == true && cmptList != null) {
			return cmptList;
		} else {
			
			if (root == null) {
				return null;
			}
			
			// update cmptList
			// width first search
			cmptList = new ArrayList<Compartment>();
			ArrayList<Compartment> children;
			Compartment cur = root;
			cmptList.add(cur);
			int i = 0;
			while (i < cmptList.size()) {
				children = (ArrayList<Compartment>) cur.getChildren();
				// add all the children
				for (Compartment child : children) {
					if (child != null)
						cmptList.add(child);
				}
				i++;
				if (i < cmptList.size()) {
					cur = (Compartment) cmptList.get(i);
				}
			}
		}
		return cmptList;
	}
	
	/**
	 * 
	 * @param cur name of current compartment
	 * @param parent name of parent compartment
	 * @return true if parent compartment is real parent for current compartment
	 */
	public boolean isChild(String cur, String parent) {
		Compartment curCmpt = (Compartment)compartments.get(cur);
		if (curCmpt == null)
			return false;
		Compartment parentCmpt = curCmpt.getParent();
		while (parentCmpt != null) {
			if (parentCmpt.getName().equals(parent)) {
				return true;
			}
			parentCmpt = parentCmpt.getParent();
		}
		return false;
	}
	
	public void print() {
		for (Enumeration e = compartments.keys(); e.hasMoreElements();) {
			Compartment cur = (Compartment) compartments.get(e.nextElement());
			System.out.print("Name: " + cur.getName());
			if (cur.getParent() != null)
				System.out.println("\tParent: " + cur.getParent().getName());
			else
				System.out.println();
		}
	}
}
