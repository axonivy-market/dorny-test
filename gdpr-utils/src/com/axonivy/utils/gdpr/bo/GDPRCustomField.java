package com.axonivy.utils.gdpr.bo;

import ch.ivyteam.ivy.workflow.custom.field.CustomFieldType;

public class GDPRCustomField {
	private String name;
	private CustomFieldType type;
	private String value;

	public GDPRCustomField() { }

	public GDPRCustomField(String name, CustomFieldType type) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CustomFieldType getType() {
		return type;
	}

	public void setType(CustomFieldType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
