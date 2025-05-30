package com.axonivy.utils.gdpr.persistence.entities;

import java.io.Serializable;

public class FinancialCaseInfo implements Serializable {
	private static final long serialVersionUID = 1247076821880372747L;
	private Long caseId;
	private String taskNumbers;
	private String result;
	private String note;

	public Long getCaseId() {
		return caseId;
	}

	public void setCaseId(Long caseId) {
		this.caseId = caseId;
	}

	public String getTaskNumbers() {
		return taskNumbers;
	}

	public void setTaskNumbers(String taskNumbers) {
		this.taskNumbers = taskNumbers;
	}

	public String getResult() {
		return result;
	}

	public void setResult(String result) {
		this.result = result;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
}
