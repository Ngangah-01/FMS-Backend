package com.example.fleetmanagementsystem.services;

import com.example.fleetmanagementsystem.DTO.ApiResponse;
import com.example.fleetmanagementsystem.model.Marshall;
import com.example.fleetmanagementsystem.model.Matatu;
import com.example.fleetmanagementsystem.model.Route;
import com.example.fleetmanagementsystem.repositories.MarshallRepository;
import com.example.fleetmanagementsystem.repositories.MatatuRepository;
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
    private final MatatuService matatuService;
    private final MatatuRepository matatuRepository;


    public RouteService(RouteRepository routeRepository,
                        MarshallService marshallService,
                        MarshallRepository marshallRepository,
                        MatatuService matatuService,
                        MatatuRepository matatuRepository) {
        this.routeRepository = routeRepository;
        this.marshallService = marshallService;
        this.marshallRepository = marshallRepository;
        this.matatuService=matatuService;
        this.matatuRepository=matatuRepository;
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

    public Optional<Route> getRouteByName(String name){
        return routeRepository.findByName(name);
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

    public List<Matatu> getMatatusInRoute(Long routeId){
        return matatuRepository.findByRoute_routeId(routeId);
    }

//    //assign matatu to route
//    public Route assignMatatuToRoute(Long routeId, String plateNumber){
//        Route route = routeRepository.findById(routeId)
//                .orElseThrow(() -> new EntityNotFoundException("Route not found with id " + routeId));
//
//        Matatu matatu = matatuRepository.findById(plateNumber)
//                .orElseThrow(() -> new EntityNotFoundException("Matatu not found with plate number " + plateNumber));
//
////        //check if matatu is already assigned to another route
////        if(!isMatatuAssigned(routeId, plateNumber)){
////            throw new IllegalArgumentException("Matatu " + plateNumber + " is already assigned to this route");
////        }
//
//        return routeRepository.save(route);
//    }
//
//    //is matatu assigned to route
//    public boolean isMatatuAssigned(Long routeId, String plateNumber){
//        Optional<Route> routeOptional = routeRepository.findById(routeId);
//        if(routeOptional.isEmpty()){
//            return false; //route not found
//        }
//
//        Route route = routeOptional.get();
//        Matatu matatu = route.getMatatu();
//
//        return matatu!=null;
//    }
//
//    //unassign matatu from route
//    public Route unassignMatatuFromRoute(String plateNumber){
//        Matatu matatu = matatuRepository.findById(plateNumber)
//                .orElseThrow(() -> new EntityNotFoundException("Matatu not found with plateNumber " + plateNumber));
//
//        Optional<Route> routeOptional = routeRepository.findByMatatu_PlateNumber(plateNumber);
//        if(routeOptional.isEmpty()){
//            throw new EntityNotFoundException("Matatu " + plateNumber + " is not assigned to any route");
//        }
//
//        Route route = routeOptional.get();
//
//        boolean unassigned = false;
//        if (route.getMatatu() != null && route.getMatatu().getPlateNumber().equals(plateNumber)){
//            route.setMatatu(null);
//            unassigned = true;
//        }
//
//        if (!unassigned) {
//            throw new IllegalArgumentException("Matatu " + plateNumber + " is not assigned to this route");
//        }
//
//        return routeRepository.save(route);
//    }

}