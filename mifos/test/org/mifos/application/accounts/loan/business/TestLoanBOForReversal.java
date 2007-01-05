package org.mifos.application.accounts.loan.business;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.mifos.application.accounts.business.AccountActionDateEntity;
import org.mifos.application.accounts.business.AccountFlagMapping;
import org.mifos.application.accounts.business.AccountPaymentEntity;
import org.mifos.application.accounts.business.AccountTrxnEntity;
import org.mifos.application.accounts.exceptions.AccountException;
import org.mifos.application.accounts.util.helpers.AccountState;
import org.mifos.application.accounts.util.helpers.AccountStateFlag;
import org.mifos.application.accounts.util.helpers.PaymentData;
import org.mifos.application.customer.center.business.CenterBO;
import org.mifos.application.customer.client.business.ClientBO;
import org.mifos.application.customer.group.business.GroupBO;
import org.mifos.application.customer.util.helpers.CustomerStatus;
import org.mifos.application.meeting.business.MeetingBO;
import org.mifos.application.productdefinition.business.LoanOfferingBO;
import org.mifos.framework.MifosTestCase;
import org.mifos.framework.hibernate.helper.HibernateUtil;
import org.mifos.framework.security.util.UserContext;
import org.mifos.framework.util.helpers.Money;
import org.mifos.framework.util.helpers.TestObjectFactory;

public class TestLoanBOForReversal extends MifosTestCase {

	private LoanBO loan = null;

	private CenterBO center = null;

	protected GroupBO group = null;

	private ClientBO client = null;

