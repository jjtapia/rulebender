package rulebender.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import prefuse.Display;
import prefuse.demo.GraphView;

public class PrefuseTestView extends ViewPart 
{

	Display d;
	
	public PrefuseTestView() 
	{
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) 
	{
		d = GraphView.getDemoDisplay();

		Composite swtAwtComponent = new Composite(parent, SWT.EMBEDDED);
		
		java.awt.Frame frame = SWT_AWT.new_Frame( swtAwtComponent );
		frame.add(d);
		
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
