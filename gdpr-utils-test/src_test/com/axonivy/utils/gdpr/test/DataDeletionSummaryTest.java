package com.axonivy.utils.gdpr.test;

import static com.codeborne.selenide.Condition.*;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;
import com.axonivy.ivy.webtest.engine.WebAppFixture;

@IvyWebTest(headless = true)
public class DataDeletionSummaryTest extends BaseTest {

	private static final String SUMMARY_PROCESS = "gdpr-utils/1943EA22591E28D4/startSummaryPage.ivp";
	private static final String VIEW_FORM = "deletion-detail-form";

	@BeforeAll
	static void setup(WebAppFixture appFixture) {
		initDefaultConfig(appFixture);
	}

	@Test
	void canChooseYearToSeeDeleteDataTest() {
		createDummyData();
		open(EngineUrl.createProcessUrl(SUMMARY_PROCESS));
		$(By.id(VIEW_FORM)).shouldBe(visible);
		$(By.id(VIEW_FORM + ":financial-year")).shouldBe(visible, enabled);
		$(By.id(VIEW_FORM + ":record-table:task-number:filter")).shouldBe(visible, enabled);
		$(By.id(VIEW_FORM + ":close-button")).shouldBe(visible, enabled);
	}

	private void createDummyData() {
		loginWithGRDPAdminRole();
		startDataDeletionProcess();
		startFirstTaskOfDataDeletionProcess();
		verifyAndGetFinancialYearDropdown().click();
		verifyAndGetFirstFinOption().click();
		verifyAndGetSubmitButton().click();
		verifyAndGetDeleteButton().click();
		verifyDeletingMessageGroup();
		verifyAndGetFinishTaskButton().click();
	}

}