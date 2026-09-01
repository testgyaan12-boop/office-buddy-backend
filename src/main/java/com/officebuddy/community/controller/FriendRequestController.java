package com.officebuddy.community.controller;

import com.officebuddy.community.dto.CommunityCompanyDto;
import com.officebuddy.community.dto.DiscoverUserDto;
import com.officebuddy.community.dto.FriendDto;
import com.officebuddy.community.dto.FriendRequestDto;
import com.officebuddy.community.service.CommunityService;
import com.officebuddy.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
public class FriendRequestController {

    private final CommunityService communityService;

    @GetMapping("/requests/incoming")
    public ResponseEntity<List<FriendRequestDto>> getIncoming(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(communityService.getIncomingRequests(user.getId()));
    }

    @GetMapping("/requests/outgoing")
    public ResponseEntity<List<FriendRequestDto>> getOutgoing(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(communityService.getOutgoingRequests(user.getId()));
    }

    @PostMapping("/requests")
    public ResponseEntity<FriendRequestDto> sendRequest(Authentication auth, @RequestBody Map<String, String> body) {
        var user = (User) auth.getPrincipal();
        var receiverId = UUID.fromString(body.get("receiverId"));
        return ResponseEntity.ok(communityService.sendRequest(user.getId(), receiverId));
    }

    @PutMapping("/requests/{id}/accept")
    public ResponseEntity<FriendRequestDto> acceptRequest(Authentication auth, @PathVariable UUID id) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(communityService.acceptRequest(id, user.getId()));
    }

    @PutMapping("/requests/{id}/reject")
    public ResponseEntity<Void> rejectRequest(Authentication auth, @PathVariable UUID id) {
        var user = (User) auth.getPrincipal();
        communityService.rejectRequest(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/friends")
    public ResponseEntity<List<FriendDto>> getFriends(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(communityService.getFriends(user.getId()));
    }

    @GetMapping("/users")
    public ResponseEntity<List<DiscoverUserDto>> getDiscoverableUsers(Authentication auth) {
        var user = (User) auth.getPrincipal();
        return ResponseEntity.ok(communityService.getDiscoverableUsers(user.getId()));
    }

    @GetMapping("/users/{userId}/companies")
    public ResponseEntity<List<CommunityCompanyDto>> getUserCompanies(@PathVariable UUID userId) {
        return ResponseEntity.ok(communityService.getUserCompanies(userId));
    }
}
