package com.axonivy.utils.gdpr.test;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.axonivy.ivy.webtest.IvyWebTest;
import com.axonivy.ivy.webtest.engine.EngineUrl;
import com.axonivy.ivy.webtest.engine.WebAppFixture;

@IvyWebTest(headless = true)
public class DataDeletionTest extends BaseTest {

	private static final String TEST_PROCESS_ID = "gdpr-utils-test/194EF1C2639450F0/";
	protected static final String CREATE_DUMMY_DATA = "gdpr-utils-demo/1948C6200884AE99/startCreateDummyData.ivp";

	@BeforeAll
	static void setup(WebAppFixture appFixture) {
		initDefaultConfig(appFixture);
	}

	public void createTestData() {
		// Create dummy task
		open(EngineUrl.createProcessUrl(CREATE_DUMMY_DATA));
	}

	@Test
	public void canChooseAllYearsAndDeleteDataTest() {
		cleanUpdata();
		createTestData();
		loginWithGRDPAdminRole();
		startDataDeletionProcess();
		startFirstTaskOfDataDeletionProcess();
		verifyAndClickOnFinancialYearDropdown();
		verifyAndGetFinancialYearSelectionPanel();
		List<String> clickedIds = new ArrayList<>();
		var firstOption = verifyAndGetFirstFinOption();
		firstOption.click();
		clickedIds.add(firstOption.getText());

		// Verify that the submit button is enabled, before clicking it.
		verifyAndGetSubmitButton().click();

		// Verify that the confirmation popup was successful.
		verifyAndGetDeleteButton().click();

		verifyDeletingMessageGroup();
		verifyAndGetFinishTaskButton().click();

		// Test no data available
		createTestData();
		startDataDeletionProcess();
		startFirstTaskOfDataDeletionProcess();

		verifyAndClickOnFinancialYearDropdown();

		verifyAndGetFinancialYearSelectionPanel();
		firstOption = verifyAndGetFirstFinOption();
		assertFalse(clickedIds.contains(firstOption.getText()), "The deleted FIN still show on");
	}

	private void verifyAndClickOnFinancialYearDropdown() {
		verifyAndGetFinancialYearDropdown().click();
	}

	private void cleanUpdata() {
		open(EngineUrl.createProcessUrl(TEST_PROCESS_ID + "cleanData.ivp"));
	}
}