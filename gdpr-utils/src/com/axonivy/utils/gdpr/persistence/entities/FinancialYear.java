package com.axonivy.utils.gdpr.persistence.entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

public class FinancialYear implements Serializable {
	private static final long serialVersionUID = 3093870580752220002L;
	private String name;
	private Long year;
	private LocalDate startFY;
	private LocalDate endFY;
	private Long numberOfTasks;
	private Long numberOfCases;
	private Boolean isProcessed;
	private List<FinancialCaseInfo> financialCaseInfos;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getYear() {
		return year;
	}

	public void setYear(Long year) {
		this.year = year;
	}

	public LocalDate getStartFY() {
		return startFY;
	}

	public void setStartFY(LocalDate startFY) {
		this.startFY = startFY;
	}

	public LocalDate getEndFY() {
		return endFY;
	}

	public void setEndFY(LocalDate endFY) {
		this.endFY = endFY;
	}

	public Long getNumberOfTasks() {
		return numberOfTasks;
	}

	public void setNumberOfTasks(Long numberOfTasks) {
		this.numberOfTasks = numberOfTasks;
	}

	public Long getNumberOfCases() {
		return numberOfCases;
	}

	public void setNumberOfCases(Long numberOfCases) {
		this.numberOfCases = numberOfCases;
	}

	public Boolean getIsProcessed() {
		return isProcessed;
	}

	public void setIsProcessed(Boolean isProcessed) {
		this.isProcessed = isProcessed;
	}

	public List<FinancialCaseInfo> getFinancialCaseInfos() {
		return financialCaseInfos;
	}

	public void setFinancialCaseInfos(List<FinancialCaseInfo> financialCaseInfos) {
		this.financialCaseInfos = financialCaseInfos;
	}
}
