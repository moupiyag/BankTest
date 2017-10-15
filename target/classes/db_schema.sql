CREATE database bankDB;

CREATE TABLE bankDB.account (
	id INTEGER AUTO_INCREMENT PRIMARY KEY, 
	account_ref VARCHAR(20) NOT NULL UNIQUE,
	amount DECIMAL(20,2) NOT NULL,
	currency VARCHAR(3) NOT NULL
);



CREATE TABLE bankDB.transaction_history (
	transaction_ref VARCHAR(20) NOT NULL,
	transaction_type VARCHAR(20),
	transaction_date DATE NOT NULL,
	PRIMARY KEY(transaction_ref)
);

CREATE TABLE bankDB.transaction_leg (
	id INTEGER AUTO_INCREMENT PRIMARY KEY, 
	transaction_ref VARCHAR(20) NOT NULL, 
	account_ref VARCHAR(20) NOT NULL, 
	amount DECIMAL(20,2) NOT NULL, 
	currency VARCHAR(3) NOT NULL,
	
	FOREIGN KEY (transaction_ref)
        REFERENCES transaction_history(transaction_ref),
    
    FOREIGN KEY (account_ref)
        REFERENCES account(account_ref)
);

