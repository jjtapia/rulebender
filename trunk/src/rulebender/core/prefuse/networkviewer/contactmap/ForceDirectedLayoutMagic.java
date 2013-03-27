package rulebender.core.prefuse.networkviewer.contactmap;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;


import prefuse.action.layout.Layout;
import prefuse.data.Graph;
import prefuse.data.Schema;
import prefuse.data.tuple.TupleSet;
import prefuse.util.PrefuseLib;
import prefuse.util.force.DragForce;
import prefuse.util.force.ForceItem;
import prefuse.util.force.ForceSimulator;
import prefuse.util.force.NBodyForce;
import prefuse.visual.EdgeItem;
import prefuse.visual.NodeItem;
import prefuse.visual.VisualItem;
import rulebender.contactmap.models.NodePosition;
import rulebender.simulationjournaling.model.MoleculeCounter;

/**
 * A modification of the ForceDirectedLayout, which allows to set all edges
 * and/or nodes magic. The forces of magic nodes/edges are still computed even
 * if the nodes/edges is invisible.
 * 
 * @author <a href="http://jheer.org">jeffrey heer</a>
 * @author <a href="http://goosebumps4all.net"> martin dudek</a>
 * 
 */
public class ForceDirectedLayoutMagic extends Layout {
	private boolean magicNodes = true;
	private boolean magicEdges = true;

	private ForceSimulator m_fsim;
	private long m_lasttime = -1L;
	private long m_maxstep = 50L;
	private boolean m_runonce;
	private int m_iterations = 160;
	private boolean m_enforceBounds;
	private Rectangle2D enforceBounds;

	protected transient VisualItem referrer;

	protected String m_nodeGroup;
	protected String m_edgeGroup;
	
	private String m_filePath;
	
	private boolean positionMapExists = false;

	/**
	 * Create a new ForceDirectedLayoutMagic. By default, this layout will not
	 * restrict the layout to the layout bounds and will assume it is being run
	 * in animated (rather than run-once) fashion.
	 * 
	 * @param graph
	 *            the data group to layout. Must resolve to a Graph instance.
	 */
	public ForceDirectedLayoutMagic(String graph) {
		this(graph, false, false);
	}

	/**
	 * Create a new ForceDirectedLayoutMagic. The layout will assume it is being
	 * run in animated (rather than run-once) fashion.
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 */
	public ForceDirectedLayoutMagic(String group, boolean enforceBounds) {
		this(group, enforceBounds, false);
	}

	/* CMAPNetworkViewer USES THIS CONSTRUCTOR */
	/* f = new ForceDirectedLayoutMagic(COMPONENT_GRAPH, true, true); */
	/* group = COMPONENT_GRAPH, enforceBounds = true, runonce = true */
	/**
	 * Create a new ForceDirectedLayoutMagic.
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 * @param runonce
	 *            indicates if the layout will be run in a run-once or animated
	 *            fashion. In run-once mode, the layout will run for a set
	 *            number of iterations when invoked. In animation mode, only one
	 *            iteration of the layout is computed.
	 */
	public ForceDirectedLayoutMagic(String group, boolean enforceBounds,
			boolean runonce) {
		super(group);
		m_nodeGroup = PrefuseLib.getGroupName(group, Graph.NODES);
		m_edgeGroup = PrefuseLib.getGroupName(group, Graph.EDGES);

		m_enforceBounds = enforceBounds;
		m_runonce = runonce;
		m_fsim = new ForceSimulator();
		m_fsim.addForce(new NBodyForce());
		m_fsim.addForce(new DeterministicSpringForce());
		m_fsim.addForce(new DragForce());
	}

	/**
	 * Create a new ForceDirectedLayoutMagic. The layout will assume it is being
	 * run in animated (rather than run-once) fashion.
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param fsim
	 *            the force simulator used to drive the layout computation
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 */
	public ForceDirectedLayoutMagic(String group, ForceSimulator fsim,
			boolean enforceBounds) {
		this(group, fsim, enforceBounds, false);
	}

