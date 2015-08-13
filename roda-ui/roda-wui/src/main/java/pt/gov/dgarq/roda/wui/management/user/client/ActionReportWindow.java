/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.data.User;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class ActionReportWindow extends WUIWindow {

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

	private final WUIButton close;

	private final UserLog actionReportPanel;

	/**
	 * Create new user action report window
	 * 
	 * @param user
	 */
	public ActionReportWindow(User user) {
		super(messages.actionResportTitle(user.getName()), 850, 500);

		// FIXME set user
		actionReportPanel = new UserLog();

		this.addTab(actionReportPanel, constants.actionReportLogTabTitle());
		this.selectTab(0);

		close = new WUIButton(constants.actionReportClose(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		close.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				hide();
			}

		});

		this.addToBottom(close);

	}
}
