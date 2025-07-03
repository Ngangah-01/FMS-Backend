package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.services.RouteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @Data
    public static class RouteDTO {
        @NotBlank
        private String name;
        @NotBlank
        private String startPoint;
        @NotBlank
        private String endPoint;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    public ResponseEntity<List<Route>> getAllRoutes() {
        return ResponseEntity.ok(routeService.getAllRoutes());
    }

    //get route by id for admin and marshall include standard response
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    public ResponseEntity<ApiResponse<Route>> getRouteById(@PathVariable String id) {
        return routeService.getRouteById(Long.valueOf(id))
                .map(route -> ResponseEntity.ok(new ApiResponse<>(1, "Route retrieved successfully", route)))
                .orElse(ResponseEntity.notFound().build());
    }

    //create route for admin and marshall include standard response
     @PostMapping
     @PreAuthorize("hasAnyRole('ADMIN','MARSHALL')")
     public ResponseEntity<ApiResponse<Route>> createRoute(@Valid @RequestBody RouteDTO routeDTO) {
         Route route = new Route();
         route.setName(routeDTO.getName());
         route.setStartPoint(routeDTO.getStartPoint());
         route.setEndPoint(routeDTO.getEndPoint());
         Route savedRoute = routeService.saveRoute(route);
         return ResponseEntity.status(HttpStatus.CREATED)
                 .body(new ApiResponse<>(1, "Route created successfully", savedRoute));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MARSHALL')")
    public ResponseEntity<ApiResponse<Route>> updateRoute(@PathVariable String id,
                                                          @Valid @RequestBody RouteDTO routeDTO) {
        if (routeService.getRouteById(Long.valueOf(id)).isEmpty()) {
            //api response standard response for not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(0, "Route not found with ID: " + id, null));
        }
        Route route = new Route();
        route.setName(routeDTO.getName());
        route.setStartPoint(routeDTO.getStartPoint());
        route.setEndPoint(routeDTO.getEndPoint());
        Route updatedRoute = routeService.updateRoute(Long.valueOf(id), route);
        return ResponseEntity.ok(new ApiResponse<>(1, "Route updated successfully", updatedRoute));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MARSHALL')")
    public ResponseEntity<ApiResponse<Object>> deleteRoute(@PathVariable String id) {
        if (routeService.getRouteById(Long.valueOf(id)).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(0, "Route not found with ID: " + id, null));
        }
        routeService.deleteRoute(Long.valueOf(id));
        return ResponseEntity.ok(new ApiResponse<>(1, "Route deleted successfully", null));
    }
}