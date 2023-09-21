package org.pch.frontend;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;
import org.pch.frontend.remote.account.AccountDTO;
import org.pch.frontend.remote.account.AccountService;
import org.pch.frontend.remote.client.ClientDTO;
import org.pch.frontend.remote.client.ClientService;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

@Path("frontend")
public class FrontendResource {

	private static final Logger LOG = Logger.getLogger(FrontendResource.class);

	@ServerExceptionMapper
	public Response mapException(WebApplicationException ex) {
		return Response.serverError().build();
	}
	
	@ServerExceptionMapper
	public Response mapException(ProcessingException ex) {
		return Response.serverError().build();
	}

	@RestClient
	private ClientService clientService;

	@RestClient
	private AccountService accountService;

	@GET
	@Path("clients")
	public List<ClientDTO> getClients() {
		List<ClientDTO> clients = clientService.getClients();
		return clients;
	}

	@GET
	@Path("clients/{id}")
	public ClientDTO getClient(@PathParam("id") UUID clientId) {
		ClientDTO client = clientService.getClient(clientId);
		return client;
	}

	@DELETE
	@Path("clients/{id}")
	@LRA(timeLimit = 30)
	public void deleteClient(@PathParam("id") UUID clientId) {
		LOG.info("Deleting client " + clientId);
		clientService.deleteClient(clientId);
		accountService.deleteClientAccounts(clientId);
	}

	@Compensate
	public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lra) throws Exception {
		LOG.info("Compensating LRA " + lra);
		return Response.ok().build();

	}

	@GET
	@Path("clients/{id}/accounts")
	public List<AccountDTO> getClientAccounts(@PathParam("id") UUID clientId) {
		List<AccountDTO> accounts = accountService.getClientAccounts(clientId);
		return accounts;
	}

}
