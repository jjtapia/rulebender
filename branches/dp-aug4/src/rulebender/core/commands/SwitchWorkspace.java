package rulebender.core.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import rulebender.core.workspace.PickWorkspaceDialog;

/**
 * This is an AbstractHandler that is called when the user wants to 
 * change the workspace.  This command is registered in the 
 * org.eclipse.ui.commands extension point of plugin.xml.
 * @author adammatthewsmith
 *
 */
public class SwitchWorkspace extends AbstractHandler
{

	public Object execute(ExecutionEvent event) throws ExecutionException 
	{
		PickWorkspaceDialog pwd = new PickWorkspaceDialog(true, null); 
        int pick = pwd.open(); 
        if (pick == Dialog.CANCEL) 
            return null; 
 
        MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Switch Workspace", "The client will now restart with the new workspace"); 
 
        PickWorkspaceDialog.willSwitchRestart();
        
        // restart client 
        PlatformUI.getWorkbench().restart(); 
        
		return null;
	}
}
