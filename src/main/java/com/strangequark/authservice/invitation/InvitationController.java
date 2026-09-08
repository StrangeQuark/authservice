package com.strangequark.authservice.invitation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/invitation")
public class InvitationController {
    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createInvitation(@RequestBody InvitationRequest invitationRequest) {
        return invitationService.createInvitation(invitationRequest);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteInvitation(@RequestBody InvitationRequest invitationRequest) {
        return invitationService.deleteInvitation(invitationRequest.getId());
    }

    @DeleteMapping("/delete-all")
    public ResponseEntity<?> deleteAllInvitations() {
        return invitationService.deleteAllInvitations();
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllInvitations() {
        return invitationService.getAllInvitations();
    }

    @GetMapping("/invite-only")
    public ResponseEntity<?> getInviteOnly() {
        return invitationService.getInviteOnly();
    }
}
