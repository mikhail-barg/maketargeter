/**
 * 
 */
package maketargeter;

import java.util.Iterator;

import org.eclipse.cdt.make.core.IMakeTargetManager;
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
	static IProject findFirstOpenProjectBySelectedResource(IStructuredSelection selection)
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
			if (checkProjectOpen(project))
			{
				return project;
			}
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
}
