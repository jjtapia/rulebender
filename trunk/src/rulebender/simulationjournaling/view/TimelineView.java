package rulebender.simulationjournaling.view;

import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import rulebender.simulationjournaling.view.TreeView;

/**
 * This class defines the ViewPart subclass that holds the treeview.
 * 
 * It uses SWT_AWT to put the prefuse (awt) Display objects into an SWT 
 * composite.
 * 
 * ViewPart classes are built by constructing swt composites inside of the 
 * createPartControl method.
 * 
 * @author johnwenskovitch
 */
public class TimelineView extends ViewPart {

	// The holding object for the timeline tree 
	private TreeView tree;
	
	// This timer is used to make sure that the panel
	// is not regenerated every time the window resize event occurs. 
	private final static Timer timer = new Timer();
	private static boolean timerRunning = false;
	
	// The awt frame that holds the timeline tree
	private java.awt.Frame frame;
	
	// This is the parent that we will add our composite to.
	private Composite parentComposite;
	
	/**
	 * Empty constructor
	 */
	public TimelineView() {
		// Do nothing for now
	} //TimelineView (constructor)
	
	/**
	 * This is the method to override for creating a new ViewPart subclass.
	 * Add all visual elements to the parent composite that is passed in. 
	 */
	@Override
	public void createPartControl(final Composite parent) {
		parentComposite = parent;
		
		// Create the composite that will hold everything.
		Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
		
		// Create the special swt/awt frame to hold the awt stuff.
		frame = SWT_AWT.new_Frame(swtAwtComponent);
		
		// Create the TreeView object
		tree = new TreeView(new Dimension(1000,250), this);
		//tree = new TreeView(new Dimension(parent.getSize().x, parent.getSize().y), this);
		
		// Add the layered pane to the frame.
		frame.add(tree);
		
		parent.addControlListener(new ControlAdapter() {	
			/*
			 * I use a timer here so that the views are only updated 1 
			 * time per second since controlResized is called rapidly
			 * when the user is dragging a corner.  This way just cuts back
			 * on wasted cycles of resizing the visualizations.
			 */
			@Override
			public void controlResized(ControlEvent e) {
				
				// If there is already a timer going, then kill it.
				if (timerRunning) {
					timer.purge();
				} //if
				
				// Schedule a time that will
				// run the resize event. 
				timer.schedule(new TimerTask() {
					@Override 
					public void run() {
						
						org.eclipse.swt.widgets.Display.getDefault().syncExec(new Runnable() {
							
							public void run() {
								tree.myResize(new Dimension(parent.getSize().x, parent.getSize().y));
								timerRunning = false;
							} //run
							
						}); //new Runnable()
						
					} // run
					
				}, // new TimerTask
				1000); //second timer parameter
				
				timerRunning = true;
				
			} //controlResized
			
		}); // new ControlAdapter()
		
	} //createPartControl
	
	/**
	 * Return the TreeView
	 * 
	 * @return - the TreeView
	 */
	public TreeView getTreeView() {
		return tree;
	} //getTreeView
	
	/**
	 * Sets the current ViewPart in focus
	 */
	@Override
	public void setFocus() {
		frame.repaint();		
	} //setFocus

	/**
	 * Returns the size of the parent Composite
	 * 
	 * @return - the size of the parent Composite
	 */
	public Dimension getSize() {
		return new Dimension(parentComposite.getSize().x, parentComposite.getSize().y); 
	} //getSize
	
} //TimelineView (class)