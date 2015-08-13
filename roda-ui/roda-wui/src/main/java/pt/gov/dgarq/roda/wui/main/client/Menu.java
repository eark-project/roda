/**
 * 
 */
package pt.gov.dgarq.roda.wui.main.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.MainConstants;
import pt.gov.dgarq.roda.wui.about.client.About;
import pt.gov.dgarq.roda.wui.common.client.AuthenticatedUser;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.LoginStatusListener;
import pt.gov.dgarq.roda.wui.common.client.UserLogin;
import pt.gov.dgarq.roda.wui.dissemination.browse.client.Browse;
import pt.gov.dgarq.roda.wui.dissemination.search.basic.client.BasicSearch;
import pt.gov.dgarq.roda.wui.home.client.Home;
import pt.gov.dgarq.roda.wui.ingest.client.Ingest;
import pt.gov.dgarq.roda.wui.ingest.list.client.IngestList;
import pt.gov.dgarq.roda.wui.ingest.pre.client.PreIngest;
import pt.gov.dgarq.roda.wui.ingest.submit.client.IngestSubmit;
import pt.gov.dgarq.roda.wui.management.client.Management;
import pt.gov.dgarq.roda.wui.management.event.client.EventManagement;
import pt.gov.dgarq.roda.wui.management.statistics.client.Statistics;
import pt.gov.dgarq.roda.wui.management.user.client.Preferences;
import pt.gov.dgarq.roda.wui.management.user.client.Register;
import pt.gov.dgarq.roda.wui.management.user.client.UserLog;
import pt.gov.dgarq.roda.wui.management.user.client.WUIUserManagement;

/**
 * @author Luis Faria
 * 
 */
public class Menu extends Composite {

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static MainConstants constants = (MainConstants) GWT.create(MainConstants.class);

	private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	interface MyUiBinder extends UiBinder<Widget, Menu> {
	}

	@UiField
	MenuBar leftMenu;

	@UiField
	MenuBar rightMenu;

	// private final MenuBar aboutMenu;
	private final MenuItem home;

	// private final MenuBar disseminationMenu;
	private MenuItem dissemination_browse;
	private MenuItem dissemination_searchBasic;
	// private MenuItem dissemination_searchAdvanced;

	private final MenuBar ingestMenu;
	private MenuItem ingest_pre;
	private MenuItem ingest_submit;
	private MenuItem ingest_list;

	private final MenuBar administrationMenu;
	private MenuItem administration_user;
	private MenuItem administration_event;
	private MenuItem administration_statistics;
	private MenuItem administration_log;

	private final MenuBar userMenu;

	/**
	 * Main menu constructor
	 * 
	 */
	public Menu() {
		initWidget(uiBinder.createAndBindUi(this));

		home = new MenuItem(constants.title_home(), createCommand(Home.RESOLVER.getHistoryPath()));

		dissemination_browse = new MenuItem(constants.title_dissemination_browse(),
				createCommand(Browse.RESOLVER.getHistoryPath()));
		dissemination_searchBasic = new MenuItem(constants.title_dissemination_search_basic(),
				createCommand(BasicSearch.RESOLVER.getHistoryPath()));

		ingestMenu = new MenuBar(true);
		ingest_pre = ingestMenu.addItem(constants.title_ingest_pre(),
				createCommand(PreIngest.RESOLVER.getHistoryPath()));
		ingest_submit = ingestMenu.addItem(constants.title_ingest_submit(),
				createCommand(IngestSubmit.RESOLVER.getHistoryPath()));
		ingest_list = ingestMenu.addItem(constants.title_ingest_list(),
				createCommand(IngestList.RESOLVER.getHistoryPath()));
		ingestMenu.addItem(constants.title_ingest_help(), createCommand(Ingest.RESOLVER.getHistoryPath() + ".help"));

		administrationMenu = new MenuBar(true);
		administration_user = administrationMenu.addItem(constants.title_administration_user(),
				createCommand(WUIUserManagement.getInstance().getHistoryPath()));
		administration_event = administrationMenu.addItem(constants.title_administration_event(),
				createCommand(EventManagement.getInstance().getHistoryPath()));
		administration_statistics = administrationMenu.addItem(constants.title_administration_statistics(),
				createCommand(Statistics.getInstance().getHistoryPath()));
		administration_log = administrationMenu.addItem(constants.title_administration_log(),
				createCommand(UserLog.RESOLVER.getHistoryPath()));
		administrationMenu.addItem(constants.title_administration_help(), createCommand(Management.RESOLVER + ".help"));

		userMenu = new MenuBar(true);
		userMenu.addItem(constants.loginLogout(), new ScheduledCommand() {

			@Override
			public void execute() {
				UserLogin.getInstance().logout(new AsyncCallback<AuthenticatedUser>() {

					@Override
					public void onFailure(Throwable caught) {
						logger.fatal("Error logging out", caught);
					}

					@Override
					public void onSuccess(AuthenticatedUser result) {
						// do nothing
					}
				});
			}
		});
		userMenu.addItem(constants.loginPreferences(), createCommand(Preferences.getInstance().getHistoryPath()));

		UserLogin.getInstance().getAuthenticatedUser(new AsyncCallback<AuthenticatedUser>() {

			public void onFailure(Throwable caught) {
				logger.fatal("Error getting Authenticated user", caught);

			}

			public void onSuccess(AuthenticatedUser user) {
				updateVisibles(user);
			}

		});

		UserLogin.getInstance().addLoginStatusListener(new LoginStatusListener() {

			public void onLoginStatusChanged(AuthenticatedUser user) {
				updateVisibles(user);
			}

		});
	}

