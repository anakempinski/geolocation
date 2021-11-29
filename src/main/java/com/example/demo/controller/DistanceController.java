package com.example.demo.controller;

import com.example.demo.model.DistanceBetween;
import com.example.demo.repository.DistanceRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@RestController
public class DistanceController {

   @Autowired
   DistanceRepository repository;


    @GetMapping("/hello")
    public ResponseEntity<Object> connectionCheck(){
        return new ResponseEntity<>(HttpStatus.OK);
    }


    @GetMapping("/distance")
    public ResponseEntity<Map<String, Object>> getDistanceBetween(@RequestParam String source,
                                                                  @RequestParam String destination) {

            DistanceBetween dist = repository.checkDistance(source, destination);
            if (dist != null) {
                dist.hits = dist.hits + 1;
                repository.save(dist);
                return new ResponseEntity<>(Map.of("distance", dist.distance), HttpStatus.OK);
            }


        String noWhitespaceSource = source.replaceAll("\\s+", "+");
        String noWhitespaceDestination = destination.replaceAll("\\s+", "+");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + noWhitespaceSource
                        + "&destinations=" + noWhitespaceDestination + "&key=AIzaSyCbjnMkud3md4--ZLo39H7tcR7Idzeifj0"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            JSONObject json = new JSONObject(response.body());
            double distance = (double)(json.getJSONArray("rows").getJSONObject(0).getJSONArray("elements").
                    getJSONObject(0).getJSONObject("distance").getInt("value")) / 1000;


            if(health().getStatusCode() == HttpStatus.OK) {
                repository.insert(new DistanceBetween(source, destination, distance, 1));
            }

            return new ResponseEntity<>(Map.of("distance", distance), HttpStatus.OK);

        } catch(Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8082/actuator/health"))
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            JSONObject json = new JSONObject(response.body());
            String status = json.getJSONObject("components").getJSONObject("mongo").getString("status");
            if(status.equals("UP")) {
                return new ResponseEntity<>(HttpStatus.OK);
            } else {
                return new ResponseEntity<>("No connection to DB", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch(Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping("/popularsearch")
    public ResponseEntity<Object> getMostPopularSearch(){
        long maxHits = Long.MIN_VALUE;
        DistanceBetween maxHitsDistance = null;

        List<DistanceBetween> distances = repository.findAll();
        if(distances.isEmpty()){
            return new ResponseEntity<>("DB is empty", HttpStatus.OK);
        }

        for (DistanceBetween dist : distances) {
            if (dist.hits > maxHits) {
                maxHits = dist.hits;
                maxHitsDistance = dist;
            }
        }
        return new ResponseEntity<>(Map.of("source", maxHitsDistance.source, "destination", maxHitsDistance.destination, "hits", maxHitsDistance.hits), HttpStatus.OK);
    }





    @PostMapping(value = "/distance", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Map<String, Object>> insert(@RequestBody DistanceBetween distanceBetween) {  // object that we want to insert
        DistanceBetween dist = repository.checkDistance(distanceBetween.source, distanceBetween.destination);
        if(dist == null){
            repository.insert(new DistanceBetween(distanceBetween.source, distanceBetween.destination, distanceBetween.distance, 0));

            return new ResponseEntity<>(Map.of("source", distanceBetween.source, "destination",
                    distanceBetween.destination, "hits", 0), HttpStatus.CREATED);
        }

        dist.distance = distanceBetween.distance;
        repository.save(dist);

        return new ResponseEntity<>(Map.of("source", dist.source, "destination",
                dist.destination, "hits", dist.hits), HttpStatus.CREATED);
    }

}


