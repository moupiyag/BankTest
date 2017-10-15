/**
 * 
 */
package com.unibet.worktest.bankservice;

import com.unibet.worktest.bank.InfrastructureException;

import org.springframework.dao.DuplicateKeyException;

import com.unibet.worktest.bank.AccountNotFoundException;
import com.unibet.worktest.model.Account;
import com.unibet.worktest.bank.AccountService;
import com.unibet.worktest.bank.Money;
import com.unibet.worktest.dao.AccountDao;
import com.unibet.worktest.exception.AccountAlreadyExistsException;

/**
 * Provides implementation of interface AccountService
 * 
 * @author Moupiya
 *
 */
public class AccountServiceImpl implements AccountService {
	
	private AccountDao accountDao;

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	@Override
	public void createAccount(String accountRef, Money amount) throws AccountAlreadyExistsException, InfrastructureException {
		try{
			if (accountDao.accountExists(accountRef)) {
				throw new AccountAlreadyExistsException("Account already exists: " + accountRef);
			}
			
			accountDao.createAccount(accountRef, amount);
		}
		catch(DuplicateKeyException exception){
			throw new AccountAlreadyExistsException("Account already exists: " + accountRef);
		}
		catch(AccountAlreadyExistsException exception) {
			throw exception;
		}
		catch(Exception exception) {
			throw new InfrastructureException(exception.getMessage());
		}
	}

	@Override
	public Money getAccountBalance(String accountRef) throws AccountNotFoundException, InfrastructureException {
		try{
			Account account = accountDao.getAccount(accountRef);
			if (account == null) {
				throw new AccountNotFoundException(accountRef);
			}
			return account.getBalance();
		}catch(AccountNotFoundException exception) {
			throw exception;
		}
		catch(Exception exception) {
			throw new InfrastructureException(exception.getMessage());
		}
	}

}
