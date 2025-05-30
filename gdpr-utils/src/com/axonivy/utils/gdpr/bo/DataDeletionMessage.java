package com.axonivy.utils.gdpr.bo;

public class DataDeletionMessage {
	private long caseId;
	private String message;

	public DataDeletionMessage() { }

	public long getCaseId() {
		return caseId;
	}

	public void setCaseId(long caseId) {
		this.caseId = caseId;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
