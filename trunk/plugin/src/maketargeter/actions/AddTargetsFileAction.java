package maketargeter.actions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import maketargeter.Plugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Mike
 *
 */
public class AddTargetsFileAction extends Action
{
	public AddTargetsFileAction()
	{
		super("Add targets file to project");
		setImageDescriptor(Plugin.getImage("/icons/enabl/file_add.gif")); //$NON-NLS-1$
		setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/file_add.gif")); //$NON-NLS-1$
	}

	@Override
	public void run()
	{
		try
		{
			ResourcesPlugin.getWorkspace().run(
					new IWorkspaceRunnable()
					{
						@Override
						public void run(IProgressMonitor monitor) throws CoreException
						{
							processAddFile(monitor);
						}
					},
					Plugin.getInstance().getCurrentProject(),
					IWorkspace.AVOID_UPDATE,
					null);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	private void processAddFile(IProgressMonitor monitor)
	{
		final IFile file = Plugin.getInstance().getTragetsFile();
		
		if (file == null)
		{
			return;
		}
		
		if (file.exists())
		{ 
			// already exist
			return; 
		}

		try
		{
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

			Element rootElement = doc.createElement(Plugin.MT_XML_ROOT_ELEMENT_NAME);
			doc.appendChild(rootElement);

			// targets section
			{
				final Element targetsSection = doc.createElement(Plugin.MT_XML_TARGETS_SECTION_ELEMENT_NAME);
				rootElement.appendChild(targetsSection);

				Element target = doc.createElement(Plugin.MT_XML_TARGET_ELEMENT_NAME);
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_TEXT_ATTR, "Build");
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_COMMAND_ATTR, "build");
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_HINT_ATTR, "Default build target");
				targetsSection.appendChild(target);

				target = doc.createElement(Plugin.MT_XML_TARGET_ELEMENT_NAME);
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_TEXT_ATTR, "Clean");
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_COMMAND_ATTR, "clean");
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_HINT_ATTR, "Clean previously built project");
				targetsSection.appendChild(target);
			}

			// options section
			{
				final Element optionsSection = doc.createElement(Plugin.MT_XML_OPTIONS_SECTION_ELEMENT_NAME);
				rootElement.appendChild(optionsSection);

				Element option = doc.createElement(Plugin.MT_XML_OPTION_ELEMENT_NAME);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR, "Single Option");
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR, "option");
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR, "Option to configure target behaviour");
				optionsSection.appendChild(option);

				Element optionsGroup = doc.createElement(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_NAME);
				optionsGroup.setAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_TEXT_ATTR, "Options Group");
				optionsGroup.setAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_HINT_ATTR, "Mutually-exclusive options might be grouped");
				optionsSection.appendChild(optionsGroup);

				option = doc.createElement(Plugin.MT_XML_OPTION_ELEMENT_NAME);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR, "Debug");
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR, "Debug=true");
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR, "Grouped option 1");
				optionsGroup.appendChild(option);

				option = doc.createElement(Plugin.MT_XML_OPTION_ELEMENT_NAME);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR, "Release");
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR, "Release=true");
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR, "Grouped option 2");
				optionsGroup.appendChild(option);
			}

			writeDocumentToFile(doc, file, monitor);
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (DOMException e)
		{
			e.printStackTrace();
		}
		
		Plugin.getInstance().updateViews();
	}

	/**
	 * @param doc
	 * @param file
	 * @param monitor
	 */
	private void writeDocumentToFile(Document doc, IFile file, IProgressMonitor monitor)
	{
		final ByteArrayOutputStream oStream = new ByteArrayOutputStream();

		try
		{
			final Transformer serializer = TransformerFactory.newInstance().newTransformer();

			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");

			serializer.transform(new DOMSource(doc), new StreamResult(oStream));

		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}

		try
		{
			file.create(new ByteArrayInputStream(oStream.toByteArray()), false, monitor);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}
}
