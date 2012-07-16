package rulebender.editors.bngl;

import java.util.List;

public interface IBNGLLinkedElementCollection  
{
	/**
	 * This was created so that views that have visualizations that are 
	 * associated with a specific open bngl file can indicate which file
	 * they are linked with.
	 * @return
	 */
	public String getLinkedBNGLPath();
	
	
	/**
	 * This must return the regular expression that will be searched
	 * in the document.  
	 * @return
	 */
	public List<IBNGLLinkedElement> getCollection();
}
