package rulebender.simulate.parameterscan;

public interface ISimulationAction 
{
	/**
	 * All ISimulationAction objects will have a text representation
	 * that is recognized by BioNetGen and will be the executed
	 * instruction.
	 * @return The BNGL executable text representation of the simulation action
	 */
	public String getText();
}
