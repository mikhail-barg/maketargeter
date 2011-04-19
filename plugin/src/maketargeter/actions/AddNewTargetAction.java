package maketargeter.actions;

import maketargeter.MainView;
import maketargeter.Plugin;
import maketargeter.Util;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;

/**
 * @author Mike
 *
 */
public class AddNewTargetAction extends Action
{
	private final MainView m_view;
	
	public AddNewTargetAction(MainView view)
	{
		super(Messages.AddNewTargetAction_action1);
		setImageDescriptor(Plugin.getImage("/icons/enabl/target_add.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/target_add.gif")); //$NON-NLS-1$
		
		m_view = view;
	}

	@Override
	public void run()
	{
		final IProject project = m_view.getCurrentProject();
		if (project == null)
		{
			return;
		}
		
		try
		{
			final IMakeTargetManager targetManager = MakeCorePlugin.getDefault().getTargetManager();
			final IMakeTarget target = Util.createTarget(
											targetManager, 
											project, 
											m_view.getTargetDescription(), 
											getTargetName(targetManager, project, m_view.getCaptionString())
											); 
			targetManager.addTarget(project, target);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	private String getTargetName(IMakeTargetManager targetManager, IProject project, String targetName)
	{
		String newName = targetName;
		int i = 0;
		try
		{
			while (targetManager.findTarget(project, newName) != null)
			{
				i++;
				newName = targetName + " (" + Integer.toString(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch (CoreException e)
		{
		}
		return newName;
	}
}
