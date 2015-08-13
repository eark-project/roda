package pt.gov.dgarq.roda.sipcreator;

import java.lang.UnsatisfiedLinkError;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.log4j.Logger;

/**
 * @author Rui Castro
 * @author Luis Faria
 * 
 */
public class SIPCreator {
	private static final Logger logger = Logger.getLogger(SIPCreator.class);

	private static SIPCreator instance = null;

	/**
	 * Get SIP Creator singleton
	 * 
	 * @return the singleton instance
	 */
	public static SIPCreator getInstance() {
		if (instance == null) {
			instance = new SIPCreator();
		}
		return instance;
	}

	private boolean initialized;
	private MainFrame mainFrame;

	private SIPCreator() {
		initialized = false;
	}

	/**
	 * Initialize sip creator
	 */
	public void init() {
		if (!initialized) {
			initialized = true;
			/* MacOS X is having a problem with the UI
			try {
				UIManager
						.setLookAndFeel(new com.nilo.plaf.nimrod.NimRODLookAndFeel());
			} catch (UnsatisfiedLinkError e) {
				logger.warn("Could not enable NimROD Look & Feel - " //$NON-NLS-1$
						+ e.getMessage(), e);
				try {
					UIManager.setLookAndFeel(UIManager
							.getSystemLookAndFeelClassName());
				} catch (ClassNotFoundException e1) {
					logger.error("Could not set system Look & Feel - "
							+ e.getMessage(), e1);
				} catch (InstantiationException e1) {
					logger.error("Could not set system Look & Feel - "
							+ e.getMessage(), e1);
				} catch (IllegalAccessException e1) {
					logger.error("Could not set system Look & Feel - "
							+ e.getMessage(), e1);
				} catch (UnsupportedLookAndFeelException e1) {
					logger.error("Could not set system Look & Feel - "
							+ e.getMessage(), e1);
				}
			} catch (UnsupportedLookAndFeelException e) {
				logger.warn("Could not enable NimROD Look & Feel - " //$NON-NLS-1$
						+ e.getMessage(), e);
			} catch (UnsatisfiedLinkError e) {
				logger.warn("Could not enable NimROD Look & Feel - " //$NON-NLS-1$
						+ e.getMessage(), e);
			}
			*/
			getMainFrame().setVisible(true);
		}
	}

	/**
	 * Get main frame
	 * 
	 * @return the main frame
	 */
	public MainFrame getMainFrame() {
		if (mainFrame == null) {
			mainFrame = new MainFrame();
		}
		return mainFrame;
	}

	/**
	 * Set the status bar message
	 * 
	 * @param message
	 */
	public static void setStatusMessage(String message) {
		getInstance().getMainFrame().getMainPanel().setStatusMessage(message);
	}

	/**
	 * Set the status bar message
	 */
	public static void setDefaultStatusMessage() {
		getInstance().getMainFrame().getMainPanel().setDefaultStatusMessage();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SIPCreator.getInstance().init();
	}

}
