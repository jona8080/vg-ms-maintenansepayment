package pe.edu.vallegrande.maintenancePayment.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto {
    private String id;
    private String profileId;
    private String academicPeriodId;
    private String studentId;
    private String status;

    private ProfileDto profile;
    private AcademicPeriodDto academicPeriod;
    private StudentDto student;

    @Override
    public String toString() {
        return "EnrollmentDto{" +
                "id='" + id + '\'' +
                ", profileId='" + profileId + '\'' +
                ", academicPeriodId='" + academicPeriodId + '\'' +
                ", studentId='" + studentId + '\'' +
                ", status='" + status + '\'' +
                ", profile=" + profile +
                ", academicPeriod=" + academicPeriod +
                ", student=" + student +
                '}';
    }
}