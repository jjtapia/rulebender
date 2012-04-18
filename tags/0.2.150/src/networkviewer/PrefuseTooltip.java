package networkviewer;

import java.awt.event.MouseEvent;

public abstract class PrefuseTooltip {
	protected javax.swing.JPanel popup;
	protected int startDelay, stopDelay;
	protected javax.swing.Timer startShowingTimer, stopShowingTimer;
	protected javax.swing.JComponent owner;
	protected boolean isSticky;

	public PrefuseTooltip(javax.swing.JComponent owner) {
		this(owner, 1000, 1000);
	}
	
	public PrefuseTooltip(javax.swing.JComponent owner, int startDelay, int stopDelay) {
		this.owner = owner;
		this.startDelay = startDelay;
		this.stopDelay = stopDelay;
		this.isSticky = false;
		
		startShowingTimer = new javax.swing.Timer(startDelay, new ShowTimerAction(true));
		stopShowingTimer = new javax.swing.Timer(stopDelay, new ShowTimerAction(false));
		startShowingTimer.setRepeats(false);
		stopShowingTimer.setRepeats(false);
	}
	
	public void startShowing(int x, int y) {
		java.awt.Component contents = getContents();
		contents.addMouseListener(new PrefuseTooltipListener());

		popup = new javax.swing.JPanel(new java.awt.BorderLayout(), true);
		popup.setVisible(false);
		popup.setLocation(x, y);
		popup.setSize(contents.getPreferredSize());
		popup.add(contents, java.awt.BorderLayout.CENTER);
		owner.add(popup);

		startShowingTimer.start();
	}
	
	public void stopShowing() {
		if(PrefuseTooltip.this.popup.isVisible() && !isSticky) {
			stopShowingTimer.start();
		} else {
			startShowingTimer.stop();
		}
	}
	
	public void startShowingImmediately() {
		stopShowingTimer.stop();
		if(!PrefuseTooltip.this.popup.isVisible()) {
			startShowingTimer.stop();
			bringToFront();
			popup.setVisible(true);
		}
	}
	
	public void stopShowingImmediately() {
		startShowingTimer.stop();
		if(PrefuseTooltip.this.popup.isVisible() && !isSticky) {
			stopShowingTimer.stop();
			popup.setVisible(false);
		}
	}
	
	public void setSticky(boolean isSticky) {
		this.isSticky = isSticky;
	}
	public boolean isSticky() { return isSticky; }
	
	public void bringToFront() {
		popup.getParent().setComponentZOrder(popup, 0);
	}
	
	protected class ShowTimerAction implements java.awt.event.ActionListener
	{
		protected boolean showTimer;
		
		protected ShowTimerAction(boolean showTimer) {
			this.showTimer = showTimer;
		}
		
		public void actionPerformed(java.awt.event.ActionEvent e) {
			if(showTimer) {
				PrefuseTooltip.this.startShowingImmediately();
			} else {
				PrefuseTooltip.this.stopShowingImmediately();
			}
		}
	}
	
	protected class PrefuseTooltipListener extends java.awt.event.MouseAdapter {
		public void mouseEntered(MouseEvent e) {
			stopShowingTimer.stop();
		}
		
		public void mouseExited(MouseEvent e) {
			if(((java.awt.Component)e.getSource()).getMousePosition() != null) {
				// don't stop showing if we are still inside the contents.
				// this is to fix the "feature" where mouseExited is fired when
				// the cursor is moved over a child component of contents. eg,
				// when the cursor is moved onto a JButton that is inside a
				// JPanel contents box, etc.
				return;
			}
			PrefuseTooltip.this.stopShowing();
		}
	}
	
	// override this method when you implement a PrefuseTooltip
	abstract protected java.awt.Component getContents();
}
