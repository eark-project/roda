/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Luis Faria
 * 
 */
public class LoadingPopup extends PopupPanel {

	// private GWTLogger logger = new GWTLogger(GWT.getTypeName(this));

	private static int UPDATE_TIME_MILISEC = 1000;

	private static int MAX_TIME_MILISEC = 30000;

	private final Image loadingImage;

	private Widget widgetCenter;

	private boolean show;

	private Timer updateTimer;

	private Timer maxTimeout;

	/**
	 * Show loading popup
	 * 
	 * @param widgetCenter
	 *            widget in which the popup will center
	 */
	public LoadingPopup(Widget widgetCenter) {
		loadingImage = new Image(GWT.getModuleBaseURL() + "images/loading.gif");
		this.setWidget(loadingImage);
		this.widgetCenter = widgetCenter;
		this.show = false;

		this.updateTimer = new Timer() {

			public void run() {
				update();
			}

		};

		this.maxTimeout = new Timer() {

			public void run() {
				hide();
			}

		};

		loadingImage.addStyleName("loadingImage");
	}

	public void show() {
		// logger.debug("Showing loading " + id);
		centerAndShow();
		updateTimer.cancel();
		updateTimer.scheduleRepeating(UPDATE_TIME_MILISEC);
		maxTimeout.cancel();
		maxTimeout.schedule(MAX_TIME_MILISEC);
	}

	public void hide() {
		// logger.debug("Hiding loading " + id);
		updateTimer.cancel();
		maxTimeout.cancel();
		show = false;
		super.hide();
	}

	protected void centerAndShow() {
		show = true;
		if (widgetCenter != null && widgetCenter.isAttached()
				&& widgetCenter.isVisible()
				&& widgetCenter.getOffsetWidth() > 0) {
			center(this.getOffsetWidth(), this.getOffsetHeight());
			super.show();
		}

	}

	protected void center(int offsetWidth, int offsetHeight) {

		int left = Math.round(widgetCenter.getAbsoluteLeft()
				+ (float) widgetCenter.getOffsetWidth() / 2
				- (float) offsetWidth / 2);

		int top = Math.round(widgetCenter.getAbsoluteTop()
				+ (float) widgetCenter.getOffsetHeight() / 2
				- (float) offsetHeight / 2);

		this.setPopupPosition(left, top);
	}

	/**
	 * Center the loading popup on a widget
	 * 
	 * @param w
	 */
	public void centerOn(Widget w) {
		widgetCenter = w;

	}

	/**
	 * Update loading popup position
	 */
	public void update() {
		if (show) {
			centerAndShow();
		}
	}

}
