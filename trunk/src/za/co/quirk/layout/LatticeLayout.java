package za.co.quirk.layout;


import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;


/**
 * LatticeLayout is a layout manager that arranges widgets in rows and columns
 * like a spreadsheet.  LatticeLayout allows each row or column to be a different
 * size.  A row or column can be given an absolute size in pixels, a percentage
 * of the available space, or it can grow and shrink to fill the remaining space
 * after other rows and columns have been resized.
 *
 * <p>Using spreadsheet terminology, a cell is the intersection of a row and
 * column.  Cells have finite, non-negative sizes measured in pixels.  The
 * dimensions of a cell depend solely upon the dimensions of its row and column.
 * </p>
 *
 * <p>A widget occupies a rectangular group of one or more cells.  If the
 * widget occupies more than one cell, the widget is resized to fit
 * perfectly in the rectangular region of cells.  If the widget occupies a
 * single cell, it can be aligned in four ways within that cell.</p>
 *
 * <p>A single cell widget can be stretched horizontally to fit the cell
 * (full justification), or it can be placed in the center of the cell.  The
 * widget could also be left justified or right justified.  Similarly, the
 * widget can be full, center, top, or bottom justified in the vertical.</p>
 *
 * <pre>
 * public static void main( String args[] )
 * {
 *   // Create a shell
 *   Display display = new Display();
 *   Shell shell = new Shell( display );
 * <spc>
 *   double border = 10;
 *   double size[][] = 
 *   {
 *     {border, 0.10, 20, LatticeLayout.FILL, 20, 0.20, border},   // Columns
 *     {border, 0.20, 20, LatticeLayout.FILL, 20, 0.20, border}    // Rows
 *   };
 * <spc>
 *   LatticeLayout layout = new LatticeLayout( size );
 *   shell.setLayout( layout );
 * <spc>
 *   // Add some widgets
 *   Button btnTop = new Button( shell, SWT.NONE );
 *   btnTop.setLayoutData( new LatticeData( "1, 1, 5, 1" ) );
 *   btnTop.setText( "Top" );
 *   Button btnBottom = new Button( shell, SWT.NONE );
 *   btnBottom.setLayoutData( new LatticeData( "1, 5, 5, 5" ) );
 *   btnBottom.setText( "Bottom" );
 *   Button btnLeft = new Button( shell, SWT.NONE );
 *   btnLeft.setLayoutData( new LatticeData( "1, 3      " ) );
 *   btnLeft.setText( "Left" );
 *   Button btnRight = new Button( shell, SWT.NONE );
 *   btnRight.setLayoutData( new LatticeData( "5, 3      " ) );
 *   btnRight.setText( "Right" );
 *   Button btnCenter = new Button( shell, SWT.NONE );
 *   btnCenter.setLayoutData( new LatticeData( "3, 3, c, c" ) );
 *   btnCenter.setText( "Center" );
 *   Button btnOverlap = new Button( shell, SWT.NONE );
 *   btnOverlap.setLayoutData( new LatticeData( "3, 3, 3, 5" ) );
 *   btnOverlap.setText( "Overlap" );
 * <spc>
 *   // Display the shell
 *   shell.pack();
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
 * @author  Daniel E. Barbalace
 * @author Craig Raw
 *
 * @version 1.0
 */
public class LatticeLayout extends Layout implements LatticeConstants
{
  /** Default row/column size */
  protected static final double[][] defaultSize = 
  {
    {  },
    {  }
  };

  /** Widths of columns expressed in absolute and relative terms */
  protected double[] columnSpec;

  /** Heights of rows expressed in absolute and relative terms */
  protected double[] rowSpec;

  /** Widths of columns in pixels */
  protected int[] columnSize;

  /** Heights of rows in pixels */
  protected int[] rowSize;

  /** Offsets of columns in pixels.  The left boarder of column n is at
   columnOffset[n] and the right boarder is at columnOffset[n + 1] for all
   columns including the last one.  columnOffset.length = columnSize.length + 1 */
  protected int[] columnOffset;

  /** Offsets of rows in pixels.  The left boarder of row n is at
   rowOffset[n] and the right boarder is at rowOffset[n + 1] for all
   rows including the last one.  rowOffset.length = rowSize.length + 1 */
  protected int[] rowOffset;

  /** Indicates whether or not the size of the cells are known for the last known
   size of the container.  If dirty is true or the container has been resized,
   the cell sizes must be recalculated using calculateSize. */
  protected boolean dirty;

  /** Previous known width of the container */
  protected int oldWidth;

  /** Previous known height of the container */
  protected int oldHeight;

  //******************************************************************************
  //** Constructors                                                            ***
  //******************************************************************************

  /**
   * Constructs an instance of LatticeLayout.  This LatticeLayout will have one row
   * and one column.
   */
  public LatticeLayout()
  {
    this( defaultSize );
  }

