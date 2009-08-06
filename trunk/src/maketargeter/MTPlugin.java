package maketargeter;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;


public class MTPlugin extends AbstractUIPlugin {

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
	
	private static MTPlugin m_plugin;
	
	public MTPlugin() {
		super();
		m_plugin = this;
	}
	
	public static MTPlugin getInstance() {
		return m_plugin;
	}
	
	public static ImageDescriptor getImage(String path) {
		URL url = null;
		
		try {
			url = getInstance().getBundle().getEntry(path);
	    } catch (IllegalStateException e) {
	    }
	    return ImageDescriptor.createFromURL(url);
	}
}
