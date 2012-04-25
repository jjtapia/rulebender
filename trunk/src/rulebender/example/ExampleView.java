package rulebender.example;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;

import rulebender.editors.bngl.BNGLEditor;
import rulebender.editors.bngl.model.BNGLModel;

public class ExampleView extends ViewPart implements IPartListener2
{

	// A label for output
	private Label m_label;
	
	private ArrayList<String> m_myModel;
	
	// Generally nothing happens in the constructor
	public ExampleView() {
	
	}

	// This is where the view itself is defined using JFace widgets or SWT.
	@Override
	public void createPartControl(Composite parent) 
	{
		m_label = new Label(parent, SWT.BORDER);
		m_label.setText("Hello from a new view!!!");

		m_myModel = new ArrayList<String>();
		
		// Register the view as a part listener.
		getSite().getPage().addPartListener(this);
	}

	// This gets called when the view gets focus from the user.  
	// You do not have to implement it, but you can tell any of the
	// view elements that you define about the focus event if you want to.
	@Override
	public void setFocus() {}

	@Override
	public void partActivated(IWorkbenchPartReference partRef) 
	{
		// If it's a bngl editor.   (Cannot remember why there is id and instanceof checking
		// but I would not change it without serious testing.)
		if(partRef.getId().equals("rulebender.editors.bngl") && partRef.getPart(false) instanceof BNGLEditor)
		{	
			// Get the model.
			BNGLModel model = ((BNGLEditor) partRef.getPart(false)).getModel();
		
			// Create a property changed listener for when files are saved and 
			// models are updated.
			PropertyChangeListener pcl = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent propertyChangeEvent) 
				{
					String filePath = ((BNGLModel) propertyChangeEvent.getSource()).getPathID();
					String propertyName = propertyChangeEvent.getPropertyName();
					
					if(propertyName.equals(BNGLModel.AST))
					{
						//Update the display object that is associated with the path and ast. 
						m_label.setText("Property Changed on AST for " + filePath);
						System.out.println("Property Changed on AST for " + filePath);
					}
				}
			};
				
			model.addPropertyChangeListener(pcl);
						
			m_label.setText("New model opened!  " + model.getPathID());
			
			createAndSetMyModel(model);
		} // end if it's an editor block	
	}
	
	private void createAndSetMyModel(BNGLModel model)
	{
		
	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partClosed(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partDeactivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partHidden(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partVisible(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void partInputChanged(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub
		
	}
}
