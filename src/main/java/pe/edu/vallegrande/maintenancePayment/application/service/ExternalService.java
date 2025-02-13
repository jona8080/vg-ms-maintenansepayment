package pe.edu.vallegrande.maintenancePayment.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.maintenancePayment.domain.dto.AcademicPeriodDto;
import pe.edu.vallegrande.maintenancePayment.domain.dto.EnrollmentDto;
import pe.edu.vallegrande.maintenancePayment.domain.dto.ProfileDto;
import pe.edu.vallegrande.maintenancePayment.domain.dto.StudentDto;
import reactor.core.publisher.Mono;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

@Service
public class ExternalService {

    private final WebClient.Builder webClientBuilder;
    private static final Logger log = LoggerFactory.getLogger(ExternalService.class);

    @Autowired
    public ExternalService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<EnrollmentDto> getEnrollmentById(String enrollmentId) {
        String url = "https://vg-ms-enrollment-production-4fb3.up.railway.app/management/api/v1/enrollment/" + enrollmentId;
        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(EnrollmentDto.class)
                .flatMap(enrollmentDto -> {
                    if (enrollmentDto.getStudentId() == null || enrollmentDto.getProfileId() == null || enrollmentDto.getAcademicPeriodId() == null) {
                        return Mono.error(new IllegalArgumentException("Enrollment data is incomplete"));
                    }

                    return Mono.zip(
                            getStudentById(enrollmentDto.getStudentId()),
                            getProfileById(enrollmentDto.getProfileId()),
                            getAcademicPeriodById(enrollmentDto.getAcademicPeriodId())
                    ).map(tuple -> {
                        StudentDto studentModel = tuple.getT1();
                        ProfileDto profileModel = tuple.getT2();
                        AcademicPeriodDto academicPeriodModel = tuple.getT3();

                        if (studentModel != null) {
                            enrollmentDto.setStudent(studentModel);
                        }

                        if (profileModel != null) {
                            enrollmentDto.setProfile(profileModel);
                        }

                        if (academicPeriodModel != null) {
                            enrollmentDto.setAcademicPeriod(academicPeriodModel);
                        }

                        return enrollmentDto;
                    });
                })
                .doOnError(error -> log.error("Error while fetching enrollment or student data: " + error.getMessage()));
    }

    public Mono<StudentDto> getStudentById(String studentId) {
        return fetchData("https://ms-vg-student-production.up.railway.app/management/api/v1/student/list/", studentId, StudentDto.class);
    }

    public Mono<ProfileDto> getProfileById(String profileId) {
        return fetchData("https://vg-ms-profile-production.up.railway.app/management/api/v1/profile/list/", profileId, ProfileDto.class);
    }

    public Mono<AcademicPeriodDto> getAcademicPeriodById(String academicPeriodId) {
        return fetchData("https://academicperiod-production.up.railway.app/management/api/v1/academic_period/id/", academicPeriodId, AcademicPeriodDto.class);
    }

    private <T> Mono<T> fetchData(String baseUrl, String id, Class<T> responseType) {
        return webClientBuilder.build()
                .get()
                .uri(baseUrl + id)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        clientResponse -> {
                            log.error("Error fetching data from " + baseUrl + id + ": " + clientResponse.statusCode());
                            return clientResponse.createException().flatMap(Mono::error);
                        })
                .bodyToMono(responseType)
                .timeout(Duration.ofSeconds(5))
                .doOnNext(response -> log.info("Response from " + baseUrl + ": " + response))
                .onErrorResume(TimeoutException.class, e -> {
                    log.error("Timeout fetching data from " + baseUrl + id, e);
                    return Mono.empty();
                })
                .onErrorResume(error -> {
                    log.error("Error fetching data from " + baseUrl + id, error);
                    return Mono.empty();
                });
    }
}
