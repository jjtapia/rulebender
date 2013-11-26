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

import rulebender.core.prefuse.SmallMultiplesPanel;
import rulebender.simulationjournaling.Message;

/**
 * This class defines the ViewPart subclass that holds the small multiples.
 * 
 * It uses SWT_AWT to put the prefuse (awt) Display objects into an SWT 
 * composite.
 * 
 * ViewPart classes are built by constructing swt composites inside of the 
 * createPartControl method.
 * 
 * @author johnwenskovitch
 */
public class SmallMultiplesView extends ViewPart {
	
	// The holding object for the contact map small multiples. 
	private SmallMultiplesPanel smPanel;
	
	// This timer is used to make sure that the panel
	// is not regenerated every time the window resize event occurs. 
	private final static Timer timer = new Timer();
	private static boolean timerRunning = false;
	
	// The awt frame that holds the small multiples.
	private java.awt.Frame frame;
	
	// This is the parent that we will add our composite to.
	private Composite parentComposite;

	public SmallMultiplesView() {
		// Do nothing for now
	} //SmallMultiplesView (constructor)

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
		
		// Create the SmallMultiplesPanel.  This is a data type that has an
		// array of jpanels for the cmaps
		
		smPanel = new SmallMultiplesPanel(new Dimension(1000,500), this);
		
		// Add the layered pane to the frame.
		frame.add(smPanel);
		
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
								smPanel.myResize(new Dimension(parent.getSize().x, parent.getSize().y));
								timerRunning = false;
							} //run
							
						}); //new Runnable()
						
					} // run
					
				}, // new TimerTask
				1000); //second timer parameter
				
				timerRunning = true;
				
			} //controlResized
			
		}); // new ControlAdapter()
		
		// Set up the selection listener.  The selectionListener basically 
		// handles everything.  If you're wanting to understand how the contact
		// map code works, go there next.
		
		//listener = new ContactMapSelectionListener(this);

	} //createPartControl
	
	/**
	 * Sets a new small multiple of a contact map in the view
	 * @param d a prefuse.Display object to use as the contact map.
	 *//*
	public void setSmallMultiple(prefuse.Display d) {
		if (d == null) {
			JLabel temp = new JLabel();
			temp.setText("The Contact Map data for this model failed to load properly.  Please try again.");
			smPanel.add(temp);
		} //if
		
		// Set the display in the layered pane
		smPanel.setDisplay(d);

		// Redraw the parent.  This is my solution to the contact map
		// not being updated when the contact map view is not in focus. 
		// The overview was updated, but not the main panel.  So,
		// there may be a better solution than this, but this works.
		parentComposite.redraw();
		
		// Apparently the above fix does not work in windows.
		frame.repaint();
	} //setSmallMultiple
	*/
	
	public void iGotAMessage(Message msg) {
		if (msg.getType().equals("ModelSelection")) {
			highlightAPanel(msg.getDetails());
		} else if (msg.getType().equals("ModelDeselection")) {
			unhighlightAPanel(msg.getDetails());
		} //if-else
	} //iGotAMessage
	
	private void highlightAPanel(String modelName) {
		int panelNum = smPanel.findPanelFromModelName(modelName);
		
		if (panelNum != -1) {
			smPanel.addHighlightedPanel(panelNum);
			System.out.println("Highlighted panel number " + panelNum);	
		} else {
			System.err.println("Could not find panel to highlight.  Provided label was " + modelName + ".");
		} //if-else
		
	} //highlightAPanel
	
	private void unhighlightAPanel(String modelName) {
		if (modelName.equals("")) {
			smPanel.removeAllSelections();
			System.out.println("Unhighlighted all panels");
		} else {
			int panelNum = smPanel.findPanelFromModelName(modelName);
			smPanel.removeHighlightedPanel(panelNum);
			System.out.println("Unhighlighted panel number " + panelNum);
		} //if-else
	} //unhighlightAPanel
	
	public SmallMultiplesPanel getSmallMultiplesPanel() {
		return smPanel;
	} //getSmallMultiplesPanel

	@Override
	public void setFocus() {
		frame.repaint();
	} //setFocus

	public Dimension getSize() {
		return new Dimension(parentComposite.getSize().x, parentComposite.getSize().y); 
	} //getSize

} //SmallMultiplesView
