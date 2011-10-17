package rulebender.views.navigator;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import rulebender.dialog.PickWorkspaceDialog;
import rulebender.models.filebrowser.BNGModelCollection;
import rulebender.models.filebrowser.FileBrowserTreeBuilder;
import rulebender.models.filebrowser2.FolderNode;
import rulebender.models.filebrowser2.TreeContentProvider;
import rulebender.models.filebrowser2.TreeLabelProvider;
import rulebender.models.filebrowser2.TreeNode;
import rulebender.views.navigator.actions.NewFolderAction;

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
		
		//---------------------------- set up the TreeViewer ------------------
		
		// Get the path of the workspace
		String rootDirPath = PickWorkspaceDialog.getLastSetWorkspaceDirectory();
		
		// create the TreeViewer
		m_treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.MULTI);
		
		// Set the content provider and the Label provider 
		m_treeViewer.setContentProvider(new TreeContentProvider());
		m_treeViewer.setLabelProvider(new TreeLabelProvider());
		
		// Register the TreeViewer with the selection service so that its selections are 
		// reported through RCP
		getSite().setSelectionProvider(m_treeViewer);
		
		// Create the root node of the tree using the workspace.
		FolderNode rootNode = new FolderNode(new File(rootDirPath));
		
		// Set the input to the tree as the root node. 
		m_treeViewer.setInput(rootNode);
		
		
		// ----------------------------- set up the double click listener -----
		
		// Add a double click listener to the tree.
		m_treeViewer.addDoubleClickListener(new NavigatorDoubleClickListener());
		
		// ----------------------------- set up the context menu --------------
		
		// Create a MenuManager object.  
		MenuManager menuMgr = new MenuManager();
		
		// TODO I think this rebuilds the menu every time it is loaded, so you can 
		// have dynamic menus depending on which object is selected. 
	    menuMgr.setRemoveAllWhenShown(true);
	    
	    // Add a menu listener to the menu manager.  
	    // menu listeners are objects that get informed when a menu is about to show.
	    menuMgr.addMenuListener(new IMenuListener() 
		    {
		       	public void menuAboutToShow(IMenuManager manager) 
		        {
		            ModelTreeView.this.fillContextMenu(manager);
		        }
		    });
	    
	    Menu menu = menuMgr.createContextMenu(m_treeViewer.getControl());
	    m_treeViewer.getControl().setMenu(menu);
	    getSite().registerContextMenu(menuMgr, m_treeViewer);
		
		//m_treeViewer.expandAll();
	}

	/**
	 * This method is called when the context menu is about to be shown.  
	 * @param manager
	 */
	protected void fillContextMenu(IMenuManager manager) 
	{
		IStructuredSelection selection = (IStructuredSelection) getSite().getSelectionProvider().getSelection();
		
		if(selection.isEmpty())
		{
			manager.add(new NewFolderAction(new File(PickWorkspaceDialog.getLastSetWorkspaceDirectory()), m_treeViewer));
			
		}
		
		else
		{
			if(selection.size() == 1 && ((TreeNode) selection.getFirstElement()).getNodeType().equals("FolderNode"))
			{
				manager.add(new NewFolderAction(new File(((FolderNode) selection.getFirstElement()).getPath()), m_treeViewer));
			}
		}
		
		//TODO Why won't this work?
		manager.add(new Action()
		{
			public String getText()
			{
				return "Refresh";
			}
			
			public void run()
			{
				m_treeViewer.refresh();
			}
		});
	}

	@Override
	public void setFocus() 
	{
		// TODO Auto-generated method stub
	}

}
