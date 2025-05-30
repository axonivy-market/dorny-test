package com.axonivy.utils.gdpr.service;

import static com.axonivy.utils.gdpr.constant.GDPRConstants.SLASH;
import static com.axonivy.utils.gdpr.constant.GDPRConstants.ZERO;
import static com.axonivy.utils.gdpr.enums.CustomField.DATA_DELETED_BY_FY;
import static com.axonivy.utils.gdpr.service.IvyService.getIntFromVariable;
import static com.axonivy.utils.gdpr.service.IvyService.getStringFromVariable;
import static com.axonivy.utils.gdpr.utils.DataDeletionUtils.convertStringToLocalDate;
import static java.lang.String.valueOf;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.utils.gdpr.bo.DataDeletionMessage;
import com.axonivy.utils.gdpr.bo.GDPRCustomField;
import com.axonivy.utils.gdpr.enums.CustomField;
import com.axonivy.utils.gdpr.enums.GDPRVariable;
import com.axonivy.utils.gdpr.persistence.entities.FinancialDataDeletion;
import com.axonivy.utils.gdpr.persistence.entities.FinancialYear;
import com.axonivy.utils.gdpr.persistence.service.FinancialDataDeletionService;
import com.axonivy.utils.gdpr.utils.DataDeletionUtils;
import com.axonivy.utils.gdpr.utils.JacksonUtils;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.exec.Sudo;
import ch.ivyteam.ivy.workflow.CaseState;
import ch.ivyteam.ivy.workflow.ICase;
import ch.ivyteam.ivy.workflow.custom.field.CustomFieldType;
import ch.ivyteam.ivy.workflow.query.CaseQuery;
import ch.ivyteam.ivy.workflow.query.CaseQuery.FilterLink;

public class DataDeletionService {
	public static final int PAGE_SIZE = 1000;
	private static final String FY_PREFIX = "FY";
	private static final int DEFAULT_MAX_FINANCIAL_YEAR_CAN_BE_SELECTED = 3;
	private static final String DEFAULT_START_DATE_FINANCIAL_YEAR = "31/01";

	private static DataDeletionService instance;

	private DataDeletionService() { }

	public static DataDeletionService getInstance() {
		if (instance == null) {
			instance = new DataDeletionService();
		}
		return instance;
	}

	public List<ICase> queryTop1000CasesByFinancialYear(FinancialYear financialYear) {
		if (financialYear == null) {
			return List.of();
		}
		return Sudo.get(() -> {
			var caseQuery = buildFinancialCaseQuery(financialYear);
			return caseQuery.executor().results(ZERO, PAGE_SIZE);
		});
	}

	public long countByFinancialYear(FinancialYear financialYear) {
		if (financialYear == null) {
			return 0l;
		}
		return Sudo.get(() -> {
			var caseQuery = buildFinancialCaseQuery(financialYear);
			return caseQuery.executor().count();
		});
	}

	private CaseQuery buildFinancialCaseQuery(FinancialYear financialYear) {
		var customFieldsQuery = CaseQuery.create();
		for (var customField : getCustomFieldsInScope()) {
			createQueryByCustomField(customFieldsQuery, customField);
		}
		Date startDate = DataDeletionUtils.getDate(financialYear.getStartFY());
		Date endDate = DataDeletionUtils.getDate(financialYear.getEndFY());
		var caseQuery = CaseQuery.businessCases();
		caseQuery.where().and(customFieldsQuery);
		return caseQuery.where().and().startTimestamp().isGreaterOrEqualThan(startDate)
				.and().startTimestamp().isLowerOrEqualThan(endDate)
				.and(queryEntityIdNotNullAndNotInFYDataDeleted())
				.orderBy().category();
	}

	private FilterLink createQueryByCustomField(CaseQuery query, GDPRCustomField gdprCustomField) {
		return switch (gdprCustomField.getType()) {
		case STRING ->
			query.where().or().customField().stringField(StringUtils.trim(gdprCustomField.getName()))
				.isEqual(StringUtils.trim(gdprCustomField.getValue()));
		case NUMBER ->
			query.where().or().customField().numberField(StringUtils.trim(gdprCustomField.getName()))
				.isEqual(NumberUtils.createNumber(gdprCustomField.getValue()));

		default -> throw new IllegalArgumentException("Unexpected value: " + gdprCustomField.getType());
		};
	}

