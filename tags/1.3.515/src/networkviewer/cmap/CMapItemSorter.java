package networkviewer.cmap;

import prefuse.Visualization;
import prefuse.visual.AggregateItem;
import prefuse.visual.DecoratorItem;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;

public class CMapItemSorter extends ItemSorter
{
  protected static final int MOLECULE = 0;
  protected static final int BUBBLE = 1;
  protected static final int EDGE = 2;
  protected static final int COMPONENT = 3;
  protected static final int DECORATOR = 4;


  /**
   * <p>
   * Return an ordering score for an item. The default scoring imparts the
   * following order: hover items > highlighted items > items in the
   * {@link prefuse.Visualization#FOCUS_ITEMS} set >
   * {@link prefuse.Visualization#SEARCH_ITEMS} set > DecoratorItem instances >
   * normal VisualItem instances. A zero score is returned for normal items,
   * with scores starting at 1&lt;&lt;27 for other items, leaving the number
   * range beneath that value open for additional nuanced scoring.
   * </p>
   * 
   * @param item
   *          the VisualItem to provide an ordering score
   * @return the ordering score
   */
  @Override
  public int score(VisualItem item)
  {
    int type = 0;

    if (item instanceof NodeItem)
    {
      type = COMPONENT;
    }

    else if (item instanceof EdgeItem)
    {
      type = EDGE;
    }

    else if (item instanceof AggregateItem)
    {
      type = MOLECULE;

      // Need to globally define some kindof labels for this.
      try
      {
        item.getString("molecule");
      }
      catch (ArrayIndexOutOfBoundsException e)
      {
        type = BUBBLE;
      }
    }

    else if (item instanceof DecoratorItem)
    {
      type = DECORATOR;
    }

    int score = (1 << (26 + type));
    /*
     * if ( item.isHover() ) { score += (1<<25); }
     */
    /*
     * if ( item.isHighlighted() ) { score += (1<<24); }
     */
    /*
     * if ( item.isInGroup(Visualization.FOCUS_ITEMS) ) { score += (1<<23); }
     */
    if (item.isInGroup(Visualization.SEARCH_ITEMS))
    {
      score += (1 << 22);
    }

    return score;
  }
}
