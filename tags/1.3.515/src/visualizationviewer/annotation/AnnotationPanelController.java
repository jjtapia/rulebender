package visualizationviewer.annotation;

import java.util.ArrayList;

import javax.swing.table.TableModel;

import networkviewer.cmap.VisualRule;
import prefuse.Visualization;
import prefuse.data.Edge;
import prefuse.data.Node;
import prefuse.visual.VisualItem;
import visualizationviewer.VisualizationViewerController;
import link.LinkedViewsReceiverInterface;

public class AnnotationPanelController implements LinkedViewsReceiverInterface
{
  // A const for the string value of the category of aggregate
  public static final String AGG_CAT_LABEL = "molecule";

  // A Link to the controller where the table model should be passed
  // in order to update the annotation panel.
  private VisualizationViewerController visviewer;


  /**
   * Constructor takes a VisualizationViewerController so it can send the
   * TableModel to the correct controller.
   * 
   * @param viewer
   */
  public AnnotationPanelController(VisualizationViewerController viewer)
  {
    visviewer = viewer;
  }


  /**
   * Handles state, hub, and component nodes in the contact map.
   * 
   * @param item
   */
  /*
   * private void setAnnotationForNode(VisualItem item) {
   * 
   * String type = item.getString("type"); // state node if
   * (type.equals("state")) { Node state_node = (Node) item.getSourceTuple();
   * ArrayList<VisualRule> rules = (ArrayList<VisualRule>)
   * state_node.get("rules"); updateAnnotationTableForStateNode(item, rules);
   * 
   * }
   * 
   * 
   * // component node else if (type.equals("component")){ String states = "";
   * ArrayList<String> stateList = (ArrayList) item.get("states"); if (stateList
   * != null) { for (int i = 0; i < stateList.size() - 1; i++) { states = states
   * + stateList.get(i) + ", "; } states = states +
   * stateList.get(stateList.size() - 1); }
   * 
   * states += "";
   * 
   * if (((String) item.get(VisualItem.LABEL)).trim().equals("")) { return; }
   * 
   * // show details in the table in the annotation panel Object[][] data =
   * {{"Name of component", ((String) item.get(VisualItem.LABEL)).trim()},
   * {"Internal states", states.trim()}, {"Molecule",
   * item.getString("molecule_expression").trim()}, {"Location", ""},
   * {"Comments", ""}};
   * 
   * if (item.canGet("annotation", TableModel.class)) { // names = {"Attribute",
   * "Value"} as default this.updateAnnotationTable(item,
   * (TableModel)item.get("annotation"), null, data); } else {
   * this.updateAnnotationTable(item, null, null, data); } }
   * 
   * else if (type.equals("hub")) { Node hub_node = (Node)
   * item.getSourceTuple();
   * 
   * // get the rules that can make that hub node. ArrayList<VisualRule> rules =
   * (ArrayList<VisualRule>) hub_node.get("rules"); if (rules != null) {
   * updateAnnotationTableForEdges(item, rules); } } }
   */
  /**
   * Handles Edges from the contact map.
   * 
   * @param item
   */
  private void setAnnotationForEdge(VisualItem item)
  {
    // Get the edge object that corresponds to the edgeitem
    Edge edge = (Edge) item.getSourceTuple();

    // get the rules that can make that edge.
    ArrayList<VisualRule> rules = (ArrayList<VisualRule>) edge.get("rules");
    if (rules != null)
    {
      updateAnnotationTableForEdges(item, rules);
    }
  }


  /**
   * Handles when aggregates (molecules or compartments) are selected in the
   * contact map. TODO separate the molecules and compartments and create a
   * LinkHub event for compartments.
   * 
   * @param item
   */
  private void setAnnotationForAggregate(VisualItem item)
  {
    if (!item.canGetString(AGG_CAT_LABEL))
    {
      return;
    }

    String moleName = ((String) item.get(AGG_CAT_LABEL)).trim();
    String moleExp = ((String) item.get("molecule_expression")).trim();
    String compartment = "";

    if (item.canGet("compartment", String.class))
    {
      compartment = (String) item.get("compartment");
    }

    Object[][] data = { { "Name of molecule", moleName },
        { "Essential info", "" }, { "Definition in BNGL", moleExp },
        { "Explanation", "" }, { "Compartment", compartment },
        { "Available info", "" }, { "Estimates of constants", "" },
        { "Online resources", "" }, { "UniProt", "" },
        { "PathwayCommons", "" }, { "HPRD", "" }, { "Reactome", "" },
        { "UCSD-Nature", "" }, { "InterPro", "" }, { "PROSITE", "" },
        { "KEGG", "" }, { "ChEBI", "" }, { "PubChem", "" } };

    if (item.canGet("annotation", TableModel.class))
    {
      // names = {"Attribute", "Value"} as default
      this.updateAnnotationTable(item, (TableModel) item.get("annotation"),
          null, data);
    }
    else
    {
      this.updateAnnotationTable(item, null, null, data);
    }
  }


