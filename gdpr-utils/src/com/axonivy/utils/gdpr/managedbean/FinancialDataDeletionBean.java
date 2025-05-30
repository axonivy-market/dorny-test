package com.axonivy.utils.gdpr.managedbean;

import static com.axonivy.utils.gdpr.constant.GDPRConstants.*;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.persistence.EntityManager;
import javax.persistence.metamodel.EntityType;
import javax.transaction.TransactionRolledbackException;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.PF;

import com.axonivy.utils.gdpr.contentstate.DataDeletionContentState;
import com.axonivy.utils.gdpr.enums.CustomField;
import com.axonivy.utils.gdpr.enums.DataDeletionState;
import com.axonivy.utils.gdpr.persistence.entities.FinancialCaseInfo;
import com.axonivy.utils.gdpr.persistence.entities.FinancialYear;
import com.axonivy.utils.gdpr.persistence.service.FinancialDataDeletionService;
import com.axonivy.utils.gdpr.service.DataDeletionService;
import com.axonivy.utils.gdpr.service.IvyService;
import com.axonivy.utils.gdpr.utils.FacesContexts;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.exec.Sudo;
import ch.ivyteam.ivy.workflow.CaseState;
import ch.ivyteam.ivy.workflow.ICase;
import ch.ivyteam.ivy.workflow.ITask;
import ch.ivyteam.log.Logger;

