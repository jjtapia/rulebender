package rulebender.core.utility;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import rulebender.editors.common.PathEditorInput;

public class FileInputUtility
{
	 /**
	 * 
	 * @param file The File object that needs to be opened in an editor.
	 * @return the String ID of the editor that should open the file.
	 */
	public static String getEditorId(File file) 
	{
		
		// Get the editor registry (defined in plugin.xml)
		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getWorkbench().getEditorRegistry();;
		
		// Get the IEditorDescriptor object for the editor that is 
		// registered to edit the file.  (The registration in plugin.xml
		// is based on filename extension)
		IEditorDescriptor descriptor = editorRegistry.getDefaultEditor(file.getName());	
		
		// If the descriptor is not null and is not equal to the default editor...
		// (I had some trouble opening files with the default.)
		if (descriptor != null && !descriptor.getId().equals("org.eclipse.ui.DefaultTextEditor"))
		{
			// Return the suggested editor ID
			return descriptor.getId();
		}
		
		// If the descriptor is null, or the default is needed, then we 
		// use the simple editor. 
		return "rulebender.editors.simple"; //$NON-NLS-1$
	}
	
	/**
	 * Create the IEditorInput object from a File.
	 * 
	 * @param file The file for which we need an IEditorInput.
	 * @return The IEditorInput object.
	 */
	public static IEditorInput createEditorInput(File file) 
	{
		// Get the IPath for the file.
		IPath location= new Path(file.getAbsolutePath());
		
		// Get an instance of PathEditorInput based on the IPath 
		PathEditorInput input= new PathEditorInput(location);
		
		// Return it.  (PathEditorInput implements IEditorInput)
		return input;
	}
	
	public static void openFileInEditor(File file)
	{
		// Get an IEditorInput object for the File object.
		IEditorInput input = FileInputUtility.createEditorInput(file);
		
		// Get the String ID for the editor that should be used to open 
		// the file. 
		String editorId = FileInputUtility.getEditorId(file);
		
		// Get the workbench page that is active so that we can access 
		// the editor that is in it. 
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		// If the editor view is not initialized correctly then an error
		// can be thrown. 
		try 
		{
			// Open the input with the specific editor.
			page.openEditor(input, editorId);
		}
		catch (PartInitException e) 
		{
			e.printStackTrace();
		}
	}
}
