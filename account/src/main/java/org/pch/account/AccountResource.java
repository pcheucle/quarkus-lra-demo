package org.pch.account;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jboss.logging.Logger;

import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

@Path("accounts")
public class AccountResource {

	private static final Logger LOG = Logger.getLogger(AccountResource.class);

	@Context
	private UriInfo uriInfo;

	private URI resourceUri(UUID uuid) {
		return URI.create(String.format("%s/%s", uriInfo.getRequestUri().toString(), uuid.toString()));
	}

	@DELETE
	@Transactional
	public Response deleteAccounts(@QueryParam("clientId") UUID clientId) {

		List<Account> accounts = Account.find("clientId", clientId).withLock(LockModeType.PESSIMISTIC_WRITE).list();

		for (Account account : accounts) {
			if (account.getBalance().compareTo(BigDecimal.ZERO) == 0) {
				account.setClosed(true);
				LOG.info("Close account " + account.getNumber());
			} else {
				LOG.info("Cannot close account " + account.getNumber()
						+ " as balance is not 0. Cancel accounts deletion.");
				Account.getEntityManager().clear();
				return Response.status(Status.CONFLICT).build();
			}
		}

		return Response.noContent().build();
	}

	@PUT
	@Transactional
	public Response updateAccount(@Valid AccountDTO accountDTO) {

		Account account = Account.findById(accountDTO.getId());

		if (account != null) {
			account.setBalance(accountDTO.getBalance());
			accountDTO.setId(account.getId());
			accountDTO.setClosed(account.isClosed());
			accountDTO.setNumber(account.getNumber());
			accountDTO.setClientId(account.getClientId());

			return Response.ok(resourceUri(accountDTO.getId())).entity(accountDTO).build();

		} else {
			return Response.noContent().build();
		}

	}

	@POST
	@Transactional
	public Response createAccount(@Valid AccountDTO accountDTO) {

		Account account = new Account();

		account.setId(accountDTO.getId());
		account.setBalance(accountDTO.getBalance());
		account.setNumber(accountDTO.getNumber());
		account.setClosed(false);
		account.setClientId(accountDTO.getClientId());

		account.persist();

		return Response.created(resourceUri(accountDTO.getId())).entity(accountDTO).build();

	}

	@GET
	public Response getAccounts(@QueryParam("clientId") UUID clientId) {
		List<Account> accounts = Account.findByClientId(clientId);

		List<AccountDTO> accountDTOs = new ArrayList<>();

		for (Account account : accounts) {
			AccountDTO accountDTO = new AccountDTO();
			accountDTO.setBalance(account.getBalance());
			accountDTO.setClientId(account.getClientId());
			accountDTO.setClosed(account.isClosed());
			accountDTO.setId(account.getId());
			accountDTO.setNumber(account.getNumber());
			accountDTOs.add(accountDTO);
		}

		return Response.ok().entity(accountDTOs).build();
	}

	@GET
	@Path(value = "/delete-all")
	@Transactional
	public Response deleteAllAccounts() {
		Account.deleteAll();
		return Response.ok().build();
	}

}
