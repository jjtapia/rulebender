package rulebender.prefuse.networkviewer.contactmap;

import java.awt.geom.Rectangle2D;

import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class LabelLayout extends Layout {
    public LabelLayout(String group) {
        super(group);
    }
    public void run(double frac) {
        Iterator iter = m_vis.items(m_group);
        while ( iter.hasNext() ) {
            DecoratorItem decorator = (DecoratorItem)iter.next();
            VisualItem decoratedItem = decorator.getDecoratedItem();
            Rectangle2D bounds = decoratedItem.getBounds();
            
            double x = bounds.getCenterX();
            double y = bounds.getMinY();
            
            setX(decorator, null, x);
            setY(decorator, null, y-10);
        }
    }
} // end of class LabelLayout
	
