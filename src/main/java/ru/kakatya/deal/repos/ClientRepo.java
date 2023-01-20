package ru.kakatya.deal.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kakatya.deal.entities.Client;

@Repository
public interface ClientRepo extends CrudRepository<Client,Long> {
}
