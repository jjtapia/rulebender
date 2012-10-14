package resultviewer.tree;

import java.util.List;
import org.eclipse.swt.graphics.Image;

import prefuse.Display;

/**
 * 
 * Species Node
 * 
 */
public class SpeciesNode extends TreeNode
{
  // species id
  private int id;
  // species name (id expression)
  private String name;
  // species expression
  private String expression = "";


  /**
   * 
   * @param parent
   *          parent node
   * @param name
   *          species name (id expression)
   */
  public SpeciesNode(ITreeNode parent, String name)
  {
    super("SpeciesNode", parent);
    this.name = name;
    String[] tmp = name.split(" ");
    this.id = Integer.parseInt(tmp[0]);
    if (tmp.length > 1)
    {
      this.expression = tmp[1];
    }
  }


  /**
   * 
   * @return species id
   */
  public int getId()
  {
    return this.id;
  }


  /**
   * @return species name (id expression)
   */
  public String getName()
  {
    return name;
  }


  /**
   * 
   * @return species expression
   */
  public String getExpression()
  {
    return this.expression;
  }


  /**
   * @return image
   */
  public Image getImage()
  {
    return null;
  }


  /**
   * Empty.
   */
  protected void createChildren(List children)
  {
  }


  /**
   * Whether has children.
   * 
   * @return false
   */
  public boolean hasChildren()
  {
    return false;
  }


  public String toString()
  {
    return this.getName();
  }

}
