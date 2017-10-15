package com.unibet.worktest.dao;

import com.unibet.worktest.bank.Money;
import com.unibet.worktest.bank.TransactionLeg;
import com.unibet.worktest.model.Account;

/**
 * Interface for data access operations on database table account
 * 
 * @author Moupiya
 *
 */
public interface AccountDao {
	
	/**
	 * Returns true if account exists for the acountRef in Database table account.
	 * 
	 * @param accountRef the client defined account reference to check
	 * @return true if account exists for the acountRef in Database table account.
	 */
	boolean accountExists(String accountRef);

	/**
	 * Store the account details in database table account
	 * 
	 * @param accountRef the client defined account reference to create the account for
	 * @param initialBalance initial amount while creating the account
	 */
	void createAccount(String accountRef, Money initialBalance);

	/**
	 * Retrieves the account details for specified accountRef from database table account
	 * 
	 * @param accountRef the client defined account reference to get the account for
	 * @return an instance of Account object or {@code null} if it does not exist
	 */
	Account getAccount(String accountRef);

	/**
	 * Deletes the data from database table account
	 */
	void deleteTable();

	/**
	 * Add transfer amount to account balance for each transaction leg
	 * 
	 * @param leg TransactionLeg
	 * @see TransactionLeg
	 */
	void updateBalance(TransactionLeg leg);
}
