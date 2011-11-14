Bubble Sets Documentation

This is an Eclipse Project and may be easiest to compile and run by importing into Eclipse.  
It depends on prefuse (not included, available from prefuse.org).  

The two provided examples are "ScatterPlot" which can be run either without any arguments 
or with a specified dataset.  With no arguments, it will attempt to use the default data 
specified in data/dataset.xml.  You can also specify an xml file (such as data/dataset2.xml).  

In the xml files you'll see how the various parts of the referenced csv datafile are specified.  
The first row of the csv file should contain column names which correspond to the columns named
in the xml file.

The scatterplot is somewhat interactive -- the up and down keys advances or decrements 
the years by 5 years per press.  When the mouse is over an item, the Bubble Set containing 
that item becomes visible.  To lock a Bubble Set open, e.g. to see it animate as years change, 
hover on an item, then slide off and onto the set itself, then click.  This should lock the set 
visible until you click another item or on the background.  Tooltips are visible by hovering over
items or sets.

The second example is the 'maps' example, which basically creates Bubble Sets around user-specified 
items which are rendered atop an image, such as a map.  This takes one argument - a reference to 
the map image, such as data/mapny2.jpg.  It will assume the associated data files for the items 
are in the same folder, or it will create them.

More information can be found in the related publication:
Collins, Christopher; Penn, Gerald; Carpendale, Sheelagh. Bubble Sets: Revealing Set Relations 
over Existing Visualizations. IEEE Transactions on Visualization and Computer Graphics (Proceedings 
of the IEEE Conference on Information Visualization (InfoVis '09)), 15(6): pp. 1009-1015, November-December, 2009.