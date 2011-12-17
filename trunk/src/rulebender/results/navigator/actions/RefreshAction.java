package rulebender.results.navigator.actions;


import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import rulebender.results.navigator.view.ResultsNavigatorView;

public class RefreshAction extends Action
{
	
	private static final ImageDescriptor m_refreshImage = ImageDescriptor.createFromImage(AbstractUIPlugin.imageDescriptorFromPlugin ("rulebender","/icons/nav_refresh.gif").createImage());
	
	private ResultsNavigatorView m_view;

	public RefreshAction(ResultsNavigatorView view)
	{
		setView(view);
	}
	
	public void run()
	{	
		m_view.rebuildWholeTree();
	}
	
	private void setView(ResultsNavigatorView view)
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

