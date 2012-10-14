/**
 * This interface declares the methods that handle links between the views.
 * 
 */
package link;

import networkviewer.cmap.VisualRule;
import prefuse.visual.VisualItem;

public interface LinkedViewsReceiverInterface
{

  /*------------------------------------------------------------------------
   * Contact Map Interactions.
   *----------------------------------------------------------------------*/

  /**
   * Called when a user clicks on a rule in the contact map. The parameter here
   * has to be a VisualRule because there is not VisualItem for a rule.
   * 
   * @param ruleText
   */
  public void ruleSelectedInContactMap(VisualRule ruleItem);


  /**
   * When the user deselects a rule in the contact map
   */
  public void clearSelectionFromContactMap();


  /**
   * When the user selects a molecule.
   * 
   * @param moleculeName
   */
  public void moleculeSelectedInContactMap(VisualItem moleculeItem);


  /**
   * When the user selects a component.
   * 
   * @param moleculeName
   */
  public void componentSelectedInContactMap(VisualItem componentItem);


  /**
   * When the user selects a state.
   * 
   * @param moleculeName
   */
  public void stateSelectedInContactMap(VisualItem stateItem);


  /**
   * When the user selects a component.
   * 
   * @param moleculeName
   */
  public void hubSelectedInContactMap(VisualItem hubItem);


  /**
   * When the user selects an edge in the contact map.
   */
  public void edgeSelectedInContactMap(VisualItem edge);


  /**
   * When the user selects a compartment in the contact map.
   */
  public void compartmentSelectedInContactMap(VisualItem compartment);


  /*-------------------------------------------------------------------------
   * Influence Graph Interactions.
   *-----------------------------------------------------------------------*/

  /**
   * Called when a user clicks on a rule in the influence graph.
   * 
   * @param ruleText
   */
  public void ruleSelectedInInfluenceGraph(VisualItem ruleItem);


  /**
   * When the user deselects a rule in the influence graph
   */
  public void clearSelectionFromInfluenceGraph();


  /*------------------------------------------------------------------------
   * Text Editor Interactions.
   * 
   * TODO  Need to figure out the best level of interaction here.  
   * 		 Text selection that is reflected in other visualizations
   * 		 without an explicit request could be confusing.  Text selection
   * 		 is used in normal text editing and it is possible that the user
   * 		 does not want their actions to always have repurcussions in the 
   * 		 other visualizations. 
   *----------------------------------------------------------------------*/

  /**
   * When the user selects a molecule in the text editor.
   * 
   * @param molecule
   */
  public void moleculeSelectedInText(String moleculeText);


  /**
   * When the user selects a molecule in the text editor.
   * 
   * @param molecule
   */
  public void ruleSelectedInText(String ruleText);


  /**
   * When the user deselects text. TODO This might not actually be the best way
   * to interact.
   */
  public void clearSelectionFromText();


  /**
   * Text is selected that represents a component definition.
   * 
   * @param componentText
   */
  public void componentSelectedInText(String componentText);


  /**
   * Text is selected that represents a state definition.
   * 
   * @param stateText
   */
  public void stateSelectedInText(String stateText);


  /**
   * Text is selected that represents a compartment definition.
   * 
   * @param compartment
   */
  public void compartmentSelectedInText(VisualItem compartment);
}