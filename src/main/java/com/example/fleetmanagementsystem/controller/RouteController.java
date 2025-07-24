package com.example.fleetmanagementsystem.controller;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.DTO.RouteAssignmentDTO;
import com.example.fleetmanagementsystem.DTO.RouteUnassignmentDTO;
import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.repositories.MarshallRepository;
import com.example.fleetmanagementsystem.services.RouteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/routes")
public class RouteController {
    @Autowired
    private MarshallRepository marshallRepository;

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


    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<Route>>> getAllRoutes() {
        List<Route> routes = routeService.getAllRoutes();
        return ResponseEntity.ok(new ApiResponse<>(1, "Routes retrieved successfully", routes));
    }


    //get route by id for admin and marshall include standard response
    @PreAuthorize("hasAnyRole('MARSHALL', 'ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Route>> getRouteById(@PathVariable String id) {
        return routeService.getRouteById(Long.valueOf(id))
                .map(route -> ResponseEntity.ok(new ApiResponse<>(1, "Route retrieved successfully", route)))
                .orElse(ResponseEntity.notFound().build());
    }

    //create route for admin and marshall include standard response
     @PreAuthorize("hasAnyRole('ADMIN','MARSHALL')")
     @PostMapping
     public ResponseEntity<ApiResponse<Route>> createRoute(@Valid @RequestBody RouteDTO routeDTO) {
         Route route = new Route();
         route.setName(routeDTO.getName());
         route.setStartPoint(routeDTO.getStartPoint());
         route.setEndPoint(routeDTO.getEndPoint());
         Route savedRoute = routeService.saveRoute(route);
         return ResponseEntity.status(HttpStatus.CREATED)
                 .body(new ApiResponse<>(1, "Route created successfully", savedRoute));
    }



    @PreAuthorize("hasAnyRole('ADMIN','MARSHALL')")
    @PutMapping("/{id}")
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


    @PreAuthorize("hasAnyRole('ADMIN','MARSHALL')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteRoute(@PathVariable String id) {
        if (routeService.getRouteById(Long.valueOf(id)).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(0, "Route not found with ID: " + id, null));
        }
        routeService.deleteRoute(Long.valueOf(id));
        return ResponseEntity.ok(new ApiResponse<>(1, "Route deleted successfully", null));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/assign-marshall-to-route-stage")
    public ResponseEntity<ApiResponse<Route>> assignRouteToMarshall(@RequestBody RouteAssignmentDTO routeAssignmentDTO){
        if(routeService.getRouteById(routeAssignmentDTO.getRouteId()).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(0, "Route not found with ID: " + routeAssignmentDTO.getRouteId(), null));
        }

        if(marshallRepository.findById(routeAssignmentDTO.getMarshallId()).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(0, "Marshall not found with Id: " + routeAssignmentDTO.getMarshallId(), null));
        }

        String position = routeAssignmentDTO.getPosition().toLowerCase();
        if(!position.equals("start") && !position.equals("end")){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(0, "Invalid position. Allowed values are 'start' or 'end'.", null));
        }

        //Check if marshall is already assigned to the route
        if (routeService.isMarshallAssignedToRoute(routeAssignmentDTO.getRouteId(), routeAssignmentDTO.getMarshallId())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(0, "Marshall with ID "+ routeAssignmentDTO.getMarshallId() + " is already assigned to route with ID " + routeAssignmentDTO.getRouteId()) );
        }

        //check if route-stage is already assigned
        if (routeService.isStageAssignedToMarshall(routeAssignmentDTO.getRouteId())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(0, "Route with ID " + routeAssignmentDTO.getRouteId() + " is already assigned to a marshall."));
        }

        Route savedRoute = routeService.assignMarshallToRoute(
                routeAssignmentDTO.getRouteId(),
                routeAssignmentDTO.getMarshallId(),
                routeAssignmentDTO.getPosition().toLowerCase()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(1,
                        "Route with ID " + routeAssignmentDTO.getRouteId() + " assigned successfully to Marshall ID " + routeAssignmentDTO.getMarshallId(),
                        savedRoute));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/unassign-marshall-from-route")
    public ResponseEntity<ApiResponse<Object>> deleteMarshallFromRoute(@RequestBody RouteUnassignmentDTO routeUnassignmentDTO){

        if(marshallRepository.findById(routeUnassignmentDTO.getMarshallId()).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(0, "Marshall not found with Id: " + routeUnassignmentDTO.getMarshallId(), null));
        }


        Route savedRoute = routeService.unassignMarshallFromRoute(
                routeUnassignmentDTO.getMarshallId());

        return ResponseEntity.ok(new ApiResponse<>(1, "Marshall "+ routeUnassignmentDTO.getMarshallId() + " successfully unassigned", null));

    }

}