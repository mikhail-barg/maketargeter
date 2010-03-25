/**
 * 
 */
package maketargeter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.ui.TargetBuild;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Mikhail Barg
 * 
 */
public class MainView extends ViewPart implements ISelectionListener
{
	private static final int SECTION_STYLE = ExpandableComposite.TWISTIE
												| ExpandableComposite.TITLE_BAR 
												| ExpandableComposite.TITLE_BAR
												/* | ExpandableComposite.CLIENT_INDENT | Section.DESCRIPTION */
												;
	private static final int GROUP_STYLE = SWT.SHADOW_NONE;
	
	private FormToolkit m_toolkit;
	private ScrolledForm m_form;
	private IProject m_project;

	private Group m_targetsGroup;
	private Group m_optionsGroup;

	private Action m_addTargetsFileAction;
	private Action m_addNewTargetAction;
	private Action m_buildTargetAction;

	private SelectionListener m_selectionListener;

	private String m_targetString = "";

	private List<Button> m_optionButtonsList;
	private List<Composite> m_optionGroupsList;

	@Override
	public void createPartControl(Composite parent)
	{
		m_project = null;

		initToolBar(parent.getShell());

		m_toolkit = new FormToolkit(parent.getDisplay());

		m_form = m_toolkit.createScrolledForm(parent);
		m_form.getBody().setLayout(new ColumnLayout());

		//targets section
		final Section targetsSection = m_toolkit.createSection(m_form.getBody(), SECTION_STYLE);
		targetsSection.setText("Targets");
		targetsSection.addExpansionListener(new ExpansionAdapter()
			{
				@Override
				public void expansionStateChanged(ExpansionEvent e)
				{
					m_form.reflow(true);
				}
			});
		targetsSection.setExpanded(true);

		m_targetsGroup = new Group(targetsSection, GROUP_STYLE);
		m_targetsGroup.setLayout(new RowLayout(SWT.VERTICAL));
		m_toolkit.adapt(m_targetsGroup);
		targetsSection.setClient(m_targetsGroup);

		//Options section
		final Section optionsSection = m_toolkit.createSection(m_form.getBody(), SECTION_STYLE);
		optionsSection.setText("Options");
		optionsSection.addExpansionListener(new ExpansionAdapter()
			{
				@Override
				public void expansionStateChanged(ExpansionEvent e)
				{
					m_form.reflow(true);
				}
			});
		optionsSection.setExpanded(true);

		m_optionsGroup = new Group(optionsSection, GROUP_STYLE);
		m_optionsGroup.setLayout(new RowLayout(SWT.VERTICAL));
		m_toolkit.adapt(m_optionsGroup);
		optionsSection.setClient(m_optionsGroup);

		m_selectionListener = new ButtonSelectionListener();

		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(this);

		m_optionButtonsList = new LinkedList<Button>();
		m_optionGroupsList = new LinkedList<Composite>();

		update();
	}

	@Override
	public void setFocus()
	{
		m_form.setFocus();
	}

	@Override
	public void dispose()
	{
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(this);
		m_toolkit.dispose();
		super.dispose();
	}

	/**
	 * @param shell
	 */
	private void initToolBar(Shell shell)
	{
		m_addTargetsFileAction = new AddTargetsFileAction();
		m_addNewTargetAction = new AddNewTargetAction();
		m_buildTargetAction = new BuildTargetAction(shell);

		IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
		tm.add(m_addTargetsFileAction);
		tm.add(m_addNewTargetAction);
		tm.add(m_buildTargetAction);
	}

	/**
	 * @param project
	 * @return
	 */
	private boolean checkProjectOpen(IProject project)
	{
		return (project != null && project.isAccessible());
	}

	/**
	 * @param selection
	 * @return
	 */
	private IProject findFirstOpenProjectBySelectedResource(IStructuredSelection selection)
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

