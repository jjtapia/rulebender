package rulebender.navigator.views;

import java.io.File;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.part.ViewPart;

import rulebender.core.workspace.PickWorkspaceDialog;
import rulebender.navigator.actions.CompareAction;
import rulebender.navigator.actions.DeleteFileAction;
import rulebender.navigator.actions.NewFileAction;
import rulebender.navigator.actions.NewFolderAction;
import rulebender.navigator.actions.NewProjectAction;
import rulebender.navigator.actions.RefreshAction;
import rulebender.navigator.model.FolderNode;
import rulebender.navigator.model.TreeContentProvider;
import rulebender.navigator.model.TreeLabelProvider;
import rulebender.navigator.model.TreeNode;

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
		m_treeViewer = new TreeViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE);
		
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
		
		// I think this rebuilds the menu every time it is loaded, so you can 
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
		
		// Create the 'new' submenu
		MenuManager newMenu = new MenuManager("New...");
		
		manager.add(newMenu);
		
		// If nothing is selected
		if(selection.isEmpty())
		{
			// Add the option to create a new folder.
			//newMenu.add(new NewFolderAction(new File(PickWorkspaceDialog.getLastSetWorkspaceDirectory()), this));	
			newMenu.add(new NewProjectAction(this));
		}
		
		// If only 1 thing was selected
		else if(selection.size() == 1)
		{
			// If it is a folder node
			if(((TreeNode) selection.getFirstElement()).getNodeType().equals("FolderNode"))
			{
				newMenu.add(new NewFolderAction(new File(((FolderNode) selection.getFirstElement()).getPath()), this));
				newMenu.add(new NewFileAction(new File(((FolderNode) selection.getFirstElement()).getPath()), this));
				newMenu.add(new NewProjectAction(this));
			}
			else if(((TreeNode) selection.getFirstElement()).getNodeType().equals("FileNode"))
			{
				manager.add(new CompareAction(selection));
				
				// TODO Add something for each type file selection (.bng, .net, .net, scan)
			}
			
			// For both folders and files
			manager.add(new DeleteFileAction(selection, this));
		 
		}
		
		// If multiple things are selected
		else if(selection.size() > 1)
		{
			// For both folders and files
			manager.add(new DeleteFileAction(selection, this));
		}
		
		// No matter what add the things below.
		
		// Add a simple action for refreshing the tree.  Used when 
		// the file system has been changed outside of the tool.
		manager.add(new RefreshAction(this));
	}

	@Override
	public void setFocus() 
	{
		// TODO Auto-generated method stub
	}

	/**
	 * Saves the state of the expanded nodes and refreshes the tree.  
	 * Used when the directories are changed.
	 *
	 */
	public void rebuildWholeTree() 
	{
		Object[] expanded = m_treeViewer.getExpandedElements();
		
		// Get the path of the workspace
		String rootDirPath = PickWorkspaceDialog.getLastSetWorkspaceDirectory();
	
		// Create the root node of the tree using the workspace.
		FolderNode rootNode = new FolderNode(new File(rootDirPath));
			
		// Set the input to the tree as the root node. 
		m_treeViewer.setInput(rootNode);
		
		m_treeViewer.setExpandedElements(expanded);			
	}

}
