package com.axonivy.utils.gdpr.model;

import ch.ivyteam.ivy.scripting.objects.Date;
import ch.ivyteam.ivy.scripting.objects.Duration;
import ch.ivyteam.ivy.workflow.WorkflowPriority;

public class Task {
	private String taskName;
	private String taskDescription;
	private Duration taskExpiryDuration;
	private String taskRole;
	private String category;
	private WorkflowPriority taskPriority;
	private int expiryCount;
	private Date expiryDate;

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	public String getTaskDescription() {
		return taskDescription;
	}

	public void setTaskDescription(String taskDescription) {
		this.taskDescription = taskDescription;
	}

	public Duration getTaskExpiryDuration() {
		return taskExpiryDuration;
	}

	public void setTaskExpiryDuration(Duration taskExpiryDuration) {
		this.taskExpiryDuration = taskExpiryDuration;
	}

	public String getTaskRole() {
		return taskRole;
	}

	public void setTaskRole(String taskRole) {
		this.taskRole = taskRole;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public WorkflowPriority getTaskPriority() {
		return taskPriority;
	}

	public void setTaskPriority(WorkflowPriority taskPriority) {
		this.taskPriority = taskPriority;
	}

	public int getExpiryCount() {
		return expiryCount;
	}

	public void setExpiryCount(int expiryCount) {
		this.expiryCount = expiryCount;
	}

	public Date getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(Date expiryDate) {
		this.expiryDate = expiryDate;
	}
}
