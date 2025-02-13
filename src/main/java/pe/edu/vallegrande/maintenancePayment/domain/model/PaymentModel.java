package pe.edu.vallegrande.maintenancePayment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Document(collection = "maintenance_payment")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentModel {
    @Id
    private String id;
    private String amount;
    private String date;
    private String enrollmentId;
    private int correlative;
    private String status;
}