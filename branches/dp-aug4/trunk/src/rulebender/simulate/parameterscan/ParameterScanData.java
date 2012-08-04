package rulebender.simulate.parameterscan;

public class ParameterScanData 
{
	private String name;
	private float minValue, maxValue, simulationTime;
	private int pointsToScan, numTimePoints;
	private boolean logScale, steadyState;
	
	/*
	 * Empty constructor.  All data fields are set manually.
	 */
	public ParameterScanData(){}
	
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setMinValue(float minValue) {
		this.minValue = minValue;
	}
	public float getMinValue() {
		return minValue;
	}
	public void setMaxValue(float maxValue) {
		this.maxValue = maxValue;
	}
	public float getMaxValue() {
		return maxValue;
	}
	public void setSimulationTime(float simulationTime) {
		this.simulationTime = simulationTime;
	}
	public float getSimulationTime() {
		return simulationTime;
	}
	public void setNumTimePoints(int numTimePoints) {
		this.numTimePoints = numTimePoints;
	}
	public int getNumTimePoints() {
		return numTimePoints;
	}
	public void setPointsToScan(int pointsToScan) {
		this.pointsToScan = pointsToScan;
	}
	public int getPointsToScan() {
		return pointsToScan;
	}
	public void setLogScale(boolean logScale) {
		this.logScale = logScale;
	}
	public boolean isLogScale() {
		return logScale;
	}
	public void setSteadyState(boolean steadState) {
		this.steadyState = steadState;
	}
	public boolean isSteadyState() {
		return steadyState;
	}
}
