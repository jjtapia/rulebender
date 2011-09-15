package prefuse.demo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.action.filter.GraphDistanceFilter;
import prefuse.action.layout.graph.ForceDirectedLayout;
import prefuse.activity.Activity;
import prefuse.controls.Control;
import prefuse.controls.DragControl;
import prefuse.controls.FocusControl;
import prefuse.controls.NeighborHighlightControl;
import prefuse.controls.PanControl;
import prefuse.controls.WheelZoomControl;
import prefuse.controls.ZoomControl;
import prefuse.controls.ZoomToFitControl;
import prefuse.data.Graph;
import prefuse.data.Table;
import prefuse.data.Tuple;
import prefuse.data.event.TupleSetListener;
import prefuse.data.io.GraphMLReader;
import prefuse.data.tuple.TupleSet;
import prefuse.render.DefaultRendererFactory;
import prefuse.render.LabelRenderer;
import prefuse.util.ColorLib;
import prefuse.util.GraphLib;
import prefuse.util.GraphicsLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.ItemBoundsListener;
import prefuse.util.force.ForceSimulator;
import prefuse.util.io.IOLib;
import prefuse.util.ui.JForcePanel;
import prefuse.util.ui.JValueSlider;
import prefuse.util.ui.UILib;
import prefuse.visual.VisualGraph;
import prefuse.visual.VisualItem;

/**
 * @author <a href="http://jheer.org">jeffrey heer</a>
 */
public class GraphView extends Display 
{
    private static final String graph = "graph";
    private static final String nodes = "graph.nodes";
    private static final String edges = "graph.edges";

    private Visualization m_vis;
    