  /**
   * Constructs an instance of LatticeLayout.
   *
   * @param size    widths of columns and heights of rows in the format,
   *                {{col0, col1, col2, ..., colN}, {row0, row1, row2, ..., rowM}}
   *                If this parameter is invalid, the LatticeLayout will have
   *                exactly one row and one column.
   */
  public LatticeLayout( double[][] size )
  {
    // Make sure rows and columns and nothing else is specified
    if( ( size != null ) && ( size.length == 2 ) )
    {
      // Get the rows and columns
      double[] tempCol = size[0];
      double[] tempRow = size[1];

      // Create new rows and columns
      columnSpec = new double[tempCol.length];
      rowSpec = new double[tempRow.length];

      // Copy rows and columns
      System.arraycopy( tempCol, 0, columnSpec, 0, columnSpec.length );
      System.arraycopy( tempRow, 0, rowSpec, 0, rowSpec.length );

      // Make sure rows and columns are valid
      for( int counter = 0; counter < columnSpec.length; counter++ )
      {
        if( ( columnSpec[counter] < 0.0 ) && ( columnSpec[counter] != FILL ) && ( columnSpec[counter] != PREFERRED ) && ( columnSpec[counter] != MINIMUM ) )
        {
          columnSpec[counter] = 0.0;
        }
      }

      for( int counter = 0; counter < rowSpec.length; counter++ )
      {
        if( ( rowSpec[counter] < 0.0 ) && ( rowSpec[counter] != FILL ) && ( rowSpec[counter] != PREFERRED ) && ( rowSpec[counter] != MINIMUM ) )
        {
          rowSpec[counter] = 0.0;
        }
      }
    }
    else
    {
      double[] tempCol = { FILL };
      double[] tempRow = { FILL };

      setColumn( tempCol );
      setRow( tempRow );
    }

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Adjusts the number and sizes of rows in this layout.  After calling this
   * method, the caller should request this layout manager to perform the
   * layout.  This can be done with the following code:
   *
   * <pre>
   *     composite.layout();
   * </pre>
   *
   *
   * If this is not done, the changes in the layout will not be seen until the
   * container is resized.
   *
   * @param column    heights of each of the columns
   *
   * @see #getColumn
   */
  public void setColumn( double[] column )
  {
    // Copy columns
    columnSpec = new double[column.length];
    System.arraycopy( column, 0, columnSpec, 0, columnSpec.length );

    // Make sure columns are valid
    for( int counter = 0; counter < columnSpec.length; counter++ )
    {
      if( ( columnSpec[counter] < 0.0 ) && ( columnSpec[counter] != FILL ) && ( columnSpec[counter] != PREFERRED ) && ( columnSpec[counter] != MINIMUM ) )
      {
        columnSpec[counter] = 0.0;
      }
    }

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Adjusts the number and sizes of rows in this layout.  After calling this
   * method, the caller should request this layout manager to perform the
   * layout.  This can be done with the following code:
   *
   * <code>
   *     composite.layout();
   * </code>
   *
   *
   * If this is not done, the changes in the layout will not be seen until the
   * container is resized.
   *
   * @param row    widths of each of the rows.  This parameter cannot be null.
   *
   * @see #getRow
   */
  public void setRow( double[] row )
  {
    // Copy rows
    rowSpec = new double[row.length];
    System.arraycopy( row, 0, rowSpec, 0, rowSpec.length );

    // Make sure rows are valid
    for( int counter = 0; counter < rowSpec.length; counter++ )
    {
      if( ( rowSpec[counter] < 0.0 ) && ( rowSpec[counter] != FILL ) && ( rowSpec[counter] != PREFERRED ) && ( rowSpec[counter] != MINIMUM ) )
      {
        rowSpec[counter] = 0.0;
      }
    }

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Adjusts the width of a single column in this layout.  After calling this
   * method, the caller should request this layout manager to perform the
   * layout.  This can be done with the following code:
   *
   * <code>
   *     composite.layout();
   * </code>
   *
   * If this is not done, the changes in the layout will not be seen until the
   * container is resized.
   *
   * @param i       zero-based index of column to set.  If this parameter is not
   *                valid, an ArrayOutOfBoundsException will be thrown.
   * @param size    width of the column.  This parameter cannot be null.
   *
   * @see #getColumn
   */
  public void setColumn( int i, double size )
  {
    // Make sure size is valid
    if( ( size < 0.0 ) && ( size != FILL ) && ( size != PREFERRED ) && ( size != MINIMUM ) )
    {
      size = 0.0;
    }

    // Copy new size
    columnSpec[i] = size;

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Adjusts the height of a single row in this layout.  After calling this
   * method, the caller should request this layout manager to perform the
   * layout.  This can be done with the following code:
   *
   * <code>
   *     composite.layout();
   * </code>
   *
   *
   * If this is not done, the changes in the layout will not be seen until the
   * container is resized.
   *
   * @param i       zero-based index of row to set.  If this parameter is not
   *                valid, an ArrayOutOfBoundsException will be thrown.
   * @param size    height of the row.  This parameter cannot be null.
   *
   * @see #getRow
   */
  public void setRow( int i, double size )
  {
    // Make sure size is valid
    if( ( size < 0.0 ) && ( size != FILL ) && ( size != PREFERRED ) && ( size != MINIMUM ) )
    {
      size = 0.0;
    }

    // Copy new size
    rowSpec[i] = size;

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Gets the sizes of columns in this layout.
   *
   * @return widths of each of the columns
   *
   * @see #setColumn
   */
  public double[] getColumn()
  {
    // Copy columns
    double[] column = new double[columnSpec.length];
    System.arraycopy( columnSpec, 0, column, 0, column.length );

    return column;
  }

  /**
   * Gets the height of a single row in this layout.
   *
   * @return height of the requested row
   *
   * @see #setRow
   */
  public double[] getRow()
  {
    // Copy rows
    double[] row = new double[rowSpec.length];
    System.arraycopy( rowSpec, 0, row, 0, row.length );

    return row;
  }

  /**
   * Gets the width of a single column in this layout.
   *
   * @param i    zero-based index of row to get.  If this parameter is not valid,
   *             an ArrayOutOfBoundsException will be thrown.
   *
   * @return width of the requested column
   *
   * @see #setRow
   */
  public double getColumn( int i )
  {
    return columnSpec[i];
  }

  /**
   * Gets the sizes of a row in this layout.
   *
   * @param i    zero-based index of row to get.  If this parameter is not valid,
   *             an ArrayOutOfBoundsException will be thrown.
   *
   * @return height of each of the requested row
   *
   * @see #setRow
   */
  public double getRow( int i )
  {
    return rowSpec[i];
  }

  /**
   * Gets the number of columns in this layout.
   *
   * @return the number of columns
   */
  public int getNumColumn()
  {
    return columnSpec.length;
  }

  /**
   * Gets the number of rows in this layout.
   *
   * @return the number of rows
   */
  public int getNumRow()
  {
    return rowSpec.length;
  }

  //******************************************************************************
  //** Insertion/Deletion methods                                              ***
  //******************************************************************************

  /**
   * Inserts a column in this layout.  All widgets to the right of the
   * insertion point are moved right one column.  The container will need to
   * be laid out after this method returns.  See <code>setColumn</code>.
   *
   * @param i       zero-based index at which to insert the column.
   * @param size    size of the column to be inserted
   *
   * @see #deleteColumn
   */
  public void insertColumn( Composite composite, int i, double size )
  {
    // Make sure position is valid
    if( ( i < 0 ) || ( i > columnSpec.length ) )
    {
      throw new IllegalArgumentException( "Parameter i is invalid.  i = " + i + ".  Valid range is [0, " + columnSpec.length + "]." );
    }

    // Make sure column size is valid
    if( ( size < 0.0 ) && ( size != FILL ) && ( size != PREFERRED ) && ( size != MINIMUM ) )
    {
      size = 0.0;
    }

    // Copy columns
    double[] column = new double[columnSpec.length + 1];
    System.arraycopy( columnSpec, 0, column, 0, i );
    System.arraycopy( columnSpec, i, column, i + 1, columnSpec.length - i );

    // Insert column
    column[i] = size;
    columnSpec = column;

    // Move all widgets that are to the right of new row
    Control[] children = composite.getChildren();

    for( int j = 0; j < children.length; j++ )
    {
      LatticeData data = (LatticeData)children[j].getLayoutData();
      if( data == null )
      {
        data = new LatticeData();
        children[j].setLayoutData( data );
      }

      // Is the first column to the right of the new column
      if( data.col1 >= i )
      {
        // Move first column
        data.col1++;
      }

      // Is the second column to the right of the new column
      if( data.col2 >= i )
      {
        // Move second column
        data.col2++;
      }
    }

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Inserts a row in this layout.  All widgets below the insertion point
   * are moved down one row.  The container will need to be laid out after this
   * method returns.  See <code>setRow</code>.
   *
   * @param i       zero-based index at which to insert the column.
   * @param size    size of the row to be inserted
   *
   * @see #setRow
   * @see #deleteRow
   */
  public void insertRow( Composite composite, int i, double size )
  {
    // Make sure position is valid
    if( ( i < 0 ) || ( i > rowSpec.length ) )
    {
      throw new IllegalArgumentException( "Parameter i is invalid.  i = " + i + ".  Valid range is [0, " + rowSpec.length + "]." );
    }

    // Make sure row size is valid
    if( ( size < 0.0 ) && ( size != FILL ) && ( size != PREFERRED ) && ( size != MINIMUM ) )
    {
      size = 0.0;
    }

    // Copy rows
    double[] row = new double[rowSpec.length + 1];
    System.arraycopy( rowSpec, 0, row, 0, i );
    System.arraycopy( rowSpec, i, row, i + 1, rowSpec.length - i );

    // Insert row
    row[i] = size;
    rowSpec = row;

    // Move all widgets that are below the new row
    Control[] children = composite.getChildren();

    for( int j = 0; j < children.length; j++ )
    {
      LatticeData data = (LatticeData)children[j].getLayoutData();
      if( data == null )
      {
        data = new LatticeData();
        children[j].setLayoutData( data );
      }      

      // Is the first row to the right of the new row
      if( data.row1 >= i )
      {
        // Move first row
        data.row1++;
      }

      // Is the second row to the right of the new row
      if( data.row2 >= i )
      {
        // Move second row
        data.row2++;
      }
    }

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Deletes a column in this layout.  All widgets to the right of the
   * deletion point are moved left one column.  The container will need to
   * be laid out after this method returns.  See <code>setColumn</code>.
   *
   * @param i    zero-based index of column to delete
   *
   * @see #setColumn
   * @see #deleteColumn
   */
  public void deleteColumn( Composite composite, int i )
  {
    // Make sure position is valid
    if( ( i < 0 ) || ( i >= columnSpec.length ) )
    {
      throw new IllegalArgumentException( "Parameter i is invalid.  i = " + i + ".  Valid range is [0, " + ( columnSpec.length - 1 ) + "]." );
    }

    // Copy columns
    double[] column = new double[columnSpec.length - 1];
    System.arraycopy( columnSpec, 0, column, 0, i );
    System.arraycopy( columnSpec, i + 1, column, i, columnSpec.length - i - 1 );

    // Delete column
    columnSpec = column;

    // Move all widgets that are to the right of row deleted
    Control[] children = composite.getChildren();

    for( int j = 0; j < children.length; j++ )
    {
      LatticeData data = (LatticeData)children[j].getLayoutData();
      if( data == null )
      {
        data = new LatticeData();
        children[j].setLayoutData( data );
      }      

      // Is the first column to the right of the new column
      if( data.col1 >= i )
      {
        // Move first column
        data.col1--;
      }

      // Is the second column to the right of the new column
      if( data.col2 >= i )
      {
        // Move second column
        data.col2--;
      }
    }

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  /**
   * Deletes a row in this layout.  All widgets below the deletion point are
   * moved up one row.  The container will need to be laid out after this method
   * returns.  See <code>setRow</code>.  There must be at least two rows in order
   * to delete a row.
   *
   * @param i    zero-based index of column to delete
   *
   * @see #setRow
   * @see #deleteRow
   */
  public void deleteRow( Composite composite, int i )
  {
    // Make sure position is valid
    if( ( i < 0 ) || ( i >= rowSpec.length ) )
    {
      throw new IllegalArgumentException( "Parameter i is invalid.  i = " + i + ".  Valid range is [0, " + ( rowSpec.length - 1 ) + "]." );
    }

    // Copy rows
    double[] row = new double[rowSpec.length - 1];
    System.arraycopy( rowSpec, 0, row, 0, i );
    System.arraycopy( rowSpec, i + 1, row, i, rowSpec.length - i - 1 );

    // Delete row
    rowSpec = row;

    // Move all widgets that are to below the row deleted
    Control[] children = composite.getChildren();

    for( int j = 0; j < children.length; j++ )
    {
      LatticeData data = (LatticeData)children[j].getLayoutData();
      if( data == null )
      {
        data = new LatticeData();
        children[j].setLayoutData( data );
      }      

      // Is the first row below the new row
      if( data.row1 >= i )
      {
        // Move first row
        data.row1--;
      }

      // Is the second row below the new row
      if( data.row2 >= i )
      {
        // Move second row
        data.row2--;
      }
    }

    // Indicate that the cell sizes are not known
    dirty = true;
  }

  //******************************************************************************
  //** Misc methods                                                            ***
  //******************************************************************************

  /**
   * Converts this TableLayout to a string.
   *
   * @return a string representing the columns and row sizes in the form
   *         "{{col0, col1, col2, ..., colN}, {row0, row1, row2, ..., rowM}}"
   */
  public String toString()
  {
    int counter;

    String value = "TableLayout {{";

    if( columnSpec.length > 0 )
    {
      for( counter = 0; counter < ( columnSpec.length - 1 ); counter++ )
        value += ( columnSpec[counter] + ", " );

      value += ( columnSpec[columnSpec.length - 1] + "}, {" );
    }
    else
    {
      value += "}, {";
    }

    if( rowSpec.length > 0 )
    {
      for( counter = 0; counter < ( rowSpec.length - 1 ); counter++ )
        value += ( rowSpec[counter] + ", " );

      value += ( rowSpec[rowSpec.length - 1] + "}}" );
    }
    else
    {
      value += "}}";
    }

    return value;
  }

  /**
   * Draws a grid on the given container.  This is useful for seeing where the
   * rows and columns go.  In the container's paint method, call this method.
   *
   * @param container    container using this TableLayout
   * @param g            graphics content of container (can be offscreen)
   */
  public void drawGrid( Composite composite, GC gc )
  {
    // Calculate the sizes of the rows and columns
    Point d = composite.getSize();

    if( dirty || ( d.x != oldWidth ) || ( d.y != oldHeight ) )
    {
      calculateSize( composite );
    }

    // Initialize y
    int y = 0;

    int[] colors = { SWT.COLOR_WHITE, SWT.COLOR_BLUE, SWT.COLOR_RED, SWT.COLOR_GREEN, SWT.COLOR_YELLOW, SWT.COLOR_BLACK, SWT.COLOR_CYAN, SWT.COLOR_DARK_BLUE, SWT.COLOR_DARK_GREEN, SWT.COLOR_DARK_RED, SWT.COLOR_DARK_YELLOW, SWT.COLOR_MAGENTA, SWT.COLOR_DARK_MAGENTA };

    for( int row = 0; row < rowSize.length; row++ )
    {
      // Initialize x
      int x = 0;

      for( int column = 0; column < columnSize.length; column++ )
      {
        // Use a random color to make things easy to see
        Color color = composite.getDisplay().getSystemColor( colors[( row + column ) % colors.length] );
        gc.setBackground( color );

        // Draw the cell as a solid rectangle
        gc.fillRectangle( x, y, columnSize[column], rowSize[row] );

        // Increment x
        x += columnSize[column];
      }

      // Increment y
      y += rowSize[row];
    }
  }

  /**
   * Determines whether or not there are any hidden widgets.  A hidden
   * widget is one that will not be shown with this layout's current
   * configuration.  Such a widget is, at least partly, in an invalid row
   * or column.  For example, on a table with five rows, row -1 and row 5 are both
   * invalid.  Valid rows are 0 through 4, inclusively.
   *
   * @return    True, if there are any hidden widgets.  False, otherwise.
   *
   * @see #overlapping
   */
  public boolean hidden( Composite composite )
  {
    // Assume no widgets are hidden
    boolean hidden = false;

    // Check all widgets
    Control[] children = composite.getChildren();

    for( int i = 0; i < children.length; i++ )
    {
      LatticeData data = (LatticeData)children[i].getLayoutData();
      if( data == null )
      {
        data = new LatticeData();
        children[i].setLayoutData( data );
      }      

      // Is this widget valid
      if( ( data.row1 < 0 ) || ( data.col1 < 0 ) || ( data.row2 > rowSpec.length ) || ( data.col2 > columnSpec.length ) )
      {
        hidden = true;

        break;
      }
    }

    return hidden;
  }

  /**
   * Determines whether or not there are any overlapping widgets.  Two
   * widgets overlap if they cover at least one common cell.
   *
   * @return    True, if there are any overlapping widgets.  False, otherwise.
   *
   * @see #hidden
   */
  public boolean overlapping( Composite composite )
  {
    Control[] children = composite.getChildren();

    // Count contraints
    int numEntry = children.length;

    // If there are no widgets, they can't be overlapping
    if( numEntry == 0 )
    {
      return false;
    }

    // Assume no widgets are overlapping
    boolean overlapping = false;

    LatticeData[] data = new LatticeData[numEntry];

    for( int i = 0; i < data.length; i++ )
    {
      LatticeData d = (LatticeData)children[i].getLayoutData();
      if( d == null )
      {
        d = new LatticeData();
        children[i].setLayoutData( d );
      }    
        
      data[i] = d;
    }

    // Check all widgets
    for( int knowUnique = 1; knowUnique < numEntry; knowUnique++ )
    {
      for( int checking = knowUnique - 1; checking >= 0; checking-- )
      {
        if( ( ( data[checking].col1 >= data[knowUnique].col1 ) && ( data[checking].col1 <= data[knowUnique].col2 ) && ( data[checking].row1 >= data[knowUnique].row1 ) && ( data[checking].row1 <= data[knowUnique].row2 ) ) || ( ( data[checking].col2 >= data[knowUnique].col1 ) && ( data[checking].col2 <= data[knowUnique].col2 ) && ( data[checking].row2 >= data[knowUnique].row1 ) && ( data[checking].row2 <= data[knowUnique].row2 ) ) )
        {
          overlapping = true;

          break;
        }
      }
    }

    return overlapping;
  }

  /**
   * Calculates the sizes of the rows and columns based on the absolute and
   * relative sizes specified in <code>rowSpec</code> and <code>columnSpec</code>
   * and the size of the container.  The result is stored in <code>rowSize</code>
   * and <code>columnSize</code>.
   *
   * @param composite    container using this TableLayout
   */
  protected void calculateSize( Composite composite )
  {
    int counter;  // Counting variable;

    // Get number of rows and columns
    int numColumn = columnSpec.length;
    int numRow = rowSpec.length;

    // Create array to hold actual sizes in pixels
    columnSize = new int[numColumn];
    rowSize = new int[numRow];

    // Get the size of the container's available space
    Rectangle rectClient = composite.getClientArea();
    int totalWidth = rectClient.width;
    int totalHeight = rectClient.height;

    // Initially, the available space is the total space
    int availableWidth = totalWidth;
    int availableHeight = totalHeight;

    // Assign absolute widths; this reduces available width
    for( counter = 0; counter < numColumn; counter++ )
    {
      // Is the current column an absolue size
      if( ( columnSpec[counter] >= 1.0 ) || ( columnSpec[counter] == 0.0 ) )
      {
        // Assign absolute width
        columnSize[counter] = (int)( columnSpec[counter] + 0.5 );

        // Reduce available width
        availableWidth -= columnSize[counter];
      }
    }

    // Assign absolute heights; this reduces available height
    for( counter = 0; counter < numRow; counter++ )
    {
      // Is the current column an absolue size
      if( ( rowSpec[counter] >= 1.0 ) || ( rowSpec[counter] == 0.0 ) )
      {
        // Assign absolute width
        rowSize[counter] = (int)( rowSpec[counter] + 0.5 );

        // Reduce available width
        availableHeight -= rowSize[counter];
      }
    }

    // Assign preferred and minimum widths; this reduces available width.
    // Assignment of preferred/minimum with is like assignment of absolute
    // widths except that each column must determine the maximum
    // preferred/minimum width of the widgets that are completely contained
    // within the column.
    for( counter = 0; counter < numColumn; counter++ )
    {
      // Is the current column a preferred size
      if( ( columnSpec[counter] == PREFERRED ) || ( columnSpec[counter] == MINIMUM ) )
      {
        // Assume a maximum width of zero
        int maxWidth = 0;

        // Find maximum preferred width of all widgets completely
        // contained within this column
        Control[] children = composite.getChildren();

        for( int i = 0; i < children.length; i++ )
        {
          LatticeData data = (LatticeData)children[i].getLayoutData();
          if( data == null )
          {
            data = new LatticeData();
            children[i].setLayoutData( data );
          }           
          
          if( ( data.col1 == counter ) && ( data.col2 == counter ) )
          {
            Point p = ( columnSpec[counter] == PREFERRED ) ? children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false ) : children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false );

            int width = ( p == null ) ? 0 : p.x;

            if( maxWidth < width )
            {
              maxWidth = width;
            }
          }
        }

        // Assign preferred width
        columnSize[counter] = maxWidth;

        // Reduce available width
        availableWidth -= maxWidth;
      }
    }

    // Assign preferred and minimum heights; this reduces available height.
    // Assignment of preferred/minimum with is like assignment of absolute
    // heights except that each row must determine the maximum
    // preferred/minimum height of the widgets that are completely contained
    // within the row.
    for( counter = 0; counter < numRow; counter++ )
    {
      // Is the current row a preferred size
      if( ( rowSpec[counter] == PREFERRED ) || ( rowSpec[counter] == MINIMUM ) )
      {
        // Assume a maximum height of zero
        int maxHeight = 0;

        // Find maximum preferred height of all widgets completely
        // contained within this row
        Control[] children = composite.getChildren();

        for( int i = 0; i < children.length; i++ )
        {
          LatticeData data = (LatticeData)children[i].getLayoutData();
          if( data == null )
          {
            data = new LatticeData();
            children[i].setLayoutData( data );
          }           

          if( ( data.row1 == counter ) && ( data.row2 == counter ) )
          {
            Point p = ( rowSpec[counter] == PREFERRED ) ? children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false ) : children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false );

            int height = ( p == null ) ? 0 : p.y;

            if( maxHeight < height )
            {
              maxHeight = height;
            }
          }
        }

        // Assign preferred height
        rowSize[counter] = maxHeight;

        // Reduce available height
        availableHeight -= maxHeight;
      }
    }

    // Remember how much space is available for relatively sized cells
    int relativeWidth = availableWidth;
    int relativeHeight = availableHeight;

    // Make sure relativeWidth and relativeHeight are non-negative
    if( relativeWidth < 0 )
    {
      relativeWidth = 0;
    }

    if( relativeHeight < 0 )
    {
      relativeHeight = 0;
    }

    // Assign relative widths
    for( counter = 0; counter < numColumn; counter++ )
    {
      // Is the current column an relative size
      if( ( columnSpec[counter] > 0.0 ) && ( columnSpec[counter] < 1.0 ) )
      {
        // Assign relative width
        columnSize[counter] = (int)( ( columnSpec[counter] * relativeWidth ) + 0.5 );

        // Reduce available width
        availableWidth -= columnSize[counter];
      }
    }

    // Assign relative heights
    for( counter = 0; counter < numRow; counter++ )
    {
      // Is the current row an relative size
      if( ( rowSpec[counter] > 0.0 ) && ( rowSpec[counter] < 1.0 ) )
      {
        // Assign relative height
        rowSize[counter] = (int)( ( rowSpec[counter] * relativeHeight ) + 0.5 );

        // Reduce available height
        availableHeight -= rowSize[counter];
      }
    }

    // Make sure availableWidth and availableHeight are non-negative
    if( availableWidth < 0 )
    {
      availableWidth = 0;
    }

    if( availableHeight < 0 )
    {
      availableHeight = 0;
    }

    // Count the number of "fill" cells
    int numFillWidth = 0;
    int numFillHeight = 0;

    for( counter = 0; counter < numColumn; counter++ )
    {
      if( columnSpec[counter] == FILL )
      {
        numFillWidth++;
      }
    }

    for( counter = 0; counter < numRow; counter++ )
    {
      if( rowSpec[counter] == FILL )
      {
        numFillHeight++;
      }
    }

    // If numFillWidth (numFillHeight) is zero, the cooresponding if statements
    // will always evaluate to false and the division will not occur.
    // If there are more than one "fill" cell, slack may occur due to rounding
    // errors
    int slackWidth = availableWidth;
    int slackHeight = availableHeight;

    // Assign "fill" cells equal amounts of the remaining space
    for( counter = 0; counter < numColumn; counter++ )
    {
      if( columnSpec[counter] == FILL )
      {
        columnSize[counter] = availableWidth / numFillWidth;
        slackWidth -= columnSize[counter];
      }
    }

    for( counter = 0; counter < numRow; counter++ )
    {
      if( rowSpec[counter] == FILL )
      {
        rowSize[counter] = availableHeight / numFillHeight;
        slackHeight -= rowSize[counter];
      }
    }

    // Add slack to the last "fill" cell
    for( counter = numColumn - 1; counter >= 0; counter-- )
    {
      if( columnSpec[counter] == FILL )
      {
        columnSize[counter] += slackWidth;

        break;
      }
    }

    for( counter = numRow - 1; counter >= 0; counter-- )
    {
      if( rowSpec[counter] == FILL )
      {
        rowSize[counter] += slackHeight;

        break;
      }
    }

    // Calculate offsets of each column (done for effeciency)
    columnOffset = new int[numColumn + 1];
    columnOffset[0] = rectClient.x;

    for( counter = 0; counter < numColumn; counter++ )
    {
      columnOffset[counter + 1] = columnOffset[counter] + columnSize[counter];
    }

    // Calculate offsets of each row (done for effeciency)
    rowOffset = new int[numRow + 1];
    rowOffset[0] = rectClient.y;

    for( counter = 0; counter < numRow; counter++ )
    {
      rowOffset[counter + 1] = rowOffset[counter] + rowSize[counter];
    }

    // Indicate that the size of the cells are known for the container's
    // current size
    dirty = false;
    oldWidth = totalWidth;
    oldHeight = totalHeight;
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
  protected Point computeSize( Composite composite, int widthHint, int heightHint, boolean flushCache )
  {
    // Set the dirty flag if changed
    dirty |= flushCache;

    Point size;  // Preferred size of current widget
    int scaledWidth = 0;  // Preferred width of scalled widgets
    int scaledHeight = 0;  // Preferred height of scalled widgets
    int temp;  // Temporary variable used to compare sizes
    int counter;  // Counting variable

    double fillWidthRatio = 1.0;
    double fillHeightRatio = 1.0;
    int numFillWidth = 0;
    int numFillHeight = 0;

    for( counter = 0; counter < columnSpec.length; counter++ )
    {
      if( ( columnSpec[counter] > 0.0 ) && ( columnSpec[counter] < 1.0 ) )
      {
        fillWidthRatio -= columnSpec[counter];
      }
      else if( columnSpec[counter] == FILL )
      {
        numFillWidth++;
      }
    }

    for( counter = 0; counter < rowSpec.length; counter++ )
    {
      if( ( rowSpec[counter] > 0.0 ) && ( rowSpec[counter] < 1.0 ) )
      {
        fillHeightRatio -= rowSpec[counter];
      }
      else if( rowSpec[counter] == FILL )
      {
        numFillHeight++;
      }
    }

    // Adjust fill ratios to reflect number of fill rows/columns
    if( numFillWidth > 1 )
    {
      fillWidthRatio /= numFillWidth;
    }

    if( numFillHeight > 1 )
    {
      fillHeightRatio /= numFillHeight;
    }

    // Cap fill ratio bottoms to 0.0
    if( fillWidthRatio < 0.0 )
    {
      fillWidthRatio = 0.0;
    }

    if( fillHeightRatio < 0.0 )
    {
      fillHeightRatio = 0.0;
    }

    // Calculate preferred/minimum column widths
    int[] columnPrefMin = new int[columnSpec.length];

    for( counter = 0; counter < columnSpec.length; counter++ )
    {
      // Is the current column a preferred/minimum size
      if( ( columnSpec[counter] == PREFERRED ) || ( columnSpec[counter] == MINIMUM ) )
      {
        // Assume a maximum width of zero
        int maxWidth = 0;

        // Find maximum preferred/minimum width of all widgets completely
        // contained within this column
        Control[] children = composite.getChildren();
    
        for( int i = 0; i < children.length; i++ )
        {
          LatticeData data = (LatticeData)children[i].getLayoutData();
          if( data == null )
          {
            data = new LatticeData();
            children[i].setLayoutData( data );
          }           
          
          if( ( data.col1 == counter ) && ( data.col2 == counter ) )
          {
            Point p = ( columnSpec[counter] == PREFERRED ) ? children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false ) : children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false );

            int width = ( p == null ) ? 0 : p.x;

            if( maxWidth < width )
            {
              maxWidth = width;
            }
          }
        }

        // Set column's preferred/minimum width
        columnPrefMin[counter] = maxWidth;
      }
    }
    
    // Calculate preferred/minimum row heights
    int[] rowPrefMin = new int[rowSpec.length];

    for( counter = 0; counter < rowSpec.length; counter++ )

      // Is the current row a preferred/minimum size
      if( ( rowSpec[counter] == PREFERRED ) || ( rowSpec[counter] == MINIMUM ) )
      {
        // Assume a maximum height of zero
        int maxHeight = 0;

        // Find maximum preferred height of all widgets completely
        // contained within this row
        Control[] children = composite.getChildren();
    
        for( int i = 0; i < children.length; i++ )
        {
          LatticeData data = (LatticeData)children[i].getLayoutData();
          if( data == null )
          {
            data = new LatticeData();
            children[i].setLayoutData( data );
          }           

          if( ( data.row1 == counter ) && ( data.row1 == counter ) )
          {
            Point p = ( rowSpec[counter] == PREFERRED ) ? children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false ) : children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false );

            int height = ( p == null ) ? 0 : p.y;

            if( maxHeight < height )
            {
              maxHeight = height;
            }
          }
        }

        // Add preferred height
        rowPrefMin[counter] += maxHeight;
      }

    // Find maximum preferred size of all scaled widgets
    Control[] children = composite.getChildren();

    for( int i = 0; i < children.length; i++ )
    {
      LatticeData data = (LatticeData)children[i].getLayoutData();
      if( data == null )
      {
        data = new LatticeData();
        children[i].setLayoutData( data );
      }       

      // Make sure entry is in valid rows and columns
      if( ( data.col1 < 0 ) || ( data.col1 >= columnSpec.length ) || ( data.col2 >= columnSpec.length ) || ( data.row1 < 0 ) || ( data.row1 >= rowSpec.length ) || ( data.row2 >= rowSpec.length ) )
      {
        // Skip the bad widget
        continue;
      }

      // Get preferred size of current widget
      size = children[i].computeSize( SWT.DEFAULT, SWT.DEFAULT, false );

      // Calculate portion of widget that is not absolutely sized
      int scalableWidth = size.x;
      int scalableHeight = size.y;

      for( counter = data.col1; counter <= data.col2; counter++ )
      {
        if( columnSpec[counter] >= 1.0 )
        {
          scalableWidth -= columnSpec[counter];
        }
        else if( ( columnSpec[counter] == PREFERRED ) || ( columnSpec[counter] == MINIMUM ) )
        {
          scalableWidth -= columnPrefMin[counter];
        }
      }

      for( counter = data.row1; counter <= data.row2; counter++ )
      {
        if( rowSpec[counter] >= 1.0 )
        {
          scalableHeight -= rowSpec[counter];
        }
        else if( ( rowSpec[counter] == PREFERRED ) || ( rowSpec[counter] == MINIMUM ) )
        {
          scalableHeight -= rowPrefMin[counter];
        }
      }

      //----------------------------------------------------------------------
      // Determine total percentage of scalable space that the widget
      // occupies by adding the relative columns and the fill columns
      double relativeWidth = 0.0;

      for( counter = data.col1; counter <= data.col2; counter++ )
      {
        // Column is scaled
        if( ( columnSpec[counter] > 0.0 ) && ( columnSpec[counter] < 1.0 ) )
        {
          // Add scaled size to relativeWidth
          relativeWidth += columnSpec[counter];
        }

        // Column is fill
        else if( ( columnSpec[counter] == FILL ) && ( fillWidthRatio != 0.0 ) )
        {
          // Add fill size to relativeWidth
          relativeWidth += fillWidthRatio;
        }
      }

      // Determine the total scaled width as estimated by this widget
      if( relativeWidth == 0 )
      {
        temp = 0;
      }
      else
      {
        temp = (int)( ( scalableWidth / relativeWidth ) + 0.5 );
      }

      // If the container needs to be bigger, make it so
      if( scaledWidth < temp )
      {
        scaledWidth = temp;
      }

      //----------------------------------------------------------------------
      // Determine total percentage of scalable space that the widget
      // occupies by adding the relative columns and the fill columns
      double relativeHeight = 0.0;

      for( counter = data.row1; counter <= data.row2; counter++ )
      {
        // Row is scaled
        if( ( rowSpec[counter] > 0.0 ) && ( rowSpec[counter] < 1.0 ) )
        {
          // Add scaled size to relativeHeight
          relativeHeight += rowSpec[counter];
        }

        // Row is fill
        else if( ( rowSpec[counter] == FILL ) && ( fillHeightRatio != 0.0 ) )
        {
          // Add fill size to relativeHeight
          relativeHeight += fillHeightRatio;
        }
      }

      // Determine the total scaled width as estimated by this widget
      if( relativeHeight == 0 )
      {
        temp = 0;
      }
      else
      {
        temp = (int)( ( scalableHeight / relativeHeight ) + 0.5 );
      }

      // If the container needs to be bigger, make it so
      if( scaledHeight < temp )
      {
        scaledHeight = temp;
      }
    }

    // totalWidth is the scaledWidth plus the sum of all absolute widths and all
    // preferred widths
    int totalWidth = scaledWidth;

    for( counter = 0; counter < columnSpec.length; counter++ )

      // Is the current column an absolute size
      if( columnSpec[counter] >= 1.0 )
      {
        totalWidth += (int)( columnSpec[counter] + 0.5 );
      }

      // Is the current column a preferred/minimum size
      else if( ( columnSpec[counter] == PREFERRED ) || ( columnSpec[counter] == MINIMUM ) )
      {
        // Add preferred/minimum width
        totalWidth += columnPrefMin[counter];
      }

    // totalHeight is the scaledHeight plus the sum of all absolute heights and
    // all preferred widths
    int totalHeight = scaledHeight;

    for( counter = 0; counter < rowSpec.length; counter++ )

      // Is the current row an absolute size
      if( rowSpec[counter] >= 1.0 )
      {
        totalHeight += (int)( rowSpec[counter] + 0.5 );
      }

      // Is the current row a preferred size
      else if( ( rowSpec[counter] == PREFERRED ) || ( rowSpec[counter] == MINIMUM ) )
      {
        // Add preferred/minimum width
        totalHeight += rowPrefMin[counter];
      }

    // Compensate for container's trim
    Rectangle totalArea = composite.computeTrim( 0, 0, totalWidth, totalHeight );
    
    totalWidth = totalArea.width;
    totalHeight = totalArea.height;
    
    // Account for hints
    if( widthHint != SWT.DEFAULT ) 
    {
      totalWidth = widthHint;
    }
    
    if( heightHint != SWT.DEFAULT ) 
    {
      totalHeight = heightHint;
    }

    return new Point( totalWidth, totalHeight );  
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
    int x;  // Coordinates of the currnet widget in pixels
    int y;  // Coordinates of the currnet widget in pixels
    int w;  // Width and height of the current widget in pixels
    int h;  // Width and height of the current widget in pixels

    // Set the dirty flag if changed
    dirty |= flushCache;

    // Calculate sizes if container has changed size or widgets were added
    Rectangle rectClient = composite.getClientArea();

    if( dirty || ( rectClient.width != oldWidth ) || ( rectClient.height != oldHeight ) )
    {
      calculateSize( composite );
    }

    // Get widgets
    Control[] children = composite.getChildren();

    // Layout widgets
    for( int counter = 0; counter < children.length; counter++ )
    {
      try
      {
        LatticeData data = (LatticeData)children[counter].getLayoutData();
        if( data == null )
        {
          data = new LatticeData();
          children[counter].setLayoutData( data );
        }         

        // Skip any widgets that have not been place in a specific cell
        if( data == null )
        {
          break;
        }

        // Does the entry occupy a single cell
        if( ( ( data.row1 == data.row2 ) && ( data.col1 == data.col2 ) ) )
        {
          // The following block of code has been optimized so that the
          // preferred size of the widget is only obtained if it is
          // needed.  There are widgets in which the getPreferredSize
          // method is extremely expensive, such as data driven controls
          // with a large amount of data.
          // Get the preferred size of the widget
          int preferredWidth = 0;
          int preferredHeight = 0;

          if( ( data.hAlign != FULL ) || ( data.vAlign != FULL ) )
          {
            Point preferredSize = children[counter].computeSize( SWT.DEFAULT, SWT.DEFAULT, false );

            preferredWidth = preferredSize.x;
            preferredHeight = preferredSize.y;
          }

          // Determine cell width and height
          int cellWidth = columnSize[data.col1];
          int cellHeight = rowSize[data.row1];

          // Determine the width of the widget
          if( ( data.hAlign == FULL ) || ( cellWidth < preferredWidth ) )
          {
            // Use the width of the cell
            w = cellWidth;
          }
          else
          {
            // Use the prefered width of the widget
            w = preferredWidth;
          }

          // Determine left and right boarders
          switch( data.hAlign )
          {
            case LEFT:

              // Align left side along left edge of cell
              x = columnOffset[data.col1];

              break;

            case RIGHT:

              // Align right side along right edge of cell
              x = columnOffset[data.col1 + 1] - w;

              break;

            case CENTER:

              // Center justify widget
              x = columnOffset[data.col1] + ( ( cellWidth - w ) >> 1 );

              break;

            case FULL:

              // Align left side along left edge of cell
              x = columnOffset[data.col1];

              break;

            default:

              // This is a never should happen case, but just in case
              x = 0;
          }

          // Determine the height of the widget
          if( ( data.vAlign == FULL ) || ( cellHeight < preferredHeight ) )
          {
            // Use the height of the cell
            h = cellHeight;
          }
          else
          {
            // Use the prefered height of the widget
            h = preferredHeight;
          }

          // Determine top and bottom boarders
          switch( data.vAlign )
          {
            case TOP:

              // Align top side along top edge of cell
              y = rowOffset[data.row1];

              break;

            case BOTTOM:

              // Align right side along right edge of cell
              y = rowOffset[data.row1 + 1] - h;

              break;

            case CENTER:

              // Center justify widget
              y = rowOffset[data.row1] + ( ( cellHeight - h ) >> 1 );

              break;

            case FULL:

              // Align right side along right edge of cell
              y = rowOffset[data.row1];

              break;

            default:

              // This is a never should happen case, but just in case
              y = 0;
          }
        }
        else
        {
          // Align left side with left boarder of first column
          x = columnOffset[data.col1];

          // Align top side along top edge of first row
          y = rowOffset[data.row1];

          // Align right side with right boarder of second column
          w = columnOffset[data.col2 + 1] - columnOffset[data.col1];

          // Align bottom side with bottom boarder of second row
          h = rowOffset[data.row2 + 1] - rowOffset[data.row1];
        }

        // Move and resize widget
        children[counter].setBounds( x, y, w, h );
      }
      catch( Exception error )
      {
        // If any error occurs, skip this widget
        continue;
      }
    }
  }
}
