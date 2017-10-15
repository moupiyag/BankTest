/**
 * 
 */
package com.unibet.worktest.bankservice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.unibet.worktest.dao.AccountDao;
import com.unibet.worktest.dao.TransactionDao;
import com.unibet.worktest.exception.TransferFundException;
import com.unibet.worktest.model.Account;
import com.unibet.worktest.bank.AccountNotFoundException;
import com.unibet.worktest.bank.InfrastructureException;
import com.unibet.worktest.bank.InsufficientFundsException;
import com.unibet.worktest.bank.TransactionLeg;
import com.unibet.worktest.bank.Transaction;
import com.unibet.worktest.bank.TransferRequest;
import com.unibet.worktest.bank.TransferService;
import com.unibet.worktest.bank.UnbalancedLegsException;

/**
 * Provides implementation of interface TransferService
 * 
 * @author Moupiya
 *
 */
public class TransferServiceImpl implements TransferService {

	private AccountDao accountDao;
	private TransactionDao transactionDao;

	public void setAccountDao(AccountDao accountDao) {
		this.accountDao = accountDao;
	}

	public void setTransactionDao(TransactionDao transactionDao) {
		this.transactionDao = transactionDao;
	}

	@Override
	@Transactional(rollbackFor=Exception.class, isolation = Isolation.SERIALIZABLE)
	public void transferFunds(TransferRequest transferRequest) 
			throws AccountNotFoundException, InsufficientFundsException, UnbalancedLegsException, TransferFundException, InfrastructureException {
		try {
			validateRequest(transferRequest);
			for (TransactionLeg transactionLeg : transferRequest.getTransactionLegs()) {
				accountDao.updateBalance(transactionLeg);
			}
			validateAccountBalance(transferRequest.getTransactionLegs());
			saveTransaction(transferRequest);
			
		}catch(IllegalArgumentException | 
				AccountNotFoundException | 
				InsufficientFundsException | 
				UnbalancedLegsException | 
				TransferFundException exception) {
			throw exception;
		}
		catch(Exception exception) {
			throw new InfrastructureException(exception.getMessage());
		}
	}
	
	@Override
	public List<Transaction> findTransactions(String accountRef) throws AccountNotFoundException, InfrastructureException {
		try {
			if (!accountDao.accountExists(accountRef)) {
				throw new AccountNotFoundException(accountRef);
			}
			Set<String> transactionRefs = transactionDao.getTransactionRefsByAccountRef(accountRef);
			if (transactionRefs.isEmpty()) {
				return new ArrayList<Transaction>();
			}
			return transactionDao.getTransactions(transactionRefs);
			
		}catch(AccountNotFoundException exception) {
			throw exception;
		}
		catch(Exception exception) {
			throw new InfrastructureException(exception.getMessage());
		}
	}

	@Override
	public Transaction getTransaction(String transactionRef) throws InfrastructureException{
		try {
			return transactionDao.getTransactionByTransactionRef(transactionRef);
			
		}catch(Exception exception) {
			throw new InfrastructureException(exception.getMessage());
		}	
	}

	private void validateRequest(TransferRequest transferRequest) {
		validateTransferRequest(transferRequest);
		isTransactionBalanced(transferRequest.getTransactionLegs());
		validateAccountRefAndCurrency(transferRequest.getTransactionLegs());
	}
	
	/**
	 * Validates if transactionRef is null or at least two account legs
	 * in a transaction request
	 * 
	 * @param transferRequest a transfer request describing the transactions
	 * @throws IllegalArgumentException if the request has less than two legs or any other missing
     * or malformed properties
	 * @see TransferRequest
	 */
	private void validateTransferRequest(TransferRequest transferRequest) {
    	if (transferRequest.getReference() == null) {
            throw new IllegalArgumentException("transactionRef is NULL in transaction request");
        }
    	if (transferRequest.getTransactionLegs().size() < 2) {
            throw new IllegalArgumentException("Expected at least two account legs in transaction request");
        }
    }

    /**
     * Validates if all transaction legs for each currency are balanced 
     * 
     * @param transactionLegs list of transaction legs 
     * @throws UnbalancedLegsException  if the transaction legs are unbalanced (sum is not zero)
     * @see TransactionLeg
     */
    private void isTransactionBalanced(List<TransactionLeg> transactionLegs) throws UnbalancedLegsException {
    	Map<Currency, BigDecimal> transactionLegsByCurrency = new HashMap<Currency, BigDecimal>();
    	BigDecimal prevSumAmount = null;
    	for (TransactionLeg transactionLeg : transactionLegs) {
    		prevSumAmount = transactionLegsByCurrency.get(transactionLeg.getAmount().getCurrency());
    		if(prevSumAmount == null)
    		{
    			prevSumAmount = BigDecimal.ZERO;
    		}
    		transactionLegsByCurrency.put(transactionLeg.getAmount().getCurrency(), prevSumAmount.add(transactionLeg.getAmount().getAmount()));
        }
        for (BigDecimal sumAmount : transactionLegsByCurrency.values()) {
        	if (BigDecimal.ZERO.compareTo(sumAmount) != 0) {
                throw new UnbalancedLegsException("Transaction legs are not balanced");
            }
        }
    }

    /**
     * Validates if all referenced accounts exist and account currency matches with 
     * transaction leg currency
     * 
     * @param transactionLegs list of transaction legs 
     * @throws TransferFundException if mismatch in account currency and transaction leg currency
     * @throws AccountNotFoundException if any referenced account does not exist
     * @see TransactionLeg
     */
    private void validateAccountRefAndCurrency(Iterable<TransactionLeg> transactionLegs) throws TransferFundException, AccountNotFoundException {
        for (TransactionLeg transactionLeg : transactionLegs) {
            Account account = accountDao.getAccount(transactionLeg.getAccountRef());
            if (account == null) {
                throw new AccountNotFoundException(transactionLeg.getAccountRef());
            }
            if (!account.getCurrency().equals(transactionLeg.getAmount().getCurrency())) {
                throw new TransferFundException("Mismatch in transaction leg currency and account currency");
            }
        }
    }

    /**
     * Validates if all referenced accounts are not overdrawn
     * 
     * @param transactionLegs a list of transaction legs
     * @throws InsufficientFundsException if any referenced account is overdrawn
     * @see TransactionLeg
     */
    private void validateAccountBalance(List<TransactionLeg> transactionLegs) throws InsufficientFundsException {
    	Set<String> accounts = new HashSet<String>();
        for (TransactionLeg transactionLeg : transactionLegs) {
            accounts.add(transactionLeg.getAccountRef());
        }
        
        for (String accountRef : accounts) {
            Account account = accountDao.getAccount(accountRef);
            if (account.isOverdrawn()) {
                throw new InsufficientFundsException(accountRef);
            }
        }
    }
    
    /**
     * Stores the transaction details and transaction legs
     * 
     * @param request  a transfer request describing the transactions
     * @see TransferRequest 
     */
    private void saveTransaction(TransferRequest request) {
		Transaction transaction = new Transaction(request.getReference(), request.getType(), new Date(), request.getTransactionLegs());
		transactionDao.saveTransaction(transaction);
	}
   
}
