package maketargeter;

/**
 * Data stored for every target
 */
public class TargetDescription
{
	private final String m_targetCommand;	//target
	private final String m_buildCommand;	//build command
	
	/**
	 * @param targetCommand cannot be null
	 * @param buildCommand cannot be null, empty string means default build command
	 */
	TargetDescription(String targetCommand, String buildCommand)
	{
		if (targetCommand == null)
		{
			throw new NullPointerException();
		}
		if (buildCommand == null)
		{
			throw new NullPointerException();
		}
		m_targetCommand = targetCommand;
		m_buildCommand = buildCommand;
	}
	
	public String getTragetCommand() { return m_targetCommand; }
	public String getBuildCommand() { return m_buildCommand; }
}
