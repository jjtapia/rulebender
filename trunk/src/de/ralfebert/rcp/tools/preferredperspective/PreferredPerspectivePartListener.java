/*******************************************************************************
 * Copyright (c) 2008, Ralf Ebert
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of Ralf Ebert nor the names of its contributors may
 *       be used to endorse or promote products derived from this software without
 *       specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY Ralf Ebert ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Ralf Ebert BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package de.ralfebert.rcp.tools.preferredperspective;

//import java.util.logging.Logger;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * PreferredPerspectivePartListener is to be registered using the extension
 * point "org.eclipse.ui.startup". It will register itself as listener for the
 * activation of parts. When a part which implements IPrefersPerspective is
 * activated it will activate the preferred perspective of this part.
 * 
 * - Ralph Ebert
 * 
 * 
 * This class is actually not use at all by RuleBender, but the code in some 
 * of the methods has been moved to the 
 * {@link rulebender.core.editorhider.EditorHider} class.  EditorHider changes 
 * perspectives as this class does, but also hides the opened editor in 
 * all other perspectives.  
 * 
 * -Adam M. Smith.
 */
public class PreferredPerspectivePartListener implements IPartListener, IStartup {

    //private static final Logger log = Logger.getLogger(PreferredPerspectivePartListener.class);

    public void partActivated(IWorkbenchPart part) {
        refresh(part);
    }

    public static void refresh(final IWorkbenchPart part) {
        if (!(part instanceof IPrefersPerspective)) {
            return;
        }

        final IWorkbenchWindow workbenchWindow = part.getSite().getPage().getWorkbenchWindow();

        IPerspectiveDescriptor activePerspective = workbenchWindow.getActivePage().getPerspective();
        final String preferredPerspectiveId = ((IPrefersPerspective) part)
                .getPreferredPerspectiveId();

        if (preferredPerspectiveId == null) {
            return;
        }

        if (activePerspective == null || !activePerspective.getId().equals(preferredPerspectiveId)) {
            // Switching of the perspective is delayed using Display.asyncExec
            // because switching the perspective while the workbench is
            // activating parts might cause conflicts.
            Display.getCurrent().asyncExec(new Runnable() {

                public void run() {
                  //  log.debug("Switching to preferred perspective " + preferredPerspectiveId
                  //          + " for " + part.getClass());
                    try {
                        workbenchWindow.getWorkbench().showPerspective(preferredPerspectiveId,
                                workbenchWindow);
                    } catch (WorkbenchException e) {
                    //    log.warn("Could not switch to preferred perspective "
                    //            + preferredPerspectiveId + " for " + part.getClass(), e);
                    }
                }

            });
        }

    }

    public void earlyStartup() {
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                try {
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                            .addPartListener(new PreferredPerspectivePartListener());
                } catch (Exception e) {
                //    log.error(e.getMessage(), e);
                }
            }

        });
    }

    public void partBroughtToTop(IWorkbenchPart part) {
        // nothing to do
    }

    public void partClosed(IWorkbenchPart part) {
        // nothing to do
    }

    public void partDeactivated(IWorkbenchPart part) {
        // nothing to do
    }

    public void partOpened(IWorkbenchPart part) {
        // nothing to do
    }

}