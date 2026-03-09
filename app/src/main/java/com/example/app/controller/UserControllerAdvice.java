package com.example.app.controller;

import com.example.model.User;
import com.example.app.service.security.SpringUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class UserControllerAdvice {
    @ModelAttribute("currentUser")
    public User getUser(@AuthenticationPrincipal SpringUser springUser) {
        System.out.println("Advice method called");
        if(springUser == null) {
            return null;
        }
        return springUser.getUser();
    }
}