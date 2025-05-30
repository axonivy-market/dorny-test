package com.axonivy.utils.gdpr.utils;

import static com.axonivy.utils.gdpr.constant.GDPRConstants.AND;
import static com.axonivy.utils.gdpr.constant.GDPRConstants.EQUAL;
import static com.axonivy.utils.gdpr.constant.GDPRConstants.ID;
import static com.axonivy.utils.gdpr.service.IvyService.getCMSMacros;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.axonivy.utils.gdpr.enums.Roles;
import com.axonivy.utils.gdpr.model.Task;
import com.axonivy.utils.gdpr.service.IvyService;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.workflow.WorkflowPriority;

public class DataDeletionUtils {
	private static final String VIEW_DETAILS_PATH = "DataDeletion/businessCaseDetails.ivp";
	private static final String DD_MM_YYYY = IvyService.translateCms("/Labels/DatePattern");
	public static final String CATEGORY_PATH = "dataDeletion";

	public static Task genrateTaskInformation() {
		var task = new Task();
		task.setTaskName(getCMSMacros("/Processes/DataDeletion/Tasks/GeneralDataProtectionRegulation/name"));
		task.setTaskDescription(getCMSMacros("/Processes/DataDeletion/Tasks/GeneralDataProtectionRegulation/description"));
		task.setCategory(CATEGORY_PATH);
		task.setTaskRole(Roles.ADMINISTRATOR.getIvyRoleName());
		task.setTaskPriority(WorkflowPriority.NORMAL);
		return task;
	}

	public static String generateCaseName() {
		return IvyService.getCMSMacrosWithParam("/Processes/DataDeletion/Cases/GeneralDataProtectionRegulation/name",
				getDateStr(Ivy.wfCase().getStartTimestamp()));
	}

	public static String getDataDeleteionCaseDetailUrl(String id) {
		Map<String, String> params = new HashMap<>();
		params.put(ID, id);
		var requestPath = Ivy.html().startInFrameLink(VIEW_DETAILS_PATH).getRelative();
		if (StringUtils.isEmpty(requestPath)) {
			return EMPTY;
		}
		
		String paramStr = params.entrySet().stream().map(e -> {
			return e.getKey() + EQUAL + URLEncoder.encode(e.getValue(), StandardCharsets.ISO_8859_1);
		}).collect(Collectors.joining(AND));
		return requestPath + (StringUtils.isNotBlank(paramStr) ? AND + paramStr : EMPTY);
	}

	public static String getDateStr(Date date) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DD_MM_YYYY);
		return date != null ? simpleDateFormat.format(date) : EMPTY;
	}

	public static Date getDate(LocalDate localDate) {
		if (localDate == null) {
			return null;
		}
		return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}

	public static LocalDate convertStringToLocalDate(String dateString) {
		return dateString == null ? null : LocalDate.parse(dateString, DateTimeFormatter.ofPattern(DD_MM_YYYY));
	}
}
