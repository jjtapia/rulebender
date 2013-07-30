package rulebender.simulationjournaling.view;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

import rulebender.contactmap.models.CMapModelBuilder;
import rulebender.contactmap.models.ContactMapModel;
import rulebender.contactmap.prefuse.ContactMapVisual;
import rulebender.contactmap.view.ContactMapView;
import rulebender.editors.bngl.BNGLEditor;
import rulebender.editors.bngl.model.BNGASTReader;
import rulebender.editors.bngl.model.BNGLModel;

public class SmallMultiplesSelectionListener implements ISelectionListener,
    IPartListener2 {

	// The contact map visualization.
	private ContactMapView m_view;

	// The model that is being visualized.
	private BNGLModel m_currentModel;

	// A HashMap that holds any pre-existing but not currently displayed
	// contact map prefuse visualizations. This is so that a new contact map
	// is not created every time a user selects a new bngl editor tab.
	private final HashMap<String, prefuse.Display> m_contactMapRegistry;

	/**
	 * Constructor sets up the data structures given the
	 * EclipseRCP/ContactMapView.
	 * 
	 * @param view
	 */
	public SmallMultiplesSelectionListener(ContactMapView view) {

		// Set the view of the class member variable
		setView(view);

		// Create the registry
		m_contactMapRegistry = new HashMap<String, prefuse.Display>();

		// Register the view as a listener for workbench selections.
		m_view.getSite().getPage().addPostSelectionListener(this);

		// Register the view as a part listener.
		m_view.getSite().getPage().addPartListener(this);
	} // SmallMultiplesSelectionListener (constructor)

	/**
	 * Used to set the view.
	 * 
	 * @param view
	 */
	private void setView(ContactMapView view) {
		m_view = view;
	} // setView

	/**
	 * Called by Eclipse RCP when a part is opened. (we care if it is an editor)
	 */
	@Override
	public void partOpened(IWorkbenchPartReference partRef) {
		// If it's a bngl editor. (Cannot remember why there is id and instanceof
		// checking
		// but I would not change it without serious testing.)
		if (partRef.getId().equals("rulebender.editors.bngl")
		    && partRef.getPart(false) instanceof BNGLEditor) {

			// Set the current file.
			setCurrentModel(((BNGLEditor) partRef.getPart(false)).getModel());

			// Create a property changed listener for when files are saved and
			// models are updated.
			PropertyChangeListener pcl = new PropertyChangeListener() {

				@Override
				public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

					String filePath = ((BNGLModel) propertyChangeEvent.getSource())
					    .getPathID();
					String propertyName = propertyChangeEvent.getPropertyName();

					if (propertyName.equals(BNGLModel.AST)) {
						// Update the display object that is associated with the path and
						// ast.
						System.out.println("Property Changed on AST");
						updateDisplayForPathAndAST(filePath,
						    (File) propertyChangeEvent.getNewValue());

					} else if (propertyName.equals(BNGLModel.ERRORS)) {
						// Don't care.
					} // if-else
				} // propertyChange

			}; // new PropertyChageListener

			m_currentModel.addPropertyChangeListener(pcl);

			// generate the display and add the initial cmap to the registry.
			m_contactMapRegistry.put(
			    m_currentModel.getPathID(),
			    generateContactMap(m_currentModel.getPathID(),
			        m_currentModel.getAST()));

			m_view.setCMap(lookupDisplay(m_currentModel));

		} // if

	} // partOpened

	/**
	 * Can be null if there is no Display (bngl compile error)
	 * 
	 * @param model
	 * @return
	 */
	private prefuse.Display lookupDisplay(BNGLModel model) {
		return m_contactMapRegistry.get(model.getPathID());
	} // lookupDisplay

	/**
	 * Change the shown CMap Display given the path of a bngl file and an ast.
	 * 
	 * @param path
	 * @param ast
	 */
	private void updateDisplayForPathAndAST(String path, File ast) {
		// Clear the current entry.
		m_contactMapRegistry.remove(path);

		// Generate a new display
		prefuse.Display display = generateContactMap(path, ast);

		// System.out.println(path);

		// Add the new representation.
		m_contactMapRegistry.put(path, display);

		// If it is the model that is currently displayed,
		// then send the Display object to the view.
		if (path.equals(m_currentModel.getPathID())) {
			m_view.setCMap(display);
		} // if

	} // updateDisplayForPathAndAST

	/**
	 * Given a new model, set it to the current model. Also looks for an existing
	 * prefuse.Display object and sets it as visible.
	 * 
	 * @param model
	 */
	private void setCurrentModel(BNGLModel model) {
		m_currentModel = model;

		// Get an existing map.
		prefuse.Display toShow = lookupDisplay(m_currentModel);

		// Set the correct contact map.
		m_view.setCMap(toShow);

		// Activate the part. This is so the property view will add listeners
		// to the view.
		m_view.getViewSite().getPage().activate(m_view.getViewSite().getPart());

	} // setCurrentModel

	/**
	 * Generates a contact map using the abstract syntax tree from the bngl
	 * parser.
	 * 
	 * @param sourcePath
	 *          The path of the resource. This is used for selection later.
	 * @param ast
	 *          - The abstract syntax tree for the bngl model.
	 * @return
	 */
	private prefuse.Display generateContactMap(String sourcePath, File ast) {
		// If the ast has not been generated for this model, then
		// just return null so there is not contact map displayed.
		// Also, an empty file produces a complete ast, so the length
		// requirement catches that.

		if (ast == null || ast.toString() == null
		    || ast.toString().split("\\n").length <= 20) {
			return null;
		} // if

		// Create the builder for the cmap
		CMapModelBuilder cmapModelBuilder = new CMapModelBuilder();

		// Create the astReader and register the cmapModelBuilder
		BNGASTReader astReader = new BNGASTReader(cmapModelBuilder);

		// Use the reader to construct the model for the given ast.
		// Sometimes an ast is not null, but is not complete due to errors.
		// This try/catch block catches those situations.
		try {
			astReader.buildModel(ast);
		} catch (NullPointerException e) {
			// e.printStackTrace();
			// Debug
			System.out.println("Failed to produce CMapModel on ast:\n"
			    + ast.toString());
			return null;
		} // try-catch

		// Get the model from the builder.
		ContactMapModel cModel = cmapModelBuilder.getCMapModel();

		// FIXME
		// This should never happen. If it does then an error should have been
		// thrown
		// by the parser. I'm only 95% sure about that statement, so I am
		// leaving this here. If there is a legitimate way for this to happen, then
		// an Exception should be thrown here.
		if (cModel == null) {
			System.out.println("The CMapModel is null. Something went wrong "
			    + "while trying to parse the Abstract Syntax Tree for the "
			    + "BNGL File.  \nExiting");
			// FIXME Even if this 'never' happens, this can be handled gracefully...
			// return null?? derp.
			System.exit(0);
		} // if

		cModel.setSourcePath(sourcePath);

		// Set a dimension
		Dimension dim = m_view.getSize();

		// Get the CMapVisual object for the CMapModel
		ContactMapVisual cVisual = new ContactMapVisual(m_view, cModel, dim);

		return cVisual.getDisplay();
	} // generateContactMap

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

	@Override
	public void partActivated(IWorkbenchPartReference partRef) {
		// TODO Auto-generated method stub

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

	/**
	 * Called on a selection event.
	 */
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
