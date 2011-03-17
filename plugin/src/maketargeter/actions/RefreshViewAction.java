package maketargeter.actions;

import maketargeter.MainView;
import maketargeter.Plugin;

import org.eclipse.jface.action.Action;

/**
 * @author Mike
 */
public class RefreshViewAction extends Action
{
	private final MainView m_view;
	
	public RefreshViewAction(MainView view)
	{
		super(Messages.RefreshViewAction_title);
		setImageDescriptor(Plugin.getImage("/icons/enabl/refresh.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/refresh.gif")); //$NON-NLS-1$
		
		m_view = view;
	}

	@Override
	public void run()
	{
		m_view.onTargetsFileChanged();
	}
}
