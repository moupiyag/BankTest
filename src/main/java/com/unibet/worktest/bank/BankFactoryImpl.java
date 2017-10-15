/**
 * 
 */
package com.unibet.worktest.bank;


import com.unibet.worktest.util.ContextProvider;
import com.unibet.worktest.dao.AccountDao;
import com.unibet.worktest.dao.TransactionDao;

/**
 * Provides implementation of interface BankFactory which provides objects of AccountService and
 * TransferService to clients and initial setup for tests.
 * 
 * @author Moupiya
 *
 */
public class BankFactoryImpl implements BankFactory {

	@Override
	public AccountService getAccountService() {
		return ContextProvider.getBean("accountService");
	}

	@Override
	public TransferService getTransferService() {
		return ContextProvider.getBean("transferService");
	}

	@Override
	public void setupInitialData() {
		
		TransactionDao transactionDao = ContextProvider.getBean("transactionDao");
        transactionDao.deleteTables();
        
		AccountDao accountDao = ContextProvider.getBean("accountDao");
        accountDao.deleteTable();
	}

}
