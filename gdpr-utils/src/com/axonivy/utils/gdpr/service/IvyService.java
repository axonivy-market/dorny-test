package com.axonivy.utils.gdpr.service;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.axonivy.utils.gdpr.constant.GDPRConstants;
import com.axonivy.utils.gdpr.enums.GDPRVariable;

import ch.ivyteam.ivy.application.ActivityState;
import ch.ivyteam.ivy.application.IApplication;
import ch.ivyteam.ivy.application.IProcessModelVersion;
import ch.ivyteam.ivy.application.app.IApplicationRepository;
import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.process.data.persistence.PersistenceContextFactory;
import ch.ivyteam.ivy.security.ISecurityContext;
import ch.ivyteam.ivy.security.exec.Sudo;
import ch.ivyteam.ivy.workflow.ICase;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public class IvyService {
	public static final String DEFAULT_PROJECT_ID = "gdpr-utils";
	private static final String CMS_MACROS_PATTERN = "<%=ivy.cms.co(\"{0}\")%>";
	private static final String CMS_MACROS_PATTERN_WITH_PARAMS = "<%=ivy.cms.co(\"{0}\",[{1}])%>";
	private static final String PARAM_PATTERN = "\"%s\"";

	private IvyService() { }
	
	public static ICase findCaseById(String caseUUID) {
		return Sudo.get(() -> {
			return Ivy.wf().findCase(caseUUID);
		});
	}

	public static String translateCms(String cmsURI, Object... objects) {
		return Ivy.cms().co(cmsURI, Arrays.asList(objects));
	}

	public static String getCMSMacros(String cmsUrl) {
		return MessageFormat.format(CMS_MACROS_PATTERN, cmsUrl);
	}

	public static String getCMSMacrosWithParam(String cmsUrl, Object... objects) {
		if (Objects.isNull(objects) || objects.length == 0) {
			return getCMSMacros(cmsUrl);
		} else {
			var params = Arrays.asList(objects).stream()
					.map(param -> String.format(PARAM_PATTERN, param))
					.collect(Collectors.joining(GDPRConstants.COMMA));
			return MessageFormat.format(CMS_MACROS_PATTERN_WITH_PARAMS, cmsUrl, params);
		}
	}

	public static void destroyCase(ICase iCase) {
		Sudo.get(() -> {
			iCase.destroy();
			return Void.class;
		});
	}

	public static int getIntFromVariable(GDPRVariable ivyVariable, int defaultVal) {
		String variable = Ivy.var().get(ivyVariable.getVariableName());
		return NumberUtils.toInt(variable, defaultVal);
	}

	public static List<String> splitIvyVariableValuesToList(GDPRVariable variable) {
		return Arrays.asList(Ivy.var().get(variable.getVariableName()).split(GDPRConstants.COMMA));
	}

	public static String getStringFromVariable(GDPRVariable ivyVariable, String defaultValue) {
		return StringUtils.defaultIfEmpty(Ivy.var().get(ivyVariable.getVariableName()), defaultValue);
	}

	public static EntityManager getConfiguredEntityManager() {
		EntityManager entityManager = null;
		List<IApplication> appsInCurrentSecurityContext = IApplicationRepository.of(ISecurityContext.current()).all();
		var activePMVs = appsInCurrentSecurityContext.stream()
				.map(IApplication::getProcessModelVersions).flatMap(Stream::distinct)
				.filter(pmv -> pmv.getActivityState() == ActivityState.ACTIVE)
				.filter(pmv -> !pmv.getProjectName().equals(DEFAULT_PROJECT_ID)).toList();
		for (var pmv : activePMVs) {
			entityManager = getConfiguredEntityManagerForPMV(pmv);
			if (entityManager != null) {
				break;
			}
		}
		return entityManager;
	}

	private static EntityManager getConfiguredEntityManagerForPMV(IProcessModelVersion pmv) {
		String persistenceUnitName = getStringFromVariable(GDPRVariable.PERSISTENCE_UNIT_NAME, EMPTY);
		try {
			return PersistenceContextFactory.of(pmv).get(persistenceUnitName).createEntityManager();
		} catch (PersistenceException e) {
			Ivy.log().error("Get the {0} unit failed by ", e, persistenceUnitName);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return null;
	}
}
