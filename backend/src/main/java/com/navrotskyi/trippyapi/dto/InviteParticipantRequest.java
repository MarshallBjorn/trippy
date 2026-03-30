package com.navrotskyi.trippyapi.dto;

public class InviteParticipantRequest {
    private String userEmail;
    private String roleName;

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
}