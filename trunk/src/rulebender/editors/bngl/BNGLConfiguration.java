package rulebender.editors.bngl;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

public class BNGLConfiguration extends SourceViewerConfiguration 
{
	// The PresentationReconciler does something when the document changes
	private PresentationReconciler m_reconciler;
	// The BNGLColorManager is used to keep track of the colors that are
	// used for syntax highlighting. 
	private BNGLColorManager m_colorManager;
	// The DoubleClickStrategy is a default class that was taken from the xml example. 
	// it handles double click related stuff.
	private BNGLDoubleClickStrategy m_doubleClickStrategy;
	// The Scanner is where the syntax highlighting rules are defined (I think)
	private BNGLScanner m_bnglScanner;
	private BNGLPartitionScanner m_bnglPartitionScanner;
	
	/**
	 * Create the new Configuration object with a reference to the color manager. 
	 * @param colorManager
	 */
	public BNGLConfiguration(BNGLColorManager colorManager)
	{
		setColorManager(colorManager);
	}
	
	/**
	 * 
	 *  From API: "Returns all configured content types for the given source viewer. 
	 * This list tells the caller which content types must be configured
	 *  for the given source viewer, i.e. for which content types the given 
	 *  source viewer's functionalities must be specified. This implementation
	 *   always returns new String[] { IDocument.DEFAULT_CONTENT_TYPE }."
	 *  
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) 
	{	
		return new String[] {
				IDocument.DEFAULT_CONTENT_TYPE, 
				BNGLPartitionScanner.BNGL_COMMENT
				};
	}
	
	/**
	 * Simple getter for double click strategy.
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(
			ISourceViewer sourceViewer,
			String contentType) {
			if (m_doubleClickStrategy == null)
				m_doubleClickStrategy = new BNGLDoubleClickStrategy();
			return m_doubleClickStrategy;
		}
	
	/**
	 * Simple getter for scanner
	 * @return
	 */
	public BNGLScanner getBNGLScanner()
	{
		if (m_bnglScanner == null) {
			m_bnglScanner = new BNGLScanner(m_colorManager);
			m_bnglScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						m_colorManager.getColor(IBNGLColorConstants.DEFAULT))));
		}
		
		return m_bnglScanner;
	}

	public BNGLPartitionScanner getBNGLPartitionScanner()
	{
		if (m_bnglPartitionScanner == null) {
			m_bnglPartitionScanner = new BNGLPartitionScanner();
			m_bnglPartitionScanner.setDefaultReturnToken(
				new Token(
					new TextAttribute(
						m_colorManager.getColor(IBNGLColorConstants.DEFAULT))));
		}
		
		return m_bnglPartitionScanner;
	}
	/**
	 * The PresentationReconciler is used to define attributes for each content type
	 * that is defined in the partitioner. Each partition gets its own damagerRepairer.
	 */
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) 
	{
		if (m_reconciler != null)
			return m_reconciler;

		m_reconciler = new PresentationReconciler();

		
		// Reused Reference.
		DefaultDamagerRepairer dr;

		//default content
		dr = new DefaultDamagerRepairer(getBNGLScanner());
		m_reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		m_reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		NonRuleBasedDamagerRepairer ndr =
				new NonRuleBasedDamagerRepairer(
					new TextAttribute(
						m_colorManager.getColor(IBNGLColorConstants.BNGL_COMMENT)));
			m_reconciler.setDamager(ndr, BNGLPartitionScanner.BNGL_COMMENT);
			m_reconciler.setRepairer(ndr, BNGLPartitionScanner.BNGL_COMMENT);
		
		return m_reconciler;

	}
	
	/**
	 * Simple Setter for color manager. 
	 * @param colorManager
	 */
	public void setColorManager(BNGLColorManager colorManager)
	{
		m_colorManager = colorManager;
	}
}
