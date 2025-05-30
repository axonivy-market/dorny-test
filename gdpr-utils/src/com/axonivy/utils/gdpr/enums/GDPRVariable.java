package com.axonivy.utils.gdpr.enums;

public enum GDPRVariable {
	INITIAL_FINANCIAL_YEAR("gdpr.InitialFinancialYear"), CUSTOM_FIELDS("gdpr.CustomFieldsInScope"),
	START_DATE_FINANCIAL_YEAR("gdpr.StartDateFinancialYear"),
	MIN_RANGE_AFTER_END_OF_FINANCIAL_YEAR("gdpr.MinRangeAfterEndOfFinancialYear"),
	MAX_TOTAL_FINANCIAL_YEAR_CAN_BE_SELECTED("gdpr.MaxTotalFinancialYearCanBeSelected"),
	ENTITY_CUSTOM_FIELD_NAME("gdpr.EntityCustomField.Name"), ENTITY_CUSTOM_FIELD_TYPE("gdpr.EntityCustomField.Type"),
	PERSISTENCE_UNIT_NAME("gdpr.PersistenceUnitName");

	private String variableName;

	private GDPRVariable(String variableName) {
		this.variableName = variableName;
	}

	public String getVariableName() {
		return variableName;
	}
}
