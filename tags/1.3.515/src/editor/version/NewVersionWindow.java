package editor.version;

import java.awt.Color;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class NewVersionWindow implements Runnable{

	static Composite detailsComposite;
	static Button detailsButton;
	static int defaultWindowWidth = 445;
	static int defaultWindowHeight = 135;
	static int detailWindowHeight = 335;
	
	private String changes;
	private Display display;
	
	public NewVersionWindow(Display in_display, String in_changes) 
	{
		changes = in_changes;
		display = in_display;// new Display();	
	}

	public void run() {		
		final Shell shell = new Shell(display, SWT.MENU);
		shell.setText("New Version Available");
		GridLayout shellGrid = new GridLayout();
		shellGrid.numColumns = 1;
		shell.setLayout(shellGrid);
		
		shell.forceActive();
		shell.forceFocus();
		shell.setSize(defaultWindowWidth, defaultWindowHeight);
		
		Composite msglabelgrp = new Composite(shell, SWT.NONE);
		msglabelgrp.setLayout(new GridLayout());
		Label label = new Label(msglabelgrp, SWT.BORDER);
		label.setText("Current version"+
	    		 " is out dated!" +
		"\n\nWould you like to exit and update the new version?");

		Composite buttongrp = new Composite(shell, SWT.NONE);
		GridLayout grid = new GridLayout();
		grid.numColumns = 3;
		grid.marginLeft = 20;
		grid.horizontalSpacing = 75;
		buttongrp.setLayout(grid);
		
		Button yesButton = new Button(buttongrp, SWT.PUSH);
		yesButton.setText("Yes");
		yesButton.forceFocus();
		
		yesButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event)
			{
				Program.launch("http://www.cs.pitt.edu/~ams292/bng/html/downloads.html");
		        System.exit(0);
			}
		});
		
		Button noButton = new Button(buttongrp, SWT.PUSH);
		noButton.setText("No");
		noButton.addListener(SWT.Selection, new Listener() {
			
			public void handleEvent(Event event)
			{
				shell.close();
			}
			
		});
		
		detailsButton = new Button(buttongrp, SWT.PUSH);
		detailsButton.setText("Show Details");

		detailsButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if ((detailsComposite != null) && (!detailsComposite.isDisposed())) {
					detailsComposite.dispose();
				}
								
				if (detailsButton.getText() == "Show Details") 
				{
					
					detailsComposite = new Composite(shell, SWT.NONE);
					
					detailsButton.setText("Hide Details");
					
					Text changesText = new Text(detailsComposite, SWT.BORDER | SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.WRAP);
					
					changesText.setBounds(0, 0, defaultWindowWidth-5, detailWindowHeight-defaultWindowHeight);
					
					changesText.setText(changes);
					shell.setSize(defaultWindowWidth, detailWindowHeight);
				} 
				else 
				{
					detailsButton.setText("Show Details");
					shell.setSize(defaultWindowWidth,defaultWindowHeight);
				}
				shell.layout(true);
			}
		});

	shell.open();
	}
}