	private FilterLink queryEntityIdNotNullAndNotInFYDataDeleted() {
		var entityCustomField = getEntityCustomField();
		return switch (entityCustomField.getType()) {
		case STRING -> CaseQuery.create().where().and().customField().stringField(entityCustomField.getName())
				.isNotNull().and().customField().stringField(DATA_DELETED_BY_FY.getFieldName()).isNull();
		case NUMBER -> CaseQuery.create().where().and().customField().numberField(entityCustomField.getName())
				.isNotNull().and().customField().stringField(DATA_DELETED_BY_FY.getFieldName()).isNull();
		default -> throw new IllegalArgumentException("Unexpected value: " + entityCustomField.getType());
		};
	}

	public boolean shouldCreateNewCase() {
		return Sudo.get(() -> {
			if (hasRunningCase()) {
				return false;
			}

			var initialFinancialYear = getInitialFinancialYear();
			List<Integer> financialYears = new ArrayList<>();
			for (int index = 0; index <= getLastFinancialYearCanBeSelected() - initialFinancialYear; index++) {
				financialYears.add(initialFinancialYear + index);
			}
			return !ListUtils.removeAll(financialYears, getAllProcessedFinancialYears()).isEmpty();
		});
	}

	private boolean hasRunningCase() {
		return Sudo.get(() -> {
			var stateQuery = CaseQuery.create();
			for (var state : CaseState.ACTIVE_STATES) {
				stateQuery.where().or().state().isEqual(state);
			}
			var existingCase = CaseQuery.businessCases().where().category()
					.isEqual(DataDeletionUtils.CATEGORY_PATH).and(stateQuery).executor().firstResult();
			return existingCase != null;
		});
	}

	public List<Integer> getAllProcessedFinancialYears() {
		return FinancialDataDeletionService.getInstance().findAllProcessedFinancialYears().stream().map(FinancialDataDeletion::getFinancialYears)
				.flatMap(List::stream)
				.map(FinancialYear::getYear).map(Long::intValue).toList();
	}

	public Object extractEntityIdValueFromCase(ICase requestCase) {
		return Sudo.get(() -> {
			GDPRCustomField entityCustomField = getEntityCustomField();
			return switch (entityCustomField.getType()) {
			case STRING -> requestCase.customFields().stringField(entityCustomField.getName()).getOrNull();
			case NUMBER -> requestCase.customFields().numberField(entityCustomField.getName()).getOrNull();
			default -> throw new IllegalArgumentException("Unexpected value: " + entityCustomField.getType());
			};
		});
	}

	public void refreshAvailableFinancialYears(List<FinancialYear> financialYears) {
		if (financialYears == null) {
			financialYears = new ArrayList<>();
		}
		var processedFinancialYears = FinancialDataDeletionService.getInstance().findAllProcessedFinancialYears()
				.stream().map(FinancialDataDeletion::getFinancialYears).flatMap(List::stream).toList();
		if (CollectionUtils.isEmpty(processedFinancialYears)) {
			financialYears = buildFinancialYearOptionsFromNow(List.of(), financialYears);
		} else {
			var processedYears = processedFinancialYears.stream().map(FinancialYear::getYear).map(Long::intValue)
					.toList();
			financialYears = buildFinancialYearOptionsFromNow(processedYears, financialYears);
		}
	}

