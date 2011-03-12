package maketargeter.actions;

import maketargeter.Plugin;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;

public class SetTargetToProjectSettings extends Action
{
	public SetTargetToProjectSettings()
	{
		super(Messages.SetTargetToProjectSettings_action1);
		setImageDescriptor(Plugin.getImage("/icons/enabl/action-editconfig.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/action-editconfig.gif")); //$NON-NLS-1$
	}

	@Override
	public void run()
	{
		final IProject project = Plugin.getInstance().getCurrentProject();
		if (project == null)
		{
			return;
		}
		
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project);
		if (projectDescription == null)
		{
			throw new RuntimeException(Messages.SetTargetToProjectSettings_error1_1 + Plugin.getInstance().getCurrentProject() + Messages.SetTargetToProjectSettings_error1_2);
		}
		
		IConfiguration configuration = ManagedBuildManager.getConfigurationForDescription(projectDescription.getActiveConfiguration());
		if (configuration.isManagedBuildOn())
		{
			//Don't change managed build - based projects
			return;
		}
		
		try
		{
			configuration.getEditableBuilder().setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, Plugin.getInstance().getTargetString());
			CoreModel.getDefault().setProjectDescription(project, projectDescription);
		}
		catch (CoreException e)
		{
			throw new RuntimeException(e);
		}
	}

	public static boolean canBeEnabled()
	{
		final IProject project = Plugin.getInstance().getCurrentProject();
		if (project == null)
		{
			return false;
		}
		
		ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project, false);
		if (projectDescription == null)
		{
			throw new RuntimeException(Messages.SetTargetToProjectSettings_error2_1 + Plugin.getInstance().getCurrentProject() + Messages.SetTargetToProjectSettings_error2_2);
		}
		IConfiguration configuration = ManagedBuildManager.getConfigurationForDescription(projectDescription.getActiveConfiguration());
		
		return !configuration.isManagedBuildOn();
	}
}
