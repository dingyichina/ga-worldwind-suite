package util;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

public class JVisibleDialog extends JDialog
{
	public static interface VisibilityListener
	{
		public void visibleChanged(boolean visible);
	}

	private List<VisibilityListener> listeners = new ArrayList<VisibilityListener>();
	
	public JVisibleDialog(Frame owner, String title)
	{
		super(owner, title);
	}

	@Override
	public void setVisible(boolean b)
	{
		super.setVisible(b);
		notifyVisibilityListeners();
	}

	public void addVisibilityListener(VisibilityListener listener)
	{
		listeners.add(listener);
	}

	public void removeVisibilityListener(VisibilityListener listener)
	{
		listeners.remove(listener);
	}

	protected void notifyVisibilityListeners()
	{
		for (VisibilityListener listener : listeners)
		{
			listener.visibleChanged(isVisible());
		}
	}
}