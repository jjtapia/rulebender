package rulebender.errorview.view;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import rulebender.errorview.model.BNGLError;
import rulebender.errorview.model.ErrorViewModelProvider;

public class ErrorView extends ViewPart
{
	public static final String ID = "rulebender.errorview.view.ErrorView";
	
	private TableViewer m_tableViewer;

	@Override
	public void createPartControl(Composite parent) 
	{
		createViewer(parent);
	}

	
	private void createViewer(Composite parent) 
	{
		m_tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.BORDER);
		createColumns(parent, m_tableViewer);
		
		final Table table = m_tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		m_tableViewer.setContentProvider(new ArrayContentProvider());
		
		// Make the selection available to other views
		getSite().setSelectionProvider(m_tableViewer);
		
		// Set the sorter for the table
		// Layout the viewer
		GridData gridData = new GridData();
		gridData.verticalAlignment = GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		m_tableViewer.getControl().setLayoutData(gridData);
		
		ErrorViewModelProvider evmp = new ErrorViewModelProvider(this);
		//Get the content for the viewer, setInput will call getElements in the
		// contentProvider
		m_tableViewer.setInput(evmp.getErrors());
	}

	public TableViewer getViewer() {
		return m_tableViewer;
	}

	// This will create the columns for the table
	private void createColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "File Path", "Line Number", "Message" };
		int[] bounds = { 100, 100, 300};

		// First column is for the file path.
		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				BNGLError p = (BNGLError) element;
				return p.getFilePath();
			}
		});

		// Second column is for the line number
		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				BNGLError p = (BNGLError) element;
				return ""+p.getLineNumber();
			}
		});

		// Now the gender
		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				BNGLError p = (BNGLError) element;
				return p.getMessage();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) 
	{
		final TableViewerColumn viewerColumn = new TableViewerColumn(m_tableViewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	@Override
	public void setFocus() 
	{
		m_tableViewer.getControl().setFocus();
	}


	public void refresh() 
	{
		m_tableViewer.refresh();		
	}	
}
