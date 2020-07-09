package com.ll.travelplanner.controllers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.TravelMode;
import com.ll.travelplanner.models.Edge;
import com.ll.travelplanner.models.Node;
import com.ll.travelplanner.utils.KruskalUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.SetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class DirectionsController {

    private static final String API_KEY = "AIzaSyCeDlUN-ORh6RWA3Aw5kwcsOl_aNA7JcOU";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final GeoApiContext geoContext = new GeoApiContext.Builder()
            .apiKey(API_KEY)
            .build();

    private final List<String> modes = Lists.newArrayList("BICYCLING", "DRIVING", "TRANSIT", "WALKING");

    @RequestMapping(value = "/direction", method = RequestMethod.GET)
    public ResponseEntity<DirectionsResult> getDirection(@RequestParam @NonNull final String origin,
                                                         @RequestParam @NonNull final String destination,
                                                         @RequestParam(required = false, defaultValue = "DRIVING") final String mode) {

        if (!modes.contains(mode)) {
            throw new IllegalArgumentException();
        }

        DirectionsResult result = new DirectionsResult();
        try {
            result = DirectionsApi.getDirections(geoContext, origin, destination).mode(TravelMode.valueOf(mode)).await();
        } catch (final ApiException | IOException | InterruptedException e) {
            logger.error("In getDirection" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/directions", method = RequestMethod.GET)
    public ResponseEntity<DirectionsResult> getDirections(
            @RequestParam @NonNull final String[] wayPoints,
            @RequestParam(required = false, defaultValue = "DRIVING") final String mode) {

        List<String> stops = Lists.newArrayList(wayPoints);
        if (stops.size() < 2 || !modes.contains(mode)) {
            throw new IllegalArgumentException();
        }

        final String origin = stops.get(0);
        final String destination = stops.get(stops.size() - 1);
        stops = stops.subList(1, stops.size() - 1);

        final DirectionsApiRequest request = new DirectionsApiRequest(geoContext);
        final List<DirectionsApiRequest.Waypoint> wayPointList = stops
                .stream()
                .map(DirectionsApiRequest.Waypoint::new)
                .collect(Collectors.toList());

        DirectionsResult result = new DirectionsResult();
        try {
            result = request
                    .origin(origin)
                    .destination(destination)
                    .mode(TravelMode.valueOf(mode))
                    .waypoints(wayPointList.toArray(new DirectionsApiRequest.Waypoint[0]))
                    .await();
        } catch (final ApiException | IOException | InterruptedException e) {
            logger.error("In getDirections" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/matrix", method = RequestMethod.GET)
    public ResponseEntity<DistanceMatrix> getDirectionMatrixSingleMode(
            @RequestParam @NonNull final String[] origins,
            @RequestParam @NonNull final String[] destinations,
            @RequestParam(required = false, defaultValue = "DRIVING") final String mode) {

        if (origins.length == 0 || destinations.length == 0 || !modes.contains(mode)) {
            throw new IllegalArgumentException();
        }


        final DistanceMatrixApiRequest request = DistanceMatrixApi
                .getDistanceMatrix(geoContext, origins, destinations)
                .mode(TravelMode.valueOf(mode));

        DistanceMatrix result = null;
        try {
            result = request.await();

        } catch (final ApiException | IOException | InterruptedException e) {
            logger.error("In getDirectionMatrixSingleMode" + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/matrices", method = RequestMethod.GET)
    public ResponseEntity<List<DistanceMatrix>> getDirectionMatrixAllModes(
            @RequestParam @NonNull final String[] origins,
            @RequestParam @NonNull final String[] destinations) {

        if (origins.length == 0 || destinations.length == 0) {
            throw new IllegalArgumentException();
        }

        final List<DistanceMatrixApiRequest> requests = modes.stream()
                .map(mode -> DistanceMatrixApi
                        .getDistanceMatrix(geoContext, origins, destinations)
                        .mode(TravelMode.valueOf(mode)))
                .collect(Collectors.toList());

        final List<DistanceMatrix> results = requests
                .stream()
                .map(request -> {
                    DistanceMatrix result = null;
                    try {
                        result = request.await();

                    } catch (final ApiException | IOException | InterruptedException e) {
                        logger.error("In getDirectionMatrixAllModes" + e.getMessage());
                    }
                    return result;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(results);
    }

//    @RequestMapping(value = "/recommendDummy", method = RequestMethod.GET)
//    public ResponseEntity<Set<Edge>> getRecommendation(@RequestParam @NonNull final String[] input) {
//        if (input.length < 2) {
//            throw new IllegalArgumentException();
//        }
//
//        final Set<String> locations = Sets.newHashSet(input);
//        final List<DistanceMatrixApiRequest> apiRequests = locations.stream()
//                .map(location -> generateMatrixRequest(location,
//                        ImmutableList.copyOf(SetUtils.difference(locations, ImmutableSet.of(location)).toSet())))
//                .collect(Collectors.toList());
//        final List<DistanceMatrix> distanceMatrices = apiRequests.stream()
//                .map(this::getMatrixWrapper)
//                .collect(Collectors.toList());
//        final Queue<Edge> edgesSortedByDistance = new PriorityQueue<>(Comparator.comparingLong(Edge::getDistance));
//        final Map<Node, List<Edge>> graph = new HashMap<>();
//
//        for (DistanceMatrix distanceMatrix : distanceMatrices) {
//            for (int i = 0; i < distanceMatrix.originAddresses.length; i++) {
//                for (int j = 0; j < distanceMatrix.destinationAddresses.length; j++) {
//                    final String origin = distanceMatrix.originAddresses[i];
//                    final String destination = distanceMatrix.destinationAddresses[j];
//                    final DistanceMatrixElement distanceMatrixElement = distanceMatrix.rows[i].elements[j];
//
//                    final Node originNode = Node.builder()
//                            .location(origin)
//                            .build();
//                    final Node destinationNode = Node.builder()
//                            .location(destination)
//                            .build();
//                    final Edge edge = Edge.builder()
//                            .origin(originNode)
//                            .destination(destinationNode)
//                            .distance(distanceMatrixElement.distance.inMeters)
//                            .duration(distanceMatrixElement.duration.inSeconds)
//                            .build();
//
//                    edgesSortedByDistance.add(edge);
//                    final List<Edge> edges = graph.getOrDefault(originNode, new ArrayList<>());
//                    edges.add(edge);
//                    graph.putIfAbsent(originNode, edges);
//                }
//            }
//        }
//
//        return ResponseEntity.ok(KruskalUtils.getOptimalRoutes(edgesSortedByDistance, graph));
//    }

    @RequestMapping(value="/recommend", method = RequestMethod.GET)
    public ResponseEntity<?> getRecommendation(@RequestParam @NonNull final String[] input) {
        if (input.length < 2) {
            throw new IllegalArgumentException();
        }

        final Set<String> locations = Sets.newHashSet(input);
        final List<DistanceMatrixApiRequest> apiRequests = locations.stream()
                .map(location -> generateMatrixRequest(location,
                        ImmutableList.copyOf(SetUtils.difference(locations, ImmutableSet.of(location)).toSet())))
                .collect(Collectors.toList());
        final List<DistanceMatrix> distanceMatrices = apiRequests.stream()
                .map(this::getMatrixWrapper)
                .collect(Collectors.toList());

        final Map<Node, List<Edge>> graph = new HashMap<>();
        for (final DistanceMatrix distanceMatrix : distanceMatrices) {
            for (int i = 0; i < distanceMatrix.originAddresses.length; i++) {
                for (int j = 0; j < distanceMatrix.destinationAddresses.length; j++) {
                    final String origin = distanceMatrix.originAddresses[i];
                    final String destination = distanceMatrix.destinationAddresses[j];
                    final DistanceMatrixElement distanceMatrixElement = distanceMatrix.rows[i].elements[j];

                    final Node originNode = Node.builder()
                            .location(origin)
                            .build();
                    final Node destinationNode = Node.builder()
                            .location(destination)
                            .build();
                    final Edge edge = Edge.builder()
                            .origin(originNode)
                            .destination(destinationNode)
                            .distance(distanceMatrixElement.distance.inMeters)
                            .duration(distanceMatrixElement.duration.inSeconds)
                            .build();

                    final List<Edge> edges = graph.getOrDefault(originNode, new ArrayList<>());
                    edges.add(edge);
                    graph.putIfAbsent(originNode, edges);
                }
            }
        }

        return null;
    }

    private DistanceMatrixApiRequest generateMatrixRequest(@NonNull final String origin,
                                                           @NonNull final List<String> destinations) {
        return DistanceMatrixApi
                .getDistanceMatrix(geoContext, new String[]{origin}, destinations.toArray(new String[0]))
                .mode(TravelMode.valueOf("DRIVING"));
    }

    private DistanceMatrix getMatrixWrapper(@NonNull final DistanceMatrixApiRequest request) {
        DistanceMatrix distanceMatrix = null;
        try {
            distanceMatrix = request.await();
        } catch (final ApiException | IOException | InterruptedException e) {
            logger.error("In getDirectionMatrixAllModes" + e.getMessage());
        }

        return distanceMatrix;
    }
}