  /**
   * Utility method for this class that parses rules.
   * 
   * @param rules
   * @param data
   * @param n_col
   * @param rowIndex_rule
   */
  private void parseRulesForAnnotation(ArrayList<VisualRule> rules,
      Object[][] data, int n_col, int rowIndex_rule)
  {
    // set the rest of rows be rules
    int i = rowIndex_rule;
    for (; i < rules.size() + rowIndex_rule; i++)
    {
      data[i] = new Object[n_col];
      // get current visualruel
      VisualRule rule = rules.get(i - rowIndex_rule);
      // whole rule
      // A() + B() <-> C() rate1, rate2 <exclude>
      String wholerule = rule.getName().trim();

      String rulename = "", rate1 = "", rate2 = "", constraints = "";

      // constraints
      if (wholerule.indexOf("include") != -1
          || wholerule.indexOf("exclude") != -1
          || wholerule.indexOf("DeleteMolecules") != -1)
      {
        constraints = wholerule.substring(wholerule.lastIndexOf(" ") + 1)
            .trim();
        wholerule = wholerule.substring(0, wholerule.lastIndexOf(" ")).trim();
      }

      // two directions, has rate2
      if (wholerule.contains("<") && wholerule.lastIndexOf(",") != -1)
      {
        rate2 = wholerule.substring(wholerule.lastIndexOf(",") + 1).trim();
        wholerule = wholerule.substring(0, wholerule.lastIndexOf(",")).trim();
      }
      // rate1
      // some model don't have )
      // some model don't have space
      // the model don't have ) should have space
      if (wholerule.lastIndexOf(")") != -1)
      {
        rate1 = wholerule.substring(wholerule.lastIndexOf(")") + 1).trim();
        rulename = wholerule.substring(0, wholerule.lastIndexOf(")") + 1)
            .trim();
      }
      else
      {
        rate1 = wholerule.substring(wholerule.lastIndexOf(" ") + 1).trim();
        rulename = wholerule.substring(0, wholerule.lastIndexOf(" ") + 1)
            .trim();
      }

      data[i][0] = rule.getLabel();
      if (rate2.equals(""))
      {
        data[i][1] = rulename + "  " + rate1 + "  " + constraints;
      }
      else
      {
        data[i][1] = rulename + "  " + rate1 + ", " + rate2 + "  "
            + constraints;
      }
    }
  }


  /**
   * This method is for visual items with an associated arraylist of rules. The
   * item and rules are passed in and the rules are used to build the table
   * model, and then the table model updates the annotation panel and is
   * attached to the item.
   * 
   * @param item
   * @param rules
   */
  private void updateAnnotationTableForEdges(VisualItem item,
      ArrayList<VisualRule> rules)
  {
    // number of items in annotation table
    int n_items = 4;
    // number of columns in annotation table
    int n_col = 2;
    // index of the row actual rules begin
    int rowIndex_rule = 3;

    Object[][] data = new Object[rules.size() + n_items][];

    data[0] = new Object[n_col];
    data[0][0] = "Brief description";
    data[0][1] = "";
    data[1] = new Object[n_col];
    data[1][0] = "Explanatory note";
    data[1][1] = "";
    data[2] = new Object[n_col];
    data[2][0] = "Rules";
    data[2][1] = "";

    parseRulesForAnnotation(rules, data, n_col, rowIndex_rule);

    int lastIndex = rules.size() + n_items - 1;
    data[lastIndex] = new Object[n_col];
    data[lastIndex][0] = "Comments";
    data[lastIndex][1] = "";

    if (item.canGet("annotation", TableModel.class))
    {
      // names = {"Attribute", "Value"} as default
      this.updateAnnotationTable(item, (TableModel) item.get("annotation"),
          null, data);
    }
    else
    {
      this.updateAnnotationTable(item, null, null, data);
    }
  }


