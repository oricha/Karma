package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "event_themes")
@IdClass(EventThemeEntity.Key.class)
public class EventThemeEntity {

    @Id
    @Column(name = "event_id", length = 64)
    private String eventId;

    @Id
    @Column(name = "theme_id", length = 64)
    private String themeId;

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public static class Key implements Serializable {
        private String eventId;
        private String themeId;

        public Key() {
        }

        public Key(String eventId, String themeId) {
            this.eventId = eventId;
            this.themeId = themeId;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof Key key)) {
                return false;
            }
            return Objects.equals(eventId, key.eventId) && Objects.equals(themeId, key.themeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(eventId, themeId);
        }
    }
}
