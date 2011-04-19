/**
 * 
 */
package maketargeter;

import java.util.Iterator;

import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
}
