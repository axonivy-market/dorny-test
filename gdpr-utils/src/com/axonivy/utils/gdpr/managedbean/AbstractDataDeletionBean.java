package com.axonivy.utils.gdpr.managedbean;

import static com.axonivy.utils.gdpr.service.IvyService.translateCms;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;

import com.axonivy.utils.gdpr.constant.GDPRConstants;
import com.axonivy.utils.gdpr.contentstate.DataDeletionContentState;
import com.axonivy.utils.gdpr.enums.CustomField;
import com.axonivy.utils.gdpr.persistence.entities.FinancialDataDeletion;
import com.axonivy.utils.gdpr.persistence.entities.FinancialYear;
import com.axonivy.utils.gdpr.persistence.service.FinancialDataDeletionService;
import com.axonivy.utils.gdpr.service.DataDeletionService;
import com.axonivy.utils.gdpr.utils.FacesContexts;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.exec.Sudo;
import ch.ivyteam.ivy.workflow.custom.field.ICustomStringField;

public abstract class AbstractDataDeletionBean {
	protected static final String LOGIC_CLOSE_METHOD = "#{logic.close}";
	protected List<FinancialYear> financialYears;
	protected List<FinancialYear> selectedFinancialYears;
	protected FinancialDataDeletion  financialDataDeletion;
	protected DataDeletionContentState contentState;

	public void init() {
		contentState = getContentStateToInit();
		selectedFinancialYears = new ArrayList<>();
		initFinancialDataDeletion();
	}

	protected void initFinancialDataDeletion() {
		String requestId = FacesContexts.evaluateValueExpression("#{data.financialDataDeletionId}", String.class);
		financialDataDeletion = FinancialDataDeletionService.getInstance().findById(requestId);
		if (financialDataDeletion == null) {
			financialDataDeletion = new FinancialDataDeletion();
			financialDataDeletion.setFinancialYears(new ArrayList<>());
		} else {
			selectedFinancialYears.addAll(Optional.ofNullable(financialDataDeletion.getFinancialYears()).orElse(new ArrayList<>()));
		}
	}

	protected abstract DataDeletionContentState getContentStateToInit();

	protected ICustomStringField getDeletedFinancialYearsCustomField() {
		return Sudo.get(() -> {
			return Ivy.wfTask().customFields().stringField(CustomField.DELETED_FINANCIAL_YEARS.getFieldName());
		});
	}

	public String getFinancialYearLabel(FinancialYear financialYear) {
		if (financialYear == null) {
			return EMPTY;
		} else if (BooleanUtils.isTrue(financialYear.getIsProcessed())) {
			return String.format(GDPRConstants.NAME_AND_CODE_FORMAT, financialYear.getName(), translateCms(
					"/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/DataDeleted"));
		}
		return financialYear.getName();
	}

	public String getTotalRecordsDeletedMessage() {
		var financialYears = financialDataDeletion.getFinancialYears();
		return translateCms("/Dialogs/com/axonivy/utils/gdpr/DataDeletionCompletedHelpText",
				financialYears.stream().map(FinancialYear::getNumberOfTasks).mapToLong(Long::longValue).sum(),
				financialYears.stream().map(FinancialYear::getNumberOfCases).mapToLong(Long::longValue).sum());
	}
	
	protected void initFinancialYears() {
		financialYears = new ArrayList<>();
		financialYears.addAll(Optional.ofNullable(financialDataDeletion)
				.map(FinancialDataDeletion::getFinancialYears).orElse(List.of()));
				
	}

	public List<FinancialYear> getFinancialYears() {
		if (CollectionUtils.isEmpty(financialYears)) {
			initFinancialYears();
			DataDeletionService.getInstance().refreshAvailableFinancialYears(financialYears);
		}
		return financialYears;
	}

	public void setFinancialYears(List<FinancialYear> financialYears) {
		this.financialYears = financialYears;
	}

	public DataDeletionContentState getContentState() {
		return contentState;
	}

	public void setContentState(DataDeletionContentState contentState) {
		this.contentState = contentState;
	}

	public FinancialDataDeletion getFinancialDataDeletion() {
		return financialDataDeletion;
	}

	public void setFinancialDataDeletion(FinancialDataDeletion financialDataDeletion) {
		this.financialDataDeletion = financialDataDeletion;
	}

	public List<FinancialYear> getSelectedFinancialYears() {
		return selectedFinancialYears;
	}

	public void setSelectedFinancialYears(List<FinancialYear> selectedFinancialYears) {
		this.selectedFinancialYears = selectedFinancialYears;
	}
}
