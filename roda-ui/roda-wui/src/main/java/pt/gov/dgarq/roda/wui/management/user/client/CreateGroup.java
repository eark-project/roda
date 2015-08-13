/**
 * 
 */
package pt.gov.dgarq.roda.wui.management.user.client;

import pt.gov.dgarq.roda.core.common.GroupAlreadyExistsException;
import pt.gov.dgarq.roda.core.data.Group;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIButton;
import pt.gov.dgarq.roda.wui.common.client.widgets.WUIWindow;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SourcesTabEvents;
import com.google.gwt.user.client.ui.TabListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.UserManagementConstants;
import config.i18n.client.UserManagementMessages;

/**
 * @author Luis Faria
 * 
 */
public class CreateGroup extends WUIWindow {

	private static UserManagementConstants constants = (UserManagementConstants) GWT
			.create(UserManagementConstants.class);

	private static UserManagementMessages messages = (UserManagementMessages) GWT.create(UserManagementMessages.class);

	private final WUIButton create;

	private final WUIButton cancel;

	private final TextBox groupName;

	private final TextBox groupFullname;

	private final GroupSelect groupSelect;

	private final PermissionsPanel permissionsPanel;

	public CreateGroup() {
		super(constants.createGroupTitle(), 690, 346);

		create = new WUIButton(constants.createGroupCreate(), WUIButton.Left.ROUND, WUIButton.Right.ARROW_DOWN);

		cancel = new WUIButton(constants.createGroupCancel(), WUIButton.Left.ROUND, WUIButton.Right.CROSS);

		create.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				final String name = groupName.getText();
				String fullname = groupFullname.getText();

				String[] memberGroups = groupSelect.getMemberGroups();
				String[] directRoles = permissionsPanel.getDirectRoles();

				Group group = new Group(name);
				group.setFullName(fullname);
				group.setGroups(memberGroups);
				group.setDirectRoles(directRoles);

				UserManagementService.Util.getInstance().createGroup(group, new AsyncCallback<Void>() {

					public void onFailure(Throwable caught) {
						if (caught instanceof GroupAlreadyExistsException) {
							Window.alert(messages.createGroupAlreadyExists(name));

						} else {
							Window.alert(messages.createGroupFailure(caught.getMessage()));
						}
					}

					public void onSuccess(Void result) {
						CreateGroup.this.hide();
						CreateGroup.this.onSuccess();
					}

				});
			}

		});

		cancel.addClickListener(new ClickListener() {

			public void onClick(Widget sender) {
				CreateGroup.this.cancel();
			}

		});

		this.addToBottom(create);
		this.addToBottom(cancel);

		VerticalPanel groupDataPanel = new VerticalPanel();

		VerticalPanel basicInfoPanel = new VerticalPanel();
		groupName = new TextBox();
		groupFullname = new TextBox();

		VerticalPanel namePanel = concatInPanel(constants.groupName(), groupName);
		VerticalPanel fullnamePanel = concatInPanel(constants.groupFullname(), groupFullname);

		basicInfoPanel.add(namePanel);
		basicInfoPanel.add(fullnamePanel);

		groupSelect = new GroupSelect(true);
		groupDataPanel.add(basicInfoPanel);
		groupDataPanel.add(groupSelect);

		permissionsPanel = new PermissionsPanel();

		this.addTab(groupDataPanel, constants.dataTabTitle());
		this.addTab(permissionsPanel, constants.permissionsTabTitle());

		this.getTabPanel().addTabListener(new TabListener() {

			public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
				if (tabIndex == 1) {
					permissionsPanel.updateLockedPermissions(groupSelect.getMemberGroups());
				}
				return true;
			}

			public void onTabSelected(SourcesTabEvents sender, int tabIndex) {

			}

		});

		this.selectTab(0);

		this.getTabPanel().addStyleName("office-create-group-tabpanel");
		basicInfoPanel.addStyleName("basicInfoPanel");
		namePanel.addStyleName("namePanel");
		fullnamePanel.addStyleName("fullnamePanel");

	}

	protected void cancel() {
		this.hide();
		super.onCancel();
	}

	private VerticalPanel concatInPanel(String title, Widget input) {
		VerticalPanel vp = new VerticalPanel();
		Label label = new Label(title);
		vp.add(label);
		vp.add(input);

		vp.addStyleName("office-input-panel");
		label.addStyleName("office-input-title");
		input.addStyleName("office-input-widget");

		return vp;
	}

}
