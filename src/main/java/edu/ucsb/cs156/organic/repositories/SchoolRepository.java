package edu.ucsb.cs156.organic.repositories;

import edu.ucsb.cs156.organic.entities.School;
import edu.ucsb.cs156.organic.entities.UserEmail;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepository extends CrudRepository<School, String> {
    public Iterable<School> findAll();
    public Optional<School> findByAbbrev(String abbrev);
}
