package com.ll.travelplanner.controllers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final String API_KEY = "";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final GeoApiContext geoContext = new GeoApiContext.Builder()
            .apiKey(API_KEY)
            .build();

    @ResponseBody
    @RequestMapping(value = "/direction", method = RequestMethod.GET, produces = "application/json")
    public String getDirection(@RequestParam @NonNull final String source,
                               @RequestParam @NonNull final String destination) {

        try {
            final DirectionsResult result = DirectionsApi.getDirections(geoContext, source, destination).await();
            return gson.toJson(result);
        } catch (final InterruptedException | IOException | ApiException e) {
            logger.error("In getDireciton" + e.getMessage());
        }

        return "nothing";
    }

    @RequestMapping(value = "/directions", method = RequestMethod.GET, produces = "application/json")
    public String getDirections(@RequestParam @NonNull final String wayPointString) {
        final List<String> wayPoints = Lists.newArrayList(wayPointString.split(","));

        if (wayPoints.size() < 2) {
            throw new IllegalArgumentException();
        }

        final String origin = wayPoints.get(0);
        final String destination = wayPoints.get(wayPoints.size()-1);

        final DirectionsApiRequest request = new DirectionsApiRequest(geoContext);
        final List<DirectionsApiRequest.Waypoint> wayPointList = wayPoints
                .stream()
                .map(DirectionsApiRequest.Waypoint::new)
                .collect(Collectors.toList());

        try{
            final DirectionsResult result = request
                    .origin(origin)
                    .destination(destination)
                    .waypoints(wayPointList.toArray(new DirectionsApiRequest.Waypoint[0]))
                    .await();
            logger.info("{}", result);
            return gson.toJson(result);
        } catch(final InterruptedException | IOException | ApiException e) {
            logger.error("In getDirections" + e.getMessage());
        }
        return "nothing";
    }

}
