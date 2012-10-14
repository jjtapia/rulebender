package resultviewer.text;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;

/**
 * 
 * Create partitioned NET Document object based on string of file content.
 * 
 */
public class NETDocumentProvider {
	public Document createDocument(String content) {
		Document document = new Document(content);

		// partitioner
		IDocumentPartitioner partitioner = new FastPartitioner(
				new NETPartitionScanner(), new String[] {
						IDocument.DEFAULT_CONTENT_TYPE,
						NETPartitionScanner.NET_COMMENT });
		partitioner.connect(document);
		document.setDocumentPartitioner(partitioner);
		return document;
	}

}
