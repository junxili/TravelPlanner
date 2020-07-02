package com.ll.travelplanner.controllers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.DistanceMatrixApi;
import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.TravelMode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class DirectionsController {

    private static final String API_KEY = "AIzaSyBKxQf4qFTT0kwKaxCRonZQ0kFEl33itSI";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final GeoApiContext geoContext = new GeoApiContext.Builder()
            .apiKey(API_KEY)
            .build();

    private final List<String> modes = Lists.newArrayList("BICYCLING", "DRIVING", "TRANSIT", "UNKNOWN", "WALKING");

    @RequestMapping(value = "/direction", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getDirection(@RequestParam @NonNull final String origin,
                                       @RequestParam @NonNull final String destination,
                                       @RequestParam(required = false, defaultValue = "DRIVING") final String mode) {

        if (!modes.contains(mode)) {
            throw new IllegalArgumentException();
        }

        DirectionsResult result = new DirectionsResult();
        try {
            result = DirectionsApi.getDirections(geoContext, origin, destination).mode(TravelMode.valueOf(mode)).await();
            return ResponseEntity.ok(gson.toJson(result));
        } catch (final InterruptedException | IOException | ApiException e) {
            logger.error("In getDirection" + e.getMessage());
        }

        return ResponseEntity.ok(gson.toJson(result));
    }

    @RequestMapping(value = "/directions", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getDirections(@RequestParam @NonNull final String[] wayPoints,
                                @RequestParam(required = false, defaultValue = "DRIVING") final String mode) {

        List<String> stops = Lists.newArrayList(wayPoints);
        if (stops.size() < 2 || !modes.contains(mode)) {
            throw new IllegalArgumentException();
        }

        final String origin = stops.get(0);
        final String destination = stops.get(stops.size()-1);
        stops = stops.subList(1, stops.size()-1);

        final DirectionsApiRequest request = new DirectionsApiRequest(geoContext);
        final List<DirectionsApiRequest.Waypoint> wayPointList = stops
                .stream()
                .map(DirectionsApiRequest.Waypoint::new)
                .collect(Collectors.toList());

        DirectionsResult result = new DirectionsResult();
        try{
            result = request
                    .origin(origin)
                    .destination(destination)
                    .mode(TravelMode.valueOf(mode))
                    .waypoints(wayPointList.toArray(new DirectionsApiRequest.Waypoint[0]))
                    .await();
        } catch(final InterruptedException | IOException | ApiException e) {
            logger.error("In getDirections" + e.getMessage());
        }
        return ResponseEntity.ok(gson.toJson(result));
    }

    @RequestMapping(value = "/matrix", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getDirectionMatrix(@RequestParam @NonNull final String[] origins,
                                     @RequestParam @NonNull final String[] destinations,
                                     @RequestParam(required = false, defaultValue = "DRIVING") final String mode) {

        if (origins.length == 0 || destinations.length == 0 || (mode != null && !modes.contains(mode))) {
            throw new IllegalArgumentException();
        }


        final DistanceMatrixApiRequest request = DistanceMatrixApi
                                                .getDistanceMatrix(geoContext, origins, destinations)
                                                .mode(TravelMode.valueOf(mode));

        DistanceMatrix result = null;
        try{
            result = request.await();

        } catch(final InterruptedException | IOException | ApiException e) {
            logger.error("In getDirectionMatrix" + e.getMessage());
        }
        return ResponseEntity.ok(gson.toJson(result));
    }

}
