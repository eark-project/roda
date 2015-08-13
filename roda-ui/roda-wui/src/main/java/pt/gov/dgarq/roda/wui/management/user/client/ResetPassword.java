/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.common.InvalidTokenException;
import pt.gov.dgarq.roda.core.common.NoSuchUserException;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.BadHistoryTokenException;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.HistoryResolver;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.home.client.Home;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;

/**
 * @author Luis Faria
 * 
 */
public class ResetPassword implements HistoryResolver {

	private static ResetPassword instance = null;

	/**
	 * Get the singleton instance
	 * 
	 * @return the instance
	 */
	public static ResetPassword getInstance() {
		if (instance == null) {
			instance = new ResetPassword();
		}
		return instance;
	}

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private boolean initialized;

	private Grid layout;

	private Label usernameLabel;

	private TextBox usernameBox;

	private Label tokenLabel;

	private TextBox tokenBox;

	private Label newPasswordLabel;

	private PasswordTextBox newPasswordBox;

	private Label newPasswordRepeatLabel;

	private PasswordTextBox newPasswordRepeatBox;

	private WUIButton resetPasswordButton;

	private ResetPassword() {
		initialized = false;
	}

	private void init() {
		if (!initialized) {
			initialized = true;

			layout = new Grid(5, 2);
			usernameLabel = new Label(constants.resetPassordUsername());
			usernameBox = new TextBox();
			tokenLabel = new Label(constants.resetPasswordToken());
			tokenBox = new TextBox();
			newPasswordLabel = new Label(constants.resetPasswordNewPassword());
			newPasswordBox = new PasswordTextBox();
			newPasswordRepeatLabel = new Label(constants.resetPasswordRepeatPassword());
			newPasswordRepeatBox = new PasswordTextBox();
			resetPasswordButton = new WUIButton(constants.resetPasswordSubmit(), WUIButton.Left.ROUND,
					WUIButton.Right.ARROW_FORWARD);

			resetPasswordButton.addClickListener(new ClickListener() {

				public void onClick(Widget sender) {
					String username = usernameBox.getText();
					String resetPasswordToken = tokenBox.getText();
					String newPassword = newPasswordBox.getText();
					UserManagementService.Util.getInstance().resetPassword(username, resetPasswordToken, newPassword,
							new AsyncCallback<Void>() {

						public void onFailure(Throwable caught) {
							if (caught instanceof InvalidTokenException) {
								Window.alert(constants.resetPasswordInvalidToken());
							} else if (caught instanceof NoSuchUserException) {
								Window.alert(constants.resetPasswordNoSuchUser());
							} else {
								logger.error("Error reseting password", caught);
							}

						}

						public void onSuccess(Void result) {
							Window.alert(constants.resetPasswordSuccess());
							History.newItem(Home.RESOLVER.getHistoryPath());
						}

					});

				}

			});

			resetPasswordButton.setEnabled(false);

			KeyboardListener listener = new KeyboardListener() {

				public void onKeyDown(Widget sender, char keyCode, int modifiers) {
				}

				public void onKeyPress(Widget sender, char keyCode, int modifiers) {
				}

				public void onKeyUp(Widget sender, char keyCode, int modifiers) {
					resetPasswordButton.setEnabled(isValid());
				}

			};

			usernameBox.addKeyboardListener(listener);
			tokenBox.addKeyboardListener(listener);
			newPasswordBox.addKeyboardListener(listener);
			newPasswordRepeatBox.addKeyboardListener(listener);

			layout.setWidget(0, 0, usernameLabel);
			layout.setWidget(0, 1, usernameBox);
			layout.setWidget(1, 0, tokenLabel);
			layout.setWidget(1, 1, tokenBox);
			layout.setWidget(2, 0, newPasswordLabel);
			layout.setWidget(2, 1, newPasswordBox);
			layout.setWidget(3, 0, newPasswordRepeatLabel);
			layout.setWidget(3, 1, newPasswordRepeatBox);
			layout.setWidget(4, 0, resetPasswordButton);

			layout.addStyleName("wui-resetPassword");
			usernameLabel.addStyleName("resetPassword-label");
			usernameBox.addStyleName("resetPassword-box");
			tokenLabel.addStyleName("resetPassword-label");
			tokenBox.addStyleName("resetPassword-box");
			newPasswordLabel.addStyleName("resetPassword-label");
			newPasswordBox.addStyleName("resetPassword-box");
			newPasswordRepeatLabel.addStyleName("resetPassword-label");
			newPasswordRepeatBox.addStyleName("resetPassword-box");
			resetPasswordButton.addStyleName("resetPassword-submit");
		}
	}

	/**
	 * Check if form is valid for submission
	 * 
	 * @return
	 */
	public boolean isValid() {
		boolean valid = true;
		if (usernameBox.getText().length() == 0) {
			valid = false;
			usernameBox.addStyleName("invalid");
		} else {
			usernameBox.removeStyleName("invalid");
		}

		if (tokenBox.getText().length() == 0) {
			valid = false;
			tokenBox.addStyleName("invalid");
		} else {
			tokenBox.removeStyleName("invalid");
		}

		if (newPasswordBox.getText().length() == 0) {
			valid = false;
			newPasswordBox.addStyleName("invalid");
		} else {
			newPasswordBox.removeStyleName("invalid");
		}

		if (newPasswordRepeatBox.getText().length() == 0) {
			valid = false;
			newPasswordRepeatBox.addStyleName("invalid");
		} else {
			newPasswordRepeatBox.removeStyleName("invalid");
		}

		if (!newPasswordBox.getText().equals(newPasswordRepeatBox.getText())) {
			valid = false;
			newPasswordRepeatBox.addStyleName("invalid");
		} else {
			newPasswordRepeatBox.removeStyleName("invalid");
		}

		return valid;
	}

	public String getHistoryPath() {
		return getHistoryToken();
	}

	public String getHistoryToken() {
		return "resetpassword";
	}

	public void isCurrentUserPermitted(final AsyncCallback<Boolean> callback) {
		UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {

			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}

			public void onSuccess(AuthenticatedUser user) {
				callback.onSuccess(new Boolean(user.isGuest()));
			}

		});
	}

	public void resolve(String[] historyTokens, AsyncCallback<Widget> callback) {
		if (historyTokens.length == 0) {
			init();
			callback.onSuccess(layout);
		} else if (historyTokens.length == 2) {
			init();
			usernameBox.setText(historyTokens[0]);
			tokenBox.setText(historyTokens[1]);
			usernameBox.setReadOnly(true);
			tokenBox.setReadOnly(true);
			callback.onSuccess(layout);
		} else {
			callback.onFailure(new BadHistoryTokenException("Wrong reset password history"));
		}

	}

}
