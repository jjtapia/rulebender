package editor.contactmap.cdata;

import java.awt.Color;
import java.util.ArrayList;

public class Molecule {
	private String expression;
	private boolean expComplete = false;
	private String name;
	private ArrayList<Component> components = new ArrayList<Component>();
	private ArrayList<String> compartments = new ArrayList<String>();
	
	int x;
	int y;
	int width;
	int height;
	int textx;
	int texty;
	Color color = null;
	
	
	/**
	 * The expression set at the very beginning may be not complete.
	 * But it should be complete before VisualItem tries to get it.
	 * So reset it when the first time get called.
	 * @return complete expression of molecule
	 */
	public String getExpression() {
		if (expComplete == false) {
			expression = "";
			expression += name + "(";
			
			// no component
			if (components.size() == 0) {
				expression += ")";
			}
			
			// each component
			for (int i = 0; i < components.size(); i++) {
				Component curC = components.get(i);
				// component name
				expression += curC.getName();
				
				ArrayList<State> states = curC.getStates();
				// each state
				for (int j = 0; j < states.size(); j++) {
					State curS = states.get(j);
					// state name
					expression += "~" + curS.getName();
				}
				
				if (i == components.size() - 1) {
					expression += ")";
				}
				else {
					expression += ",";
				}
			}
			expComplete = true;
		}
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setComponents(ArrayList<Component> components) {
		this.components = components;
	}
	public ArrayList<Component> getComponents() {
		return components;
	}
	
	public void addCompartment(String compartment) {
		this.compartments.add(compartment);
	}
	
	public ArrayList<String> getCompartments() {
		return compartments;
	}
	
	public String getFirstCompartment() {
		if (compartments.size() >= 1) {
			return compartments.get(0);
		}
		
		return null;
	}
}
