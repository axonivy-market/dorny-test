package com.axonivy.utils.gdpr.demo.service;

import java.util.Random;
import java.util.UUID;

import com.axonivy.utils.gdpr.demo.entity.Company;
import com.axonivy.utils.gdpr.demo.entity.Employee;

import ch.ivyteam.ivy.environment.Ivy;
import ch.ivyteam.ivy.security.IUser;

public class DemoService {
	private static final String LOCAL_PERSISTENCE_UNIT = "local";

	public static String createUserTestData() {
		Employee employee = new Employee();
		employee.setId(UUID.randomUUID().toString());
		IUser user = Ivy.security().users().find("WilliamJGarcia");
		employee.setName(user.getFullName());
		employee.setEmailAddress(user.getEMailAddress());
		return persistEntity(employee).getId();
	}

	public static Integer createClassTestData() {
		Company company = new Company();
		company.setId(new Random().nextInt());
		company.setName("Langworth PLC");
		company.setPhone(263482790);
		return persistEntity(company).getId();
	}

	private static <T> T persistEntity(T user) {
		return Ivy.persistence().get(LOCAL_PERSISTENCE_UNIT).persist(user);
	}
}
