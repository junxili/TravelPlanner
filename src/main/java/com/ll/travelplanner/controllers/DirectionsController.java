package com.ll.travelplanner.controllers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.maps.DirectionsApi;
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

@Controller
@RequestMapping("/directions")
@Slf4j
public class DirectionsController {

    private static final String API_KEY = "AIzaSyAu6taXwDtoaIt*********";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public String enact(@RequestParam @NonNull final String source,
                        @RequestParam @NonNull final String destination) {
        final GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(API_KEY)
                .build();
        try {
            final DirectionsResult result = DirectionsApi.getDirections(context, source, destination).await();
            return gson.toJson(result);
        } catch (InterruptedException | IOException | ApiException e) {
            logger.error(e.getMessage());
        }

        return "nothing";
    }
}
