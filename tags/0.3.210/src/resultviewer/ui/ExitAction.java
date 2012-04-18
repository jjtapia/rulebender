package resultviewer.ui;

import org.eclipse.jface.action.*;
import org.eclipse.jface.window.*;

public class ExitAction extends Action {
	ResultViewer window;

	public ExitAction(ResultViewer w) {
		window = w;
		setText("E&xit");
	}

	public void run() {
		if (window != null && window.getGraphFrame() != null
				&& window.getGraphFrame().isShowing()) {
			window.getGraphFrame().dispose(); // close the graph
		}
		window.close();
	}
}
