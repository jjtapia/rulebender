package rulebender.editors.bngl;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.editors.text.TextEditor;

import rulebender.core.modelcollection.ModelCollectionController;
import rulebender.editors.bngl.BNGLConfiguration;
import rulebender.editors.bngl.BNGLDocumentProvider;

public class BNGLEditor extends TextEditor {
	
	private BNGLColorManager m_colorManager;
	
	public BNGLEditor() 
	{
		super();
		m_colorManager = new BNGLColorManager();
		setSourceViewerConfiguration(new BNGLConfiguration(m_colorManager));
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
	
	@Override
	public void editorSaved()
	{
		String path = this.getPartName(); 
		ModelCollectionController.getModelCollectionController().fileHasBeenSaved(path);
	}	
}
