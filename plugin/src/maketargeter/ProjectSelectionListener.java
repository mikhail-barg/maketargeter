package maketargeter;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
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
	private final MainView m_view;
	
	ProjectSelectionListener(MainView view)
	{
		m_view = view;
	}
	
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
			if (project.hasNature(CProjectNature.C_NATURE_ID) 
					|| project.hasNature(CCProjectNature.CC_NATURE_ID)) 
			{
				m_view.setSelectedProject(project);
			}
			else
			{
				m_view.setSelectedProject(null);
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

}
