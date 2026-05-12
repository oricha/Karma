package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_types")
public class TicketTypeEntity extends AuditableEntity {

    @Id
    @Column(length = 64)
    private String id;

    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false, length = 16)
    private String currency;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "sold_count", nullable = false)
    private int soldCount;

    @Column(name = "early_bird_price")
    private Double earlyBirdPrice;

    @Column(name = "early_bird_quantity")
    private Integer earlyBirdQuantity;

    @Column(name = "early_bird_deadline")
    private LocalDateTime earlyBirdDeadline;

    @Column(name = "sale_start")
    private LocalDateTime saleStart;

    @Column(name = "sale_end")
    private LocalDateTime saleEnd;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
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

    public int getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(int soldCount) {
        this.soldCount = soldCount;
    }

    public Double getEarlyBirdPrice() {
        return earlyBirdPrice;
    }

    public void setEarlyBirdPrice(Double earlyBirdPrice) {
        this.earlyBirdPrice = earlyBirdPrice;
    }

    public Integer getEarlyBirdQuantity() {
        return earlyBirdQuantity;
    }

    public void setEarlyBirdQuantity(Integer earlyBirdQuantity) {
        this.earlyBirdQuantity = earlyBirdQuantity;
    }

    public LocalDateTime getEarlyBirdDeadline() {
        return earlyBirdDeadline;
    }

    public void setEarlyBirdDeadline(LocalDateTime earlyBirdDeadline) {
        this.earlyBirdDeadline = earlyBirdDeadline;
    }

    public LocalDateTime getSaleStart() {
        return saleStart;
    }

    public void setSaleStart(LocalDateTime saleStart) {
        this.saleStart = saleStart;
    }

    public LocalDateTime getSaleEnd() {
        return saleEnd;
    }

    public void setSaleEnd(LocalDateTime saleEnd) {
        this.saleEnd = saleEnd;
    }
}
