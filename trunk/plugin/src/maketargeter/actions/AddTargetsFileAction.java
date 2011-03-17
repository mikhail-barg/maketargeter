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

import maketargeter.MainView;
import maketargeter.Plugin;
import maketargeter.Util;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
	private static final class AddFileJob extends WorkspaceJob
	{
		private final MainView m_view;
		final IFile m_targetsFile;
		
		public AddFileJob(MainView view)
		{
			super(Messages.AddTargetsFileAction_adding_targets_file);
			m_view = view;
			m_targetsFile = Util.getTragetsFile(m_view.getCurrentProject());
			setRule(ResourcesPlugin.getWorkspace().getRuleFactory().createRule(m_targetsFile));
		}

		@Override
		public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException
		{
			monitor.beginTask("", 3); //$NON-NLS-1$
			
			IStatus subtaskStatus = processAddFile(m_targetsFile, monitor);
			
			if (subtaskStatus != Status.OK_STATUS)
			{
				monitor.done();
				return subtaskStatus;
			}
			
			m_view.onTargetsFileChanged();
			monitor.worked(1);
			
			openEditor(m_targetsFile);
			monitor.done();
			return Status.OK_STATUS;
		}
	}
	
	
	private final ImageDescriptor m_imageAddFileEnabled = Plugin.getImage("/icons/enabl/file_add.gif");	//$NON-NLS-1$
	private final ImageDescriptor m_imageEditFileEnabled = Plugin.getImage("/icons/enabl/file_edit.gif");	//$NON-NLS-1$
	private final ImageDescriptor m_imageAddFileDisabled = Plugin.getImage("/icons/disabl/file_add.gif"); //$NON-NLS-1$
	
	private final MainView m_view;
	
	public AddTargetsFileAction(MainView view)
	{
		m_view = view;
	}
	
	public void update()
	{
		final IProject project = m_view.getCurrentProject();
		setEnabled(project != null);
		
		if (Util.isFileExists(Util.getTragetsFile(project)))
		{
			setText(Messages.AddTargetsFileAction_action1);
			setImageDescriptor(m_imageEditFileEnabled); 
			setDisabledImageDescriptor(m_imageAddFileDisabled);
		}
		else
		{
			setText(Messages.AddTargetsFileAction_action2);
			setImageDescriptor(m_imageAddFileEnabled); 
			setDisabledImageDescriptor(m_imageAddFileDisabled);
		}
	}
	
	@Override
	public void run()
	{
		final IFile targetsFile = Util.getTragetsFile(m_view.getCurrentProject());
		if (targetsFile == null)
		{
			return;
		}
		
		if (!Util.isFileExists(targetsFile))
		{
			final Job job = new AddFileJob(m_view);
			job.schedule();
		}
		else
		{
			openEditor(targetsFile);
		}
	}
	
	private static void openEditor(final IFile targetsFile)
	{
		PlatformUI.getWorkbench().getDisplay().syncExec(
			new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							//should be called from UI thread because of getActiveWorkbenchWindow();
							org.eclipse.ui.ide.IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), targetsFile);
						}
						catch (PartInitException e)
						{
							throw new RuntimeException(Messages.AddTargetsFileAction_error1);
						}
					}
				});
	}

	private static IStatus processAddFile(IFile targetsFile, IProgressMonitor monitor)
	{
		if (targetsFile == null)
		{
			return new Status(IStatus.WARNING, Plugin.PLUGIN_ID, Messages.AddTargetsFileAction_error_bad_targets_file);
		}
		
		if (targetsFile.exists())
		{ 
			// already exist
			return new Status(IStatus.WARNING, Plugin.PLUGIN_ID, Messages.AddTargetsFileAction_error_targets_file_already_exists);
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
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_TEXT_ATTR, Messages.AddTargetsFileAction_def_target1);
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_COMMAND_ATTR, "build"); //$NON-NLS-1$
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_HINT_ATTR, Messages.AddTargetsFileAction_def_target1_comment);
				targetsSection.appendChild(target);

				target = doc.createElement(Plugin.MT_XML_TARGET_ELEMENT_NAME);
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_TEXT_ATTR, Messages.AddTargetsFileAction_def_target2);
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_COMMAND_ATTR, "clean"); //$NON-NLS-1$
				target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_HINT_ATTR, Messages.AddTargetsFileAction_def_target2_comment);
				targetsSection.appendChild(target);
			}

			// options section
			{
				final Element optionsSection = doc.createElement(Plugin.MT_XML_OPTIONS_SECTION_ELEMENT_NAME);
				rootElement.appendChild(optionsSection);

				Element option = doc.createElement(Plugin.MT_XML_OPTION_ELEMENT_NAME);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR, Messages.AddTargetsFileAction_def_option1);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR, "option"); //$NON-NLS-1$
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR, Messages.AddTargetsFileAction_def_option1_comment);
				optionsSection.appendChild(option);

				Element optionsGroup = doc.createElement(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_NAME);
				optionsGroup.setAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_TEXT_ATTR, Messages.AddTargetsFileAction_def_opt_group1);
				optionsGroup.setAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_HINT_ATTR, Messages.AddTargetsFileAction_def_opt_group1_comment);
				optionsSection.appendChild(optionsGroup);

				option = doc.createElement(Plugin.MT_XML_OPTION_ELEMENT_NAME);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR, Messages.AddTargetsFileAction_def_option2);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR, "Debug=true"); //$NON-NLS-1$
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR, Messages.AddTargetsFileAction_def_option2_comment);
				optionsGroup.appendChild(option);

				option = doc.createElement(Plugin.MT_XML_OPTION_ELEMENT_NAME);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR, Messages.AddTargetsFileAction_def_option3);
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR, "Release=true"); //$NON-NLS-1$
				option.setAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR, Messages.AddTargetsFileAction_def_option3_comment);
				optionsGroup.appendChild(option);
			}

			monitor.worked(1);
			return writeDocumentToFile(doc, targetsFile, monitor);
		}
		catch (ParserConfigurationException e)
		{
			return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, Messages.AddTargetsFileAction_error_failed_to_create_targets_file, e);
		}
		catch (DOMException e)
		{
			return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, Messages.AddTargetsFileAction_error_failed_to_create_targets_file, e);
		}
	}

	/**
	 * @param doc
	 * @param file
	 * @param monitor
	 */
	private static IStatus writeDocumentToFile(Document doc, IFile file, IProgressMonitor monitor)
	{
		final ByteArrayOutputStream oStream = new ByteArrayOutputStream();

		try
		{
			final Transformer serializer = TransformerFactory.newInstance().newTransformer();

			serializer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
			serializer.setOutputProperty(OutputKeys.STANDALONE, "yes"); //$NON-NLS-1$

			serializer.transform(new DOMSource(doc), new StreamResult(oStream));

		}
		catch (TransformerException e)
		{
			return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, Messages.AddTargetsFileAction_error_failed_to_save_targets_file, e);
		}

		try
		{
			file.create(new ByteArrayInputStream(oStream.toByteArray()), false, monitor);
		}
		catch (CoreException e)
		{
			return new Status(IStatus.ERROR, Plugin.PLUGIN_ID, Messages.AddTargetsFileAction_error_failed_to_save_targets_file, e);
		}
		
		monitor.worked(1);
		return Status.OK_STATUS;
	}
}
