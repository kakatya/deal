package ru.kakatya.deal.repos;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.kakatya.deal.entities.Credit;
@Repository
public interface CreditRepo extends CrudRepository<Credit, Long> {
}
