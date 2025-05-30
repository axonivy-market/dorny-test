package com.axonivy.utils.gdpr.demo.enums;

public enum CustomField {
	ENTITY_ID("entityId"), LEGAL_ENTITY("LegalEntity"), RDE("RDE");

	private String fieldName;

	private CustomField(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getFieldName() {
		return fieldName;
	}
}
