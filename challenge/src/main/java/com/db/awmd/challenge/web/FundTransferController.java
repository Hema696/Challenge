package com.db.awmd.challenge.web;

import java.math.BigDecimal;

import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficiantFundsException;
import com.db.awmd.challenge.service.FundTransferService;

@RestController
@Validated
@RequestMapping(value ="/v1/transfer-funds", produces = MediaType.APPLICATION_JSON_VALUE)
public class FundTransferController {
	@Autowired
	private FundTransferService fundTransferService;

	@PostMapping
	public ResponseEntity<String> transferFunds(@RequestParam("from-account-id") @NotBlank String fromAccountId,
			@RequestParam("to-account-id") @NotBlank String toAccountId,
			@Min(value = 1L, message = "to transfer is below limit") @RequestParam BigDecimal amount) {
		try {
			fundTransferService.transferFunds(fromAccountId, toAccountId, amount);
		} catch (AccountNotFoundException | InsufficiantFundsException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
		return ResponseEntity.ok("Funds transferred succssfully");
	}

}
