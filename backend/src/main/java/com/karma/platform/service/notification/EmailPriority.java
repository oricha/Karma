package com.karma.platform.service.notification;

public enum EmailPriority {
    TRANSACTIONAL(1, 30),
    REMINDER(2, 0),
    DIGEST(3, 0),
    NEWS(4, 0);

    private final int rank;
    private final int reservedPercentOfQuota;

    EmailPriority(int rank, int reservedPercentOfQuota) {
        this.rank = rank;
        this.reservedPercentOfQuota = reservedPercentOfQuota;
    }

    public int rank() {
        return rank;
    }

    public int reservedPercentOfQuota() {
        return reservedPercentOfQuota;
    }
}
