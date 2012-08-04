package rulebender.editors.bngl.model.ruledata;

public class BondData 
{
		private String sourceMol;
		private String sourceComp;
		private int sourceID;
		private String sourceState;
	
		private String targetMol;
		private String targetComp;
		private int targetID;
		private String targetState;
	
	public BondData(String sourceMolIn, String sourceCompIn, int compID1, String sourceStateIn, 
					String targetMolIn, String targetCompIn, int compID2, String targetStateIn)
	{
		setSourceMol(sourceMolIn);
		setSourceComp(sourceCompIn);
		setSourceID(compID1);
		setSourceState(sourceStateIn);

		setTargetMol(targetMolIn);
		setTargetComp(targetCompIn);
		setTargetID(compID2);
		setTargetState(targetStateIn);
	}

	public String getSourceMol() {
		return sourceMol;
	}

	public void setSourceMol(String sourceMol) {
		this.sourceMol = sourceMol;
	}

	public String getSourceComp() {
		return sourceComp;
	}

	public void setSourceComp(String sourceComp) {
		this.sourceComp = sourceComp;
	}

	public String getTargetState() {
		return targetState;
	}

	public void setTargetState(String targetState) 
	{
		this.targetState = targetState;
	}

	public String getTargetComp() {
		return targetComp;
	}

	public void setTargetComp(String targetComp) {
		this.targetComp = targetComp;
	}

	public String getTargetMol() {
		return targetMol;
	}

	public void setTargetMol(String targetMol) {
		this.targetMol = targetMol;
	}

	public String getSourceState() {
		return sourceState;
	}

	public void setSourceState(String sourceState) {
		this.sourceState = sourceState;
	}

	public int getSourceID() {
		return sourceID;
	}

	public void setSourceID(int sourceID) {
		this.sourceID = sourceID;
	}

	public int getTargetID() {
		return targetID;
	}

	public void setTargetID(int targetID) {
		this.targetID = targetID;
	}
}
