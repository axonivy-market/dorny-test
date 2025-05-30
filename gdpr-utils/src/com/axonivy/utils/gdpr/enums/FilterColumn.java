package com.axonivy.utils.gdpr.enums;

public enum FilterColumn {
	TASK("taskNumbers"), CASE("caseId"), RESULT("result");

	private String field;

	private FilterColumn(String field) {
		this.field = field;
	}

	public String getField() {
		return field;
	}
}
