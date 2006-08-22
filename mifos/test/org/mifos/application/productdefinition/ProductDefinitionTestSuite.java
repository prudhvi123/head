package org.mifos.application.productdefinition;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.mifos.application.productdefinition.business.TestPrdCategoryStatusEntity;
import org.mifos.application.productdefinition.business.TestProductCategoryBO;
import org.mifos.application.productdefinition.business.TestProductCategoryBusinessService;
import org.mifos.application.productdefinition.business.TestSavingsOfferingBO;
import org.mifos.application.productdefinition.persistence.TestProductCategoryPersistence;
import org.mifos.application.productdefinition.persistence.TestProductDefinitionPersistence;
import org.mifos.application.productdefinition.persistence.service.TestProductDefinitionPersistenceService;
import org.mifos.framework.MifosTestSuite;

public class ProductDefinitionTestSuite extends MifosTestSuite {

	public ProductDefinitionTestSuite() throws Exception {
		super();
	}

	public static Test suite() throws Exception {
		TestSuite testSuite = new ProductDefinitionTestSuite();
		testSuite.addTestSuite(TestProductDefinitionPersistenceService.class);
		testSuite.addTestSuite(TestProductDefinitionPersistence.class);
		testSuite.addTestSuite(TestSavingsOfferingBO.class);
		testSuite.addTestSuite(TestPrdCategoryStatusEntity.class);
		testSuite.addTestSuite(TestProductCategoryBO.class);
		testSuite.addTestSuite(TestProductCategoryBusinessService.class);
		testSuite.addTestSuite(TestProductCategoryPersistence.class);
		return testSuite;
	}
	
}
