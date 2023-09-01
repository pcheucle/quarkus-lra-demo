package org.pch.account;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Account extends PanacheEntityBase {

    public static List<Account> findByClientId(UUID clientId){
        return list("clientId", clientId);
    }

    @Id
    private UUID id;

    private UUID clientId;

    private String number;

    private BigDecimal balance;

    private boolean closed;
    
    private URI lra;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getClientId() {
        return clientId;
    }

    public void setClientId(UUID clientId) {
        this.clientId = clientId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
    
	public URI getLra() {
		return lra;
	}

	public void setLra(URI lra) {
		this.lra = lra;
	}
}
