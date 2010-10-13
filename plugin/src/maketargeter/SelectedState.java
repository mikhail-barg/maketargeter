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
class SelectedState
{
	/** stores caption of the selected target */
	private String m_selectedTarget = null;
	
	/** set of the selected Options' captions*/ 
	private final Set<String> m_selectedOptions = new HashSet<String>();
	
	/** maps Option group caption -> selected element caption */
	private final Map<String, String> m_optionGroupsElements = new HashMap<String, String>();
	
}
