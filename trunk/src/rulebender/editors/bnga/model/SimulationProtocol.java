package rulebender.editors.bnga.model;

import java.util.ArrayList;


public class SimulationProtocol<T extends ISimulationAction> 
{
	private ArrayList<T> m_setupActions;
	private ArrayList<T> m_simulationActions;
	private int m_repetitions;
	
	public SimulationProtocol()
	{
		m_setupActions = new ArrayList<T>();
		m_simulationActions = new ArrayList<T>();
	}
	
	public void setRepetitions(int reps)
	{
		m_repetitions = reps;
	}
	
	public void addSetupAction(T action)
	{
		m_setupActions.add(action);
	}
	
	public void addSimulationAction(T action)
	{
		m_simulationActions.add(action);
	}	
	
	public int getRepititions()
	{
		return m_repetitions;
	}
	
	
}
