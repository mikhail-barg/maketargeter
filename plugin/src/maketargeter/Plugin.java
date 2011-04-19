package maketargeter;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Plugin extends AbstractUIPlugin
{

	/////////////////////////////////////////////////////////////
	public static final String PLUGIN_ID = "maketargeter"; //$NON-NLS-1$
	
	public static final String MT_TARGETS_FILE_NAME = "make.targets"; //$NON-NLS-1$

	public static final String MT_XML_ROOT_ELEMENT_NAME = "makeTargetsDescription"; //$NON-NLS-1$

	public static final String MT_XML_TARGETS_SECTION_ELEMENT_NAME = "targets"; //$NON-NLS-1$
	public static final String MT_XML_TARGET_ELEMENT_NAME = "target"; //$NON-NLS-1$
	public static final String MT_XML_TARGET_ELEMENT_TEXT_ATTR = "caption"; //$NON-NLS-1$
	public static final String MT_XML_TARGET_ELEMENT_COMMAND_ATTR = "command"; //$NON-NLS-1$
	public static final String MT_XML_TARGET_ELEMENT_BUILD_COMMAND_ATTR = "build_command"; //$NON-NLS-1$
	public static final String MT_XML_TARGET_ELEMENT_BUILD_LOCATION_ATTR = "build_path"; //$NON-NLS-1$
	public static final String MT_XML_TARGET_ELEMENT_HINT_ATTR = "hint"; //$NON-NLS-1$

	public static final String MT_XML_OPTIONS_SECTION_ELEMENT_NAME = "options"; //$NON-NLS-1$
	public static final String MT_XML_OPTION_ELEMENT_NAME = "option"; //$NON-NLS-1$
	public static final String MT_XML_OPTION_ELEMENT_TEXT_ATTR = "caption"; //$NON-NLS-1$
	public static final String MT_XML_OPTION_ELEMENT_COMMAND_ATTR = "command"; //$NON-NLS-1$
	public static final String MT_XML_OPTION_ELEMENT_HINT_ATTR = "hint"; //$NON-NLS-1$

	public static final String MT_XML_OPTIONS_GROUP_ELEMENT_NAME = "options_group"; //$NON-NLS-1$
	public static final String MT_XML_OPTIONS_GROUP_ELEMENT_TEXT_ATTR = "caption"; //$NON-NLS-1$
	public static final String MT_XML_OPTIONS_GROUP_ELEMENT_HINT_ATTR = "hint"; //$NON-NLS-1$

	public static final String MT_TARGET_LINE_SEPARATOR = " "; //$NON-NLS-1$
	public static final String MT_CAPTION_LINE_SEPARATOR = " "; //$NON-NLS-1$

	private static Plugin s_instance;

	/////////////////////////////////////////////////////////////
	public Plugin()
	{
		if (s_instance != null)
		{
			throw new IllegalStateException(Messages.Plugin_error1);
		}
		s_instance = this;
	}

	public static Plugin getInstance()
	{
		return s_instance;
	}

	public static ImageDescriptor getImage(String path)
	{
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/////////////////////////////////////////////////////////////
	
	MainView m_view;
	private final Map<IProject, SelectedState> m_selectionStorage = new HashMap<IProject, SelectedState>();
	private final ISelectedStateData EMPTY_SELECTION = new SelectedState();
	
	/**
	 * Returns a selected state for the given project 
	 * @param project can be null
	 * @return selected state for the project. 
	 * If the project is null or no selected state is known for the project, would return empty selection. Do not modify it! 
	 */
	ISelectedStateData getSelectedState(IProject project)
	{
		ISelectedStateData result = m_selectionStorage.get(project);
		if (result == null)
		{
			result = EMPTY_SELECTION;
		}
		return result;
	}
	
	void setSelectedState(IProject project, SelectedState state)
	{
		if (project == null)
		{
			return;
		}
		m_selectionStorage.put(project, state);
	}
	
	public void logError(String message, Exception e)
	{
		getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message, e));
	}
}
