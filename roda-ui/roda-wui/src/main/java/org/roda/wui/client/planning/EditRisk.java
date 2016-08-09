/**
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE file at the root of the source
 * tree and available online at
 *
 * https://github.com/keeps/roda
 */
package org.roda.wui.client.planning;

import java.util.List;

import org.roda.core.data.common.RodaConstants;
import org.roda.core.data.exceptions.NotFoundException;
import org.roda.core.data.v2.index.SelectedItems;
import org.roda.core.data.v2.risks.IndexedRisk;
import org.roda.core.data.v2.risks.Risk;
import org.roda.core.data.v2.risks.RiskIncidence;
import org.roda.wui.client.browse.BrowserService;
import org.roda.wui.client.common.UserLogin;
import org.roda.wui.client.common.utils.AsyncCallbackUtils;
import org.roda.wui.client.management.MemberManagement;
import org.roda.wui.common.client.HistoryResolver;
import org.roda.wui.common.client.tools.Tools;
import org.roda.wui.common.client.widgets.Toast;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import config.i18n.client.ClientMessages;

public class EditRisk extends Composite {

  public static final HistoryResolver RESOLVER = new HistoryResolver() {

    @Override
    public void resolve(List<String> historyTokens, final AsyncCallback<Widget> callback) {
      if (historyTokens.size() == 1) {
        String riskId = historyTokens.get(0);
        BrowserService.Util.getInstance().retrieve(Risk.class.getName(), riskId, new AsyncCallback<IndexedRisk>() {

          @Override
          public void onFailure(Throwable caught) {
            callback.onFailure(caught);
          }

          @Override
          public void onSuccess(IndexedRisk risk) {
            EditRisk editRisk = new EditRisk(risk);
            callback.onSuccess(editRisk);
          }
        });
      } else {
        Tools.newHistory(RiskRegister.RESOLVER);
        callback.onSuccess(null);
      }
    }

    @Override
    public void isCurrentUserPermitted(AsyncCallback<Boolean> callback) {
      UserLogin.getInstance().checkRoles(new HistoryResolver[] {MemberManagement.RESOLVER}, false, callback);
    }

    public List<String> getHistoryPath() {
      return Tools.concat(RiskRegister.RESOLVER.getHistoryPath(), getHistoryToken());
    }

    public String getHistoryToken() {
      return "edit_risk";
    }
  };

  interface MyUiBinder extends UiBinder<Widget, EditRisk> {
  }

  private static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private Risk risk;
  private static ClientMessages messages = GWT.create(ClientMessages.class);

  @UiField
  Button buttonApply;

  @UiField
  Button buttonCancel;

  @UiField
  Button buttonRemove;

  @UiField(provided = true)
  RiskDataPanel riskDataPanel;

  /**
   * Create a new panel to create a user
   *
   * @param user
   *          the user to create
   */
  public EditRisk(IndexedRisk risk) {
    this.risk = risk;
    this.riskDataPanel = new RiskDataPanel(true, risk, RodaConstants.RISK_CATEGORY, RodaConstants.RISK_IDENTIFIED_BY,
      RodaConstants.RISK_MITIGATION_OWNER);
    initWidget(uiBinder.createAndBindUi(this));
  }

  @UiHandler("buttonApply")
  void buttonApplyHandler(ClickEvent e) {
    if (riskDataPanel.isChanged()) {
      if (riskDataPanel.isValid()) {
        String riskId = risk.getId();
        risk = riskDataPanel.getRisk();
        risk.setId(riskId);
        BrowserService.Util.getInstance().updateRisk(risk, messages.modifyRiskMessage(), new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            errorMessage(caught);
          }

          @Override
          public void onSuccess(Void result) {
            Tools.newHistory(RiskRegister.RESOLVER);
          }

        });
      }
    }
  }

  @UiHandler("buttonCancel")
  void buttonCancelHandler(ClickEvent e) {
    cancel();
  }

  private void cancel() {
    Tools.newHistory(RiskRegister.RESOLVER);
  }

  private void errorMessage(Throwable caught) {
    if (caught instanceof NotFoundException) {
      Toast.showError(messages.editRiskNotFound(risk.getName()));
      cancel();
    } else if (!AsyncCallbackUtils.treatCommonFailures(caught)) {
      Toast.showError(messages.editRiskFailure(caught.getMessage()));
    }
  }

  @UiHandler("buttonRemove")
  void buttonRemoveHandler(ClickEvent e) {
    SelectedItems<RiskIncidence> incidences = riskDataPanel.getSelectedIncidences();

    BrowserService.Util.getInstance().deleteRiskIncidences(risk.getId(), incidences, new AsyncCallback<Void>() {

      @Override
      public void onFailure(Throwable caught) {
        errorMessage(caught);
      }

      @Override
      public void onSuccess(Void result) {
        Tools.newHistory(ShowRisk.RESOLVER, risk.getId());
      }

    });
  }

}
