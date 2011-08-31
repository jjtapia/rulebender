package za.co.quirk.layout;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;


/**
 * LatticeData is the layout data object associated with LatticeLayout. 
 * To set a LatticeData object into a control, you use the setLayoutData() method. 
 * <p>
 * A LatticeData object can be created in several ways. The first is to set the fields
 * directly, like this:
 * <pre>
 * LatticeData data = new LatticeData();
 * data.col1 = 0;
 * data.row1 = 0;
 * data.col2 = 2;
 * data.row2 = 1;
 * </pre>
 * Alternatively, you can pass in a <code>String</code> to acheive the same settings:
 * <pre>
 * LatticeData data = new LatticeData( "0, 0, 2, 1" );
 * </pre>
 * It is also possible to specify layout data in the constructor:
 * <pre>
 * LatticeData data = new LatticeData( 0, 0, 2, 1, LatticeConstants.FULL, LatticeConstants.FULL );
 * </pre>
 * </p>
 * <p>
 * NOTE: Do not reuse <code>LatticeData</code> objects. Every control in a 
 * <code>Composite</code> that is managed by a <code>LatticeLayout</code>
 * must have a unique <code>LatticeData</code> object. If the layout data 
 * for a control in a <code>LatticeLayout</code> is null at layout time, 
 * a unique <code>LatticeData</code> object is created for it.
 * </p> 
 *
 * @author  Daniel E. Barbalace
 * @author Craig Raw
 *
 * @version 1.0
 */
public class LatticeData implements LatticeConstants
{
  /** Cell in which the upper left corner of the component lays */
  public int col1;

  /** Cell in which the upper left corner of the component lays */
  public int row1;

  /** Cell in which the lower right corner of the component lays */
  public int col2;

  /** Cell in which the lower right corner of the component lays */
  public int row2;

  /** Horizontal justification if component occupies just one cell */
  public int hAlign;

  /** Verical justification if component occupies just one cell */
  public int vAlign;

  /**
   * Constructs an LatticeData with the default settings.  This
   * constructor is equivalent to LatticeLayoutConstraints(0, 0, 0, 0, FULL, FULL).
   */
  public LatticeData()
  {
    col1 = row1 = col2 = col2 = 0;
    hAlign = vAlign = FULL;
  }

  /**
   * Constructs an LatticeData from a string.
   *
   * @param constraints    indicates LatticeData's position and justification
   *                       as a string in the form "row, column" or
   *                       "row, column, horizontal justification, vertical
   *                       justification" or "row 1, column 1, row 2, column 2".
   *                       It is also acceptable to delimit the paramters with
   *                       spaces instead of commas.
   */
  public LatticeData( String constraints )
  {
    // Parse constraints using spaces or commas
    StringTokenizer st = new StringTokenizer( constraints, ", " );

    // Use default values for any parameter not specified or specified
    // incorrectly.  The default parameters place the component in a single
    // cell at column 0, row 0.  The component is fully justified.
    col1 = 0;
    row1 = 0;
    col2 = 0;
    row2 = 0;
    hAlign = FULL;
    vAlign = FULL;

    String token = null;

    try
    {
      // Get the first column (assume component is in only one column)
      token = st.nextToken();
      col1 = new Integer( token ).intValue();
      col2 = col1;

      // Get the first row (assume component is in only one row)
      token = st.nextToken();
      row1 = new Integer( token ).intValue();
      row2 = row1;

      // Get the second column
      token = st.nextToken();
      col2 = new Integer( token ).intValue();

      // Get the second row
      token = st.nextToken();
      row2 = new Integer( token ).intValue();
    }
    catch( NoSuchElementException error ) {}
    catch( NumberFormatException error )
    {
      try
      {
        // Check if token means horizontally justification the component
        if( token.equalsIgnoreCase( "L" ) )
        {
          hAlign = LEFT;
        }
        else if( token.equalsIgnoreCase( "C" ) )
        {
          hAlign = CENTER;
        }
        else if( token.equalsIgnoreCase( "F" ) )
        {
          hAlign = FULL;
        }
        else if( token.equalsIgnoreCase( "R" ) )
        {
          hAlign = RIGHT;
        }

        // There can be one more token for the vertical justification even
        // if the horizontal justification is invalid
        token = st.nextToken();

        // Check if token means horizontally justification the component
        if( token.equalsIgnoreCase( "T" ) )
        {
          vAlign = TOP;
        }
        else if( token.equalsIgnoreCase( "C" ) )
        {
          vAlign = CENTER;
        }
        else if( token.equalsIgnoreCase( "F" ) )
        {
          vAlign = FULL;
        }
        else if( token.equalsIgnoreCase( "B" ) )
        {
          vAlign = BOTTOM;
        }
      }
      catch( NoSuchElementException error2 ) {}
    }

    // Make sure row2 >= row1
    if( row2 < row1 )
    {
      row2 = row1;
    }

    // Make sure col2 >= col1
    if( col2 < col1 )
    {
      col2 = col1;
    }
  }

  /**
   * Constructs an LatticeData a set of constraints.
   *
   * @param col1      column where upper-left cornor of the component is placed
   * @param row1      row where upper-left cornor of the component is placed
   * @param col2      column where lower-right cornor of the component is placed
   * @param row2      row where lower-right cornor of the component is placed
   * @param hAlign    horizontal justification of a component in a single cell
   * @param vAlign    vertical justification of a component in a single cell
   */
  public LatticeData( int col1, int row1, int col2, int row2, int hAlign, int vAlign )
  {
    this.col1 = col1;
    this.row1 = row1;
    this.col2 = col2;
    this.row2 = row2;

    if( ( hAlign < MIN_ALIGN ) || ( hAlign > MAX_ALIGN ) )
    {
      this.hAlign = FULL;
    }
    else
    {
      this.hAlign = hAlign;
    }

    if( ( vAlign < MIN_ALIGN ) || ( vAlign > MAX_ALIGN ) )
    {
      this.vAlign = FULL;
    }
    else
    {
      this.vAlign = vAlign;
    }
  }

  /**
   * Gets a string representation of this LatticeData.
   *
   * @return a string in the form "LatticeData{ row, column, row, column }" or
   *         "LatticeData{ row, column, horizontal justification, vertical justification }"
   */
  public String toString()
  {
    StringBuffer buffer = new StringBuffer( "LatticeData{ " );

    buffer.append( row1 );
    buffer.append( ", " );
    buffer.append( col1 );
    buffer.append( ", " );

    if( ( row1 == row2 ) && ( col1 == col2 ) )
    {
      final char[] h = { 'L', 'C', 'F', 'R' };
      final char[] v = { 'T', 'C', 'F', 'B' };

      buffer.append( h[hAlign] );
      buffer.append( ", " );
      buffer.append( v[vAlign] );
    }
    else
    {
      buffer.append( row2 );
      buffer.append( ", " );
      buffer.append( col2 );
    }

    buffer.append( " }" );

    return buffer.toString();
  }
}