    public GraphView(Graph g, String label) 
    {
        // create a new, empty visualization for our data
        m_vis = new Visualization();
        
        // mine
        m_vis.addFocusGroup("selected");
                
        // --------------------------------------------------------------------
        // set up the renderers
        
        LabelRenderer tr = new LabelRenderer();
        tr.setRoundedCorner(8, 8);
        m_vis.setRendererFactory(new DefaultRendererFactory(tr));

        // --------------------------------------------------------------------
        // register the data with a visualization
        
        // adds graph to visualization and sets renderer label field
        setGraph(g, label);
        
        // fix selected focus nodes
        /*
        TupleSet focusGroup = m_vis.getGroup(Visualization.FOCUS_ITEMS); 
        focusGroup.addTupleSetListener(new TupleSetListener() {
            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem)
            {
                for ( int i=0; i<rem.length; ++i )
                    ((VisualItem)rem[i]).setFixed(false);
                for ( int i=0; i<add.length; ++i ) {
                    ((VisualItem)add[i]).setFixed(false);
                    ((VisualItem)add[i]).setFixed(true);
                }
                if ( ts.getTupleCount() == 0 ) {
                    ts.addTuple(rem[0]);
                    ((VisualItem)rem[0]).setFixed(false);
                }
                m_vis.run("draw");
            }
        });
        */
        
        TupleSet focusGroup = m_vis.getGroup("selected"); 
        focusGroup.addTupleSetListener(new TupleSetListener() 
        {
            public void tupleSetChanged(TupleSet ts, Tuple[] add, Tuple[] rem)
            {
            	System.out.println("ch ch ch ch changes");
            }
        });
        
        // --------------------------------------------------------------------
        // create actions to process the visual data

               ColorAction fill = new ColorAction(nodes, 
                VisualItem.FILLCOLOR, ColorLib.rgb(200,200,255));
        //fill.add(VisualItem.FIXED, ColorLib.rgb(255,100,100));
       // fill.add(Visualization.FOCUS_ITEMS, ColorLib.rgb(255,100,100));
       // fill.add(VisualItem.HIGHLIGHT, ColorLib.rgb(255,200,125));
        
        fill.add("ingroup('selected')", ColorLib.rgb(225, 100, 100));
        
        ActionList draw = new ActionList();
        draw.add(fill);
        //draw.add(new ColorAction(nodes, VisualItem.STROKECOLOR, 0));
        draw.add(new ColorAction(nodes, VisualItem.TEXTCOLOR, ColorLib.rgb(0,0,0)));
        draw.add(new ColorAction(edges, VisualItem.FILLCOLOR, ColorLib.gray(200)));
        draw.add(new ColorAction(edges, VisualItem.STROKECOLOR, ColorLib.gray(200)));
        
        ActionList animate = new ActionList(Activity.INFINITY);
        animate.add(new ForceDirectedLayout(graph));
        animate.add(fill);
        animate.add(new RepaintAction());
        
        // finally, we register our ActionList with the Visualization.
        // we can later execute our Actions by invoking a method on our
        // Visualization, using the name we've chosen below.
        m_vis.putAction("draw", draw);
        m_vis.putAction("layout", animate);

        m_vis.runAfter("draw", "layout");
        
        
        // --------------------------------------------------------------------
        // set up a display to show the visualization
        
        setVisualization(m_vis);
        
        setSize(700,700);
        pan(350, 350);
        setForeground(Color.GRAY);
        setBackground(Color.WHITE);
        
        // main display controls
        //display.addControlListener(new FocusControl(1));
        addControlListener(new DragControl());
        addControlListener(new PanControl());
        addControlListener(new ZoomControl());
        addControlListener(new WheelZoomControl());
        addControlListener(new ZoomToFitControl());
        addControlListener(new Control(){

			public boolean isEnabled() {
				// TODO Auto-generated method stub
				return true;
			}

			public void setEnabled(boolean enabled) {
				// TODO Auto-generated method stub
				
			}

			public void itemDragged(VisualItem item, MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemMoved(VisualItem item, MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemWheelMoved(VisualItem item, MouseWheelEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemClicked(VisualItem item, MouseEvent e) 
			{
				System.out.println("Changing selected item");
				
				TupleSet focused = m_vis.getFocusGroup("selected");
				
				focused.clear();
				
				focused.addTuple(item);
				
				m_vis.run("draw");
			}

			public void itemPressed(VisualItem item, MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemReleased(VisualItem item, MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemEntered(VisualItem item, MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemExited(VisualItem item, MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemKeyPressed(VisualItem item, KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemKeyReleased(VisualItem item, KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void itemKeyTyped(VisualItem item, KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				
			}});

        // overview display
//        Display overview = new Display(vis);
//        overview.setSize(290,290);
//        overview.addItemBoundsListener(new FitOverviewListener());
        
        	setForeground(Color.GRAY);
        	setBackground(Color.WHITE);
        
        // --------------------------------------------------------------------        
        // launch the visualization
             
        // now we run our action list
        m_vis.run("draw");
        
    }
    
    public void setGraph(Graph g, String label) {
        // update labeling
        DefaultRendererFactory drf = (DefaultRendererFactory)
                                                m_vis.getRendererFactory();
        ((LabelRenderer)drf.getDefaultRenderer()).setTextField(label);
        
        // update graph
        m_vis.removeGroup(graph);
        VisualGraph vg = m_vis.addGraph(graph, g);
        m_vis.setValue(edges, null, VisualItem.INTERACTIVE, Boolean.FALSE);
        VisualItem f = (VisualItem)vg.getNode(0);
        m_vis.getGroup(Visualization.FOCUS_ITEMS).setTuple(f);
        f.setFixed(false);
    }
    
    // ------------------------------------------------------------------------
    // Main and demo methods
    
    public static Display getDemoDisplay()
    { 
       return new GraphView(GraphLib.getGrid(15,15), "graph");   
    }
    
    // ------------------------------------------------------------------------
    
    /**
     * Swing menu action that loads a graph into the graph viewer.
     */
    public abstract static class GraphMenuAction extends AbstractAction {
        private GraphView m_view;
        public GraphMenuAction(String name, String accel, GraphView view) {
            m_view = view;
            this.putValue(AbstractAction.NAME, name);
            this.putValue(AbstractAction.ACCELERATOR_KEY,
                          KeyStroke.getKeyStroke(accel));
        }
        public void actionPerformed(ActionEvent e) {
            m_view.setGraph(getGraph(), "label");
        }
        protected abstract Graph getGraph();
    }
    
    public static class OpenGraphAction extends AbstractAction {
        private GraphView m_view;

        public OpenGraphAction(GraphView view) {
            m_view = view;
            this.putValue(AbstractAction.NAME, "Open File...");
            this.putValue(AbstractAction.ACCELERATOR_KEY,
                          KeyStroke.getKeyStroke("ctrl O"));
        }
        public void actionPerformed(ActionEvent e) {
            Graph g = IOLib.getGraphFile(m_view);
            if ( g == null ) return;
            String label = getLabel(m_view, g);
            if ( label != null ) {
                m_view.setGraph(g, label);
            }
        }
        public static String getLabel(Component c, Graph g) {
            // get the column names
            Table t = g.getNodeTable();
            int  cc = t.getColumnCount();
            String[] names = new String[cc];
            for ( int i=0; i<cc; ++i )
                names[i] = t.getColumnName(i);
            
            // where to store the result
            final String[] label = new String[1];

            // -- build the dialog -----
            // we need to get the enclosing frame first
            while ( c != null && !(c instanceof JFrame) ) {
                c = c.getParent();
            }
            final JDialog dialog = new JDialog(
                    (JFrame)c, "Choose Label Field", true);
            
            // create the ok/cancel buttons
            final JButton ok = new JButton("OK");
            ok.setEnabled(false);
            ok.addActionListener(new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                   dialog.setVisible(false);
               }
            });
            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    label[0] = null;
                    dialog.setVisible(false);
                }
            });
            
            // build the selection list
            final JList list = new JList(names);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.getSelectionModel().addListSelectionListener(
            new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int sel = list.getSelectedIndex(); 
                    if ( sel >= 0 ) {
                        ok.setEnabled(true);
                        label[0] = (String)list.getModel().getElementAt(sel);
                    } else {
                        ok.setEnabled(false);
                        label[0] = null;
                    }
                }
            });
            JScrollPane scrollList = new JScrollPane(list);
            
            JLabel title = new JLabel("Choose a field to use for node labels:");
            
            // layout the buttons
            Box bbox = new Box(BoxLayout.X_AXIS);
            bbox.add(Box.createHorizontalStrut(5));
            bbox.add(Box.createHorizontalGlue());
            bbox.add(ok);
            bbox.add(Box.createHorizontalStrut(5));
            bbox.add(cancel);
            bbox.add(Box.createHorizontalStrut(5));
            
            // put everything into a panel
            JPanel panel = new JPanel(new BorderLayout());
            panel.add(title, BorderLayout.NORTH);
            panel.add(scrollList, BorderLayout.CENTER);
            panel.add(bbox, BorderLayout.SOUTH);
            panel.setBorder(BorderFactory.createEmptyBorder(5,2,2,2));
            
            // show the dialog
            dialog.setContentPane(panel);
            dialog.pack();
            dialog.setLocationRelativeTo(c);
            dialog.setVisible(true);
            dialog.dispose();
            
            // return the label field selection
            return label[0];
        }
    }
    
    public static class FitOverviewListener implements ItemBoundsListener {
        private Rectangle2D m_bounds = new Rectangle2D.Double();
        private Rectangle2D m_temp = new Rectangle2D.Double();
        private double m_d = 15;
        public void itemBoundsChanged(Display d) {
            d.getItemBounds(m_temp);
            GraphicsLib.expand(m_temp, 25/d.getScale());
            
            double dd = m_d/d.getScale();
            double xd = Math.abs(m_temp.getMinX()-m_bounds.getMinX());
            double yd = Math.abs(m_temp.getMinY()-m_bounds.getMinY());
            double wd = Math.abs(m_temp.getWidth()-m_bounds.getWidth());
            double hd = Math.abs(m_temp.getHeight()-m_bounds.getHeight());
            if ( xd>dd || yd>dd || wd>dd || hd>dd ) {
                m_bounds.setFrame(m_temp);
                DisplayLib.fitViewToBounds(d, m_bounds, 0);
            }
        }
    }
    
} // end of class GraphView
