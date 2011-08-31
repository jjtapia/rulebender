package rulebender.commands;

import java.io.File;

import rulebender.editors.common.PathEditorInput;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class OpenFileHandler extends AbstractHandler 
{

	/**
	 * Loads a file into an editor
	 */
    public Object execute(ExecutionEvent event) 
    {
    	// Get the file from the queryFile method which utilizes a FileDialog.
		File file= queryFile();
		
		// If the file is not null (the user didn't close the shell without
		// choosing a file).
		if (file != null) 
		{
			// Get an IEditorInput object for the File object.
			IEditorInput input = createEditorInput(file);
			
			// Get the String ID for the editor that should be used to open 
			// the file. 
			String editorId = getEditorId(file);
			
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
		
		else 
		{
			// Do Nothing most likely.
			// MessageDialog.openWarning(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), "Problem", "File is 'null'"); //$NON-NLS-1$
		}
		
		return null;
	}

    /**
     * Creates a FileDialog that asks the user to give a file.  
     * @return
     */
    private File queryFile() 
    {
    	// Create the dialog.
    	FileDialog dialog= new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.OPEN);
    	
    	// Set up the label for the Shell
    	dialog.setText("Open File");
    	
    	// Set the filters based on the file extension.
    	dialog.setFilterExtensions(new String[] {"*.bngl","*.net", "*.*"});
    	
    	// Set the labels to be used for the filters.  These are selectable
    	// in a dropdown menu for the filedialog.
    	dialog.setFilterNames(new String[] {"BNGL File","NET Files", "All Files"});
    	
    	// Open the dialog.  The return value is the String path to the selected file.
    	String path = dialog.open();
    	
    	// If the path is not null and is not empty then return it.  
    	if (path != null && path.length() > 0)
    		return new File(path);
    	
    	// Otherwise return null.
    	return null;
    }
    
    /**
     * 
     * @param file The File object that needs to be opened in an editor.
     * @return the String ID of the editor that should open the file.
     */
	private String getEditorId(File file) 
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
	private IEditorInput createEditorInput(File file) 
	{
		// Get the IPath for the file.
		IPath location= new Path(file.getAbsolutePath());
		
		// Get an instance of PathEditorInput based on the IPath 
		PathEditorInput input= new PathEditorInput(location);
		
		// Return it.  (PathEditorInput implements IEditorInput)
		return input;
	}
}