  /**
   * Used for state nodes and associated rules.
   * 
   * @param item
   * @param rules
   */
  private void updateAnnotationTableForStateNode(VisualItem item,
      ArrayList<VisualRule> rules)
  {
    // number of items in annotation table
    int n_items = 5;
    // number of columns in annotation table
    int n_col = 2;
    // index of the row actual rules begin
    int rowIndex_rule = 4;
    // number of rules
    int num_rules = 0;
    if (rules != null)
    {
      num_rules = rules.size();
    }

    Object[][] data = new Object[num_rules + n_items][];

    data[0] = new Object[n_col];
    data[0][0] = "Name of state";
    data[0][1] = ((String) item.get(VisualItem.LABEL)).trim();
    data[1] = new Object[n_col];
    data[1][0] = "Component";
    data[1][1] = item.getString("component").trim();
    data[2] = new Object[n_col];
    data[2][0] = "Molecule";
    data[2][1] = item.getString("molecule_expression").trim();
    data[3] = new Object[n_col];
    data[3][0] = "Rules";
    data[3][1] = "";

    if (rules != null)
    {
      parseRulesForAnnotation(rules, data, n_col, rowIndex_rule);
    }

    int lastIndex = num_rules + n_items - 1;
    data[lastIndex] = new Object[n_col];
    data[lastIndex][0] = "Comments";
    data[lastIndex][1] = "";

    if (item.canGet("annotation", TableModel.class))
    {
      // names = {"Attribute", "Value"} as default
      this.updateAnnotationTable(item, (TableModel) item.get("annotation"),
          null, data);
    }
    else
    {
      this.updateAnnotationTable(item, null, null, data);
    }
  }


  /**
   * Used to send the TableModel to the VisualizationViewerController. All
   * annotation updates eventuall call this.
   * 
   * @param item
   * @param tableModel
   * @param names
   * @param data
   */
  private void updateAnnotationTable(VisualItem item, TableModel tableModel,
      String[] names, Object[][] data)
  {
    if (tableModel != null)
    {
      visviewer.updateAnnotationTable(tableModel);
      return;
    }

    // table model is NULL
    if (names == null)
    {
      if (data == null)
      {
        System.out
            .println("Sending null to the visviewer controller because names and data are null");
        visviewer.updateAnnotationTable(null);
        return;
      }
      else
      {
        names = new String[2];
        names[0] = "Attribute";
        names[1] = "Value";
        TableModel tm = new AnnotationTableModel(names, data);
        visviewer.updateAnnotationTable(tm);
        if (item != null)
        {
          item.set("annotation", tm);
        }
        return;
      }
    }
    else
    {
      TableModel tm = new AnnotationTableModel(names, data);
      visviewer.updateAnnotationTable(tm);
      if (item != null)
      {
        item.set("annotation", tm);
      }
    }
  }


  /*
   * Begin LinkedViewsReceiverInterface implementations. TODO A lot of these
   * just call a single method that is implemented above. I can move those
   * implementations down here.
   */

  /**
   * 
   * @see link.LinkedViewsReceiverInterface#ruleSelectedInContactMap(networkviewer.cmap.VisualRule)
   */
  public void ruleSelectedInContactMap(VisualRule ruleItem)
  {

  }


  /**
   * 
   * @see link.LinkedViewsReceiverInterface#ruleSelectedInInfluenceGraph(prefuse.visual.VisualItem)
   */
  public void ruleSelectedInInfluenceGraph(VisualItem ruleItem)
  {
    // show details in the table in the annotation panel
    String[] names = { "ID", "Rule", "Rate", "Constraints" };
    Object[][] data = new Object[1][];

    data[0] = new Object[names.length];

    // whole rule
    // A() + B() <-> C() rate1, rate2
    String wholerule = (String) ruleItem.get("rulename");

    String rulename = "", rate1 = "", constraints = "";

    // constraints
    if (wholerule.indexOf("include") != -1
        || wholerule.indexOf("exclude") != -1
        || wholerule.indexOf("DeleteMolecules") != -1)
    {
      constraints = wholerule.substring(wholerule.lastIndexOf(" ") + 1).trim();
      wholerule = wholerule.substring(0, wholerule.lastIndexOf(" ")).trim();
    }

    // rate1
    // some model don't have )
    // some model don't have space
    // the model don't have ) should have space
    int index_rightP = wholerule.lastIndexOf(")");
    // last right parenthesis on the right half and belongs to the last
    // molecule
    if (index_rightP != -1 && index_rightP > wholerule.lastIndexOf(">")
        && index_rightP > wholerule.lastIndexOf("+"))
    {
      rate1 = wholerule.substring(wholerule.lastIndexOf(")") + 1).trim();
      rulename = wholerule.substring(0, wholerule.lastIndexOf(")") + 1).trim();
    }
    else
    {
      rate1 = wholerule.substring(wholerule.lastIndexOf(" ") + 1).trim();
      rulename = wholerule.substring(0, wholerule.lastIndexOf(" ") + 1).trim();
    }

    // ID
    data[0][0] = ruleItem.getString(VisualItem.LABEL);
    // rulename
    data[0][1] = rulename;
    // rate1
    data[0][2] = rate1;

    // constraints
    data[0][3] = constraints;

    // update table
    TableModel tm = new AnnotationTableModel(names, data);
    System.out.println("tm in iGraph click control is "
        + (tm == null ? "null" : "not null"));
    visviewer.updateAnnotationTable(tm);

  }


