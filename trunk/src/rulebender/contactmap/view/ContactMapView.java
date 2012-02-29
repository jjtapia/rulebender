package rulebender.contactmap.view;

import java.awt.Dimension;
import java.util.Timer;
import java.util.TimerTask;

import rulebender.core.prefuse.LayeredPane;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

// Use explicitly
//import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

public class ContactMapView extends ViewPart  
{

	// The holding object for the contact map visualizaitons and the 
	// overview. 
	private LayeredPane layeredPane;
	
	// This timer is used to make sure that the contact map
	// is not regenerated every time the window resize event occurs. 
	private final static Timer timer = new Timer();
	private static boolean timerRunning = false;
		
	// The awt frame that holds the contact map.
	private java.awt.Frame frame;
	
	private ContactMapSelectionListener listener;
	
	private Composite parentComposite;
	
	public ContactMapView() 
	{
		// Do nothing
	}

	@Override
	public void createPartControl(final Composite parent) 
	{	
		parentComposite = parent;
		
		// Create the composite that will hold everything.
		Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
		
		// Create the special swt/awt frame to hold the awt stuff.
		frame = SWT_AWT.new_Frame(swtAwtComponent);
		
		// Create the ContactMapLayeredPane.  This is my data type that has a
		// large jpanel for the cmap, and an overlayed jpanel for the overview
		// in the bottom left corner.
		layeredPane = new LayeredPane(new Dimension(400,600));
		
		// Add the layered pane to the frame.
		frame.add(layeredPane);
				
		// Add a listener that updates the views when the 
		// parent object is resized.
		parent.addControlListener(new ControlAdapter() 
		{	
			@Override
			public void controlResized(ControlEvent e) 
			{
				// If there is already a timer going, then kill it.
				if(timerRunning)
				{
					timer.purge();
				}
				
				// Schedule a time that will
				// run the cmap resize event. 
				timer.schedule(new TimerTask()
				{
					@Override 
					public void run() 
					{
						org.eclipse.swt.widgets.Display.getDefault().syncExec(new Runnable(){
							public void run() 
							{
								layeredPane.myResize(new Dimension(parent.getSize().x, parent.getSize().y));
								timerRunning = false;
							}
						});
					}
				},
				1000);
				
				timerRunning = true;
			}
		});
		
		// Set up the selection listener.  The selectionListener basically 
		// handles everything.  If you're wanting to understand how the contact
		// map code works, go there next.
		listener = new ContactMapSelectionListener(this);
	}

	/**
	 * Sets a new contact map in the view
	 * @param d a prefuse.Display object to use as the contact map.
	 */
	public void setCMap(prefuse.Display d)
	{
		// Set the display in the layered pane
		layeredPane.setDisplay(d);

		// Redraw the parent.  This is my solution to the contact map
		// not being updated when the contact map view is not in focus. 
		// The overview was updated, but not the main panel.  So,
		// there may be a better solution than this, but this works.
		parentComposite.redraw();
		
		// Aparrently the above fix does not work in windows.
		frame.repaint();
	}
	
	/*
	public void tempRefresh() 
	{
		listener.tempRefresh();
	}
	 */
	@Override
	public void setFocus() 
	{
		// TODO Auto-generated method stub
		
	}
	
	public Dimension getSize()
	{
		return new Dimension(parentComposite.getSize().x, parentComposite.getSize().y); 
	}
}