package com.axonivy.utils.gdpr.managedbean;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.axonivy.utils.gdpr.persistence.entities.FinancialDataDeletion;
import com.axonivy.utils.gdpr.persistence.entities.FinancialYear;
import com.axonivy.utils.gdpr.persistence.service.FinancialDataDeletionService;
import com.axonivy.utils.gdpr.service.IvyService;

@ManagedBean
@ViewScoped
public class GeneralDataProtectionSummaryBean extends GDPRBusinessDetailsBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private List<FinancialDataDeletion> financialDataDeletions;

	@Override
	@PostConstruct
	public void init() {
		contentState = getContentStateToInit();
		setTitle(IvyService.translateCms("/Processes/DataDeletionSummary/Cases/GeneralDataProtectionSummary/name"));

		financialDataDeletions = FinancialDataDeletionService.getInstance().findAllProcessedFinancialYears();
		initFilterData();
		getFinancialYears();
		initTargetFinancialYear();
		refreshTargetFinancialYear();
		setSelectedFinancialYears(financialYears);
	}

	@Override
	public List<FinancialYear> getFinancialYears() {
		financialYears = financialDataDeletions.stream()
				.map(FinancialDataDeletion::getFinancialYears)
				.flatMap(Collection::stream)
				.sorted(Comparator.comparing(FinancialYear::getName)).toList();
		return financialYears;
	}

}