package maketargeter.actions;

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
	public AddNewTargetAction()
	{
		super(Messages.AddNewTargetAction_action1);
		setImageDescriptor(Plugin.getImage("/icons/enabl/target_add.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/target_add.gif")); //$NON-NLS-1$
	}

	@Override
	public void run()
	{
		final IProject project = Plugin.getInstance().getCurrentProject();
		if (project == null)
		{
			return;
		}
		
		final IMakeTargetManager targetManager = MakeCorePlugin.getDefault().getTargetManager();
		
		try
		{
			final IMakeTarget target = targetManager.createTarget(
											project, 
											getTargetName(targetManager, project, Plugin.getInstance().getCaptionString()), 
											Util.getTargetBuildId(targetManager, project));
			target.setStopOnError(true);
			target.setRunAllBuilders(true);
			target.setUseDefaultBuildCmd(true);
			target.setBuildAttribute(IMakeTarget.BUILD_TARGET, Plugin.getInstance().getTargetString());
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