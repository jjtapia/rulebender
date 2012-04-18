package resultviewer.data;

import java.util.ArrayList;

public class SComponent {
	private int id; // component id
	private String name; // component name
	private ArrayList<String> states; // list of states

	/**
	 * 
	 * @param id
	 *            component id
	 * @param name
	 *            component name
	 */
	public SComponent(int id, String name) {
		this.id = id;
		this.name = name;
		this.states = new ArrayList<String>();
	}

	/**
	 * 
	 * @param id
	 *            component id
	 * @param name
	 *            component name
	 * @param states
	 *            list of states
	 */
	public SComponent(int id, String name, ArrayList<String> states) {
		this.id = id;
		this.name = name;
		this.states = states;
	}

	/**
	 * 
	 * @return component id
	 */
	public int getId() {
		return id;
	}

	/**
	 * 
	 * @return component name
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return list of states
	 */
	public ArrayList<String> getStates() {
		return states;
	}

	/**
	 * 
	 * @param states
	 *            list of states
	 */
	public void setStates(ArrayList<String> states) {
		this.states = states;
	}

	/**
	 * Add a state to the list of states
	 * 
	 * @param state
	 *            one state
	 */
	public void addState(String state) {
		this.states.add(state);
	}
	
	/**
	 * Get state id
	 * 
	 * @param state
	 * @return state id
	 */
	public int getStateId(String state) {
		return states.indexOf(state);
	}

}
