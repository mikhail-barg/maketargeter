/**
 * 
 */
package maketargeter;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @author Mikhail Barg
 * 
 */
public class MainView extends ViewPart
{
	private static final int SECTION_STYLE = ExpandableComposite.TWISTIE
												| ExpandableComposite.TITLE_BAR 
												| ExpandableComposite.TITLE_BAR
												/* | ExpandableComposite.CLIENT_INDENT | Section.DESCRIPTION */
												;
	private static final int GROUP_STYLE = SWT.SHADOW_NONE;
	
	private FormToolkit m_toolkit;
	private ScrolledForm m_form;

	private Group m_targetsGroup;
	private Group m_optionsGroup;

	private Action m_addTargetsFileAction;
	private Action m_addNewTargetAction;
	private Action m_buildTargetAction;

	private SelectionListener m_selectionListener;

	private List<Button> m_optionButtonsList;
	private List<Composite> m_optionGroupsList;

	@Override
	public void createPartControl(Composite parent)
	{
		initToolBar(parent.getShell());

		m_toolkit = new FormToolkit(parent.getDisplay());

		m_form = m_toolkit.createScrolledForm(parent);
		m_form.getBody().setLayout(new ColumnLayout());

		//targets section
		{
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
		}

		//Options section
		{
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
		}

		m_selectionListener = new ButtonSelectionListener();

		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(ProjectSelectionListener.INSTANCE);

		m_optionButtonsList = new LinkedList<Button>();
		m_optionGroupsList = new LinkedList<Composite>();

		Plugin.getInstance().registerView(this);
		
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
		Plugin.getInstance().removeView(this);
		
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(ProjectSelectionListener.INSTANCE);
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
	 * 
	 */
	void update()
	{
		m_addTargetsFileAction.setEnabled(false);
		m_addNewTargetAction.setEnabled(false);
		m_buildTargetAction.setEnabled(false);
		m_form.setVisible(false);
		
		final Plugin plugin = Plugin.getInstance(); 

		if (!plugin.isCurrentProjectOpened())
		{
			this.setContentDescription("No project is selected");
			return;
		}

		this.setContentDescription(plugin.getCurrentProject().getName());

		final boolean fileExists = plugin.getTragetsFile().exists();
		
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
		parseFile(Plugin.getInstance().getTragetsFile());
		
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
		if (file == null || !file.isAccessible())
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
		if (Util.getSelectedRadioButton(m_targetsGroup) == null)
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
		if (Util.getSelectedRadioButton(groupControl) == null)
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
				addButtonData(buffer, Util.getSelectedRadioButton(group));	
			}
		}

		// target
		{
			addButtonData(buffer, Util.getSelectedRadioButton(m_targetsGroup));
		}

		String targetString = buffer.toString().trim();
		m_form.setText(targetString);
		Plugin.getInstance().setTargetString(targetString);
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
