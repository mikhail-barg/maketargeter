package maketargeter.actions;

import maketargeter.Plugin;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class CopyTargetToClipboardAction extends Action
{
	public CopyTargetToClipboardAction()
	{
		super(Messages.CopyTargetToClipboardAction_action1);
		setImageDescriptor(Plugin.getImage("/icons/enabl/copy_edit.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/copy_edit.gif")); //$NON-NLS-1$
	}
	
	@Override
	public void run()
	{
		setToClipboard(Plugin.getInstance().getTargetString());
	}
	
	private void setToClipboard(String text)
	{
		final Clipboard clipboard = new Clipboard(Display.getCurrent());
		
		clipboard.setContents(new Object[]{ text }, new Transfer[]{ TextTransfer.getInstance() });
		
		clipboard.dispose();
	}
}
