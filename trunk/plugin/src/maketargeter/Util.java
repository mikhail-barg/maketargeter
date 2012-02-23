/**
 * 
 */
package maketargeter;

import java.util.Iterator;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author Mike
 * 
 * Miscellaneous utility functions.
 *
 */
public class Util
{
	/**to prevent external instantiation*/
	private Util() {}

	/**
	 * @param project
	 * @return
	 */
	static boolean checkProjectOpen(IProject project)
	{
		return (project != null && project.isAccessible());
	}
	
	/**
	 * @param selection
	 * @return
	 */
	static IProject findFirstProjectBySelectedResource(IStructuredSelection selection)
	{
		for (Iterator<?> e = selection.iterator(); e.hasNext();)
		{
			final Object obj = e.next();
			IResource resource = null;
			if (obj instanceof IResource)
			{
				resource = (IResource) obj;
			}
			else if (obj instanceof IAdaptable)
			{
				resource = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
			}
			if (resource == null)
			{
				continue;
			}
			
			//found resource
			IProject project = resource.getProject();
			if (project != null)
			{
				return project;
			}
//			if (checkProjectOpen(project))
//			{
//				return project;
//			}
		}
		return null;
	}
	
	static Button getSelectedRadioButton(Composite group)
	{
		for (Control control : group.getChildren())
		{
			if (control instanceof Button)
			{
				if (((Button) control).getSelection())
				{
					return (Button) control;
				}
			}
		}
		return null;
	}

	static void setSelectedRadioButton(Composite group, String selectedButtonCaption)
	{
		Button firstButton = null;
		for (Control control : group.getChildren())
		{
			if (control instanceof Button)
			{
				final Button button = (Button) control;
				if (firstButton == null)
				{
					firstButton = button;
				}
				
				if (selectedButtonCaption != null && button.getText().equals(selectedButtonCaption))
				{
					button.setSelection(true);
					return;
				}
			}
		}
		
		//haven't found the button with given caption
		if (firstButton != null)
		{
			firstButton.setSelection(true);
		}
	}
	
	static boolean isButtonSelected(Button button)
	{
		return (button != null && button.getSelection() && button.getData() != null);
	}
	
	public static String getTargetBuildId(IMakeTargetManager targetManager, IProject project)
	{
		String[] id = targetManager.getTargetBuilders(project);
		if (id.length == 0)
		{
			/*
			 * throw new CoreException(new Status(IStatus.ERROR,
			 * MakeUIPlugin.getUniqueIdentifier(), -1,
			 * MakeUIPlugin.getResourceString
			 * ("MakeTargetDialog.exception.noTargetBuilderOnProject"),
			 * null)); //$NON-NLS-1$
			 */
			return null;
		}
		return id[0];
	}
	
	/**
	 * @return file handle of a resource file. Can be null.
	 */
	public static IFile getTragetsFile(IProject project)
	{
		if (!checkProjectOpen(project))
		{
			// bad project.. probably already closed or something
			return null;
		}

		return project.getFile(Plugin.MT_TARGETS_FILE_NAME);
	}
	
	
	public static boolean isFileExists(IFile file)
	{
		return file != null && file.exists();
	}
	
	public static IMakeTarget createTarget(IMakeTargetManager targetManager, IProject project, TargetDescription targetDescription, String caption) throws CoreException
	{
		final IMakeTarget target = targetManager.createTarget(
				project, 
				caption, 
				getTargetBuildId(targetManager, project));
		target.setStopOnError(true);
		target.setRunAllBuilders(true);
		final boolean defaultBuildCommand = targetDescription.isDefaultBuildCommand();
		target.setUseDefaultBuildCmd(defaultBuildCommand);
		if (!defaultBuildCommand)
		{
			target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND, targetDescription.getBuildCommand());
		}
		target.setBuildAttribute(IMakeTarget.BUILD_TARGET, targetDescription.getTragetCommand());
		return target;
	}
	
	public static IContainer getBuildContainerForTarget(IProject project, TargetDescription targetDescription)
	{
		if (targetDescription.isDefaultBuildLocation())
		{
			return project;
		}
		
		IFolder folder = project.getFolder(targetDescription.getBuildLocation());
		if (!folder.exists())
		{
			String message = "The specified build path does not exist :" + folder; //$NON-NLS-1$
			Plugin.getInstance().logError(message, null);
			throw new IllegalArgumentException(message);
		}
		return folder;
	}

	public static TargetDescription getTargetDescriptionFromProject(IProject project)
	{
		if (project == null)
		{
			return null;
		}
		
		final ICProjectDescription projectDescription = CoreModel.getDefault().getProjectDescription(project);
		if (projectDescription == null)
		{
			throw new RuntimeException("Failed to get a description for the project '" + project + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		final IConfiguration configuration = ManagedBuildManager.getConfigurationForDescription(projectDescription.getActiveConfiguration());
		final IBuilder builder = configuration.getBuilder(); 
		String targetCommand = builder.getBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, ""); //$NON-NLS-1$
		String buildLocation = builder.getBuildAttribute(IBuilder.BUILD_LOCATION, ""); //$NON-NLS-1$
		String buildCommand = configuration.getBuildCommand();
		if (buildCommand == null || builder.isDefaultBuildCmd())
		{
			buildCommand = ""; //$NON-NLS-1$
		}
		
		return new TargetDescription(targetCommand, buildCommand, buildLocation);
	}
}
