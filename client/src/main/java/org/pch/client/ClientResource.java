package org.pch.client;

import static org.eclipse.microprofile.lra.annotation.ws.rs.LRA.LRA_HTTP_CONTEXT_HEADER;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.lra.annotation.Compensate;
import org.eclipse.microprofile.lra.annotation.Complete;
import org.eclipse.microprofile.lra.annotation.ws.rs.LRA;
import org.jboss.logging.Logger;

import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("clients")
public class ClientResource {

	private static final Logger LOG = Logger.getLogger(ClientResource.class);

	@Inject 
	ManagedExecutor managedExecutor;

	@Context
	private UriInfo uriInfo;

	private URI resourceUri(UUID uuid) {
		return URI.create(String.format("%s/%s", uriInfo.getRequestUri().toString(), uuid.toString()));
	}

	@POST
	@Transactional
	public Response createClient(@Valid ClientDTO clientDTO) {

		Client client = new Client();
		client.setId(clientDTO.getId());
		client.setEmail(clientDTO.getEmail());
		client.setFullName(clientDTO.getFullName());
		client.setDirty(false);

		client.persist();

		return Response.created(resourceUri(client.getId())).entity(clientDTO).build();

	}

	@DELETE
	@Transactional
	public Response deleteAllClients() {

		Client.deleteAll();
		return Response.status(Response.Status.NOT_FOUND).build();

	}

	@DELETE
	@Path("{id}")
	@Transactional
	@LRA(value = LRA.Type.MANDATORY, end = false)
	public CompletionStage<Response> deleteClient(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lra,
			@PathParam("id") UUID clientId) {

		final CompletableFuture<Response> response = new CompletableFuture<>();
		
		LOG.info("Before submit");

		managedExecutor.submit(() -> {
			LOG.info("Deleting client " + clientId);

			Client client = Client.findById(clientId);
			LOG.info("client " + client);

			if (client != null) {
				client.setLra(lra);
				client.setDeleted(true);
				client.setDirty(true);
				LOG.info("Client " + clientId + " deleted");
				response.complete(Response.noContent().build());

			} else {
				LOG.info("Client " + clientId + " not found");
				response.complete(Response.status(Response.Status.NOT_FOUND).build());

			}
		});
		
		LOG.info("Before return response");

		return response;

	}

	@Path("compensate")
	@Compensate
	@Transactional
	public Response compensate(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lra) throws Exception {

		LOG.info("Compensating LRA " + lra);
		Client client = Client.find("lra", lra).withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();
		if (client != null) {
			LOG.info("Revert client " + client.getId() + " deletion corresponding to LRA " + lra);
			client.setDeleted(false);
			client.setDirty(false);
		}

		return Response.ok().build();

	}

	@Path("complete")
	@Complete
	@Transactional
	public Response complete(@HeaderParam(LRA_HTTP_CONTEXT_HEADER) URI lra) {

		LOG.info("Completing LRA " + lra);
		Client client = Client.find("lra", lra).withLock(LockModeType.PESSIMISTIC_WRITE).firstResult();
		if (client != null) {
			LOG.info("Confirm client " + client.getId() + " deletion corresponding to LRA " + lra);
			client.setDirty(false);
		}

		return Response.ok().build();

	}

	@GET
	@Path("{id}")
	public Response getClient(@PathParam("id") UUID clientId) {

		Client client = Client.findById(clientId);

		if (client != null) {
			ClientDTO clientDTO = new ClientDTO();
			clientDTO.setEmail(client.getEmail());
			clientDTO.setFullName(client.getFullName());
			clientDTO.setId(client.getId());
			clientDTO.setDeleted(client.isDeleted());
			clientDTO.setDirty(client.isDirty());
			return Response.ok().entity(clientDTO).build();

		} else {

			return Response.noContent().build();
		}

	}

	@GET
	public List<ClientDTO> getAllClients() {

		List<Client> clients = Client.listAll(Sort.ascending("fullName"));

		List<ClientDTO> clientDTOs = new ArrayList<>();

		for (Client client : clients) {

			ClientDTO clientDTO = new ClientDTO();
			clientDTO.setEmail(client.getEmail());
			clientDTO.setFullName(client.getFullName());
			clientDTO.setId(client.getId());
			clientDTO.setDeleted(client.isDeleted());
			clientDTO.setDirty(client.isDirty());

			clientDTOs.add(clientDTO);
		}

		return clientDTOs;
	}

}