@ManagedBean
@ViewScoped
public class FinancialDataDeletionBean  extends AbstractDataDeletionBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = Ivy.log();
	private static final String ENABLE_ACTION_BUTTONS_RC = "enableActionButtonsRC()";
	private StringBuilder deletingMessage;
	private boolean isDeletionSuccessful;
	private boolean canBeFinishedTask;
	private EntityManager entityManager;
	private Set<EntityType<?>> configuredEntityTypes;

	@Override
	@PostConstruct
	public void init() {
		super.init();
		canBeFinishedTask = getDeletedFinancialYearsCustomField().getOrNull() != null;
		if (canBeFinishedTask) {
			contentState.setReadOnly(true);
			updateContentForSubmittingDeletion();
		}
		deletingMessage = new StringBuilder(DataDeletionService.getInstance().getDeletionMessageForCurrentCase());
	}

	public void confirmDeleteData() {
		contentState.setInProgressMessageVisible(true);
		updateContentForSubmittingDeletion();
		isDeletionSuccessful = false;
		deletingMessage = new StringBuilder();
	}

	private void updateContentForSubmittingDeletion() {
		contentState.setDeletionStatusVisible(true);
		contentState.setSelectFinancialYearReadOnly(true);
		contentState.setCloseButtonVisible(false);
		contentState.setSubmitButtonVisible(false);
		contentState.setFinishButtonVisible(true);
	}

	public void deleteData() throws InterruptedException, TransactionRolledbackException {
		if (financialDataDeletion.getFinancialYears() == null) {
			financialDataDeletion.setFinancialYears(new ArrayList<>());
		}
		Set<String> deletedIds = new HashSet<>();
		var deletedFYs = EMPTY;
		addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/StartProcess");
		initEntityManagerAndModels();

		for (FinancialYear financialYear : CollectionUtils.emptyIfNull(selectedFinancialYears)) {
			financialYear.setNumberOfTasks(0l);
			financialYear.setNumberOfCases(0l);
			financialYear.setFinancialCaseInfos(new ArrayList<>());
			financialYear.setIsProcessed(true);
			addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/FindingCasesOfFinancialYear",
					financialYear.getName(), financialYear.getStartFY(), financialYear.getEndFY());
			long totalFinancialCases = DataDeletionService.getInstance().countByFinancialYear(financialYear);
			if (totalFinancialCases > 0) {
				addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/CheckAndDeleteEntities");
				checkAndDeteleEntities(deletedIds, financialYear, totalFinancialCases);
			}
			deletedFYs = deletedFYs.concat(COMMA).concat(financialYear.getName());
			financialDataDeletion.getFinancialYears().add(financialYear);
		}
		closeEntityManager();
		getDeletedFinancialYearsCustomField().set(deletedFYs);
		canBeFinishedTask = true;
		if (deletedIds.size() == 0) {
			addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/NoDataToDelete");
		} else {
			addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/DeletedEntitiesInDB",
					deletedIds.size());
		}
		addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/Finished");

		isDeletionSuccessful = true;
		contentState.setInProgressMessageVisible(false);
		FinancialDataDeletionService.getInstance().save(financialDataDeletion);
		DataDeletionService.getInstance().saveDeletionMessageToCurrentCase(getDeletingMessage());
	}

	private void closeEntityManager() {
		// Close entity manager
		if (entityManager != null && entityManager.isOpen()) {
			entityManager.close();
		}
	}

	private void initEntityManagerAndModels() {
		entityManager = IvyService.getConfiguredEntityManager();
		if (entityManager != null) {
			configuredEntityTypes = entityManager.getMetamodel().getEntities();
		}
	}

	private void checkAndDeteleEntities(Set<String> deletedIds, FinancialYear financialYear, long totalFinancialCases)
			throws TransactionRolledbackException {
		List<ICase> financialCases = new ArrayList<>();
		do {
			List<FinancialCaseInfo> financialCaseInfoList = new ArrayList<>();
			financialCases = DataDeletionService.getInstance().queryTop1000CasesByFinancialYear(financialYear);
			for (var deletedCase : financialCases) {
				var caseId = deletedCase.getId();
				var isCaseDestroyed = false;
				var financialCaseInfo = new FinancialCaseInfo();
				String entityId = deleteEntitiesOnDB(deletedIds, deletedCase, financialCaseInfo);
				List<ITask> allTasksOfDeletedCase = getAllTasksOfCase(deletedCase);
				String deletedTaskId = extractDeletedTaskIds(allTasksOfDeletedCase);
				if (deletedCase.getState() != CaseState.DONE && deletedCase.getState() != CaseState.DESTROYED) {
					var caseName = deletedCase.getName();
					updateNoteAndCustomfieldForDeletedCase(financialYear.getName(), deletedCase, deletedTaskId);
					IvyService.destroyCase(deletedCase);
					isCaseDestroyed = true;
					addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/DestroyedCase", caseId,
							caseName);
				}
				if (deletedIds.contains(entityId) || isCaseDestroyed) {
					financialCaseInfo.setCaseId(caseId);
					financialCaseInfo.setTaskNumbers(deletedTaskId);
					if (StringUtils.isEmpty(financialCaseInfo.getResult())) {
						financialCaseInfo.setResult(IvyService.translateCms("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/DataDeleted"));
					}
					financialCaseInfoList.add(financialCaseInfo);

					// Update count for FY
					financialYear.setNumberOfTasks(financialYear.getNumberOfTasks() + allTasksOfDeletedCase.size());
					financialYear.setNumberOfCases(financialYear.getNumberOfCases() + 1);
				}
			}
			financialYear.getFinancialCaseInfos().addAll(financialCaseInfoList);
		} while (CollectionUtils.isNotEmpty(financialCases) && totalFinancialCases > DataDeletionService.PAGE_SIZE);
	}

	private String extractDeletedTaskIds(List<ITask> tasks) {
		return Sudo.get(() -> {
			return CollectionUtils.emptyIfNull(tasks).stream().map(ITask::getId).map(Object::toString)
					.collect(Collectors.joining(SEMICOLON));
		});
	}

	private List<ITask> getAllTasksOfCase(ICase selectedCase) {
		return Sudo.get(() -> {
			return selectedCase.tasks().all();
		});
	}

	private void updateNoteAndCustomfieldForDeletedCase(String financialYearName, ICase deletedCase,
			String deletedTaskId) {
		Sudo.run(() -> {
			deletedCase.customFields().stringField(CustomField.DATA_DELETED_BY_FY.getFieldName())
					.set(financialYearName);
			deletedCase.createNote(Ivy.session(), Ivy.cms().co(
					"/Dialogs/com/axonivy/utils/gdpr/DeletedCaseNoteMessage", List.of(deletedTaskId)));
		});
	}

	private String deleteEntitiesOnDB(Set<String> deletedIds, ICase existedCase,
			FinancialCaseInfo dataDeletionCaseInfo) {
		var entityId = DataDeletionService.getInstance().extractEntityIdValueFromCase(existedCase);
		if (entityManager == null || CollectionUtils.isEmpty(configuredEntityTypes)) {
			return null;
		}
		// Loops all entities in the current persistence configuration and find by ID
		// Then if found, delete it
		for (EntityType<?> entityType : configuredEntityTypes) {
			String entityName = entityType.getName();
			Class<?> entityClass = entityType.getJavaType();
			Class<?> idClass = entityType.getIdType().getJavaType();
			if (idClass.isInstance(entityId)) {
				var transaction = entityManager.getTransaction();
				if (!transaction.isActive()) {
					transaction.begin();
				}
				try {
					var foundEntity = entityManager.find(entityClass, idClass.cast(entityId));
					if (foundEntity != null) {
						entityManager.remove(foundEntity);
						addNewMessage("/Dialogs/com/axonivy/utils/gdpr/ProcessStatusMessage/DeletingEntity",
								entityName, entityId);
						deletedIds.add(String.valueOf(entityId));
						transaction.commit();
						break;
					}
				} catch (Exception persistenceException) {
					LOG.error("GDPR: Cannot delete entities", persistenceException);
					dataDeletionCaseInfo.setResult(IvyService.translateCms(
							"/Dialogs/com/ricoh/hr/core/DataDeletion/ProcessStatusMessage/CouldNotDelete"));
					dataDeletionCaseInfo.setNote(persistenceException.getMessage());
				}
			} else {
				LOG.warn("GDPR: Skip {0} entity due to missmatch ID type: expected {1} - actual {2}", entityName,
						entityId.getClass().getSimpleName(), idClass.getSimpleName());
			}
		}

		return String.valueOf(entityId);
	}

	public void finishTask() {
		Sudo.run(() -> {
			Ivy.wfCase().customFields().stringField(CustomField.HIGHEST_STATE.getFieldName())
					.set(DataDeletionState.BUSINESS_DETAILS.name());
		});
		FacesContexts.invokeMethodByExpression(LOGIC_CLOSE_METHOD, new Object[] {}, null);
	}

	private void addNewMessage(String message, Object... params) {
		deletingMessage.append(StringUtils.LF);
		if (params == null || params.length == 0) {
			deletingMessage.append(IvyService.translateCms(message));
			return;
		}
		deletingMessage.append(IvyService.translateCms(message, params));
	}

	public String getDeletingMessage() {
		if (deletingMessage == null) {
			return EMPTY;
		}
		return deletingMessage.toString();
	}

	public boolean isCanBeFinishedTask() {
		return canBeFinishedTask;
	}

	public boolean stopPollMessage() {
		if (isDeletionSuccessful || contentState.isReadOnly()) {
			PF.current().executeScript(ENABLE_ACTION_BUTTONS_RC);
			return true;
		}
		return false;
	}

	@Override
	protected DataDeletionContentState getContentStateToInit() {
		return new DataDeletionContentState(DataDeletionState.DATA_DELETION);
	}
}