	private List<FinancialYear> buildFinancialYearOptionsFromNow(List<Integer> processedYears,
			List<FinancialYear> financialYears) {
		String startDateFY = getStartDateFinancialYear();
		int maxTotalFYCanBeSelected = getMaxTotalFinancialYearCanBeSelected();
		int initialFY = getInitialFinancialYear();
		var lastFYCanBeSelected = getLastFinancialYearCanBeSelected();
		var enoughtData = false;
		var startIndex = 0;
		do {
			var year = lastFYCanBeSelected - startIndex;
			enoughtData = year < initialFY || financialYears.size() == maxTotalFYCanBeSelected;
			if (processedYears.contains(year) || enoughtData) {
				startIndex++;
				continue;
			}

			var financialYear = new FinancialYear();
			financialYear.setYear(Long.valueOf(year));
			LocalDate startDate = convertStringToLocalDate(startDateFY.concat(SLASH).concat(valueOf(year - 1)));
			financialYear.setStartFY(startDate);
			financialYear.setEndFY(startDate.plusYears(1).minusDays(1));
			financialYear.setName(FY_PREFIX.concat(valueOf(year)));
			financialYears.add(financialYear);
			startIndex++;
		} while (!enoughtData);

		return financialYears;
	}

	public String getDeletionMessageForCurrentCase() {
		return Sudo.get(() -> {
			var messageId = Ivy.wfCase().customFields().stringField(CustomField.DATA_DELETION_MESSAGE_ID.getFieldName())
					.getOrNull();
			var result = Ivy.repo().find(messageId, DataDeletionMessage.class);
			return Objects.isNull(result) ? StringUtils.EMPTY : result.getMessage();
		});
	}

	public String saveDeletionMessageToCurrentCase(String deletionMessage) {
		return Sudo.get(() -> {
			var dataDeletionMessage = new DataDeletionMessage();
			dataDeletionMessage.setCaseId(Ivy.wfCase().getId());
			dataDeletionMessage.setMessage(deletionMessage);
			var messageId = Ivy.repo().save(dataDeletionMessage).getId();
			Ivy.wfCase().customFields().stringField(CustomField.DATA_DELETION_MESSAGE_ID.getFieldName()).set(messageId);
			return messageId;
		});
	}

	public int getLastFinancialYearCanBeSelected() {
		return LocalDateTime.now().getYear() - getMinRangeAfterEndOfFinancialYear();
	}

	public int getMinRangeAfterEndOfFinancialYear() {
		return getIntFromVariable(GDPRVariable.MIN_RANGE_AFTER_END_OF_FINANCIAL_YEAR, ZERO);
	}

	public int getInitialFinancialYear() {
		return getIntFromVariable(GDPRVariable.INITIAL_FINANCIAL_YEAR, LocalDate.now().getYear());
	}

	public String getStartDateFinancialYear() {
		return getStringFromVariable(GDPRVariable.START_DATE_FINANCIAL_YEAR, DEFAULT_START_DATE_FINANCIAL_YEAR);
	}

	public int getMaxTotalFinancialYearCanBeSelected() {
		return getIntFromVariable(GDPRVariable.MAX_TOTAL_FINANCIAL_YEAR_CAN_BE_SELECTED, DEFAULT_MAX_FINANCIAL_YEAR_CAN_BE_SELECTED);
	}

	private List<GDPRCustomField> getCustomFieldsInScope() {
		String customFields = getStringFromVariable(GDPRVariable.CUSTOM_FIELDS, EMPTY);
		return JacksonUtils.jsonValueToEntities(customFields, GDPRCustomField.class);
	}
	
	public GDPRCustomField getEntityCustomField() {
		String name = getStringFromVariable(GDPRVariable.ENTITY_CUSTOM_FIELD_NAME, EMPTY);
		if (StringUtils.isBlank(name)) {
			throw new RuntimeException("Missing required variable " + GDPRVariable.ENTITY_CUSTOM_FIELD_NAME.getVariableName());
		}
		String type = getStringFromVariable(GDPRVariable.ENTITY_CUSTOM_FIELD_TYPE, EMPTY);
		if (StringUtils.isBlank(type)) {
			throw new RuntimeException("Missing required variable " + GDPRVariable.ENTITY_CUSTOM_FIELD_TYPE.getVariableName());
		}
		return new GDPRCustomField(StringUtils.trim(name), CustomFieldType.fromStringOrDefault(type));
	}
}
