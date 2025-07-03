package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.repositories.RouteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RouteService {

    private final RouteRepository routeRepository;

    public RouteService(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    public Route saveRoute(Route route) {
        return routeRepository.save(route);
    }

    public Optional<Route> getRouteById(Long id) {
        return routeRepository.findById(id);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }
    //update route
    public Route updateRoute(Long id, Route routeDetails) {
        return routeRepository.findById(id)
                .map(route -> {
                    route.setName(routeDetails.getName());
                    route.setStartPoint(routeDetails.getStartPoint());
                    route.setEndPoint(routeDetails.getEndPoint());
                    return routeRepository.save(route);
                })
                .orElseThrow(() -> new RuntimeException("Route not found with id " + id));
    }

    public void deleteRoute(Long id) {
        routeRepository.deleteById(id);
    }
}