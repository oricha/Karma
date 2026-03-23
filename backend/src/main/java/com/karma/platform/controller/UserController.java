package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.EventDtos;
import com.karma.platform.dto.GroupDtos;
import com.karma.platform.dto.OrderDtos;
import com.karma.platform.dto.UserDtos;
import com.karma.platform.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me")
public class UserController {

    private final UserService userService;
    private final CurrentUser currentUser;

    public UserController(UserService userService, CurrentUser currentUser) {
        this.userService = userService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public UserDtos.UserResponse me() {
        return userService.currentUser(currentUser.id());
    }

    @PutMapping
    public UserDtos.UserResponse update(@Valid @RequestBody UserDtos.UpdateUserRequest request) {
        return userService.update(currentUser.id(), request);
    }

    @GetMapping("/preferences")
    public UserDtos.UserPreferenceResponse preferences() {
        return userService.getPreferences(currentUser.id());
    }

    @PutMapping("/preferences")
    public UserDtos.UserPreferenceResponse updatePreferences(@Valid @RequestBody UserDtos.UpdatePreferenceRequest request) {
        return userService.updatePreferences(currentUser.id(), request);
    }

    @PutMapping("/preferences/themes")
    public UserDtos.UserPreferenceResponse updateThemes(@RequestBody UserDtos.UpdateThemePreferencesRequest request) {
        return userService.updateThemes(currentUser.id(), request);
    }

    @GetMapping("/saved-events")
    public List<EventDtos.EventResponse> savedEvents() {
        return userService.savedEvents(currentUser.id());
    }

    @PostMapping("/saved-events/{eventId}")
    public void saveEvent(@PathVariable String eventId) {
        userService.saveEvent(currentUser.id(), eventId);
    }

    @DeleteMapping("/saved-events/{eventId}")
    public void unsaveEvent(@PathVariable String eventId) {
        userService.unsaveEvent(currentUser.id(), eventId);
    }

    @GetMapping("/orders")
    public List<OrderDtos.OrderResponse> orders() {
        return userService.orders(currentUser.id());
    }

    @GetMapping("/groups")
    public List<GroupDtos.GroupResponse> groups() {
        return userService.myGroups(currentUser.id());
    }

    @GetMapping("/events")
    public List<EventDtos.EventResponse> events() {
        return userService.myEvents(currentUser.id());
    }
}
