package ru.kakatya.deal.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kakatya.deal.entities.Application;
@Repository
public interface ApplicationRepo extends CrudRepository<Application, Long> {
}
