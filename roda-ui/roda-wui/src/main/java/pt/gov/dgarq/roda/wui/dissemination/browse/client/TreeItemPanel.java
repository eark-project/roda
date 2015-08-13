/**
 * 
 */
package pt.gov.dgarq.roda.wui.dissemination.browse.client;

import java.util.List;
import java.util.Vector;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.BrowseConstants;
import pt.gov.dgarq.roda.core.data.v2.SimpleDescriptionObject;
import pt.gov.dgarq.roda.wui.common.client.ClientLogger;
import pt.gov.dgarq.roda.wui.common.client.tools.DescriptionLevelUtils;
import pt.gov.dgarq.roda.wui.common.client.tools.StringUtility;

/**
 * @author Luis Faria
 * 
 */
public class TreeItemPanel extends FocusPanel implements SourcesSliderEvents {

	private static boolean SHOW_ITEM_POPUP = false;

	private ClientLogger logger = new ClientLogger(getClass().getName());

	private static BrowseConstants constants = (BrowseConstants) GWT.create(BrowseConstants.class);

	private final List<SliderEventListener> sliderListeners;

	private SimpleDescriptionObject sdo;

	private final HorizontalPanel layout;

	private Image image;

	private final Label label;

	// private final int childrenCount;

	final private Image waitImage = new Image(GWT.getModuleBaseURL() + "images/loadingSmall.gif");

	private boolean showInfo;

	private Label title;

	private Label startDate;

	private Label endDate;

	private MouseListener itemPopupMouseListener;

	/**
	 * Create a new tree item panel
	 * 
	 * @param sdo
	 *            the simple description object relative to this item
	 * @param childrenCount
	 *            the number of children this item has
	 * @param showInfo
	 *            whereas extended information should be presented
	 */
	public TreeItemPanel(SimpleDescriptionObject sdo, int childrenCount, boolean showInfo) {
		this.sdo = sdo;
		this.showInfo = false;
		// this.childrenCount = childrenCount;
		this.sliderListeners = new Vector<SliderEventListener>();

		layout = new HorizontalPanel();
		this.setWidget(layout);

		addStyleName("TreeItemPanel");

		layout.add(waitImage);
		waitImage.setVisible(false);
		waitImage.addStyleName("itemWaitingImage");

		final String labeltext = sdo.getLabel();

		label = new Label(labeltext);
		label.setWordWrap(false);

		image = DescriptionLevelUtils.getElementLevelIconImage(sdo.getLevel());
		if (image != null) {
			layout.add(image);
		}

		layout.add(label);

		layout.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
		layout.setCellVerticalAlignment(image, HorizontalPanel.ALIGN_MIDDLE);
		layout.setCellHorizontalAlignment(image, HorizontalPanel.ALIGN_CENTER);
		layout.setCellWidth(image, "24px");
		layout.setCellVerticalAlignment(label, HorizontalPanel.ALIGN_MIDDLE);
		layout.setCellHorizontalAlignment(label, HorizontalPanel.ALIGN_LEFT);

		layout.setCellWidth(label, "100%");
		image.addStyleName("treeitem-icon");
		label.addStyleName("treeitem-label");
		layout.addStyleName("treeitem-layout");

		setShowInfo(showInfo);

		if (SHOW_ITEM_POPUP) {
			itemPopupMouseListener = createPopupMouseListener();
			this.addMouseListener(itemPopupMouseListener);
		}
	}

	/**
	 * Show or hide extended information
	 * 
	 * @param showInfo
	 */
	public void setShowInfo(boolean showInfo) {
		if (this.showInfo != showInfo) {
			this.showInfo = showInfo;
			if (showInfo) {
				title = new Label();
				String normalizedTitle = StringUtility.normalizeSpaces(sdo.getTitle());
				title.setText(normalizedTitle == null ? constants.noTitle() : normalizedTitle);
				startDate = new Label(
						sdo.getDateInitial() == null ? constants.noDate() : sdo.getDateInitial().toString());
				endDate = new Label(sdo.getDateFinal() == null ? constants.noDate() : sdo.getDateFinal().toString());

				title.addStyleName("treeitem-info-title");
				startDate.addStyleName("treeitem-info-data");
				endDate.addStyleName("treeitem-info-data");

				layout.add(title);
				layout.add(startDate);
				layout.add(endDate);

			} else {
				layout.remove(title);
				layout.remove(startDate);
				layout.remove(endDate);
			}
		}
	}