	private UserContext userContext;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		userContext = TestObjectFactory.getContext();
	}

	@Override
	protected void tearDown() throws Exception {
		if (loan != null)
			loan = (LoanBO) HibernateUtil.getSessionTL().get(LoanBO.class,
					loan.getAccountId());
		if (client != null)
			client = (ClientBO) HibernateUtil.getSessionTL().get(
					ClientBO.class, client.getCustomerId());
		if (group != null)
			group = (GroupBO) HibernateUtil.getSessionTL().get(GroupBO.class,
					group.getCustomerId());
		if (center != null)
			center = (CenterBO) HibernateUtil.getSessionTL().get(
					CenterBO.class, center.getCustomerId());
		TestObjectFactory.cleanUp(loan);
		TestObjectFactory.cleanUp(client);
		TestObjectFactory.cleanUp(group);
		TestObjectFactory.cleanUp(center);

		HibernateUtil.closeSession();
		super.tearDown();
	}

	private void createInitialCustomers() {
		MeetingBO meeting = TestObjectFactory.createMeeting(TestObjectFactory
				.getMeetingHelper(1, 1, 4, 2));
		center = TestObjectFactory.createCenter("Center", meeting);
		group = TestObjectFactory.createGroupUnderCenter("Group",
				CustomerStatus.GROUP_ACTIVE, center);
	}

	private void createLoanAccount() {
		createInitialCustomers();
		LoanOfferingBO loanOffering = TestObjectFactory.createLoanOffering(
				"Loan", Short.valueOf("2"),
				new Date(System.currentTimeMillis()), Short.valueOf("1"),
				300.0, 1.2, Short.valueOf("3"), Short.valueOf("1"), Short
						.valueOf("1"), Short.valueOf("1"), Short.valueOf("1"),
				Short.valueOf("1"), center.getCustomerMeeting().getMeeting());
		loan = TestObjectFactory.createLoanAccount("42423142341", group,
				AccountState.LOANACC_APPROVED.getValue(), new Date(System
						.currentTimeMillis()), loanOffering);
	}

	private void disburseLoan() throws AccountException {
		loan.setUserContext(userContext);
		loan.disburseLoan("4534", new Date(), Short.valueOf("1"), group
				.getPersonnel(), new Date(), Short.valueOf("1"));
		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();
	}

	private LoanBO retrieveLoanAccount() {
		return (LoanBO) HibernateUtil.getSessionTL().get(LoanBO.class,
				loan.getAccountId());
	}

	private void applyPaymentForLoan() throws AccountException {
		loan = retrieveLoanAccount();
		loan.setUserContext(userContext);
		List<AccountActionDateEntity> accntActionDates = new ArrayList<AccountActionDateEntity>();
		accntActionDates.addAll(loan.getAccountActionDates());
		Date currentDate = new Date(System.currentTimeMillis());
		PaymentData paymentData = TestObjectFactory.getLoanAccountPaymentData(
				accntActionDates,
				TestObjectFactory.getMoneyForMFICurrency(200), null, loan
						.getPersonnel(), "receiptNum", Short.valueOf("1"),
				currentDate, currentDate);
		loan.applyPayment(paymentData);
		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();
	}

	private void reverseLoan() throws AccountException {
		loan = retrieveLoanAccount();
		loan.setUserContext(userContext);
		loan.reverseLoanDisbursal(group.getPersonnel(), "Loan Disbursal");
		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();
	}

	private void adjustLastPayment() throws AccountException {
		loan = retrieveLoanAccount();
		loan.setUserContext(userContext);
		loan.adjustPmnt("loan account has been adjusted by test code");
		HibernateUtil.commitTransaction();
		HibernateUtil.closeSession();
	}

	public void testLoanDisbursalReversal() throws AccountException {
		createLoanAccount();
		disburseLoan();
		applyPaymentForLoan();
		loan = retrieveLoanAccount();
		int noOfPayments = loan.getAccountPayments().size();
		int noOfTransactions = 0;
		int noOfFinancialTransactions = 0;
		int noOfActivities = loan.getLoanActivityDetails().size();
		int noOfNotes = loan.getAccountNotes().size();
		for (AccountPaymentEntity accountPayment : loan.getAccountPayments()) {
			noOfTransactions += accountPayment.getAccountTrxns().size();
			for (AccountTrxnEntity accountTrxn : accountPayment
					.getAccountTrxns()) {
				noOfFinancialTransactions += accountTrxn
						.getFinancialTransactions().size();
			}
		}

		reverseLoan();
		loan = retrieveLoanAccount();
		assertEquals(noOfPayments, loan.getAccountPayments().size());
		int noOfTransactionsAfterReversal = 0;
		int noOfFinancialTransactionsAfterReversal = 0;
		for (AccountPaymentEntity accountPayment : loan.getAccountPayments()) {
			assertEquals(new Money(), accountPayment.getAmount());
			noOfTransactionsAfterReversal += accountPayment.getAccountTrxns()
					.size();
			for (AccountTrxnEntity accountTrxn : accountPayment
					.getAccountTrxns()) {
				noOfFinancialTransactionsAfterReversal += accountTrxn
						.getFinancialTransactions().size();
			}
		}
		assertEquals(2 * noOfFinancialTransactions,
				noOfFinancialTransactionsAfterReversal);
		assertEquals(2 * noOfTransactions, noOfTransactionsAfterReversal);
		assertEquals(2 * noOfActivities, loan.getLoanActivityDetails().size());
		assertEquals(AccountState.LOANACC_CANCEL, loan.getState());
		assertEquals(1, loan.getAccountFlags().size());
		for (AccountFlagMapping accountFlagMapping : loan.getAccountFlags()) {
			assertEquals(AccountStateFlag.LOAN_REVERSAL.getValue(),
					accountFlagMapping.getFlag().getId());
		}
		assertEquals(noOfNotes + 1, loan.getAccountNotes().size());
		assertEquals(loan.getLoanAmount(), loan.getLoanSummary()
				.getPrincipalDue());
		assertEquals(new Money(), loan.getLoanSummary().getTotalAmntPaid());
		HibernateUtil.closeSession();
	}

	public void testLoanDisbursalReversalWithAdjustment()
			throws AccountException {
		createLoanAccount();
		disburseLoan();
		applyPaymentForLoan();
		loan = retrieveLoanAccount();
		int noOfPayments = loan.getAccountPayments().size();
		int noOfTransactions = 0;
		int noOfFinancialTransactions = 0;
		int noOfActivities = loan.getLoanActivityDetails().size();
		int noOfNotes = loan.getAccountNotes().size();
		for (AccountPaymentEntity accountPayment : loan.getAccountPayments()) {
			noOfTransactions += accountPayment.getAccountTrxns().size();
			for (AccountTrxnEntity accountTrxn : accountPayment
					.getAccountTrxns()) {
				noOfFinancialTransactions += accountTrxn
						.getFinancialTransactions().size();
			}
		}
		adjustLastPayment();
		applyPaymentForLoan();
		reverseLoan();
		loan = retrieveLoanAccount();
		assertEquals(noOfPayments + 1, loan.getAccountPayments().size());
		int noOfTransactionsAfterReversal = 0;
		int noOfFinancialTransactionsAfterReversal = 0;
		for (AccountPaymentEntity accountPayment : loan.getAccountPayments()) {
			assertEquals(new Money(), accountPayment.getAmount());
			noOfTransactionsAfterReversal += accountPayment.getAccountTrxns()
					.size();
			for (AccountTrxnEntity accountTrxn : accountPayment
					.getAccountTrxns()) {
				noOfFinancialTransactionsAfterReversal += accountTrxn
						.getFinancialTransactions().size();
			}
		}
		assertEquals((3 * noOfFinancialTransactions) - 2,
				noOfFinancialTransactionsAfterReversal);
		assertEquals((3 * noOfTransactions) - 1, noOfTransactionsAfterReversal);
		assertEquals((3 * noOfActivities) - 1, loan.getLoanActivityDetails()
				.size());
		assertEquals(AccountState.LOANACC_CANCEL, loan.getState());
		assertEquals(1, loan.getAccountFlags().size());
		for (AccountFlagMapping accountFlagMapping : loan.getAccountFlags()) {
			assertEquals(AccountStateFlag.LOAN_REVERSAL.getValue(),
					accountFlagMapping.getFlag().getId());
		}
		assertEquals(noOfNotes + 1, loan.getAccountNotes().size());
		assertEquals(loan.getLoanAmount(), loan.getLoanSummary()
				.getPrincipalDue());
		assertEquals(new Money(), loan.getLoanSummary().getTotalAmntPaid());
		HibernateUtil.closeSession();
	}

}
