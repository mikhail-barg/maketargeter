package maketargeter;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Singleton
 * 
 * @author Mike
 */
class ProjectSelectionListener implements ISelectionListener
{
	
	/** To prevent external instantiation. */
	private ProjectSelectionListener() {}
	
	static final ProjectSelectionListener INSTANCE = new ProjectSelectionListener();
	
	@Override
	public void selectionChanged(IWorkbenchPart part, ISelection selection)
	{
		if (selection == null 
				|| !(selection instanceof IStructuredSelection)
				|| selection.isEmpty()
			)
		{
			return;
		}

		IProject project = Util.findFirstOpenProjectBySelectedResource((IStructuredSelection) selection);

		if (project == null)
		{
			// TODO: try some other ways to get the project, for example from
			// the IWorkbenchPart
			return;
		}

		try
		{
			if (project.hasNature("org.eclipse.cdt.core.cnature")) //$NON-NLS-1$ 
			{
				Plugin.getInstance().setCurrentProject(project);
			}
			else
			{
				Plugin.getInstance().setCurrentProject(null);
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

}
