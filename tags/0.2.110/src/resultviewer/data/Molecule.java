package resultviewer.data;

import java.util.ArrayList;

public class Molecule {
	private int id;
	private String name;
	private ArrayList<Component> components;
	
	
	/*
	 * Constructor using molecule name
	 */
	public Molecule(int id, String name) {
		this.id = id;
		this.name = name;
		this.components = new ArrayList<Component>();
	}
	
	/*
	 * Constructor using molecule name and list of components
	 */
	public Molecule(int id, String name, ArrayList<Component> components) {
		this.id = id;
		this.name = name;
		this.components = components;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ArrayList<Component> getComponents() {
		return components;
	}

	public void setComponents(ArrayList<Component> components) {
		this.components = components;
	}
	
	public void addComponent(Component c) {
		this.components.add(c);
	}	

}
