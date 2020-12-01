package com.thoughtmechanix.zuulserver.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbtestingRoute {
    String serviceName;
    String active;
    String endpoint;
    int weight;
}
