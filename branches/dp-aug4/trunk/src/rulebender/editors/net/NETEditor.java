package rulebender.editors.net;

import org.eclipse.ui.editors.text.TextEditor;

public class NETEditor extends TextEditor
{	
	public NETEditor() 
	{
		super();
		setSourceViewerConfiguration(new NETConfiguration());
		setDocumentProvider(new NETDocumentProvider());
	}

	public void dispose() {
		super.dispose();
	}
	
	@Override
	public boolean isEditable()
	{
		return false;
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