	/**
	 * Show or hide the loading image
	 * 
	 * @param visible
	 */
	public void setWaitImageVisible(boolean visible) {
		waitImage.setVisible(visible);
	}

	public void addSliderEventListener(SliderEventListener listener) {
		sliderListeners.add(listener);
	}

	public void removeSliderEventListener(SliderEventListener listener) {
		sliderListeners.remove(listener);
	}

	class ItemPopup extends PopupPanel {

		private static final int SHOW_DELAY_MS = 3500;

		private static final int HIDE_DELAY_MS = 500;

		private final FocusPanel focus;

		private boolean showPopup;

		private boolean isMouseInsidePanel;

		private final VerticalPanel layout;

		private final Label header;

		private final ScrollPanel scroll;

		private final Label description;

		private final Timer showDelayTimer;

		private final Timer hideDelayTimer;

		/**
		 * Create a new item popup
		 */
		public ItemPopup() {
			super(true, false);
			focus = new FocusPanel();
			layout = new VerticalPanel();
			header = new Label(sdo.getId());

			scroll = new ScrollPanel();
			description = new Label(sdo.getDescription() == null ? constants.noDescription()
					: StringUtility.normalizeSpaces(sdo.getDescription()));

			scroll.setWidget(description);
			layout.add(header);
			layout.add(scroll);
			focus.setWidget(layout);
			this.setWidget(focus);

			showDelayTimer = new Timer() {
				public void run() {
					if (showPopup) {
						setPopupPositionAndShow(new PositionCallback() {

							public void setPosition(int offsetWidth, int offsetHeight) {
								ItemPopup.this.setPopupPosition(label.getAbsoluteLeft(),
										label.getAbsoluteTop() - offsetHeight);
							}

						});
						logger.warn("Setting popup position and showing");

					}
				}
			};

			hideDelayTimer = new Timer() {
				public void run() {
					if (!showPopup) {
						ItemPopup.super.hide();
					}
				}
			};

			showPopup = false;
			isMouseInsidePanel = false;

			focus.addMouseListener(new MouseListener() {

				public void onMouseDown(Widget sender, int x, int y) {
				}

				public void onMouseEnter(Widget sender) {
					showPopup = true;
					isMouseInsidePanel = true;
					hideDelayTimer.cancel();
				}

				public void onMouseLeave(Widget sender) {
					showPopup = false;
					isMouseInsidePanel = false;
					hideDelayTimer.schedule(HIDE_DELAY_MS);
				}

				public void onMouseMove(Widget sender, int x, int y) {
				}

				public void onMouseUp(Widget sender, int x, int y) {

				}

			});

			this.addStyleName("itemPopup");
			focus.addStyleName("itemPopup-focus");
			scroll.addStyleName("itemPopup-scroll");
			layout.addStyleName("itemPopup-layout");
			header.addStyleName("header");
			description.addStyleName("description");

		}

		/**
		 * Schedule show after delay
		 */
		public void scheduleShow() {
			showPopup = true;
			hideDelayTimer.cancel();
			showDelayTimer.schedule(SHOW_DELAY_MS);
		}

		/**
		 * Schedule hide after delay
		 */
		public void scheduleHide() {
			if (!isMouseInsidePanel) {
				showPopup = false;
				showDelayTimer.cancel();
				hideDelayTimer.schedule(HIDE_DELAY_MS);
			}
		}

	}

	private MouseListener createPopupMouseListener() {
		MouseListener listener = new MouseListener() {
			ItemPopup itemPopup = new ItemPopup();

			public void onMouseDown(Widget sender, int x, int y) {
				itemPopup.hide();
			}

			public void onMouseEnter(Widget sender) {
				itemPopup.scheduleShow();
			}

			public void onMouseLeave(Widget sender) {
				itemPopup.scheduleHide();
			}

			public void onMouseMove(Widget sender, int x, int y) {
			}

			public void onMouseUp(Widget sender, int x, int y) {

			}

		};
		return listener;
	}

	public void setFocus(boolean inFocus) {
		// XXX this is a workarround to prevent focus jumping to top in FireFox
	}

}