	/**
	 * Create a new ForceDirectedLayoutMagic.
	 * 
	 * @param group
	 *            the data group to layout. Must resolve to a Graph instance.
	 * @param fsim
	 *            the force simulator used to drive the layout computation
	 * @param enforceBounds
	 *            indicates whether or not the layout should require that all
	 *            node placements stay within the layout bounds.
	 * @param runonce
	 *            indicates if the layout will be run in a run-once or animated
	 *            fashion. In run-once mode, the layout will run for a set
	 *            number of iterations when invoked. In animation mode, only one
	 *            iteration of the layout is computed.
	 */
	public ForceDirectedLayoutMagic(String group, ForceSimulator fsim,
			boolean enforceBounds, boolean runonce) {
		super(group);
		m_nodeGroup = PrefuseLib.getGroupName(group, Graph.NODES);
		m_edgeGroup = PrefuseLib.getGroupName(group, Graph.EDGES);

		m_enforceBounds = enforceBounds;
		m_runonce = runonce;
		m_fsim = fsim;
	}

	// ------------------------------------------------------------------------

	/**
	 * Get the maximum timestep allowed for integrating node settings between
	 * runs of this layout. When computation times are longer than desired, and
	 * node positions are changing dramatically between animated frames, the max
	 * step time can be lowered to suppress node movement.
	 * 
	 * @return the maximum timestep allowed for integrating between two layout
	 *         steps.
	 */
	public long getMaxTimeStep() {
		return m_maxstep;
	}

	/**
	 * Set the maximum timestep allowed for integrating node settings between
	 * runs of this layout. When computation times are longer than desired, and
	 * node positions are changing dramatically between animated frames, the max
	 * step time can be lowered to suppress node movement.
	 * 
	 * @param maxstep
	 *            the maximum timestep allowed for integrating between two
	 *            layout steps
	 */
	public void setMaxTimeStep(long maxstep) {
		this.m_maxstep = maxstep;
	}

	/**
	 * Get the force simulator driving this layout.
	 * 
	 * @return the force simulator
	 */
	public ForceSimulator getForceSimulator() {
		return m_fsim;
	}

	/**
	 * Set the force simulator driving this layout.
	 * 
	 * @param fsim
	 *            the force simulator
	 */
	public void setForceSimulator(ForceSimulator fsim) {
		m_fsim = fsim;
	}

	/**
	 * Get the number of iterations to use when computing a layout in run-once
	 * mode.
	 * 
	 * @return the number of layout iterations to run
	 */
	public int getIterations() {
		return m_iterations;
	}

	/**
	 * Set the number of iterations to use when computing a layout in run-once
	 * mode.
	 * 
	 * @param iter
	 *            the number of layout iterations to run
	 */
	public void setIterations(int iter) {
		if (iter < 1)
			throw new IllegalArgumentException(
					"Iterations must be a positive number!");
		m_iterations = iter;
	}

	/**
	 * Explicitly sets the node and edge groups to use for this layout,
	 * overriding the group setting passed to the constructor.
	 * 
	 * @param nodeGroup
	 *            the node data group
	 * @param edgeGroup
	 *            the edge data group
	 */
	public void setDataGroups(String nodeGroup, String edgeGroup) {
		m_nodeGroup = nodeGroup;
		m_edgeGroup = edgeGroup;
	}

	// ------------------------------------------------------------------------

	/**
	 * Set enforce bounds
	 * 
	 * @param enforceBounds
	 */
	public void setEnforceBounds(Rectangle2D enforceBounds) {
		this.enforceBounds = enforceBounds;
	}

	/**
	 * Set the path to the file that contains molecule/component positions
	 * 
	 * @param path
	 */
	public void setPositionFilepath(String path) {
		m_filePath = path;
	} //setPositionFilePath
	
	/**
	 * Get the path to the file that contains molecule/component positions
	 * 
	 * @return the filepath
	 */
	public String getPositionFilepath() {
		return m_filePath;
	} //getPositionFilepath
	
