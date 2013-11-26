package rulebender.simulationjournaling.view;

import java.awt.Dimension;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

//import rulebender.simulationjournaling.Message;
import rulebender.simulationjournaling.Message;
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
		tree = new TreeView(new Dimension(1000, 250), this, /*"C:\\Users\\John\\runtime-rulebender.product\\stat\\stat.info"*/ null /*"C:\\Users\\John\\runtime-rulebender.product\\fceri\\fceri.info"*/);
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

	public void repaint() {
		tree.repaint();
	} //repaint
	
	/**
	 * Returns the size of the parent Composite
	 * 
	 * @return - the size of the parent Composite
	 */
	public Dimension getSize() {
		return new Dimension(parentComposite.getSize().x, parentComposite.getSize().y); 
	} //getSize
	
	public void iGotAMessage(Message msg) {
		if (msg.getType().equals("DirectorySelection")) {
			updateTree(msg);
		} else if (msg.getType().equals("ModelSelection")) {
			//highlightLabel(msg.getDetails());
		} //if
	} //iGotAMessage
    
	public void updateTree(Message msg) {
		frame.remove(0);
		frame.validate();
		
		// // Create the composite that will hold everything.
		//Composite swtAwtComponent = new Composite(parentComposite, SWT.EMBEDDED);
		
		// // Create the special swt/awt frame to hold the awt stuff.
		//frame = SWT_AWT.new_Frame(swtAwtComponent);
		
		tree = null;
		tree = new TreeView(new Dimension(1000,250), this, getINFOPath(msg.getDetails()));
		
		frame.add(tree);
		frame.validate();
		frame.repaint();
		
		this.repaint();
		
		tree.repaint();
		tree.getMyVis().repaint();
		tree.getMyVis().run("treeLayout");
		
		parentComposite.layout();
		parentComposite.update();
		//tree.setDirectory(msg.getDetails());
		//tree.reloadTree();		
	} //updateTree
    
	
   	public String getINFOPath(String dir) {
    	
    	String infoPath = null;
    	
    	// Find .info file in this directory or its parent
		File d = new File(dir);
		boolean infoFileFound = false;
		
		// Iterate across all files in the provided directory
		if (d.isDirectory()) {
			for (File child : d.listFiles()) {
				if (isINFOFile(child)) {
					infoFileFound = true;
					infoPath = child.getAbsolutePath();
					break;
				} //if
			} //for
		} //if
    	
		// If we haven't found the info file yet, step up a level and look again
    	if (!infoFileFound) {
    		d = new File(d.getParent());
			for (File child : d.listFiles()) {
				if (isINFOFile(child)) {
					infoFileFound = true;
					infoPath = child.getAbsolutePath();
					break;
				} //if
			} //for
    		
    	} //if
    	
    	// Check to see if we found the new one
    	if (!infoFileFound) {
    		System.err.println("INFO file not found for timeline tree construction.");
    		return null;
    	} else {
    		return infoPath;
    	} //if-else
    	
    } //setDirectory
    
	private boolean isINFOFile(File child) {
		String filepath = child.getPath();
		return ((filepath.substring(filepath.length()-5, filepath.length()).equals(".info")) || (filepath.substring(filepath.length()-5, filepath.length()).equals(".INFO")));
	} //isBNGLFile
	
	/*
	public void highlightLabel(String modelName) {
		tree.setSelection(modelName);
	} //highlightLabel
	*/
} //TimelineView (class)