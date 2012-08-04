package za.co.quirk.layout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * PlanLayout is a simple layout manager that arranges widgets in columns 
 * and rows like a spreadsheet.  All columns and rows are the same width 
 * and height respectively. A widget can be specified to span multiple rows
 * or columns in its <code>PlanData</code> layout data object. A widget 
 * will be resized to fit into its allotted cells exactly.
 * <p>
 * The number of rows and columns can be set when <code>PlanLayout</code> is
 * constructed. This can be later changed by assigning a new value and calling
 * <code>layout()</code> on the <code>Composite</code>.
 * </p>
 * <p>
 * The following code creates a 4 column by 5 row <code>PlanLayout</code> and
 * adds two widgets to it:
 * </p>
 * <pre>
 * public static void main( String args[] )
 * {
 *   // Create a shell
 *   Display display = new Display();
 *   Shell shell = new Shell( display );
 * <spc>
 *   PlanLayout layout = new PlanLayout( 4, 5 );
 *   shell.setLayout( layout );
 * <spc>
 *   // Add some widgets
 *   Button btnTop = new Button( shell, SWT.NONE );
 *   btnTop.setLayoutData( new PlanData( 0, 0, 4, 1 ) );
 *   btnTop.setText( "Top" );
 *   Button btnLeft = new Button( shell, SWT.NONE );
 *   btnLeft.setLayoutData( new PlanData( 0, 1, 1, 4 ) );
 *   btnLeft.setText( "Left" );  
 * <spc>
 *   // Show the shell
 *   shell.setSize( 100, 150 );
 *   shell.open();
 * <spc>
 *   while( !shell.isDisposed() )
 *   {
 *     if( !display.readAndDispatch(  ) )
 *     {
 *       display.sleep();
 *     }
 *   }
 * <spc>
 *   display.dispose(); 
 * }
 * </pre>
 * 
 * @author Craig Raw
 *
 * @version 1.0
 */
public class PlanLayout extends Layout
{
  public int numColumns = 1;
  public int numRows = 1;

  /**
   * Constructs an instance of PlanLayout.  This PlanLayout will have one row
   * and one column.
   */
  public PlanLayout()
  {

  }

  /**
   * Constructs an instance of LatticeLayout.
   *
   * @param numColumns    Number of columns in this layout
   * @param numRows       Number of rows in this layout
   */
  public PlanLayout( int numColumns, int numRows )
  {
    this.numColumns = numColumns;
    this.numRows = numRows;
  }

  /**
   * Computes and returns the size of the specified
   * composite's client area according to this layout.
   * <p>
   * This method computes the minimum size that the
   * client area of the composite must be in order to
   * position all children at their minimum size inside
   * the composite according to the layout algorithm
   * encoded by this layout.
   * </p>
   * <p>
   * When a width or height hint is supplied, it is
   * used to constrain the result. For example, if a
   * width hint is provided that is less than the minimum
   * width of the client area, the layout may choose
   * to wrap and increase height, clip, overlap, or
   * otherwise constrain the children.
   * </p>
   *
   * @param composite a composite widget using this layout
   * @param wHint width (<code>SWT.DEFAULT</code> for minimum)
   * @param hHint height (<code>SWT.DEFAULT</code> for minimum)
   * @param flushCache <code>true</code> means flush cached layout values
   * @return a point containing the computed size (width, height)
   * 
   * @see #layout
   * @see Control#getBorderWidth
   * @see Control#getBounds
   * @see Control#getSize
   * @see Control#pack
   * @see "computeTrim, getClientArea for controls that implement them"
   */
	protected Point computeSize( Composite composite, int wHint, int hHint, boolean flushCache )
	{
		int width = 0;
    int height = 0;
    
    if( wHint == SWT.DEFAULT || hHint == SWT.DEFAULT )
    {
      Control[] children = composite.getChildren();
      
      for( int i = 0; i < children.length; i++ ) 
			{
				PlanData data = (PlanData)children[i].getLayoutData();
        Point pntChild = children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false );
        
        width = Math.max( width, pntChild.x / data.width );
        height = Math.max( height, pntChild.y /data.height );
			}
      
    }
    
    width = ( wHint == SWT.DEFAULT ? width : wHint );
    height = ( hHint == SWT.DEFAULT ? height : hHint );
    
    return new Point( width, height );
	}

  /**
   * Lays out the children of the specified composite
   * according to this layout.
   * <p>
   * This method positions and sizes the children of a
   * composite using the layout algorithm encoded by this
   * layout. Children of the composite are positioned in
   * the client area of the composite. The position of
   * the composite is not altered by this method.
   * </p>
   * <p>
   * When the flush cache hint is true, the layout is
   * instructed to flush any cached values associated
   * with the children. Typically, a layout will cache
   * the preferred sizes of the children to avoid the
   * expense of computing these values each time the
   * widget is layed out.
   * </p>
   * <p>
   * When layout is triggered explicitly by the programmer
   * the flush cache hint is true. When layout is triggered
   * by a resize, either caused by the programmer or by the
   * user, the hint is false.
   * </p>
   *
   * @param composite a composite widget using this layout
   * @param flushCache <code>true</code> means flush cached layout values
   */
	protected void layout( Composite composite, boolean flushCache )
	{
    Rectangle rectClient = composite.getClientArea();
    
    int cellWidth = rectClient.width / numColumns;
    int cellHeight = rectClient.height / numRows;
	
    Control[] children = composite.getChildren();
    
    for( int i = 0; i < children.length; i++ ) 
		{
			PlanData data = (PlanData)children[i].getLayoutData();
      if( data == null )
      {
        data = new PlanData();
        children[i].setLayoutData( data );
      }       
      
      int x = cellWidth * data.x;
      int y = cellHeight * data.y;
      int width = cellWidth * data.width;
      int height = cellHeight * data.height;
      
      children[i].setBounds( x, y, width, height );
		}  
  }

}