	/**
	 * @see prefuse.action.Action#run(double)
	 */
	@Override
	public void run(double frac) {
		// perform different actions if this is a run-once or
		// run-continuously layout
		if (m_runonce) {

			
			Point2D anchor = getLayoutAnchor();

			//System.out.println("Anchor is at " + anchor.getX() + " "
			//		+ anchor.getY());
			
			
			// Retrieve the node positions from the file
			// ContactMapPosition class will determine whether it's a BNGL or POS path
			ArrayList<NodePosition> positionMap;
			try {
				 positionMap = ContactMapPosition.loadMoleculePositions(m_filePath);
			} catch (Exception e) {
				positionMap = null;
			} //try-catch
			
			// Temp find the average of all positions, and set that to the anchor point
			float x = 0, y = 0;
			String mol = null;
			int count = 0;
			String boundsLine = null;

			// Initialize the simulator
			m_fsim.clear();
			long timestep = 500L;
			initSimulator(m_fsim); 
			
			if (positionMap != null) {
				positionMapExists = true; // Note that we have position info for the nodes
				
				Iterator fileIter = positionMap.iterator();
				while (fileIter.hasNext()) {
					NodePosition fileItem = (NodePosition) fileIter.next();
					mol = fileItem.getMolecule();
					
					// Don't include the special case line for bounds
					if (!mol.equals("BOUNDS")) {
						x += fileItem.getX();
						y += fileItem.getY();
						count++;						
					} else {
						// While we're looping through, let's pick up the bounds and parse it
						boundsLine = fileItem.getComponent();
					} //if-else

				} //while
				
				x /= count;
				y /= count;
			} //if
			
			// Set a temporary anchor point
			//Point2D tempAnchor = new Point2D.Double(x, y);
			//setLayoutAnchor(tempAnchor);
			
			// Begin bounds stuff
			
			Rectangle2D bounds;
			double boundsCenterX, boundsCenterY, boundsHeight, boundsWidth;
			
			// Parse the bounds line
			if (boundsLine != null) {
				StringTokenizer st;
				st = new StringTokenizer(boundsLine, "|");
				
				st.nextToken(); //Skip the first token, it just says "BOUNDS"
				boundsCenterX = Double.parseDouble(st.nextToken());
				boundsCenterY = Double.parseDouble(st.nextToken());
				boundsHeight = Double.parseDouble(st.nextToken());
				boundsWidth = Double.parseDouble(st.nextToken());
			} else {
				// If there was no position file, set the bounds based on the old defaults
				Iterator iter = getMagicIterator(m_nodeGroup);
				int i = 0;
				while (iter.hasNext()) {
					VisualItem item = (NodeItem) iter.next();
					i++;
				} //while
				
				if (i > 100) {
					boundsCenterX = -1200;
					boundsCenterY = -1200;
					boundsHeight = 2400;
					boundsWidth = 2400;
				} else { 
					boundsCenterX = -600;
					boundsCenterY = -600;
					boundsHeight = 1200;
					boundsWidth = 1200;
				} //if-else
				
			} //if-else
			
			bounds = new Rectangle2D.Double(boundsCenterX, boundsCenterY, boundsWidth, boundsHeight);
			setEnforceBounds(bounds);
			
			// End bounds stuff

			// Skip this position loader if the position file doesn't exist (or if a problem occurred when loading the positionMap)
			if (positionMap != null) {
			
				for (int j = 0; j < m_iterations; j++) {
					// use an annealing schedule to set time step
					timestep *= (1.0 - j / (double) m_iterations);
					long step = 30 + timestep;
					// run simulator
					m_fsim.runSimulator(step);
							
					// Molecule tracker, to keep track of how many times we've seen each molecule/component pair
					ArrayList<MoleculeCounter> tracker = new ArrayList<MoleculeCounter>();
					boolean found = false;
				
					// Iterate over all nodes in the graph
					Iterator iter = getMagicIterator(m_nodeGroup);
					while (iter.hasNext()) {
						VisualItem item = (NodeItem) iter.next();
						
						item.setFixed(false);
						
						// Get the properties of that VisualItem
						String id = item.getString("ID");
						String molecule = item.getString("molecule");
						String component = item.getString(VisualItem.LABEL);
						
						// Correction for the null items
						if (molecule == null) {
							molecule = "null";
							//component = "IGNORE";
						} //if					
					
						// Add the current molecule to the tracker
						for (int i = 0; i < tracker.size(); i++) {
							if ((molecule.equals(tracker.get(i).getMolecule())) && (component.equals(tracker.get(i).getComponent()))) {
								MoleculeCounter tempMol = new MoleculeCounter(molecule, component, tracker.get(i).getCount() + 1);

								id = Integer.toString(tracker.get(i).getCount() + 1);

								tracker.remove(i);
								tracker.add(tempMol);
							
								found = true;
								break;
							} //if
						} //for
					
						// If we find a new molecule/component pair, add a new item to the array
						if (!found) {
							MoleculeCounter tempMol = new MoleculeCounter(molecule, component, 0);
							tracker.add(tempMol);
							id = Integer.toString(0);
						} //if
					
						// Iterate over all node positions in the file
						Iterator fileIter = positionMap.iterator();
						while (fileIter.hasNext()) {
							NodePosition fileItem = (NodePosition) fileIter.next();
				
							// If we found a matching ID, set the position of that VisualItem and fix the item in place 
							if ((molecule.equals(fileItem.getMolecule())) && (component.equals(fileItem.getComponent())) && (id.equals(fileItem.getID()))) {
								item.setX(fileItem.getX());
								item.setY(fileItem.getY());
								item.setFixed(true);
								break;
							} //if
						} //while
					
						found = false;
				
					} //while
				
				} //for
			
			} else { 
				
				// Run force computations if no position file exists
				m_fsim.clear();
				timestep = 500L;
				initSimulator(m_fsim);

				for (int i = 0; i < m_iterations; i++) {
					// use an annealing schedule to set time step
					timestep *= (1.0 - i / (double) m_iterations);
					long step = 30 + timestep;
					// run simulator
					m_fsim.runSimulator(step);
				
				} //for
			} //if-else
			
			updateNodePositions();
			moveToCenter();
		}

		else {
			// get timestep
			if (m_lasttime == -1)
				m_lasttime = System.currentTimeMillis() - 20;
			long time = System.currentTimeMillis();
			long timestep = Math.min(m_maxstep, time - m_lasttime) + 10;
			m_lasttime = time;

			// run force simulator
			m_fsim.clear();
			initSimulator(m_fsim);

			m_fsim.runSimulator(timestep);
			updateNodePositions();
			moveToCenter();
		}

		if (frac == 1.0) {
			reset();
		}
	}

