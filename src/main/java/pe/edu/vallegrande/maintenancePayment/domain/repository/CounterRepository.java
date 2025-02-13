package pe.edu.vallegrande.maintenancePayment.domain.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.maintenancePayment.domain.model.Counter;

public interface CounterRepository extends ReactiveMongoRepository<Counter, String> {
}