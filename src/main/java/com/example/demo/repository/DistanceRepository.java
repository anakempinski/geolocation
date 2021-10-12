package com.example.demo.repository;

import com.example.demo.model.DistanceBetween;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DistanceRepository {

    @Autowired
    private MongoTemplate template;

    public DistanceBetween checkDistance(String source, String destination){
        Query query = new Query();
        Criteria criteria1 = Criteria.where("source").is(source).and("destination").is(destination);
        Criteria criteria2 = Criteria.where("source").is(destination).and("destination").is(source);
        query.addCriteria(new Criteria().orOperator(criteria1, criteria2));

        List<DistanceBetween> res = template.find(query, DistanceBetween.class);
        return res.get(0);
    }

    public void save(DistanceBetween dist) {
        template.save(dist);
    }

    public void insert(DistanceBetween distanceBetween) {
        template.insert(distanceBetween);
    }

    public List<DistanceBetween> findAll() {
        return template.findAll(DistanceBetween.class);
    }
}
