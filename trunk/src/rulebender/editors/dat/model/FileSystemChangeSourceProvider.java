/**
 * This is not used.  It is an example of a source provider for a state.
 */
package rulebender.editors.dat.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;

public class FileSystemChangeSourceProvider extends AbstractSourceProvider 
{
	public final static String FILE_SYSTEM_UPDATE = "rulebender.navigator.model.FileSystemDirty";
	public final static String FS_DIRTY = "dirty";
	public final static String FS_CLEAN = "clean";

	public boolean m_dirty = true;
	
	@Override
	public void dispose() 
	{
		// TODO Auto-generated method stub	
	}

	@Override
	public Map<String, String> getCurrentState() 
	{
		Map<String, String> map = new HashMap<String, String>(1);
		String value = (m_dirty? FS_DIRTY : FS_CLEAN);
		map.put(FILE_SYSTEM_UPDATE, value);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() 
	{
		return new String[] {FILE_SYSTEM_UPDATE};
	}
	
	
	public void setDirty(boolean dirty)
	{
		m_dirty = dirty;
	}
	
}
