package pe.edu.vallegrande.maintenancePayment.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pe.edu.vallegrande.maintenancePayment.domain.dto.PaymentDto;
import pe.edu.vallegrande.maintenancePayment.application.service.PaymentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/management/${api.version}/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public Mono<ResponseEntity<PaymentDto>> createPayment(@RequestBody PaymentDto paymentDto) {
        return paymentService.createPayment(paymentDto)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(null)))  // Personalizar mensaje aquí si es necesario
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<PaymentDto>> updatePayment(@PathVariable String id, @RequestBody PaymentDto paymentDto) {
        return paymentService.updatePayment(id, paymentDto)
                .map(ResponseEntity::ok)
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.badRequest().body(null));
                    }
                    return Mono.error(e);
                });
    }

    @GetMapping("/active")
    public Flux<PaymentDto> listActivePayments() {
        return paymentService.listActivePayments();
    }

    @GetMapping("/inactive")
    public Flux<PaymentDto> listInactivePayments() {
        return paymentService.listInactivePayments();
    }

    @PutMapping("/activate/{id}")
    public Mono<PaymentDto> activatePayment(@PathVariable String id) {
        return paymentService.activatePayment(id);
    }

    @PutMapping("/inactivate/{id}")
    public Mono<PaymentDto> deactivatePayment(@PathVariable String id) {
        return paymentService.deactivatePayment(id);
    }

    @DeleteMapping("/delete/{id}")
    public Mono<Void> deleteInactivePayment(@PathVariable String id) {
        return paymentService.deleteInactivePayment(id)
                .onErrorResume(e -> {
                    if (e instanceof IllegalStateException) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                    }
                    return Mono.error(e);
                });
    }

    @GetMapping("/id/{id}")
    public Mono<ResponseEntity<PaymentDto>> getPaymentById(@PathVariable String id) {
        return paymentService.findPaymentById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}