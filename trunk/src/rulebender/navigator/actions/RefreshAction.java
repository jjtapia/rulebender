package rulebender.navigator.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import rulebender.navigator.views.ModelTreeView;

public class RefreshAction extends Action
{
	
	private static final ImageDescriptor m_refreshImage = ImageDescriptor.createFromImage(AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/views/nav_refresh.gif").createImage());
	
	private ModelTreeView m_view;

	public RefreshAction(ModelTreeView view)
	{
		setView(view);
	}
	
	public void run()
	{	
		m_view.rebuildWholeTree();
	}
	
	private void setView(ModelTreeView view)
	{
		m_view = view;
	}
	
	public String getText()
	{
		return "Refresh";
	}
	
	public ImageDescriptor getImageDescriptor()
	{
		return m_refreshImage;
	}
}

