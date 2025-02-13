package pe.edu.vallegrande.maintenancePayment.domain.repository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.maintenancePayment.domain.model.PaymentModel;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentRepository extends ReactiveMongoRepository<PaymentModel, String> {
    Flux<PaymentModel> findByStatus(String status);
}