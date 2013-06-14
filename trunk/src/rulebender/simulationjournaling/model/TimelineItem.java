package rulebender.simulationjournaling.model;

import java.util.ArrayList;

public class TimelineItem {

	private String m_name;
	private String m_parent;
	private ArrayList<String> m_simulations;
	
	public TimelineItem() {
		setName(null);
		setParent(null);
		m_simulations = new ArrayList<String>();
	} //TimelineItem (constructor)
	
	public TimelineItem(String name) {
		setName(name);
		setParent(null);
		m_simulations = new ArrayList<String>();
	} //TimelineItem (constructor)
	
	public TimelineItem(String name, String parent) {
		setName(name);
		setParent(parent);
		m_simulations = new ArrayList<String>();
	} //TimelineItem (constructor)
	
	public void setName(String name) {
		m_name = name;
	} //setName
	
	public String getName() {
		return m_name;
	} //getName
	
	public void setParent(String parent) {
		m_parent = parent;		
	} //setParent
	
	public String getParent() {
		return m_parent;
	} //getParent
	
	public void addSimulation(String sim) {
		m_simulations.add(sim);
	} //addSimulation
	
	public ArrayList<String> getSimulations() {
		return m_simulations;
	} //getSimulations
		
} //TimelineItem