package com.axonivy.utils.gdpr.enums;

public enum CustomField {
	ENTITY_ID("entityId"), CUSTOMIZATION_ADDITIONAL_CASE_DETAILS_PAGE("CUSTOMIZATION_ADDITIONAL_CASE_DETAILS_PAGE"),
	HIGHEST_STATE("highestState"), DELETED_FINANCIAL_YEARS("DeletedFinancialYears"),
	DATA_DELETED_BY_FY("DataDeletedByFinancialYear"), DATA_DELETION_MESSAGE_ID("DataDeletionMessageId");

	private String fieldName;

	private CustomField(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}
}
