package networkviewer.cmap;

import prefuse.action.assignment.ColorAction;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;

public class EnterColorAction extends ColorAction
{

  private int[] palette;
  private VisualItem agg;


  public EnterColorAction(String group, String colorField, int[] palette,
      VisualItem agg)
  {
    super(group, colorField);

    this.palette = palette;
    this.agg = agg;
  }


  public int getColor(VisualItem item)
  {
    if (item.equals(agg))
    {
      // highlight color
      return palette[2];
    }
    else
    {
      // edge
      if (item instanceof EdgeItem)
      {
        return palette[1];
      }
      // node or aggregate
      else
      {
        return palette[0];
      }
    }
  }

}
