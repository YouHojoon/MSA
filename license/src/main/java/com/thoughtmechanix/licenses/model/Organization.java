package com.thoughtmechanix.licenses.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class Organization implements Serializable {
    String id;
    String name;
    String contactName;
    String contactEmail;
    String contactPhone;
}
