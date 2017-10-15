/**
 * 
 */
package com.unibet.worktest.dao;

import java.util.List;
import java.util.Set;

import com.unibet.worktest.bank.Transaction;


/**
 * Interface for data access operations on database tables transaction_history
 * and transaction_leg
 * 
 * @author Moupiya
 *
 */
public interface TransactionDao {
	
	/**
	 * Stores the transaction details in transaction_history table
	 * and details of transaction legs in transaction_leg table
	 * 
	 * @param transaction a monetary transaction between at least 
	 * two different accounts
	 * @see Transaction
	 */
	void saveTransaction(Transaction transaction);

	/**
	 * Fetches a set of transaction references for specified account reference
	 * from transaction_history table
	 * 
	 * @param accountRef client specified account reference
	 * @return a set of transaction references
	 */
	Set<String> getTransactionRefsByAccountRef(String accountRef);

	/**
	 * Fetches transaction details for a set of transaction references
	 * from transaction_history table
	 * 
	 * @param transactionRefs client specified transaction reference
	 * @return a list of transaction objects or an empty list if not found 
	 * @see Transaction
	 */
	List<Transaction> getTransactions(Set<String> transactionRefs);
	
	/**
	 * Fetches transaction details for specified transaction reference
	 * from transaction_history table
	 * 
	 * @param transactionRef client specified transaction reference
	 * @return an instance of transaction object or {@code null} if not 
	 * found in transaction_history table
	 * @see Transaction
	 */
	Transaction getTransactionByTransactionRef(String transactionRef);
	
	/**
	 * Deletes data from transaction_history and transaction_leg for
	 * testing
	 */
	void deleteTables();
}