	private void updateNodePositions() {
		Rectangle2D bounds = enforceBounds;
		double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		if (bounds != null) {
			x1 = bounds.getMinX();
			y1 = bounds.getMinY();
			x2 = bounds.getMaxX();
			y2 = bounds.getMaxY();
		}

		// update positions
		Iterator iter = getMagicIterator(m_nodeGroup);

		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			
			//try {
				ForceItem fitem = (ForceItem) item.get(FORCEITEM);

				if (item.isFixed()) {
					// clear any force computations
					fitem.force[0] = 0.0f;
					fitem.force[1] = 0.0f;
					fitem.velocity[0] = 0.0f;
					fitem.velocity[1] = 0.0f;
				
					if (Double.isNaN(item.getX())) {
						super.setX(item, referrer, 0.0);
						super.setY(item, referrer, 0.0);
					} //if
					continue;
				} //if

				double x = fitem.location[0];
				double y = fitem.location[1];
			//} catch (Exception e) {
				//TODO: figure out why ForceItem line crashes on every iteration after the first...
				//x = item.getX();
				//y = item.getY();
			//} //try-catch
						
			
			if (m_enforceBounds && bounds != null) {
				Rectangle2D b = item.getBounds();
				double hw = b.getWidth() / 2;
				double hh = b.getHeight() / 2;
				if (x + hw > x2)
					x = x2 - hw;
				if (x - hw < x1)
					x = x1 + hw;
				if (y + hh > y2)
					y = y2 - hh;
				if (y - hh < y1)
					y = y1 + hh;
			} //if

			// set the actual position
			super.setX(item, referrer, x);
			super.setY(item, referrer, y);
		} //while
	}

	private void moveToCenter() {
		Rectangle2D bounds = enforceBounds;
		Rectangle2D windowbounds = getLayoutBounds();
		double x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		if (bounds != null) {
			x1 = bounds.getMinX();
			y1 = bounds.getMinY();
			x2 = bounds.getMaxX();
			y2 = bounds.getMaxY();
		}

		// update positions
		Iterator iter = getMagicIterator(m_nodeGroup);

		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			
			double x = item.getX();
			double y = item.getY();
			
			
			//if (x > 453) {
			//	x++;
			//}
			

			// find max x
			if (x > x1) {
				x1 = x;
			}

			// find min x
			if (x < x2) {
				x2 = x;
			}

			// find max y
			if (y > y1) {
				y1 = y;
			}

			// find min y
			if (y < y2) {
				y2 = y;
			}

		}

		// center position of current graph
		double center_x = (x1 + x2) / 2;
		double center_y = (y1 + y2) / 2;
		
		// center position of the window
		double window_center_x = windowbounds.getCenterX();
		double window_center_y = windowbounds.getCenterY();
		
		// compute the offset
		double offset_x = window_center_x - center_x;
		double offset_y = window_center_y - center_y;

		// move the visual items one by one
		iter = getMagicIterator(m_nodeGroup);
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();

			double x = item.getX() + offset_x;
			double y = item.getY() + offset_y;

			// set the actual position
			item.setX(x);
			item.setY(y);
		}
	}

	/**
	 * Reset the force simulation state for all nodes processed by this layout.
	 */
	public void reset() {
		Iterator iter = getMagicIterator(m_nodeGroup);

		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			ForceItem fitem = (ForceItem) item.get(FORCEITEM);
			if (fitem != null) {
				fitem.location[0] = (float) item.getEndX();
				fitem.location[1] = (float) item.getEndY();
				fitem.force[0] = fitem.force[1] = 0;
				fitem.velocity[0] = fitem.velocity[1] = 0;
			}
		}
		m_lasttime = -1L;
	}

	/**
	 * Loads the simulator with all relevant force items and springs.
	 * 
	 * @param fsim
	 *            the force simulator driving this layout
	 */
	protected void initSimulator(ForceSimulator fsim) {
		// make sure we have force items to work with
		TupleSet ts = m_vis.getGroup(m_nodeGroup);
		if (ts == null)
			return;
		try {
			ts.addColumns(FORCEITEM_SCHEMA);
		} catch (IllegalArgumentException iae) { /* ignored */
		}

		float startX = (referrer == null ? 0f : (float) referrer.getX());
		float startY = (referrer == null ? 0f : (float) referrer.getY());
		startX = Float.isNaN(startX) ? 0f : startX;
		startY = Float.isNaN(startY) ? 0f : startY;

		Iterator iter = getMagicIterator(m_nodeGroup);
		while (iter.hasNext()) {
			VisualItem item = (VisualItem) iter.next();
			ForceItem fitem = (ForceItem) item.get(FORCEITEM);
			fitem.mass = getMassValue(item);
			double x = item.getEndX();
			double y = item.getEndY();
			fitem.location[0] = (Double.isNaN(x) ? startX : (float) x);
			fitem.location[1] = (Double.isNaN(y) ? startY : (float) y);
			fsim.addItem(fitem);
		}
		if (m_edgeGroup != null) {
			iter = getMagicIterator(m_edgeGroup);
			while (iter.hasNext()) {
				EdgeItem e = (EdgeItem) iter.next();
				NodeItem n1 = e.getSourceItem();
				ForceItem f1 = (ForceItem) n1.get(FORCEITEM);
				NodeItem n2 = e.getTargetItem();
				ForceItem f2 = (ForceItem) n2.get(FORCEITEM);
				float coeff = getSpringCoefficient(e);
				float slen = getSpringLength(e);
				fsim.addSpring(f1, f2, (coeff >= 0 ? coeff : -1.f),
						(slen >= 0 ? slen : -1.f));
			}
		}
	}

	/**
	 * Get the mass value associated with the given node. Subclasses should
	 * override this method to perform custom mass assignment.
	 * 
	 * @param n
	 *            the node for which to compute the mass value
	 * @return the mass value for the node. By default, all items are given a
	 *         mass value of 1.0.
	 */
	protected float getMassValue(VisualItem n) {
		if (n instanceof NodeItem) {
			String type = n.getString("type");
			if (type == null) {
				return -1.f;
			}
			
			// component node
			if (type.equals("component")) {
				return 2.0f;
			}
			// state node
			else if (type.equals("state")) {
				return 1.0f;
			}
			else {
				return 1.0f;
			}
		}
		
		return -1.f;
	}

	/**
	 * Get the spring length for the given edge. Subclasses should override this
	 * method to perform custom spring length assignment.
	 * 
	 * @param e
	 *            the edge for which to compute the spring length
	 * @return the spring length for the edge. A return value of -1 means to
	 *         ignore this method and use the global default.
	 */
	protected float getSpringLength(EdgeItem e) {
		String type = e.getString("type");
		if (type == null) {
			return -1.f;
		}
		
		if (type.equals("componentVisible_edge")) {
			return 80.0f;
		}
		else if (type.equals("componentInvisible_edge")) {
			return 50.0f;
		}
		else if (type.equals("stateVisible_edge")) {
			return 80.0f;
		}
		else if (type.equals("stateInvisible_edge")) {
			return 5.0f;
		}
		else if (type.equals("compartment_edge")) {
			return 150.0f;
		}
		else if (type.equals("moleConnection")) {
			return 50.0f;
		}
		else {
			return -1.f;
		}
	}

	/**
	 * Get the spring coefficient for the given edge, which controls the tension
	 * or strength of the spring. Subclasses should override this method to
	 * perform custom spring tension assignment.
	 * 
	 * @param e
	 *            the edge for which to compute the spring coefficient.
	 * @return the spring coefficient for the edge. A return value of -1 means
	 *         to ignore this method and use the global default.
	 */
	protected float getSpringCoefficient(EdgeItem e) {
		return -1.f;
	}

	/**
	 * Get the referrer item to use to set x or y coordinates that are
	 * initialized to NaN.
	 * 
	 * @return the referrer item.
	 * @see prefuse.util.PrefuseLib#setX(VisualItem, VisualItem, double)
	 * @see prefuse.util.PrefuseLib#setY(VisualItem, VisualItem, double)
	 */
	public VisualItem getReferrer() {
		return referrer;
	}

	/**
	 * Set the referrer item to use to set x or y coordinates that are
	 * initialized to NaN.
	 * 
	 * @param referrer
	 *            the referrer item to use.
	 * @see prefuse.util.PrefuseLib#setX(VisualItem, VisualItem, double)
	 * @see prefuse.util.PrefuseLib#setY(VisualItem, VisualItem, double)
	 */
	public void setReferrer(VisualItem referrer) {
		this.referrer = referrer;
	}

	// ------------------------------------------------------------------------
	// ForceItem Schema Addition

	/**
	 * The data field in which the parameters used by this layout are stored.
	 */
	public static final String FORCEITEM = "_forceItem";
	/**
	 * The schema for the parameters used by this layout.
	 */
	public static final Schema FORCEITEM_SCHEMA = new Schema();
	static {
		FORCEITEM_SCHEMA.addColumn(FORCEITEM, ForceItem.class, new ForceItem());
	}

	public Iterator getMagicIterator(String group) {

		if (group.equals(m_nodeGroup)) 
		{
			if (magicNodes) 
			{
				return m_vis.items(group);
			} 
			
			else 
			{
				return m_vis.visibleItems(group);
			}
		} 
		
		else 
		{
			if (magicEdges) 
			{
				return m_vis.items(group);
			} 
			
			else 
			{
				return m_vis.visibleItems(group);
			}
		}
	}

	public boolean hasMagicEdges() {
		return magicEdges;
	}

	public void setMagicEdges(boolean enabled) {
		this.magicEdges = enabled;
	}

	public boolean hasMagicNodes() {
		return magicNodes;
	}

	public void setMagicNodes(boolean enabled) {
		this.magicNodes = enabled;
	}

}
