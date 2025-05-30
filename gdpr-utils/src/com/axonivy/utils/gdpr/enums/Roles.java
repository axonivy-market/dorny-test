package com.axonivy.utils.gdpr.enums;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.IRole;

public enum Roles {
	DEFAULT("Everybody"), ADMINISTRATOR("GDPRAdministrator");

	private String ivyRoleName;

	private Roles() {
		this.ivyRoleName = name();
	}

	private Roles(String ivyRoleName) {
		this.ivyRoleName = ivyRoleName;
	}

	public String getIvyRoleName() {
		return ivyRoleName;
	}

	public IRole getIvyRole() {
		return Ivy.wf().getSecurityContext().roles().find(ivyRoleName);
	}

	public boolean getCanUserActAsThisRole() {
		final IRole role = getIvyRole();

		if (role != null) {
			return Ivy.session().hasRole(role, true);
		} else {
			Ivy.log().warn("Role not found, will not show features connected to this role:" + ivyRoleName);
			return false;
		}
	}
}
