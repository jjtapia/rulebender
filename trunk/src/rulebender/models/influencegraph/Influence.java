package rulebender.models.influencegraph;

public class Influence
{
	private int startrulenodeindex;
	private int endrulenodeindex;
	private int activation;//-1: None, 0: possible, 1: definite
	private int inhibition;//-1: None, 0: possible, 1: definite
	
	public Influence(int in1, int in2)
	{
		setStartrulenodeindex(in1);
		setEndrulenodeindex(in2);
		setActivation(-1);
		setInhibition(-1);
	}
	
	public void setInhibition(boolean definite)
	{
		if(definite)
			inhibition = 1;
		else if(inhibition == -1)
			inhibition = 0;
		else
			;//Do Nothing	
	}
	
	public void setActivation(boolean definite)
	{
		if(definite)
			activation = 1;
		else if(activation == -1)
			activation = 0;
		else
			;//Do Nothing	
	}
	
	boolean equals(Influence in)
	{
		if(in.getStartrulenodeindex() == this.getStartrulenodeindex() && in.getEndrulenodeindex() == this.getEndrulenodeindex())
			return true;
		return false;
	}

	public void setInhibition(int inhibition) {
		this.inhibition = inhibition;
	}

	public int getInhibition() {
		return inhibition;
	}

	public void setStartrulenodeindex(int startrulenodeindex) {
		this.startrulenodeindex = startrulenodeindex;
	}

	public int getStartrulenodeindex() {
		return startrulenodeindex;
	}

	public void setEndrulenodeindex(int endrulenodeindex) {
		this.endrulenodeindex = endrulenodeindex;
	}

	public int getEndrulenodeindex() {
		return endrulenodeindex;
	}

	public void setActivation(int activation) {
		this.activation = activation;
	}

	public int getActivation() {
		return activation;
	}
}