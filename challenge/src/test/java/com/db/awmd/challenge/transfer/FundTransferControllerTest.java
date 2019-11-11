package com.db.awmd.challenge.transfer;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficiantFundsException;
import com.db.awmd.challenge.service.FundTransferService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class FundTransferControllerTest {

	private MockMvc mockMvc;

	@MockBean
	private FundTransferService fundTransferService;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Before
	public void prepareMockMvc() {
		this.mockMvc = webAppContextSetup(this.webApplicationContext).build();
	}

	@Test
	public void transferFunds() throws Exception {
		doNothing().when(fundTransferService).transferFunds(Matchers.anyString(), Matchers.anyString(), Matchers.any());
		mockMvc.perform(post("/v1/transfer-funds?from-account-id=2345&to-account-id=1234&amount=10")
				.contentType(MediaType.TEXT_PLAIN)).andExpect(status().isOk());
	}

	@Test
	public void transferFundsWithInsufficientFunds() throws Exception {
		doThrow(new InsufficiantFundsException(
				"You do not have sufficiant funds in your account to perform this transfer.")).when(fundTransferService)
						.transferFunds(Matchers.anyString(), Matchers.anyString(), Matchers.any());
		MvcResult mvcResult = mockMvc
				.perform(post("/v1/transfer-funds?from-account-id=2345&to-account-id=1234&amount=10")
						.contentType(MediaType.TEXT_PLAIN))
				.andExpect(status().isBadRequest()).andReturn();
		Assert.assertTrue(mvcResult.getResponse().getContentAsString()
				.contains("You do not have sufficiant funds in your account to perform this transfer."));
	}

	@Test
	public void transferFundsWithInvalidAccountIds() throws Exception {
		doThrow(new AccountNotFoundException("Account with id 2345 does not exit")).when(fundTransferService)
				.transferFunds(Matchers.anyString(), Matchers.anyString(), Matchers.any());
		MvcResult mvcResult = mockMvc
				.perform(post("/v1/transfer-funds?from-account-id=2345&to-account-id=1234&amount=10")
						.contentType(MediaType.TEXT_PLAIN))
				.andExpect(status().isBadRequest()).andReturn();
		Assert.assertTrue(mvcResult.getResponse().getContentAsString().contains("Account with id 2345 does not exit"));
	}

	@Test
	public void transferFundsWithNegitiveAmount() throws Exception {
		mockMvc.perform(post("/v1/transfer-funds?from-account-id=2345&to-account-id=1234&amount=-10")
				.contentType(MediaType.TEXT_PLAIN)).andExpect(status().isBadRequest());
	}

	@Test
	public void transferFundsWithoutRequiredParams() throws Exception {
		mockMvc.perform(
				post("/v1/transfer-funds?from-account-id=2345&to-account-id=1234").contentType(MediaType.TEXT_PLAIN))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void transferFundsBelowMinimumLimit() throws Exception {
		mockMvc.perform(post("/v1/transfer-funds?from-account-id=2345&to-account-id=1234&amount=0")
				.contentType(MediaType.TEXT_PLAIN)).andExpect(status().isBadRequest());
	}
}
