package editor.contactmap.cdata;

import java.awt.Color;
import java.util.ArrayList;

public class Molecule {
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
