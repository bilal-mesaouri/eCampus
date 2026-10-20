package com.example.eCampus.Admin;

import com.example.eCampus.User.User;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
public class Admin extends User {

    @Min(1)
    @Max(3)
    private int privilegeLevel;

    public Admin() {
    }

    public Admin(Long id, String login, String hashPassword, int privilegeLevel) {
        super(id, login, hashPassword);
        setPrivilegeLevel(privilegeLevel);
    }

    public int getPrivilegeLevel() {
        return privilegeLevel;
    }

    public void setPrivilegeLevel(int privilegeLevel) {
        if (privilegeLevel < 1 || privilegeLevel > 3) {
            throw new IllegalArgumentException("Privilege level must be between 1 and 3");
        }
        this.privilegeLevel = privilegeLevel;
    }
}