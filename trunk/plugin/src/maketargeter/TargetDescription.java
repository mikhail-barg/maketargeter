package maketargeter;

/**
 * Data stored for every target in the file
 * Also used to store the resulting target info
 */
public class TargetDescription
{
	private final String m_targetCommand;	//target
	private final String m_buildCommand;	//build command
	private final String m_buildLocation;	//build location
	
	/**
	 * @param targetCommand cannot be null
	 * @param buildCommand cannot be null, empty string means default build command
	 */
	TargetDescription(String targetCommand, String buildCommand, String buildLocation)
	{
		if (targetCommand == null)
		{
			throw new NullPointerException();
		}
		if (buildCommand == null)
		{
			throw new NullPointerException();
		}
		if (buildLocation == null)
		{
			throw new NullPointerException();
		}
		m_targetCommand = targetCommand;
		m_buildCommand = buildCommand;
		m_buildLocation = buildLocation;
	}
	
	public String getTragetCommand() { return m_targetCommand; }
	public String getBuildCommand() { return m_buildCommand; }
	public String getBuildLocation() { return m_buildLocation; }
	public boolean isDefaultBuildCommand() { return m_buildCommand.isEmpty(); }
	public boolean isDefaultBuildLocation() { return m_buildLocation.isEmpty(); }
}
