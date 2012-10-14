/**
 * LinkHub.java
 * This file implements the ActionHub class as a singleton.   
 * 
 * 
 *  Text <-> contact map is a straight forward representation,  There are no rules in the contact map that do not directly appear
    in the text and vice-versa.  For all concerns, the contact map and text can be thought of as having the same set of textual rules.
		
     Text/Contact Map -> Influence Graph:  Rules will only be forward or reverse.  In either case they could actually be from a bidirectional rule.
		// r <-> p
		// and they could be represented elsewhere as  
		// r -> p
		// for the forward reaction, and 
		// p -> r
		// for the reverse
		
		// So when a rule is selected in the contact map, if it is bidirectional then we need to look for r -> p or p->r in the influence graph.  
		// If it is unidirectional then we need to look for only the r->p.
		
		// If a rule r->p is selected in the influence graph, it's source in the text/contact map could be r->p if it is unidirectional, or if it is 
		// bidirectional then it could be r <-> p or p <-> r.
		
 */
package link;

import java.util.ArrayList;

import networkviewer.cmap.VisualRule;

import prefuse.visual.VisualItem;

import editor.BNGEditor;

public class LinkHub
{
  // Singleton element
  private static LinkHub theActionHub;

  // ArrayList of listeners for when a rule is selected
  private ArrayList<LinkedViewsReceiverInterface> listeners;


  /**
   * Private constructor
   */
  private LinkHub()
  {
    // Initialize the listener ArrayList.
    listeners = new ArrayList<LinkedViewsReceiverInterface>();
  }


  /**
   * Public way to acquire the hub.
   * 
   * @return
   */
  public static synchronized LinkHub getLinkHub()
  {
    if (theActionHub == null)
      theActionHub = new LinkHub();

    return theActionHub;
  }


  /**
   * Cloning is not supported.
   */
  public Object clone() throws CloneNotSupportedException
  {
    throw new CloneNotSupportedException();
  }


  public void registerLinkedViewsListener(
      LinkedViewsReceiverInterface linkedViewListener)
  {
    listeners.add(linkedViewListener);
  }


  /*---------------------------------------------------------------
   * Contact Map
   *-------------------------------------------------------------*/

  /**
   * Inform the listeners that a molecule has been selected.
   */
  public void moleculeSelectedInContactMap(final VisualItem molecule)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.moleculeSelectedInContactMap(molecule);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that a component has been selected.
   */
  public void componentSelectedInContactMap(final VisualItem component)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.componentSelectedInContactMap(component);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that a hub node has been selected.
   */
  public void hubSelectedInContactMap(final VisualItem hubItem)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.hubSelectedInContactMap(hubItem);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that a state node has been selected.
   */
  public void stateSelectedInContactMap(final VisualItem stateItem)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.stateSelectedInContactMap(stateItem);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that a rule has been selected in the contact map.
   */
  public void ruleSelectedInContactMap(final VisualRule rule)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.ruleSelectedInContactMap(rule);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that an edge has been selected in the contact map.
   */
  public void edgeSelectedInContactMap(final VisualItem edge)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.edgeSelectedInContactMap(edge);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Select a compartment in the text.
   * 
   * @param compartment
   */
  public void compartmentSelectedInContactMap(final VisualItem compartmentItem)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.compartmentSelectedInContactMap(compartmentItem);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /*---------------------------------------------------------------
   * Influence Graph
   *-------------------------------------------------------------*/

  /**
   * Inform the listeners that a rule has been selected in the contact map.
   */
  public void ruleSelectedInInfluenceGraph(final VisualItem rule)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.ruleSelectedInInfluenceGraph(rule);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that a rule has been deselected in the contact map.
   */
  public void clearSelectionFromContactMap()
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.clearSelectionFromContactMap();
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that a rule has been deselected in the igraph.
   */
  public void clearSelectionFromInfluenceGraph()
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.clearSelectionFromInfluenceGraph();
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /*---------------------------------------------------------------
   * Text
   *-------------------------------------------------------------*/

  /**
   * Inform the listeners that a molecule has been selected in the text
   */
  public void moleculeSelectedInText(final String moleculeText)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.moleculeSelectedInText(moleculeText);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Inform the listeners that a molecule has been selected in the text
   */
  public void ruleSelectedInText(final String ruleText)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.ruleSelectedInText(ruleText);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Clear the selection from the text.
   */
  public void clearSelectionFromText()
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.clearSelectionFromText();
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Select a component in the text.
   * 
   * @param componentText
   */
  public void componentSelectedInText(final String componentText)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.componentSelectedInText(componentText);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Select a state from the text.
   * 
   * @param stateText
   */
  public void stateSelectedInText(final String stateText)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.stateSelectedInText(stateText);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }


  /**
   * Select a compartment in the text.
   * 
   * @param compartment
   */
  public void compartmentSelectedInText(final String compartmentText)
  {
    Runnable job = new Runnable()
    {
      public void run()
      {
        for (LinkedViewsReceiverInterface o : listeners)
        {
          o.componentSelectedInText(compartmentText);
        }
      }
    };

    BNGEditor.getMainEditorShell().getDisplay().syncExec(job);
  }
}
