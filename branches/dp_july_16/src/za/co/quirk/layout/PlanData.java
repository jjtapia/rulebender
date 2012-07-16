package za.co.quirk.layout;

/**
 * PlanData is the layout data object associated with PlanLayout. 
 * To set a PlanData object into a control, you use the setLayoutData() method. 
 * <p>
 * A PlanData object can be created in several ways. The first is to set the fields
 * directly, like this:
 * <pre>
 * PlanData data = new PlanData();
 * data.x = 0;
 * data.y = 0;
 * data.width = 2;
 * data.height = 1;
 * </pre>
 * It is also possible to specify layout data in the constructor:
 * <pre>
 * PlanData data = new PlanData( 0, 0, 2, 1 );
 * </pre>
 * </p>
 * <p>
 * NOTE: Do not reuse <code>PlanData</code> objects. Every control in a 
 * <code>Composite</code> that is managed by a <code>PlanLayout</code>
 * must have a unique <code>PlanData</code> object. If the layout data 
 * for a control in a <code>PlanLayout</code> is null at layout time, 
 * a unique <code>PlanData</code> object is created for it.
 * </p> 
 * 
 * @author Craig Raw
 *
 * @version 1.0
 */
public class PlanData
{
  /** Column where upper-left cornor of the component is placed (zero-based) */
  public int x = 0;
  
  /** Row where upper-left cornor of the component is placed (zero-based) */
  public int y = 0;
  
  /** Width of the cell in columns */
  public int width = 1;
  
  /** Height of the cell in rows */
  public int height = 1;
  
  /**
   * Constructs an PlanData with the default settings.  This
   * constructor is equivalent to LatticeLayoutConstraints(0, 0, 0, 0, FULL, FULL).
   */  
  public PlanData() {}
  
  /**
   * Constructs a PlanData with the provided settings.
   *
   * @param x         column where upper-left cornor of the component is placed
   * @param y         row where upper-left cornor of the component is placed
   * @param width     width of the cell in columns
   * @param height    height of the cell in rows
   */
  public PlanData( int x, int y, int width, int height ) 
  {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }  

  /**
   * Gets a string representation of this PlanData.
   *
   * @return a string in the form "PlanData{ x, y, width, height }"
   */  
  public String toString()
  {
    StringBuffer strb = new StringBuffer( "PlanData { " );
    strb.append( x );
    strb.append( " ," );
    strb.append( y );     
    strb.append( " ," );
    strb.append( width ); 
    strb.append( " ," );
    strb.append( height );  
    strb.append( " }" );
    
    return strb.toString();       
  }
}
