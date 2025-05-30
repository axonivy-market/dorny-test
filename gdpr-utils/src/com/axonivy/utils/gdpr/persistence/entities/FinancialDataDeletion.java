package com.axonivy.utils.gdpr.persistence.entities;

import java.io.Serializable;
import java.util.List;

public class FinancialDataDeletion implements Serializable {
	private static final long serialVersionUID = 573489167337567498L;
	private String id;
	private String caseId;
	private String legalEntities;
	private List<FinancialYear> financialYears;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public String getLegalEntities() {
		return legalEntities;
	}

	public void setLegalEntities(String legalEntities) {
		this.legalEntities = legalEntities;
	}

	public List<FinancialYear> getFinancialYears() {
		return financialYears;
	}

	public void setFinancialYears(List<FinancialYear> financialYears) {
		this.financialYears = financialYears;
	}
}
