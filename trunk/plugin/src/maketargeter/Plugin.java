package maketargeter;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class Plugin extends AbstractUIPlugin
{

	/////////////////////////////////////////////////////////////
	public static final String MT_TARGETS_FILE_NAME = "make.targets";

	public static final String MT_XML_ROOT_ELEMENT_NAME = "makeTargetsDescription";

	public static final String MT_XML_TARGETS_SECTION_ELEMENT_NAME = "targets";
	public static final String MT_XML_TARGET_ELEMENT_NAME = "target";
	public static final String MT_XML_TARGET_ELEMENT_TEXT_ATTR = "caption";
	public static final String MT_XML_TARGET_ELEMENT_COMMAND_ATTR = "command";
	public static final String MT_XML_TARGET_ELEMENT_HINT_ATTR = "hint";

	public static final String MT_XML_OPTIONS_SECTION_ELEMENT_NAME = "options";
	public static final String MT_XML_OPTION_ELEMENT_NAME = "option";
	public static final String MT_XML_OPTION_ELEMENT_TEXT_ATTR = "caption";
	public static final String MT_XML_OPTION_ELEMENT_COMMAND_ATTR = "command";
	public static final String MT_XML_OPTION_ELEMENT_HINT_ATTR = "hint";

	public static final String MT_XML_OPTIONS_GROUP_ELEMENT_NAME = "options_group";
	public static final String MT_XML_OPTIONS_GROUP_ELEMENT_TEXT_ATTR = "caption";
	public static final String MT_XML_OPTIONS_GROUP_ELEMENT_HINT_ATTR = "hint";

	public static final String MT_TARGET_LINE_SEPARATOR = " ";

	private static Plugin m_plugin;

	/////////////////////////////////////////////////////////////
	public Plugin()
	{
		if (m_plugin != null)
		{
			throw new IllegalStateException("Trying to initialize second instance of singleton plugin");
		}
		m_plugin = this;
	}

	static Plugin getInstance()
	{
		return m_plugin;
	}

	static ImageDescriptor getImage(String path)
	{
		URL url = null;

		try
		{
			url = getInstance().getBundle().getEntry(path);
		}
		catch (IllegalStateException e)
		{
		}
		return ImageDescriptor.createFromURL(url);
	}

	/////////////////////////////////////////////////////////////
	
	private final List<MainView> m_views = new LinkedList<MainView>();
	private IProject m_currentProject;
	private String m_targetString = "";
	

	/**
	 * @return currently selected project. Can be null;
	 */
	IProject getCurrentProject()
	{
		return m_currentProject;
	}
	
	/**
	 * Sets a new current project
	 * @param project
	 * @return true if given project and previously seletced are both not null and different 
	 */
	boolean setCurrentProject(IProject project)
	{
		if (m_currentProject == null && project == null)
		{
			return false;
		}
		if (m_currentProject != null && m_currentProject.equals(project))
		{
			return false;
		}
	
		m_currentProject = project;
		
		updateViews();
		
		return true;
	}
	
	boolean isCurrentProjectOpened()
	{
		return Util.checkProjectOpen(m_currentProject);
	}
	
	/**
	 * @return file handle of a resource file. Can be null.
	 */
	IFile getTragetsFile()
	{
		if (!isCurrentProjectOpened())
		{
			// bad project.. probably already closed or something
			return null;
		}

		return m_currentProject.getFile(Plugin.MT_TARGETS_FILE_NAME);
	}

	void registerView(MainView view)
	{
		m_views.add(view);
	}
	
	void removeView(MainView view)
	{
		m_views.remove(view);
	}
	
	void updateViews()
	{
		for (MainView view : m_views)
		{
			view.update();
		}
	}
	
	String getTargetString()
	{
		return m_targetString;
	}
	
	void setTargetString(String string)
	{
		m_targetString = string;
	}
}
