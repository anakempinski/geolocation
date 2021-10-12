package com.example.demo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection = "distances")
public class DistanceBetween{
    @Id
    public String documentId;
    public String source;
    public String destination;
    public double distance;
    public long hits;


    public DistanceBetween(String source, String destination, double distance, long hits){
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.hits = hits;
    }



}