	private ScheduledCommand createCommand(final String path) {
		return new ScheduledCommand() {

			@Override
			public void execute() {
				History.newItem(path);
			}
		};
	}

	private ScheduledCommand createLoginCommand() {
		return new ScheduledCommand() {

			@Override
			public void execute() {
				UserLogin.getInstance().login();
			}
		};
	}

	private void updateVisibles(AuthenticatedUser user) {

		logger.info("Updating menu visibility for user " + user.getName());

		leftMenu.clearItems();
		leftMenuItemCount = 0;
		rightMenu.clearItems();

		// TODO make creating sync (not async)

		// About
		About.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean permitted) {
				if (permitted) {
					insertIntoLeftMenu(home, 0);
				}
			}

		});

		// Dissemination
		Browse.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse permissions", caught);

			}

			public void onSuccess(Boolean asRole) {
				if (asRole) {
					insertIntoLeftMenu(dissemination_browse, 1);
				}
			}

		});

		BasicSearch.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting basic search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				if (asRole) {
					insertIntoLeftMenu(dissemination_searchBasic, 2);
				}
			}

		});

		// Ingest
		PreIngest.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest_pre.setVisible(asRole);
			}

		});

		IngestSubmit.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest_submit.setVisible(asRole);
			}

		});

		IngestList.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				ingest_list.setVisible(asRole);
			}

		});

		Ingest.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				if (asRole) {
					insertIntoLeftMenu(new MenuItem(constants.title_ingest(), ingestMenu), 3);
				}
			}

		});

		// Administration

		WUIUserManagement.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting browse role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_user.setVisible(asRole);
			}

		});

		EventManagement.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_event.setVisible(asRole);
			}

		});

		Statistics.getInstance().isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_statistics.setVisible(asRole);
			}

		});

		UserLog.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting advanced search role", caught);
			}

			public void onSuccess(Boolean asRole) {
				administration_log.setVisible(asRole);
			}

		});

		Management.RESOLVER.isCurrentUserPermitted(new AsyncCallback<Boolean>() {

			public void onFailure(Throwable caught) {
				logger.error("Error getting roles", caught);
			}

			public void onSuccess(Boolean asRole) {
				if (asRole) {
					insertIntoLeftMenu(new MenuItem(constants.title_administration(), administrationMenu), 4);

				}
			}

		});

		// User
		if (user.isGuest()) {
			rightMenu.addItem(constants.loginLogin(), createLoginCommand());
			rightMenu.addItem(constants.loginRegister(), createCommand(Register.getInstance().getHistoryPath()));
		} else {
			rightMenu.addItem(user.getName(), userMenu);
		}

	}

	private int leftMenuItemCount = 0;

	private void insertIntoLeftMenu(MenuItem item, int index) {
		int indexToInsert = index <= leftMenuItemCount ? index : leftMenuItemCount;
		leftMenu.insertItem(item, indexToInsert);
		leftMenuItemCount++;
	}

}
