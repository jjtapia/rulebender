package rulebender.editors.bngl;

import org.eclipse.ui.editors.text.TextEditor;

import rulebender.editors.bngl.BNGLConfiguration;
import rulebender.editors.bngl.BNGLDocumentProvider;

public class BNGLEditor extends TextEditor {
	
	public BNGLEditor() 
	{
		super();
		setSourceViewerConfiguration(new BNGLConfiguration());
		setDocumentProvider(new BNGLDocumentProvider());
	}

	public void dispose() 
	{
		super.dispose();
	}
	
	@Override
	public boolean isEditable()
	{
		return true;
	}
	
	@Override
	public boolean isEditorInputModifiable() {
	    return true;
	}

	@Override
	public boolean isEditorInputReadOnly() {
	    return false;
	}
}
