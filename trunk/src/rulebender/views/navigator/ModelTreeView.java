package rulebender.views.navigator;

import java.io.File;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import rulebender.dialog.PickWorkspaceDialog;
import rulebender.models.filebrowser.BNGModelCollection;
import rulebender.models.filebrowser.FileBrowserTreeBuilder;
import rulebender.models.filebrowser2.FolderNode;
import rulebender.models.filebrowser2.TreeContentProvider;
import rulebender.models.filebrowser2.TreeLabelProvider;

public class ModelTreeView extends ViewPart 
{

	public static final String ID = "rulebender.views.Navigator";
	
	private TreeViewer m_treeViewer;
	
	public ModelTreeView() {
		// TODO Auto-generated constructor stub
	}

	public void createPartControl(Composite parent) 
	{
		/*
		BNGModelCollection root = FileBrowserTreeBuilder.buildTree(new File("/Users/mr_smith22586/Documents/workspace/BNGModels"));
		
		m_treeViewer = new TreeViewer(parent);
		m_treeViewer.setContentProvider(new ModelTreeContentProvider());
		m_treeViewer.setLabelProvider(new ModelTreeLabelProvider());
		m_treeViewer.setInput(root);
		*/
		
		String rootDirPath = PickWorkspaceDialog.getLastSetWorkspaceDirectory();
		
		m_treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
		m_treeViewer.setContentProvider(new TreeContentProvider());
		m_treeViewer.setLabelProvider(new TreeLabelProvider());
		
		getSite().setSelectionProvider(m_treeViewer);
		
		FolderNode rootNode = new FolderNode(new File(rootDirPath));
		m_treeViewer.setInput(rootNode);
		
		m_treeViewer.addDoubleClickListener(new NavigatorDoubleClickListener());
		
		//m_treeViewer.expandAll();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
