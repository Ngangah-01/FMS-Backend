package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.Marshall;
import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.repositories.MarshallRepository;
import com.example.fleetmanagementsystem.repositories.RouteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;
    private final MarshallService marshallService;
    private final MarshallRepository marshallRepository;

    public RouteService(RouteRepository routeRepository,
                        MarshallService marshallService,
                        MarshallRepository marshallRepository) {
        this.routeRepository = routeRepository;
        this.marshallService = marshallService;
        this.marshallRepository = marshallRepository;
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    public Optional<Route> getRouteById(Long routeId) {
        return routeRepository.findById(routeId);
    }


    //update route
    public Route updateRoute(Long routeId, Route routeDetails) {
        return routeRepository.findById(routeId)
                .map(route -> {
                    route.setName(routeDetails.getName());
                    route.setStartPoint(routeDetails.getStartPoint());
                    route.setEndPoint(routeDetails.getEndPoint());
                    return routeRepository.save(route);
                })
                .orElseThrow(() -> new RuntimeException("Route not found with id " + routeId));
    }

    public void deleteRoute(Long routeId) {
        routeRepository.deleteById(routeId);
    }

    //assign marshall to route
    public Route assignMarshallToRoute(Long routeId, Long marshallId, String position){
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id " + routeId));

        Marshall marshall = marshallRepository.findById(marshallId)
                .orElseThrow(() -> new EntityNotFoundException("Marshall not found with id " + marshallId));

        //check if marshall is already assigned to another route
        if (isMarshallAssignedToRoute(routeId, marshallId)) {
            throw new IllegalArgumentException("Marshall " + marshall.getMarshallId() + " is already assigned to this route");
        }

        //check if position is valid
        if(position.equals("start") && !isStageAssignedToMarshall(routeId)) {
            route.setStartMarshall(marshall);
        } else if (position.equals("end") && !isStageAssignedToMarshall(routeId)) {
            route.setEndMarshall(marshall);
        }else{
            throw new IllegalArgumentException("Invalid position. Use 'start' or 'end'.");
        }

        return routeRepository.save(route);
    }

    public boolean isMarshallAssignedToRoute(Long routeId, Long marshallId){
        Optional<Route> routeOptional = routeRepository.findById(routeId);
        if(routeOptional.isEmpty()){
            return false; //route not found
        }

        Route route = routeOptional.get();
        Marshall startMarshall = route.getStartMarshall();
        Marshall endMarshall = route.getEndMarshall();

        return (startMarshall != null && startMarshall.getMarshallId().equals(marshallId)) ||
                (endMarshall != null && endMarshall.getMarshallId().equals(marshallId));
    }

    public boolean isStageAssignedToMarshall(Long routeId){
        Route route = routeRepository.findById(routeId)
                .orElseThrow(() -> new EntityNotFoundException("Route not found with id " + routeId));

//        if(route.getEndMarshall()!=null){
//            throw new IllegalArgumentException("End marshall " + route.getEndMarshall() + " is already assigned to this route");
//        }
//        if (route.getStartMarshall()!=null){
//            throw new IllegalArgumentException("Start marshall "+ route.getStartMarshall() + " is already assigned to this route");
//        }

        return (route.getEndMarshall()!=null || route.getStartMarshall()!=null);
    }

    //unassign marshall from route
    public Route unassignMarshallFromRoute(Long marshallId){
        Marshall marshall = marshallRepository.findById(marshallId)
                .orElseThrow(() -> new EntityNotFoundException("Marshall not found with id " + marshallId));

        Optional<Route> routeOptional = routeRepository.findByStartMarshall_MarshallIdOrEndMarshall_MarshallId(marshallId, marshallId);
        if(routeOptional.isEmpty()){
            throw new EntityNotFoundException("Marshall " + marshallId + " is not assigned to any route");
        }

        Route route = routeOptional.get();

        boolean unassigned = false;
        if (route.getStartMarshall() != null && route.getStartMarshall().getMarshallId().equals(marshallId)){
            route.setStartMarshall(null);
            unassigned = true;
        }
        if (route.getEndMarshall() != null && route.getEndMarshall().getMarshallId().equals(marshallId)){
            route.setEndMarshall(null);
            unassigned = true;
        }

        if (!unassigned) {
            throw new IllegalArgumentException("Marshall " + marshallId + " is not assigned to this route");
        }

        return routeRepository.save(route);

//        //check if marshall is assigned to this route
//        if (!isMarshallAssignedToRoute(route.getRouteId(), marshallId)){
//            throw new IllegalArgumentException("Marshall " + marshallId + " is not assigned to this route");
//        }
//
//        if (route.getStartMarshall()!=null && route.getStartMarshall().equals(marshallId)){
//            route.setStartMarshall(null);
//        }
//        if (route.getEndMarshall()!=null && route.getEndMarshall().equals(marshallId)){
//            route.setEndMarshall(null);
//        }
//
//        return routeRepository.save(route);

    }

}