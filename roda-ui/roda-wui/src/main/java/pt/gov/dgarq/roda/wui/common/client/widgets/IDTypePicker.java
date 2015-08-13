/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client.widgets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.ListBox;

import config.i18n.client.CommonConstants;

/**
 * @author Luis Faria
 * 
 */
public class IDTypePicker extends ListBox {

	/**
	 * Identifier type
	 * 
	 */
	public static enum IDType {
		SIMPLE_ID, FULL_ID
	}

	private static String ATTRIBUTE_ALTRENDER_SIMPLE_ID = "id";
	private static String ATTRIBUTE_ALTRENDER_FULL_ID = "full_id";

	private static CommonConstants constants = (CommonConstants) GWT
			.create(CommonConstants.class);

	/**
	 * Create a new id picker
	 */
	public IDTypePicker() {
		this.setVisibleItemCount(1);
		init();
		this.addStyleName("idpicker");
	}

	protected void init() {
		addItem(constants.simpleID());
		addItem(constants.fullID());
		setSelectedIndex(0);
	}

	public void setSelected(int id) {
		setSelectedIndex(id);
	}

	/**
	 * Get selected id type
	 * 
	 * @return
	 */
	public IDType getSelectedIDType() {
		int id = getSelectedIndex();
		return (id == 0) ? IDType.SIMPLE_ID : IDType.FULL_ID;
	}

	public static String getIDTypeLabel(String type) {
		String label = null;
		if (type.equals(ATTRIBUTE_ALTRENDER_SIMPLE_ID)) {
			label = constants.simpleID();
		} else if (type.equals(ATTRIBUTE_ALTRENDER_FULL_ID)) {
			label = constants.fullID();
		}
		return label;
	}
}
