/*
 * PROPRIETARY SOFTWARE - User Registration Application
 * 
 * Copyright (c) 2024 User Registration Team
 * All rights reserved.
 * 
 * This software is proprietary and confidential. Commercial use, redistribution, 
 * or modification requires explicit written permission from the copyright holder.
 * 
 * For licensing inquiries, please contact: rume@codemarks.com
 */

package com.codehacks.user_registration.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

/**
 * Root Controller
 * 
 * Provides the root endpoint for the User Registration API.
 * This controller handles requests to the base URL of the application.
 */
@RestController
public class RootController {
    
    /**
     * Root endpoint that returns a welcome message
     * 
     * @return ResponseEntity with welcome message
     */
    @GetMapping("/")
    public ResponseEntity<String> root() {
        return ResponseEntity.ok("Welcome to the User Registration API!");
    }
} 