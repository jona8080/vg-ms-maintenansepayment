package pe.edu.vallegrande.maintenancePayment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import pe.edu.vallegrande.maintenancePayment.application.service.ExternalService;
import pe.edu.vallegrande.maintenancePayment.application.service.PaymentService;
import pe.edu.vallegrande.maintenancePayment.domain.dto.*;
import pe.edu.vallegrande.maintenancePayment.domain.model.PaymentModel;
import pe.edu.vallegrande.maintenancePayment.domain.repository.PaymentRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class PagoApplicationTests {

	@Mock
	private PaymentRepository paymentRepository;

	@Mock
	private ExternalService externalService;

	@InjectMocks
	private PaymentService paymentService;

	private static final String DATE_FORMAT = "dd-MMM-yyyy";

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void contextLoads() {
	}

	@Test
	void createPayment_Success() {
		// Arrange
		PaymentDto paymentDto = new PaymentDto();
		paymentDto.setAmount(new BigDecimal("50"));
		EnrollmentDto enrollmentDto = new EnrollmentDto();
		enrollmentDto.setId("67071153f265445896b1fe6e");

		ProfileDto profileDto = new ProfileDto("66fa22ca2b7ad126abc276db", "Artemio Dolorier Abregu", "A");
		AcademicPeriodDto academicPeriodDto = new AcademicPeriodDto("66fc3c507b00b7620a91d91a", "2021-I", "20-03-2021", "20-07-2021", "M", "A", "A");
		StudentDto studentDto = new StudentDto("66ff8f536f9f4e2f93aefd07", "78901234", "JULIA PATRICIA", "SANDOVAL", "MARTINEZ", "DNI", "juli.sandoval@gmail.com", "A");

		enrollmentDto.setProfile(profileDto);
		enrollmentDto.setAcademicPeriod(academicPeriodDto);
		enrollmentDto.setStudent(studentDto);

		paymentDto.setEnrollment(enrollmentDto);

		when(externalService.getEnrollmentById(anyString())).thenReturn(Mono.just(enrollmentDto));
		when(paymentRepository.save(any(PaymentModel.class))).thenAnswer(invocation -> {
			PaymentModel paymentModel = invocation.getArgument(0);
			paymentModel.setId("generatedId");
			return Mono.just(paymentModel);
		});

		// Act
		Mono<PaymentDto> result = paymentService.createPayment(paymentDto);

		// Assert
		PaymentDto createdPayment = result.block();
		assertNotNull(createdPayment);
		assertEquals("generatedId", createdPayment.getId());
		assertEquals("50.00", createdPayment.getAmount().toString());
		assertEquals("67071153f265445896b1fe6e", createdPayment.getEnrollment().getId());
		assertEquals("A", createdPayment.getStatus());
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		assertEquals(sdf.format(new Date()), createdPayment.getDate());

		verify(externalService).getEnrollmentById("67071153f265445896b1fe6e");
		verify(paymentRepository).save(any(PaymentModel.class));
	}

	@Test
	void createPayment_EnrollmentIdNull() {
		// Arrange
		PaymentDto paymentDto = new PaymentDto();
		paymentDto.setAmount(new BigDecimal("50"));
		paymentDto.setEnrollment(new EnrollmentDto());

		// Act
		Mono<PaymentDto> result = paymentService.createPayment(paymentDto);

		// Assert
		assertThrows(IllegalArgumentException.class, result::block);
		verify(externalService, never()).getEnrollmentById(anyString());
		verify(paymentRepository, never()).save(any(PaymentModel.class));
	}

	@Test
	void createPayment_ExternalServiceError() {
		// Arrange
		PaymentDto paymentDto = new PaymentDto();
		paymentDto.setAmount(new BigDecimal("50"));
		EnrollmentDto enrollmentDto = new EnrollmentDto();
		enrollmentDto.setId("67071153f265445896b1fe6e");
		paymentDto.setEnrollment(enrollmentDto);

		when(externalService.getEnrollmentById(anyString())).thenReturn(Mono.error(new RuntimeException("External service error")));

		// Act
		Mono<PaymentDto> result = paymentService.createPayment(paymentDto);

		// Assert
		assertThrows(RuntimeException.class, result::block);
		verify(externalService).getEnrollmentById("67071153f265445896b1fe6e");
		verify(paymentRepository, never()).save(any(PaymentModel.class));
	}

	@Test
	void updatePayment_Success() {
		// Arrange
		PaymentDto paymentDto = new PaymentDto();
		paymentDto.setId("existingId");
		paymentDto.setAmount(new BigDecimal("50"));
		EnrollmentDto enrollmentDto = new EnrollmentDto();
		enrollmentDto.setId("67071153f265445896b1fe6e");

		ProfileDto profileDto = new ProfileDto("66fa22ca2b7ad126abc276db", "Artemio Dolorier Abregu", "A");
		AcademicPeriodDto academicPeriodDto = new AcademicPeriodDto("66fc3c507b00b7620a91d91a", "2021-I", "20-03-2021", "20-07-2021", "M", "A", "A");
		StudentDto studentDto = new StudentDto("66ff8f536f9f4e2f93aefd07", "78901234", "JULIA PATRICIA", "SANDOVAL", "MARTINEZ", "DNI", "juli.sandoval@gmail.com", "A");

		enrollmentDto.setProfile(profileDto);
		enrollmentDto.setAcademicPeriod(academicPeriodDto);
		enrollmentDto.setStudent(studentDto);

		paymentDto.setEnrollment(enrollmentDto);

		PaymentModel existingPayment = new PaymentModel();
		existingPayment.setId("existingId");
		existingPayment.setAmount("30.00");
		existingPayment.setDate("12-Oct-2024");
		existingPayment.setEnrollmentId("67071153f265445896b1fe6e");
		existingPayment.setStatus("A");

		when(paymentRepository.findById("existingId")).thenReturn(Mono.just(existingPayment));
		when(externalService.getEnrollmentById(anyString())).thenReturn(Mono.just(enrollmentDto));
		when(paymentRepository.save(any(PaymentModel.class))).thenAnswer(invocation -> {
			PaymentModel paymentModel = invocation.getArgument(0);
			return Mono.just(paymentModel);
		});

		// Act
		Mono<PaymentDto> result = paymentService.updatePayment("existingId", paymentDto);

		// Assert
		PaymentDto updatedPayment = result.block();
		assertNotNull(updatedPayment);
		assertEquals("existingId", updatedPayment.getId());
		assertEquals("50.00", updatedPayment.getAmount().toString());
		assertEquals("67071153f265445896b1fe6e", updatedPayment.getEnrollment().getId());
		assertEquals("A", updatedPayment.getStatus());
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		assertEquals(sdf.format(new Date()), updatedPayment.getDate());

		verify(paymentRepository).findById("existingId");
		verify(externalService).getEnrollmentById("67071153f265445896b1fe6e");
		verify(paymentRepository).save(any(PaymentModel.class));
	}

	@Test
	void updatePayment_PaymentNotFound() {
		// Arrange
		PaymentDto paymentDto = new PaymentDto();
		paymentDto.setId("nonExistingId");
		paymentDto.setAmount(new BigDecimal("50"));
		EnrollmentDto enrollmentDto = new EnrollmentDto();
		enrollmentDto.setId("67071153f265445896b1fe6e");
		paymentDto.setEnrollment(enrollmentDto);

		when(paymentRepository.findById("nonExistingId")).thenReturn(Mono.empty());

		// Act
		Mono<PaymentDto> result = paymentService.updatePayment("nonExistingId", paymentDto);

		// Assert
		assertThrows(IllegalArgumentException.class, result::block);
		verify(paymentRepository).findById("nonExistingId");
		verify(externalService, never()).getEnrollmentById(anyString());
		verify(paymentRepository, never()).save(any(PaymentModel.class));
	}

	@Test
	void updatePayment_ExternalServiceError() {
		// Arrange
		PaymentDto paymentDto = new PaymentDto();
		paymentDto.setId("existingId");
		paymentDto.setAmount(new BigDecimal("50"));
		EnrollmentDto enrollmentDto = new EnrollmentDto();
		enrollmentDto.setId("67071153f265445896b1fe6e");

		ProfileDto profileDto = new ProfileDto("66fa22ca2b7ad126abc276db", "Artemio Dolorier Abregu", "A");
		AcademicPeriodDto academicPeriodDto = new AcademicPeriodDto("66fc3c507b00b7620a91d91a", "2021-I", "20-03-2021", "20-07-2021", "M", "A", "A");
		StudentDto studentDto = new StudentDto("66ff8f536f9f4e2f93aefd07", "78901234", "JULIA PATRICIA", "SANDOVAL", "MARTINEZ", "DNI", "juli.sandoval@gmail.com", "A");

		enrollmentDto.setProfile(profileDto);
		enrollmentDto.setAcademicPeriod(academicPeriodDto);
		enrollmentDto.setStudent(studentDto);

		paymentDto.setEnrollment(enrollmentDto);

		PaymentModel existingPayment = new PaymentModel();
		existingPayment.setId("existingId");
		existingPayment.setAmount("30.00");
		existingPayment.setDate("12-Oct-2024");
		existingPayment.setEnrollmentId("67071153f265445896b1fe6e");
		existingPayment.setStatus("A");

		when(paymentRepository.findById("existingId")).thenReturn(Mono.just(existingPayment));
		when(externalService.getEnrollmentById(anyString())).thenReturn(Mono.error(new RuntimeException("External service error")));

		// Act
		Mono<PaymentDto> result = paymentService.updatePayment("existingId", paymentDto);

		// Assert
		assertThrows(RuntimeException.class, result::block);
		verify(paymentRepository).findById("existingId");
		verify(externalService).getEnrollmentById("67071153f265445896b1fe6e");
		verify(paymentRepository, never()).save(any(PaymentModel.class));
	}
}