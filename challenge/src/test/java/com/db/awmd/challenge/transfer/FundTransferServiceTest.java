package com.db.awmd.challenge.transfer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClientResponseException;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficiantFundsException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.service.FundTransferService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FundTransferServiceTest {
	@MockBean
	private AccountsService accountsService;
	@MockBean
	private EmailNotificationService emailService;
	@Autowired
	private FundTransferService fundTransferService;

	private Account fromAccount, toAccount;

	@Before
	public void setUp() {
		fromAccount = new Account("1234", new BigDecimal(10));
		toAccount = new Account("2345", new BigDecimal(10));
		when(accountsService.updateAccount(Matchers.any())).thenReturn(fromAccount);
		doNothing().when(emailService).notifyAboutTransfer(Matchers.any(), Matchers.any());
	}

	@Test
	public void transferFunds() {
		when(accountsService.getAccount("1234")).thenReturn(fromAccount);
		when(accountsService.getAccount("2345")).thenReturn(toAccount);
		fundTransferService.transferFunds("1234", "2345", new BigDecimal(10));

		assertTrue(fromAccount.getBalance().intValue() == 0);
		assertTrue(toAccount.getBalance().intValue() == 20);
	}

	@Test
	public void transferFundsWithLowBalance() {
		fromAccount.setBalance(BigDecimal.ZERO);
		when(accountsService.getAccount("1234")).thenReturn(fromAccount);
		when(accountsService.getAccount("2345")).thenReturn(toAccount);
		try {
			fundTransferService.transferFunds("1234", "2345", new BigDecimal(10));
			fail("Should have failed when trying to transfer");
		} catch (InsufficiantFundsException ex) {
			assertThat(ex.getMessage())
					.isEqualTo("You do not have sufficiant funds in your account to perform this transfer.");
		}
	}

	@Test
	public void transferFundsWithNegitiveAmount() {
		fromAccount.setBalance(BigDecimal.ZERO);
		when(accountsService.getAccount("1234")).thenReturn(fromAccount);
		when(accountsService.getAccount("2345")).thenReturn(toAccount);
		try {
			fundTransferService.transferFunds("1234", "2345", new BigDecimal(-10));
			fail("Should have failed when trying to transfer");
		} catch (RestClientResponseException ex) {
			assertThat(ex.getMessage()).isEqualTo("Amount to transfer must be valid");
		}
	}

	@Test
	public void transferFundsWhenAccountDoesnotExist() {
		fromAccount.setBalance(BigDecimal.ZERO);
		when(accountsService.getAccount("1234")).thenReturn(null);
		when(accountsService.getAccount("2345")).thenReturn(toAccount);
		try {
			fundTransferService.transferFunds("1234", "2345", new BigDecimal(10));
			fail("Should have failed when trying to transfer");
		} catch (AccountNotFoundException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account with id 1234 does not exit");
		}
	}
	
	@Test
	public void transferFundsWithNullAccountIds() {
		
		try {
			fundTransferService.transferFunds(null, null, new BigDecimal(10));
			fail("Should have failed when trying to transfer");
		} catch (AccountNotFoundException ex) {
			assertThat(ex.getMessage()).isEqualTo("Account with id null does not exit");
		}
	}
}
