package maketargeter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS
{
	private static final String BUNDLE_NAME = "maketargeter.messages"; //$NON-NLS-1$
	public static String MainView_error1_1;
	public static String MainView_error1_2;
	public static String MainView_error1_3;
	public static String MainView_no_project;
	public static String MainView_options_section;
	public static String MainView_targets_section;
	public static String MainView_updating_vew_status;
	public static String MainView_updating_view_task;
	public static String Plugin_error1;
	public static String Plugin_error2;
	public static String Plugin_error3;
	public static String Plugin_error4;
	public static String Plugin_error5;
	static
	{
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages()
	{
	}
}