  /**
   * 
   * @see link.LinkedViewsReceiverInterface#clearSelectionFromContactMap()
   */
  public void clearSelectionFromContactMap()
  {
    updateAnnotationTable(null, null, null, null);
  }


  /**
   * 
   * @see link.LinkedViewsReceiverInterface#clearSelectionFromInfluenceGraph()
   */
  public void clearSelectionFromInfluenceGraph()
  {
    updateAnnotationTable(null, null, null, null);
  }


  /**
   * 
   * @see link.LinkedViewsReceiverInterface#moleculeSelectedInContactMap(prefuse.visual.VisualItem)
   */
  public void moleculeSelectedInContactMap(VisualItem moleculeItem)
  {
    setAnnotationForAggregate(moleculeItem);
  }


  /**
   * 
   * @see link.LinkedViewsReceiverInterface#edgeSelectedInContactMap(prefuse.visual.VisualItem)
   */
  public void edgeSelectedInContactMap(VisualItem edge)
  {
    setAnnotationForEdge(edge);
  }


  /**
   * 
   * @see link.LinkedViewsReceiverInterface#componentSelectedInContactMap(prefuse.visual.VisualItem)
   */
  public void componentSelectedInContactMap(VisualItem componentItem)
  {
    // setAnnotationForNode(componentItem);
    String states = "";
    ArrayList<String> stateList = (ArrayList) componentItem.get("states");
    if (stateList != null)
    {
      for (int i = 0; i < stateList.size() - 1; i++)
      {
        states = states + stateList.get(i) + ", ";
      }
      states = states + stateList.get(stateList.size() - 1);
    }

    states += "";

    if (((String) componentItem.get(VisualItem.LABEL)).trim().equals(""))
    {
      return;
    }

    // show details in the table in the annotation panel
    Object[][] data = {
        { "Name of component",
            ((String) componentItem.get(VisualItem.LABEL)).trim() },
        { "Internal states", states.trim() },
        { "Molecule", componentItem.getString("molecule_expression").trim() },
        { "Location", "" }, { "Comments", "" } };

    if (componentItem.canGet("annotation", TableModel.class))
    {
      // names = {"Attribute", "Value"} as default
      this.updateAnnotationTable(componentItem,
          (TableModel) componentItem.get("annotation"), null, data);
    }
    else
    {
      this.updateAnnotationTable(componentItem, null, null, data);
    }
  }


  public void stateSelectedInContactMap(VisualItem stateItem)
  {
    Node state_node = (Node) stateItem.getSourceTuple();
    ArrayList<VisualRule> rules = (ArrayList<VisualRule>) state_node
        .get("rules");
    updateAnnotationTableForStateNode(stateItem, rules);
  }


  public void hubSelectedInContactMap(VisualItem hubItem)
  {
    Node hub_node = (Node) hubItem.getSourceTuple();

    // get the rules that can make that hub node.
    ArrayList<VisualRule> rules = (ArrayList<VisualRule>) hub_node.get("rules");
    if (rules != null)
    {
      updateAnnotationTableForEdges(hubItem, rules);
    }
  }


  public void moleculeSelectedInText(String moleculeText)
  {
    System.out
        .println("moleculeSelectedInText called in AnnotationPanelController for "
            + moleculeText);
    Visualization vis = visviewer.getContactMapVisualization();

    // TODO Get the Visualization from the controller and do a search for
    // the VisualItem.
    // Then send the VisualItem to the same method that handles it for the
    // contact map.
  }


  public void ruleSelectedInText(String ruleText)
  {
    System.out
        .println("ruleSelectedInText called in AnnotationPanelController for "
            + ruleText);
    Visualization vis = visviewer.getInfluenceGraphVisualization();

    // TODO Get the Visualization from the controller and do a search.
    // Then send the VisualItem to the same method that handles it for the
    // influence graph.
  }


  public void compartmentSelectedInContactMap(VisualItem compartment)
  {
    // TODO Auto-generated method stub

  }


  public void clearSelectionFromText()
  {
    // TODO Auto-generated method stub

  }


  public void componentSelectedInText(String componentText)
  {
    // TODO Auto-generated method stub

  }


  public void stateSelectedInText(String stateText)
  {
    // TODO Auto-generated method stub

  }


  public void compartmentSelectedInText(VisualItem compartment)
  {
    // TODO Auto-generated method stub

  }

}
