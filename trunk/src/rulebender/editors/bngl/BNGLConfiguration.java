package rulebender.editors.bngl;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import rulebender.editors.net.NETPartitionScanner;
import rulebender.editors.net.NETScanner;

public class BNGLConfiguration extends SourceViewerConfiguration 
{
	private PresentationReconciler reconciler;

	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) 
	{	
		return new String[] {IDocument.DEFAULT_CONTENT_TYPE, NETPartitionScanner.NET_COMMENT};
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) 
	{
		if (reconciler != null)
			return reconciler;

		reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr;
		
		//comments

		dr = new DefaultDamagerRepairer(NETScanner.getCommentScanner());
		reconciler.setDamager(dr, NETPartitionScanner.NET_COMMENT);
		reconciler.setRepairer(dr, NETPartitionScanner.NET_COMMENT);

		//default content

		dr = new DefaultDamagerRepairer(NETScanner.getScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;

	}
}
