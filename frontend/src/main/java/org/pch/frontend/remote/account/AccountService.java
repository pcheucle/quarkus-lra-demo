package org.pch.frontend.remote.account;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@RegisterRestClient
public interface AccountService {

	@DELETE
	CompletionStage<Response> deleteClientAccounts(@QueryParam("clientId") UUID clientId);

	@GET
	List<AccountDTO> getClientAccounts(@QueryParam("clientId") UUID clientId);

}
