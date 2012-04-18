package resultviewer.data;

import java.util.ArrayList;

public class Component {
	private int id;
	private String name;
	private ArrayList<String> states;
	
	/*
	 * Constructor using component name
	 */
	public Component(int id, String name) {
		this.id = id;
		this.name = name;
		this.states = new ArrayList<String>();
	}
	
	/*
	 * Constructor using component name and list of states
	 */
	public Component(int id, String name, ArrayList<String> states) {
		this.id = id;
		this.name = name;
		this.states = states;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public ArrayList<String> getStates() {
		return states;
	}

	public void setStates(ArrayList<String> states) {
		this.states = states;
	}
	
	public void addState(String state) {
		this.states.add(state);
	}

}
