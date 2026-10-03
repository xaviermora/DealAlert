package com.games_price_tracker.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.games_price_tracker.api.core.response.ApiResponseBody;
import com.games_price_tracker.api.core.response.ApiResponseBodyBuilder;
import com.games_price_tracker.api.tracking.enqueue_games.enums.CancelEnqueueResult;
import com.games_price_tracker.api.tracking.enqueue_games.enums.StartEnqueueResult;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/admin")
public class AdminController {
    private final AdminService adminService;

    AdminController(AdminService adminService){
        this.adminService = adminService;
    }

    @PostMapping("/save-games")
    public ResponseEntity<Void> syncGames(@RequestParam(name = "max_games") @Min(1) @Max(10000) Integer maxGames) {
        adminService.saveAppList(maxGames);

        return ResponseEntity.accepted().build();
    }   

    @PostMapping("/cancel-tracking-enqueue")
    public ResponseEntity<ApiResponseBody<Void>> cancelTracking() {
        CancelEnqueueResult result = adminService.cancelTrackingEnqueue();
                
        return result == CancelEnqueueResult.CANCELED 
        ? ResponseEntity.noContent().build() 
        : ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseBodyBuilder.error(result));
    }
    
    @PostMapping("/start-tracking-enqueue")
    public ResponseEntity<ApiResponseBody<Void>> startTracking(@RequestParam @Min(1) int gamesPerRequest) {
        StartEnqueueResult result = adminService.startTracking(gamesPerRequest);
        
        return result == StartEnqueueResult.STARTED 
        ? ResponseEntity.noContent().build() 
        : ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponseBodyBuilder.error(result));
    }
}
