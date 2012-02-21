package rulebender.core.editorhider;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public class EditorHider implements IStartup, IPerspectiveListener {

	@Override
	public void perspectiveActivated(IWorkbenchPage page,
			IPerspectiveDescriptor perspective) 
	{
		
		// Model
		if (perspective.getId().equals("rulebender.perspective"))
		{
			//Hide gdat, cdat, scan
			String[] toHide = {"rulebender.editors.gdat", "rulebender.editors.cdat", "rulebender.editors.scan"};
			hideEditors(page, toHide);
		}

		//Simulate
		else if(perspective.getId().equals("rulebender.simulate.SimulatePerspective"))
		{
			//Hide all
			hideAllEditors(page);
		
		}
		
		// Results
		else if(perspective.getId().equals("rulebender.ResultsPerspective"))
		{
			//Hide bngl, net, simple
			String[] toHide = {"rulebender.editors.bngl", "rulebender.editors.net", "rulebender.editors.simple"};
			hideEditors(page, toHide);
			
		}
	}

	@Override
	public void perspectiveChanged(IWorkbenchPage page,
			IPerspectiveDescriptor perspective, String changeId) 
	{
		// Do Nothing
	}

	@Override
	public void earlyStartup() 
	{
	  Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                try {
                 	IPageService service = (IPageService) PlatformUI.getWorkbench()
                 			.getActiveWorkbenchWindow().getActivePage().getActivePart()
                 			.getSite().getService(IPageService.class);

                 	service.addPerspectiveListener(new EditorHider());
                } catch (Exception e) {
                //    log.error(e.getMessage(), e);
                }
            }

        });
	}

	private void hideAllEditors(IWorkbenchPage page)
	{
		System.out.println("Hiding all editors. ");
		
		IEditorReference[] editorReferences = page.getEditorReferences();
		
		for(IEditorReference editorReference : editorReferences)
		{
			page.hideEditor(editorReference);
		}	
	}
	
	private void hideEditors(IWorkbenchPage page, String[] editorIDs)
	{
		System.out.println("Hiding editors: ");
		for(String editorID : editorIDs)
		{
			System.out.println("\t" + editorID);
		}
				
		IEditorReference[] editorReferences = page.getEditorReferences();
		
		for(IEditorReference editorReference : editorReferences)
		{
			System.out.println("Editor:" + editorReference.getId());
			
			for(String editorID : editorIDs)
			{
				
				if(editorReference.getId().equals(editorID))
				{
					System.out.println("\tHiding:" + editorReference.getId());
					page.hideEditor(editorReference);
				}
				else
				{
					System.out.println("Should Restore: " + editorReference.getId());
					page.showEditor(editorReference);
				}
			}
		}	
	}
}
