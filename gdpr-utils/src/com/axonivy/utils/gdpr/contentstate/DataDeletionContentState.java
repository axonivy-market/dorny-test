package com.axonivy.utils.gdpr.contentstate;

import com.axonivy.utils.gdpr.enums.DataDeletionState;

public class DataDeletionContentState {
	protected boolean isReadOnly;
	protected boolean isCloseButtonVisible;
	protected boolean isSubmitButtonVisible;
	protected boolean isFinishButtonVisible;
	protected boolean isDeletionStatusVisible;
	protected boolean isInProgressMessageVisible;
	protected boolean isSelectFinancialYearReadOnly;

	public DataDeletionContentState() {
	}

	public DataDeletionContentState(DataDeletionState state) {
		switch (state) {
		case DATA_DELETION: {
			isReadOnly = false;
			isCloseButtonVisible = true;
			isSubmitButtonVisible = true;
			isFinishButtonVisible = false;
			isDeletionStatusVisible = false;
			isInProgressMessageVisible = false;
			isSelectFinancialYearReadOnly = false;
			break;
		}
		case BUSINESS_DETAILS:
			break;
		default:
			break;
		}
	}

	public boolean isReadOnly() {
		return isReadOnly;
	}

	public void setReadOnly(boolean isReadOnly) {
		this.isReadOnly = isReadOnly;
	}

	public boolean isSubmitButtonVisible() {
		return isSubmitButtonVisible;
	}

	public void setSubmitButtonVisible(boolean isSubmitButtonVisible) {
		this.isSubmitButtonVisible = isSubmitButtonVisible;
	}

	public boolean isFinishButtonVisible() {
		return isFinishButtonVisible;
	}

	public void setFinishButtonVisible(boolean isFinishButtonVisible) {
		this.isFinishButtonVisible = isFinishButtonVisible;
	}

	public boolean isDeletionStatusVisible() {
		return isDeletionStatusVisible;
	}

	public void setDeletionStatusVisible(boolean isDeletionStatusVisible) {
		this.isDeletionStatusVisible = isDeletionStatusVisible;
	}

	public boolean isInProgressMessageVisible() {
		return isInProgressMessageVisible;
	}

	public void setInProgressMessageVisible(boolean isInProgressMessageVisible) {
		this.isInProgressMessageVisible = isInProgressMessageVisible;
	}

	public boolean isSelectFinancialYearReadOnly() {
		return isSelectFinancialYearReadOnly;
	}

	public void setSelectFinancialYearReadOnly(boolean isSelectFinancialYearReadOnly) {
		this.isSelectFinancialYearReadOnly = isSelectFinancialYearReadOnly;
	}

	public boolean isCloseButtonVisible() {
		return isCloseButtonVisible;
	}

	public void setCloseButtonVisible(boolean isCloseButtonVisible) {
		this.isCloseButtonVisible = isCloseButtonVisible;
	}
}
