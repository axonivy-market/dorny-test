package com.axonivy.utils.gdpr.managedbean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.commons.collections.CollectionUtils;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.MatchMode;

import com.axonivy.utils.gdpr.contentstate.DataDeletionContentState;
import com.axonivy.utils.gdpr.enums.DataDeletionState;
import com.axonivy.utils.gdpr.persistence.entities.FinancialCaseInfo;
import com.axonivy.utils.gdpr.persistence.entities.FinancialYear;
import com.axonivy.utils.gdpr.service.IvyService;
import static com.axonivy.utils.gdpr.enums.FilterColumn.*;
import ch.ivyteam.ivy.workflow.ICase;

@ManagedBean
@ViewScoped
public class GDPRBusinessDetailsBean extends AbstractDataDeletionBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private FinancialYear targetFinancialYear;
	private List<FinancialCaseInfo> financialCaseInfos;
	private List<FinancialCaseInfo> filteredByFinancialCaseInfos;
	private List<FilterMeta> filterBy;
	private long taskCounts;
	private long caseCounts;
	private String title;

	@Override
	@PostConstruct
	public void init() {
		super.init();
		getFinancialYears();
		initTargetFinancialYear();
		ICase finCase = IvyService.findCaseById(financialDataDeletion.getCaseId());
		if (finCase != null) {
			title = finCase.getName();
		} else {
			title = IvyService
					.translateCms("/Processes/DataDeletionSummary/Cases/GeneralDataProtectionSummary/name");
		}
		initFilterData();
	}

	protected void initFilterData() {
		filterBy = new ArrayList<>();
		filterBy.add(FilterMeta.builder().field(TASK.getField()).matchMode(MatchMode.CONTAINS).build());
		filterBy.add(FilterMeta.builder().field(CASE.getField()).matchMode(MatchMode.CONTAINS).build());
		filterBy.add(FilterMeta.builder().field(RESULT.getField()).matchMode(MatchMode.CONTAINS).build());
	}

	@Override
	protected DataDeletionContentState getContentStateToInit() {
		return new DataDeletionContentState(DataDeletionState.DATA_DELETION);
	}

	protected void initTargetFinancialYear() {
		if (CollectionUtils.isNotEmpty(financialYears)) {
			targetFinancialYear = financialYears.get(0);
		}
		refreshTargetFinancialYear();
	}

	public void refreshTargetFinancialYear() {
		financialCaseInfos = new ArrayList<>();
		financialCaseInfos.addAll(Optional.ofNullable(targetFinancialYear).map(FinancialYear::getFinancialCaseInfos)
				.orElse(new ArrayList<FinancialCaseInfo>()));
		taskCounts = Optional.ofNullable(targetFinancialYear).map(FinancialYear::getNumberOfTasks).orElse((long) 0);
		caseCounts = Optional.ofNullable(targetFinancialYear).map(FinancialYear::getNumberOfCases).orElse((long) 0);
	}

	public FinancialYear getTargetFinancialYear() {
		return targetFinancialYear;
	}

	public void setTargetFinancialYear(FinancialYear targetFinancialYear) {
		this.targetFinancialYear = targetFinancialYear;
	}

	public List<FinancialCaseInfo> getFinancialCaseInfos() {
		return financialCaseInfos;
	}

	public void setFinancialCaseInfos(List<FinancialCaseInfo> financialCaseInfos) {
		this.financialCaseInfos = financialCaseInfos;
	}

	public List<FinancialCaseInfo> getFilteredByFinancialCaseInfos() {
		return filteredByFinancialCaseInfos;
	}

	public void setFilteredByFinancialCaseInfos(List<FinancialCaseInfo> filteredByFinancialCaseInfos) {
		this.filteredByFinancialCaseInfos = filteredByFinancialCaseInfos;
	}

	public long getTaskCounts() {
		return taskCounts;
	}

	public void setTaskCounts(long taskCounts) {
		this.taskCounts = taskCounts;
	}

	public long getCaseCounts() {
		return caseCounts;
	}

	public void setCaseCounts(long caseCounts) {
		this.caseCounts = caseCounts;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<FilterMeta> getFilterBy() {
		return filterBy;
	}

	public void setFilterBy(List<FilterMeta> filterBy) {
		this.filterBy = filterBy;
	}
	
	public List<FinancialYear> getSelectedFinancialYears() {
		return selectedFinancialYears;
	}

	public void setSelectedFinancialYears(List<FinancialYear> selectedFinancialYears) {
		this.selectedFinancialYears = selectedFinancialYears;
	}
}