package za.co.sheeptracker.dto.treatmentplan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record TreatmentPlanRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}