	/**
	 * @param project
	 */
	private void setProject(IProject project)
	{
		if (m_project == null && project == null)
		{
			return;
		}

		if (m_project != null && m_project.equals(project))
		{
			return;
		}

		m_project = project;

		update();
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

		IProject project = findFirstOpenProjectBySelectedResource((IStructuredSelection) selection);

		if (project == null)
		{
			// TODO: try some other ways to get the project, for example from
			// the IWorkbenchPart
			return;
		}

		try
		{
			if (project.hasNature("org.eclipse.cdt.core.cnature"))
			{
				setProject(project);
			}
			else
			{
				setProject(null);
			}
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * @return file handle of a resource file. Cannot be null.
	 */
	private IFile getTragetsFile()
	{
		if (!checkProjectOpen(m_project))
		{
			// bad project.. probably already closed or something
			setProject(null);
			return null;
		}

		return m_project.getFile(Plugin.MT_TARGETS_FILE_NAME);
	}

	/**
	 * 
	 */
	private void update()
	{
		m_addTargetsFileAction.setEnabled(false);
		m_addNewTargetAction.setEnabled(false);
		m_buildTargetAction.setEnabled(false);
		m_form.setVisible(false);

		if (!checkProjectOpen(m_project))
		{
			this.setContentDescription("No project is selected");
			return;
		}

		this.setContentDescription(m_project.getName());

		final boolean fileExists = getTragetsFile().exists();
		
		m_addTargetsFileAction.setEnabled(!fileExists);
		
		if (!fileExists)
		{
			return;
		}

		/*
		IWorkspaceRunnable runnable = new IWorkspaceRunnable()
			{
				public void run(IProgressMonitor monitor) throws CoreException
				{
					processParse();
				}
			};

		try
		{
			ResourcesPlugin.getWorkspace().run(runnable, m_project, IWorkspace.AVOID_UPDATE, null);
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		*/
		processParse();
	}

	/**
	 * 
	 */
	private void processParse()
	{
		parseFile(getTragetsFile());
		
		updateTargetString();
		
		m_form.setVisible(true);
		m_addNewTargetAction.setEnabled(true);
		m_buildTargetAction.setEnabled(true);
	}
	
	/**
	 * @param file
	 */
	private void parseFile(IFile file)
	{
		if (!file.isAccessible())
		{
			return;
		}

		cleanupView();

		try
		{
			InputStream inputStream = file.getContents();

			final Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(inputStream);

			Element rootElement = doc.getDocumentElement();

			if (!rootElement.getNodeName().equals(Plugin.MT_XML_ROOT_ELEMENT_NAME))
			{
				inputStream.close();
				return;
			}

			// targets sections
			{
				NodeList targetSections = rootElement.getElementsByTagName(Plugin.MT_XML_TARGETS_SECTION_ELEMENT_NAME);
				for (int i = 0; i < targetSections.getLength(); ++i)
				{
					processTargetsSection((Element) targetSections.item(i));
				}
			}

			// options sections
			{
				NodeList optionsSections = rootElement.getElementsByTagName(Plugin.MT_XML_OPTIONS_SECTION_ELEMENT_NAME);
				for (int i = 0; i < optionsSections.getLength(); ++i)
				{
					processOptionsSection((Element) optionsSections.item(i));
				}
			}

			inputStream.close();
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}
		catch (SAXException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}

		m_form.reflow(true);
	}

	/**
	 * 
	 */
	private void cleanupView()
	{
		for (Widget widget : m_targetsGroup.getChildren())
		{
			widget.dispose();
		}
		for (Widget widget : m_optionsGroup.getChildren())
		{
			widget.dispose();
		}
		
		m_form.reflow(true);

		m_optionButtonsList.clear();
		m_optionGroupsList.clear();
	}
	
	/**
	 * @param section
	 */
	private void processTargetsSection(Element section)
	{
		if (section == null)
		{
			return;
		}
		
		final NodeList targets = section.getElementsByTagName(Plugin.MT_XML_TARGET_ELEMENT_NAME);
		for (int i = 0; i < targets.getLength(); ++i)
		{
			final Element target = (Element) targets.item(i);
			addTarget(target.getAttribute(Plugin.MT_XML_TARGET_ELEMENT_TEXT_ATTR),
						target.getAttribute(Plugin.MT_XML_TARGET_ELEMENT_COMMAND_ATTR),
						target.getAttribute(Plugin.MT_XML_TARGET_ELEMENT_HINT_ATTR)
					);
		}
	}

	/**
	 * @param section
	 */
	private void processOptionsSection(Element section)
	{
		if (section == null)
		{
			return;
		}
		final NodeList sectionChildren = section.getChildNodes();
		for (int i = 0; i < sectionChildren.getLength(); ++i)
		{
			Node node = sectionChildren.item(i);
			if (node.getNodeName().equals(Plugin.MT_XML_OPTION_ELEMENT_NAME))
			{
				final Element element = (Element) node;
				addOption(element.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR),
							element.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR),
							element.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR)
				);
			}
			else if (node.getNodeName().equals(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_NAME))
			{
				addOptionsGroup((Element) node);
			}
		}
	}

	/**
	 * @param targetName
	 * @param targetCommand
	 * @param hint
	 */
	private void addTarget(String targetName, String targetCommand, String hint)
	{
		final Button button = m_toolkit.createButton(m_targetsGroup, targetName, SWT.RADIO);
		button.setData(targetCommand);
		button.setToolTipText(hint);
		button.addSelectionListener(m_selectionListener);
		if (getSelectedRadioButton(m_targetsGroup) == null)
		{
			button.setSelection(true);
		}
	}

	/**
	 * @param optionName
	 * @param optionCommand
	 * @param hint
	 */
	private void addOption(String optionName, String optionCommand, String hint)
	{
		final Button button = m_toolkit.createButton(m_optionsGroup, optionName, SWT.CHECK);
		button.setData(optionCommand);
		button.setToolTipText(hint);
		button.addSelectionListener(m_selectionListener);
		m_optionButtonsList.add(button);
	}

	/**
	 * @param groupControl
	 * @param optionName
	 * @param optionCommand
	 * @param hint
	 */
	private void addGroupedOption(Composite groupControl, String optionName, String optionCommand, String hint)
	{
		final Button button = m_toolkit.createButton(groupControl, optionName, SWT.RADIO);
		button.setData(optionCommand);
		button.setToolTipText(hint);
		button.addSelectionListener(m_selectionListener);
		if (getSelectedRadioButton(groupControl) == null)
		{
			button.setSelection(true);
		}
	}

	/**
	 * @param groupElement
	 */
	private void addOptionsGroup(Element groupElement)
	{
		final Composite group = addOptionsGroup(groupElement.getAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_TEXT_ATTR));
		group.setToolTipText(groupElement.getAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_HINT_ATTR));

		final NodeList options = groupElement.getElementsByTagName(Plugin.MT_XML_OPTION_ELEMENT_NAME);
		for (int i = 0; i < options.getLength(); ++i)
		{
			final Element option = (Element) options.item(i);
			addGroupedOption(group,
								option.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR),
								option.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR),
								option.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR)
							);
		}
		m_optionGroupsList.add(group);
	}

	/**
	 * @param groupName
	 * @return
	 */
	private Composite addOptionsGroup(String groupName)
	{
		final Group group = new Group(m_optionsGroup, GROUP_STYLE);
		group.setLayout(new RowLayout(SWT.VERTICAL));
		m_toolkit.adapt(group);
		group.setText(groupName);
		return group;
	}

	/**
	 * 
	 */
	private void updateTargetString()
	{
		final StringBuilder buffer = new StringBuilder();

		// options
		{
			for (Button button : m_optionButtonsList)
			{
				addButtonData(buffer, button);
			}
		}

		// option groups
		{
			for (Composite group : m_optionGroupsList)
			{
				addButtonData(buffer, getSelectedRadioButton(group));	
			}
		}

		// target
		{
			addButtonData(buffer, getSelectedRadioButton(m_targetsGroup));
		}

		m_targetString = buffer.toString().trim();
		m_form.setText(m_targetString);
	}

	/**
	 * @param buffer
	 * @param button
	 */
	private void addButtonData(StringBuilder buffer, Button button)
	{
		if (button == null)
		{
			return;
		}
		if (!button.getSelection())
		{
			return;
		}
		if (button.getData() == null)
		{
			return;
		}
		
		buffer.append(button.getData().toString()).append(Plugin.MT_TARGET_LINE_SEPARATOR);
	}

	/**
	 * @param group
	 * @return
	 */
	private Button getSelectedRadioButton(Composite group)
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

	//////////////////////////////////////////////////////////
	class AddTargetsFileAction extends Action
	{
		public AddTargetsFileAction()
		{
			super("Add targets file to project");
			setImageDescriptor(Plugin.getImage("/icons/enabl/file_add.gif"));
			setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/file_add.gif"));
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
						m_project, IWorkspace.AVOID_UPDATE, null);
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}

		private void processAddFile(IProgressMonitor monitor)
		{
			final IFile file = getTragetsFile();
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
					target.setAttribute(Plugin.MT_XML_TARGET_ELEMENT_HINT_ATTR, "Clean breviously built project");
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
			
			update();
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

	//////////////////////////////////////////////////////////
	class AddNewTargetAction extends Action
	{
		private final IMakeTargetManager m_targetManager;

		public AddNewTargetAction()
		{
			super("Create new target");
			setImageDescriptor(Plugin.getImage("/icons/enabl/target_add.gif"));
			setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/target_add.gif"));
			m_targetManager = MakeCorePlugin.getDefault().getTargetManager();
		}

		@Override
		public void run()
		{
			try
			{
				IMakeTarget target = m_targetManager.createTarget(m_project, getTargetName("New target"), getTargetBuildId());
				target.setStopOnError(true);
				target.setRunAllBuilders(true);
				target.setUseDefaultBuildCmd(true);
				target.setBuildAttribute(IMakeTarget.BUILD_TARGET, m_targetString);
				m_targetManager.addTarget(m_project, target);
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}

		private String getTargetName(String targetName)
		{
			String newName = targetName;
			int i = 0;
			try
			{
				while (m_targetManager.findTarget(m_project, newName) != null)
				{
					i++;
					newName = targetName + " (" + Integer.toString(i) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			catch (CoreException e)
			{
			}
			return newName;
		}

		private String getTargetBuildId()
		{
			String[] id = m_targetManager.getTargetBuilders(m_project);
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

	// ////////////////////////////////////////////////////////
	class BuildTargetAction extends Action
	{

		private final IMakeTargetManager m_targetManager;
		private final Shell m_shell;

		public BuildTargetAction(Shell shell)
		{
			super("Build target");
			setImageDescriptor(Plugin.getImage("/icons/enabl/target_build.png"));
			setDisabledImageDescriptor(Plugin.getImage("/icons/disabl/target_build.png"));
			m_shell = shell;
			m_targetManager = MakeCorePlugin.getDefault().getTargetManager();
		}

		@Override
		public void run()
		{
			try
			{
				IMakeTarget target = m_targetManager.createTarget(m_project, "Custom target", getTargetBuildId());
				target.setStopOnError(true);
				target.setRunAllBuilders(true);
				target.setUseDefaultBuildCmd(true);
				target.setBuildAttribute(IMakeTarget.BUILD_TARGET, m_targetString);
				TargetBuild.buildTargets(m_shell, new IMakeTarget[] { target });
			}
			catch (CoreException e)
			{
				e.printStackTrace();
			}
		}

		private String getTargetBuildId()
		{
			String[] id = m_targetManager.getTargetBuilders(m_project);
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

	//////////////////////////////////////////////////////////
	class ButtonSelectionListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			updateTargetString();
		}
	}
}
