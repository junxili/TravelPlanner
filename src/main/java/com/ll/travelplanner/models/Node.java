package com.ll.travelplanner.models;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Node {
    private String location;
}