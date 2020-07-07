package com.ll.travelplanner.models;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Edge {
    private Node origin;
    private Node destination;
    private Long duration;
    private Long distance;
}