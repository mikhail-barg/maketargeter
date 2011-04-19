package maketargeter.actions;

import maketargeter.MainView;
import maketargeter.Plugin;
import maketargeter.TargetDescription;

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
	private final MainView m_view;
	public SetTargetToProjectSettings(MainView view)
	{
		super(Messages.SetTargetToProjectSettings_action1);
		setImageDescriptor(Plugin.getImage("/icons/enabl/action-editconfig.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/action-editconfig.gif")); //$NON-NLS-1$
		
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
		
		final ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project);
		if (projectDescription == null)
		{
			throw new RuntimeException(Messages.SetTargetToProjectSettings_error1_1 + project + Messages.SetTargetToProjectSettings_error1_2);
		}
		
		final IConfiguration configuration = ManagedBuildManager.getConfigurationForDescription(projectDescription.getActiveConfiguration());
//		if (configuration.isManagedBuildOn())
//		{
//			//Don't change managed build - based projects
//			return;
//		}
		
		try
		{
			TargetDescription targetDescription = m_view.getTargetDescription();
			
			//build target
			configuration.getEditableBuilder().setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, targetDescription.getTragetCommand());
			//configuration.getEditableBuilder().setBuildPath(location);
			
			//build command
			configuration.setBuildCommand(targetDescription.isDefaultBuildCommand()? null : targetDescription.getBuildCommand());
			
			CoreModel.getDefault().setProjectDescription(project, projectDescription);
		}
		catch (CoreException e)
		{
			throw new RuntimeException(e);
		}
	}
}
