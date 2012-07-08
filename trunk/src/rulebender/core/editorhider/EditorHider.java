package rulebender.core.editorhider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageService;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.part.EditorPart;

import de.ralfebert.rcp.tools.preferredperspective.IPrefersPerspective;

/**
 * This class is a combination of Ralf Ebert's
 * de.ralfebert.rcp.tools.preferredperspective package and another package that
 * I found for remembering where an editor was opened and transferring to that
 * perspective when it is selected in a Resource browser.
 * 
 * @author adammatthewsmith
 * 
 */
public class EditorHider implements IStartup, IPerspectiveListener,
    IPartListener
{

  private HashMap<String, ArrayList<IEditorReference>> perspectiveEditors = 
      new HashMap<String, ArrayList<IEditorReference>>();
  
  private HashMap<String, IEditorReference> lastActiveEditors = 
      new HashMap<String, IEditorReference>();

  // private HashMap<IEditorReference, String> openEditors = new
  // HashMap<IEditorReference, String>();

  @Override
  public void earlyStartup()
  {
    Display.getDefault().asyncExec(new Runnable()
    {

      public void run()
      {
        try
        {
          EditorHider eh = new EditorHider();

          IPageService pageService = (IPageService) PlatformUI.getWorkbench()
              .getActiveWorkbenchWindow().getActivePage().getActivePart()
              .getSite().getService(IPageService.class);

          pageService.addPerspectiveListener(eh);

          IPartService partService = (IPartService) PlatformUI.getWorkbench()
              .getActiveWorkbenchWindow().getActivePage().getActivePart()
              .getSite().getService(IPartService.class);

          partService.addPartListener(eh);

        }
        catch (Exception e)
        {
          // log.error(e.getMessage(), e);
        }
      }

    });
  }

  public void perspectiveActivated(IWorkbenchPage page,
      IPerspectiveDescriptor perspective)
  {
    // updateTitle();

    // Hide the editor area

    // Hide all the editors
    IEditorReference[] editors = page.getEditorReferences();
    for (int i = 0; i < editors.length; i++)
    {
      page.hideEditor(editors[i]);
    }

    // Show the editors associated with this perspective
    ArrayList<IEditorReference> editorRefs = perspectiveEditors.get(perspective
        .getId());
    if (editorRefs != null)
    {
      for (Iterator<IEditorReference> it = editorRefs.iterator(); it.hasNext();)
      {
        IEditorReference editorInput = it.next();
        page.showEditor(editorInput);
      }

      // Send the last active editor to the top
      IEditorReference lastActiveRef = lastActiveEditors.get(perspective
          .getId());
      if (lastActiveRef != null)
      {
        page.bringToTop(lastActiveRef.getPart(true));
      }
    }
  }

  public void perspectiveSavedAs(IWorkbenchPage page,
      IPerspectiveDescriptor oldPerspective,
      IPerspectiveDescriptor newPerspective)
  {
    // updateTitle();
  }

  public void perspectiveDeactivated(IWorkbenchPage page,
      IPerspectiveDescriptor perspective)
  {
    // updateTitle();
    IEditorPart activeEditor = page.getActiveEditor();
    if (activeEditor != null)
    {

      // Find the editor reference that relates to this editor input
      IEditorReference[] editorRefs = page.findEditors(
          activeEditor.getEditorInput(), null, IWorkbenchPage.MATCH_INPUT);
      if (editorRefs.length > 0)
      {
        lastActiveEditors.put(perspective.getId(), editorRefs[0]);
      }
    }
  }

  /*******************************************************************************
   * Copyright (c) 2008, Ralf Ebert All rights reserved.
   * 
   * Redistribution and use in source and binary forms, with or without
   * modification, are permitted provided that the following conditions are met:
   * * Redistributions of source code must retain the above copyright notice,
   * this list of conditions and the following disclaimer. * Redistributions in
   * binary form must reproduce the above copyright notice, this list of
   * conditions and the following disclaimer in the documentation and/or other
   * materials provided with the distribution. * Neither the name of Ralf Ebert
   * nor the names of its contributors may be used to endorse or promote
   * products derived from this software without specific prior written
   * permission.
   * 
   * THIS SOFTWARE IS PROVIDED BY Ralf Ebert ''AS IS'' AND ANY EXPRESS OR
   * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
   * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
   * NO EVENT SHALL Ralf Ebert BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
   * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
   * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
   * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
   * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
   * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
   *******************************************************************************/
  public void partActivated(IWorkbenchPart part)
  {
    System.out.println("Part activated");

    if (!(part instanceof IPrefersPerspective))
    {
      return;
    }

    final IWorkbenchWindow workbenchWindow = part.getSite().getPage()
        .getWorkbenchWindow();

    IPerspectiveDescriptor activePerspective = workbenchWindow.getActivePage()
        .getPerspective();
    final String preferredPerspectiveId = ((IPrefersPerspective) part)
        .getPreferredPerspectiveId();

    if (preferredPerspectiveId == null)
    {
      return;
    }

    if (activePerspective == null
        || !activePerspective.getId().equals(preferredPerspectiveId))
    {
      changeToPerspective(workbenchWindow, preferredPerspectiveId);
    }

  }

  public void changeToPerspective(final IWorkbenchWindow workbenchWindow,
      final String perspectiveId)
  {
    // Switching of the perspective is delayed using Display.asyncExec
    // because switching the perspective while the workbench is
    // activating parts might cause conflicts.
    Display.getCurrent().asyncExec(new Runnable()
    {
      public void run()
      {
        try
        {
          workbenchWindow.getWorkbench().showPerspective(perspectiveId,
              workbenchWindow);
        }
        catch (WorkbenchException e)
        {
        }
      }
    });

  }

  public void partOpened(IWorkbenchPart part)
  {
    System.out.println("Part Opened");

    // If it is an editor.
    if (part instanceof EditorPart)
    {

      /*
       * String alreadyOpened = alreadyOpened(part);
       * 
       * // if(alreadyOpened != null) { // close it here.
       * 
       * // change to the perspective
       * changeToPerspective(part.getSite().getPage().getWorkbenchWindow(),
       * alreadyOpened);
       * 
       * // put it on top.
       * 
       * return; }
       */
      // Get the activePerspective
      EditorPart editor = (EditorPart) part;
      IWorkbenchPage page = part.getSite().getPage();
      IEditorInput editorInput = editor.getEditorInput();
      IPerspectiveDescriptor activePerspective = page.getPerspective();

      // Set the default preferredPerspectiveId as the active perspective's id
      String preferredPerspectiveId = activePerspective.getId();

      // If we can get another preferredPerspectiveId from the part, then set
      // it.
      if ((part instanceof IPrefersPerspective))
      {
        preferredPerspectiveId = ((IPrefersPerspective) part)
            .getPreferredPerspectiveId();
        changeToPerspective(part.getSite().getPage().getWorkbenchWindow(),
            preferredPerspectiveId);
      }

      // Get the arraylist associated with the perspective where this editor
      // should be.
      ArrayList<IEditorReference> editors = perspectiveEditors
          .get(preferredPerspectiveId);
      if (editors == null)
        editors = new ArrayList<IEditorReference>();

      // Find the editor reference that relates to this editor input
      IEditorReference[] editorRefs = page.findEditors(editorInput, null,
          IWorkbenchPage.MATCH_INPUT);

      System.out.println("Found " + editorRefs.length);
      for (IEditorReference ref : editorRefs)
      {
        System.out.println("\t" + ref.getTitle());
      }

      if (editorRefs.length > 0)
      {
        editors.add(editorRefs[0]);
        perspectiveEditors.put(preferredPerspectiveId, editors);
      }
    }
  }

  public void partBroughtToTop(IWorkbenchPart part)
  {
  }

  public void partClosed(IWorkbenchPart part)
  {
  }

  public void partDeactivated(IWorkbenchPart part)
  {
  }

  @Override
  public void perspectiveChanged(IWorkbenchPage page,
      IPerspectiveDescriptor perspective, String changeId)
  {

  }
}
