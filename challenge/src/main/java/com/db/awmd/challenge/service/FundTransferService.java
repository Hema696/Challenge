package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficiantFundsException;

import lombok.extern.slf4j.Slf4j;

/**
 * Service class to transfer funds.
 */
@Service
@Slf4j

public class FundTransferService {
	@Autowired
	private AccountsService accountsService;
	@Autowired
	private EmailNotificationService emailService;

	/**
	 * Transfers funds from one account to other.
	 *
	 * @param fromAccountId the from account id
	 * @param toAccountId the to account id
	 * @param amount the amount to transfer
	 */
	public synchronized void transferFunds(String fromAccountId, String toAccountId, BigDecimal amount) {
		log.info("Transfer amount: {} from account with id: {} to account with id: {}", amount, fromAccountId,
				toAccountId);
		// additional check since its a public method exposed by service
		if (amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new RestClientResponseException("Amount to transfer must be valid", HttpStatus.BAD_REQUEST.value(),
					HttpStatus.BAD_REQUEST.getReasonPhrase(), null, null, null);
		}
		Account fromAccount = getAccount(fromAccountId);
		Account toAccount = getAccount(toAccountId);
		BigDecimal subtract = fromAccount.getBalance().subtract(amount);
		if (subtract.doubleValue() < 0) {
			log.error("Insufficiant funds in account: {} hence cannot transfer specified amount : {}");
			throw new InsufficiantFundsException(
					"You do not have sufficiant funds in your account to perform this transfer.");
		}
		fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
		toAccount.setBalance(toAccount.getBalance().add(amount));
		updateAccounts(fromAccount, toAccount);
		sentNotificationToAccountHolders(fromAccount, toAccount, amount);
	}

	private void sentNotificationToAccountHolders(Account fromAccount, Account toAccount, BigDecimal amount) {
		log.info("Send notifications to account holders");
		String now = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now());
		String availableBalanceMsg = "The Available Balance on " + now + " in your Account is ";
		emailService.notifyAboutTransfer(fromAccount,
				"A sum of " + amount + " debited from your account." + availableBalanceMsg + fromAccount.getBalance());
		emailService.notifyAboutTransfer(toAccount,
				"An sum of " + amount + " credited to your account." + availableBalanceMsg + toAccount.getBalance());

	}

	private void updateAccounts(Account... accounts) {
		Arrays.stream(accounts).forEach(accountsService::updateAccount);
	}

	private Account getAccount(String accountId) {
		Account account = accountsService.getAccount(accountId);
		if (Objects.isNull(account)) {
			log.error("Account with id {} does not exit", accountId);
			throw new AccountNotFoundException("Account with id " + accountId + " does not exit");
		}
		return account;
	}
}
