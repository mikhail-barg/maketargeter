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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * @author Mike
 *
 */
public class AddTargetsFileAction extends Action
{
	private final ImageDescriptor m_imageAddFileEnabled = Plugin.getImage("/icons/enabl/file_add.gif");	//$NON-NLS-1$
	private final ImageDescriptor m_imageEditFileEnabled = Plugin.getImage("/icons/enabl/file_edit.gif");	//$NON-NLS-1$
	private final ImageDescriptor m_imageAddFileDisabled = Plugin.getImage("/icons/disabl/file_add.gif"); //$NON-NLS-1$
	
	public AddTargetsFileAction()
	{
		super();
	}

	public void update()
	{
		setEnabled(Plugin.getInstance().getCurrentProject() != null);
		
		if (Plugin.getInstance().targetFileExists())
		{
			setText("Edit targets description file");
			setImageDescriptor(m_imageEditFileEnabled); 
			setDisabledImageDescriptor(m_imageAddFileDisabled);
		}
		else
		{
			setText("Add targets description file to project");
			setImageDescriptor(m_imageAddFileEnabled); 
			setDisabledImageDescriptor(m_imageAddFileDisabled);
		}
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
							if (!Plugin.getInstance().targetFileExists())
							{
								processAddFile(monitor);
							}
							
							openEditor();
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
	
	private void openEditor()
	{
		final IFile file = Plugin.getInstance().getTragetsFile();
		
		try
		{
			org.eclipse.ui.ide.IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), file);
		}
		catch (PartInitException e)
		{
			throw new RuntimeException("Failed to open editor for the file");
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
			doc.setXmlStandalone(true);

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
