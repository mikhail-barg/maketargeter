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

import maketargeter.actions.AddNewTargetAction;
import maketargeter.actions.AddTargetsFileAction;
import maketargeter.actions.BuildTargetAction;
import maketargeter.actions.CopyTargetToClipboardAction;
import maketargeter.actions.SetTargetToProjectSettings;

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
import org.eclipse.ui.forms.events.IExpansionListener;
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

	private AddTargetsFileAction m_addTargetsFileAction;
	private Action m_addNewTargetAction;
	private Action m_buildTargetAction;
	private Action m_copyToClipboardAction;
	private Action m_setToProjectAction;

	private final SelectionListener m_selectionListener = new ButtonSelectionListener();
	private final IExpansionListener m_expansionListener = new ExpansionListener();

	private final List<Button> m_optionButtonsList = new LinkedList<Button>();
	private final List<Group> m_optionGroupsList = new LinkedList<Group>(); 
	
	private boolean m_disableSelectionUpdates = false;
	
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
			targetsSection.addExpansionListener(m_expansionListener);
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
			optionsSection.addExpansionListener(m_expansionListener);
			optionsSection.setExpanded(true);
	
			m_optionsGroup = new Group(optionsSection, GROUP_STYLE);
			m_optionsGroup.setLayout(new RowLayout(SWT.VERTICAL));
			m_toolkit.adapt(m_optionsGroup);
			optionsSection.setClient(m_optionsGroup);
		}

		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(ProjectSelectionListener.INSTANCE);

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
		m_copyToClipboardAction = new CopyTargetToClipboardAction();
		m_setToProjectAction = new SetTargetToProjectSettings();

		IToolBarManager tm = getViewSite().getActionBars().getToolBarManager();
		tm.add(m_addTargetsFileAction);
		tm.add(m_setToProjectAction);
		tm.add(m_addNewTargetAction);
		tm.add(m_buildTargetAction);
		tm.add(m_copyToClipboardAction);
	}

	/**
	 * 
	 */
	void update()
	{
		m_addTargetsFileAction.update();
		m_addNewTargetAction.setEnabled(false);
		m_buildTargetAction.setEnabled(false);
		m_copyToClipboardAction.setEnabled(false);
		m_setToProjectAction.setEnabled(false);
		m_form.setVisible(false);
		
		final Plugin plugin = Plugin.getInstance(); 

		if (!plugin.isCurrentProjectOpened())
		{
			this.setContentDescription("No project is selected");
			return;
		}

		this.setContentDescription(plugin.getCurrentProject().getName());

		
		
		if (!plugin.targetFileExists())
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
		try
		{
			m_disableSelectionUpdates = true;
			parseFile(Plugin.getInstance().getTragetsFile());
		}
		finally
		{
			m_disableSelectionUpdates = false;
		}
		
		onSelectionChanged();
		
		m_form.setVisible(true);
		m_addNewTargetAction.setEnabled(true);
		m_buildTargetAction.setEnabled(true);
		m_copyToClipboardAction.setEnabled(true);
		m_setToProjectAction.setEnabled(SetTargetToProjectSettings.canBeEnabled());
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
				throw new IllegalArgumentException("Bad root element for targets file :'" + rootElement.getNodeName() + "', should be '" + Plugin.MT_XML_ROOT_ELEMENT_NAME + "'");
			}

			ISelectedStateData selectedState = Plugin.getInstance().getSelectedStateForCurrentProject();
			
			// targets sections
			{
				NodeList targetSections = rootElement.getElementsByTagName(Plugin.MT_XML_TARGETS_SECTION_ELEMENT_NAME);
				for (int i = 0; i < targetSections.getLength(); ++i)
				{
					processTargetsSection((Element) targetSections.item(i), selectedState);
				}
			}

			// options sections
			{
				NodeList optionsSections = rootElement.getElementsByTagName(Plugin.MT_XML_OPTIONS_SECTION_ELEMENT_NAME);
				for (int i = 0; i < optionsSections.getLength(); ++i)
				{
					processOptionsSection((Element) optionsSections.item(i), selectedState);
				}
			}

			inputStream.close();
		}
		catch (CoreException e)
		{
			throw new RuntimeException(e);
		}
		catch (SAXException e)
		{
			throw new RuntimeException(e);
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		catch (ParserConfigurationException e)
		{
			throw new RuntimeException(e);
		}

		m_form.reflow(true);
	}

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
	
	private void processTargetsSection(Element section, ISelectedStateData selectedState)
	{
		if (section == null)
		{
			return;
		}
		
		final NodeList targets = section.getElementsByTagName(Plugin.MT_XML_TARGET_ELEMENT_NAME);
		for (int i = 0; i < targets.getLength(); ++i)
		{
			final Element target = (Element) targets.item(i);
			addButton(m_targetsGroup, true, 
						target.getAttribute(Plugin.MT_XML_TARGET_ELEMENT_TEXT_ATTR),
						target.getAttribute(Plugin.MT_XML_TARGET_ELEMENT_COMMAND_ATTR),
						target.getAttribute(Plugin.MT_XML_TARGET_ELEMENT_HINT_ATTR)
						);
		}
		Util.setSelectedRadioButton(m_targetsGroup, selectedState.getSelectedTarget());
	}

	private void processOptionsSection(Element section, ISelectedStateData selectedState)
	{
		if (section == null)
		{
			return;
		}
		
		final NodeList sectionChildren = section.getChildNodes();
		
		for (int i = 0; i < sectionChildren.getLength(); ++i)
		{
			final Node node = sectionChildren.item(i);
			if (node.getNodeName().equals(Plugin.MT_XML_OPTION_ELEMENT_NAME))
			{
				final Element element = (Element) node;
				final Button button = addButton(m_optionsGroup, false,
							element.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR),
							element.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR),
							element.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR)
							);
				if (selectedState.isOptionSelected(button.getText()))
				{
					button.setSelection(true);
				}
				m_optionButtonsList.add(button);
			}
			else if (node.getNodeName().equals(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_NAME))
			{
				addOptionsGroup((Element) node, selectedState);
			}
		}
	}

	private void addOptionsGroup(Element groupElement, ISelectedStateData selectedState)
	{
		final Group group = addOptionsGroup(
									groupElement.getAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_TEXT_ATTR),
									groupElement.getAttribute(Plugin.MT_XML_OPTIONS_GROUP_ELEMENT_HINT_ATTR)
									);

		final NodeList options = groupElement.getElementsByTagName(Plugin.MT_XML_OPTION_ELEMENT_NAME);
		for (int i = 0; i < options.getLength(); ++i)
		{
			final Element option = (Element) options.item(i);
			addButton(group, true,
						option.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_TEXT_ATTR),
						option.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_COMMAND_ATTR),
						option.getAttribute(Plugin.MT_XML_OPTION_ELEMENT_HINT_ATTR)
						);
		}
		Util.setSelectedRadioButton(group, selectedState.getSelectedOptionGroupElement(group.getText()));
		m_optionGroupsList.add(group);
	}
	
	private Button addButton(Composite groupControl, boolean isRadio, String caption, String command, String hint)
	{
		final Button button = m_toolkit.createButton(groupControl, caption, isRadio? SWT.RADIO : SWT.CHECK);
		button.setData(command);
		button.setToolTipText(hint);
		button.addSelectionListener(m_selectionListener);
		return button;
	}
	
	private Group addOptionsGroup(String groupName, String hint)
	{
		final Group group = new Group(m_optionsGroup, GROUP_STYLE);
		group.setLayout(new RowLayout(SWT.VERTICAL));
		m_toolkit.adapt(group);
		group.setText(groupName);
		group.setToolTipText(hint);
		
		return group;
	}

	private void onSelectionChanged()
	{
		if (m_disableSelectionUpdates)
		{
			return;
		}
		
		final SelectedState selectedState = new SelectedState();
		final StringBuilder targetStringBuilder = new StringBuilder();
		final StringBuilder captionStringBuilder = new StringBuilder();
		String captionString = ""; 

		// options
		{
			for (Button button : m_optionButtonsList)
			{
				if (isButtonSelected(button))
				{
					targetStringBuilder.append(button.getData().toString()).append(Plugin.MT_TARGET_LINE_SEPARATOR);
					captionStringBuilder.append(button.getText()).append(Plugin.MT_CAPTION_LINE_SEPARATOR);
					selectedState.addSelectedOption(button.getText());
				}
			}
		}

		// option groups
		{
			for (Group group : m_optionGroupsList)
			{
				final Button selectedButton = Util.getSelectedRadioButton(group);
				if (isButtonSelected(selectedButton))
				{
					targetStringBuilder.append(selectedButton.getData().toString()).append(Plugin.MT_TARGET_LINE_SEPARATOR);
					captionStringBuilder.append(selectedButton.getText()).append(Plugin.MT_CAPTION_LINE_SEPARATOR);
					selectedState.setSelectedOptionGroupElement(group.getText(), selectedButton.getText());
				}
			}
		}

		// target
		{
			final Button selectedButton = Util.getSelectedRadioButton(m_targetsGroup);
			if (isButtonSelected(selectedButton))
			{
				targetStringBuilder.append(selectedButton.getData().toString());
				captionString = selectedButton.getText() + Plugin.MT_CAPTION_LINE_SEPARATOR;
				selectedState.setSelectedTarget(selectedButton.getText());
			}
		}

		String targetString = targetStringBuilder.toString().trim();
		captionString = captionString + captionStringBuilder.toString().trim();
		
		m_form.setText(targetString);
		Plugin.getInstance().setTargetString(targetString);
		Plugin.getInstance().setCaptionString(captionString);
		Plugin.getInstance().setSelectedStateForCurrentProject(selectedState);
	}

	private boolean isButtonSelected(Button button)
	{
		return (button != null && button.getSelection() && button.getData() != null);
	}
	
	//////////////////////////////////////////////////////////
	class ButtonSelectionListener extends SelectionAdapter
	{
		@Override
		public void widgetSelected(SelectionEvent e)
		{
			onSelectionChanged();
		}
	}
	
	class ExpansionListener extends ExpansionAdapter
	{
		@Override
		public void expansionStateChanged(ExpansionEvent e)
		{
			m_form.reflow(true);
		}
	}
}
