package com.karma.platform.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "user_theme_preferences")
@IdClass(UserThemePreferenceEntity.Key.class)
public class UserThemePreferenceEntity {

    @Id
    @Column(name = "user_id", length = 64)
    private String userId;

    @Id
    @Column(name = "theme_id", length = 64)
    private String themeId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String themeId) {
        this.themeId = themeId;
    }

    public static class Key implements Serializable {
        private String userId;
        private String themeId;

        public Key() {
        }

        public Key(String userId, String themeId) {
            this.userId = userId;
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
            return Objects.equals(userId, key.userId) && Objects.equals(themeId, key.themeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, themeId);
        }
    }
}
