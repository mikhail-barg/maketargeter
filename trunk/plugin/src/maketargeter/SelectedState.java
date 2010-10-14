/**
 * 
 */
package maketargeter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Class containing the selections made for the project
 * Targets, Options, Option groups and selected elements of Option groups are referenced by the caption
 * 
 * @author Mike
 * 
 *
 */
class SelectedState implements ISelectedStateData
{
	/** stores caption of the selected target */
	private String m_selectedTarget = null;
	
	/** set of the selected Options' captions*/ 
	private final Set<String> m_selectedOptions = new HashSet<String>();
	
	/** maps Option group caption -> selected element caption */
	private final Map<String, String> m_optionGroupsElements = new HashMap<String, String>();
	
	
	void setSelectedTarget(String targetCaption)
	{
		m_selectedTarget = targetCaption;
	}
	
	/** can be null*/
	@Override
	public String getSelectedTarget()
	{
		return m_selectedTarget;
	}
	
	void addSelectedOption(String optionCaption)
	{
		m_selectedOptions.add(optionCaption);
	}
	
	@Override
	public boolean isOptionSelected(String optionCaption)
	{
		return m_selectedOptions.contains(optionCaption);
	}
	
	void setSelectedOptionGroupElement(String optionGroupCaption, String selectedElementCaption)
	{
		m_optionGroupsElements.put(optionGroupCaption, selectedElementCaption);
	}
	
	/** can be null*/
	@Override
	public String getSelectedOptionGroupElement(String optionGroupCaption)
	{
		return m_optionGroupsElements.get(optionGroupCaption);
	}
}
