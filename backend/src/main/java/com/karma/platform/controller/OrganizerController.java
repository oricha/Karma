package com.karma.platform.controller;

import com.karma.platform.common.CurrentUser;
import com.karma.platform.dto.OrganizerDtos;
import com.karma.platform.service.OrganizerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/organizers/me")
public class OrganizerController {

    private final OrganizerService organizerService;
    private final CurrentUser currentUser;

    public OrganizerController(OrganizerService organizerService, CurrentUser currentUser) {
        this.organizerService = organizerService;
        this.currentUser = currentUser;
    }

    @GetMapping("/dashboard")
    public OrganizerDtos.DashboardResponse dashboard() {
        return organizerService.dashboard(currentUser.id());
    }
}
