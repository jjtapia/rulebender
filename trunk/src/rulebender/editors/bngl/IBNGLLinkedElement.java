package rulebender.editors.bngl;

public interface IBNGLLinkedElement 
{
	/**
	 * This was created so that views that have visualizations that are 
	 * associated with a specific open bngl file can indicate which file
	 * they are linked with.
	 * @return
	 */
	public String getLinkedBNGLPath();
}
