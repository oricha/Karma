package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "event_order_items")
public class OrderItemEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_order_id", nullable = false, length = 64)
    private String eventOrderId;

    @Column(name = "ticket_type_id", length = 64)
    private String ticketTypeId;

    @Column(name = "ticket_name", nullable = false, length = 255)
    private String ticketName;

    @Column(name = "unit_price", nullable = false)
    private double unitPrice;

    @Column(nullable = false, length = 16)
    private String currency;

    @Column(nullable = false)
    private int quantity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventOrderId() {
        return eventOrderId;
    }

    public void setEventOrderId(String eventOrderId) {
        this.eventOrderId = eventOrderId;
    }

    public String getTicketTypeId() {
        return ticketTypeId;
    }

    public void setTicketTypeId(String ticketTypeId) {
        this.ticketTypeId = ticketTypeId;
    }

    public String getTicketName() {
        return ticketName;
    }

    public void setTicketName(String ticketName) {
        this.ticketName = ticketName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
