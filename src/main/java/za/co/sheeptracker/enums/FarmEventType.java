package za.co.sheeptracker.enums;

/** Stored as text; must match ck_fe_type in V18. */
public enum FarmEventType {
    HEALTH_OBSERVED,
    HEALTH_INCIDENT_REPORTED,
    DIAGNOSIS_RECORDED,
    TREATMENT_ADMINISTERED,
    TREATMENT_COMPLETED,
    FOLLOW_UP_RECORDED,
    INCIDENT_RESOLVED,
    SHEEP_MOVED,
    VACCINATION,
    WEIGHT_RECORDED,
    GENERAL_NOTE;

    /** Types a user may add directly. The others are written by the incident and treatment flows. */
    public boolean isManual() {
        return this == HEALTH_OBSERVED || this == SHEEP_MOVED || this == VACCINATION
                || this == WEIGHT_RECORDED || this == GENERAL_NOTE;
    }
}
