# Quarkus LRA Demo

Goal of this project is to demonstrate how to use MicroProfile LRA in a Quarkus application.

## Run the application

Prerequisites: JDK 17, a recent version of Maven (3.9.3), curl (i'am using Git BASH).

The application is composed of 3 microservices: frontend, client and account.

For each microservice, run:

```
mvn quarkus:dev 
```

The app is now running at localhost:8080.

## Populate data

You will have to populate some data before executing scenarios, by running:

```
./init.sh 
```


## Scenarios

### Client deletion

Checkout master branch.

Populate data.

Go to http://localhost:8080.

Click on view to access client details page.

Delete the client.

Everything is OK: the client and its accounts have been deleted.

### Client deletion in an inconsistent state

Same as before, but uncomment the last curl command in init.sh before populating data.

Delete the client.

Because the balance of account 2 is not 0 anymore, client accounts deletion in Account will fail.

So we have a data consistency issue: the client has been deleted but its accounts are still open.

### Client deletion with LRA

Checkout lra branch.

Populate the data

Delete the client.

This time the client has not been deleted and there is no data consistency issue.

LRA has been cancelled from Account service as accounts can't be closed.

The work done in Client service has been compensated (undelete client).

**NOTE:**
You should see this INFO log in Frontend:

```
LRA025023: Could not compensate LRA 'http://localhost:8080/lra-coordinator/0_ffffc0a80114_cf43_64c101e5_f': coordinator 'http://localhost:8080/lra-coordinator' responded with status 'Not Found'
```

This is normal: 
 - when Account service fails to close client accounts, it cancels the LRA and sends a 409 response to Frontend
 - Frontend maps the 409 response to a WebApplicationException, then we map this exception to a 500 response (ServerExceptionMapper) to be sent to the UI.
 - Returning this 500 response will also cancel the LRA
 - but it's too late as the LRA has already been canceled and compensated, and doesn't exist anymore in the LRA coordinator

### Unavailability of Account service

We still use lra branch for this scenario.

This time, we stop Account service just before deleting the client.

We notice that the LRA remains active, no compensation has been done, so we are in an inconsistent state.

We can cancel the LRA manually by calling http://localhost:8080/lra-coordinator/{lra}/cancel.

But we need a more robust solution to prevent this.

First, we need to add the missing exception to the handler so an HTTP 500 error is returned when this exception is thrown.

We can do that by adding following code in FrontendResource class:

```
@ServerExceptionMapper
public Response mapException(ProcessingException ex) {
	return Response.serverError().build();
}
```

This will cause Frontend service to cancel the LRA when connecting to Account service failed.

Then, we need to setup a timeout for the LRA on the deleteClient method.

```
@DELETE
@Path("clients/{id}")
@LRA(timeLimit = 5)
```

This will ensure that the LRA is cancelled anyway if still active after 5 seconds.

You can see the solution in branch lra_timeout.

#### Eventual consistency observation

We use lra_timeout branch for this scenario. 

We run Account service in debug, and we put a breakpoint on the first statement in deleteAccounts() method.

After populating data, and after deleting the client, your IDE should suspend Account service execution.

After reloading client page, you should see the inconsistent state : the client is deleted, but not the accounts. 

You can increase the LRA timeLimit to avoid the LRA to timeout and cancel during your observation.

```
@DELETE
@Path("clients/{id}")
@LRA(timeLimit = 30)
```

### Mitigate eventual consistency with semantic lock

We use lra_semantic_lock branch for this scenario.

Do the same steps as before.

After reloading client page, you still see the inconsistent state, but this time with a dirty deleted flag.

And if you let the execution continues in Account service, and reload the page again, you will see that this flag disappears when state become consistent. 

### Async on the client

Checkout lra_async_client branch.

Delete client.

LRA works as usual, but speed has been improved as this time we don't wait Client service to respond before calling Account.

### Async on the server

Checkout lra_async_client branch.

Delete client.

LRA works as usual, client deletion has been executed asynchronously on the server.
