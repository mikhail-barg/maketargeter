package maketargeter.actions;

import maketargeter.MainView;
import maketargeter.Plugin;
import maketargeter.Util;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Shell;

public class BuildTargetAction extends Action
{
	private final Shell m_shell;
	private final MainView m_view;

	public BuildTargetAction(MainView view, Shell shell)
	{
		super(Messages.BuildTargetAction_action1);
		setImageDescriptor(Plugin.getImage("/icons/enabl/target_build.png")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/target_build.png")); //$NON-NLS-1$
		m_shell = shell;
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
		
		final IMakeTargetManager targetManager = MakeCorePlugin.getDefault().getTargetManager();
		
		try
		{
			IMakeTarget target = targetManager.createTarget(project, "Custom target", Util.getTargetBuildId(targetManager, project)); //$NON-NLS-1$
			target.setStopOnError(true);
			target.setRunAllBuilders(true);
			target.setUseDefaultBuildCmd(true);
			target.setBuildAttribute(IMakeTarget.BUILD_TARGET, m_view.getTargetString());
			TargetBuild.buildTargets(m_shell, new IMakeTarget[] { target });
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
}
