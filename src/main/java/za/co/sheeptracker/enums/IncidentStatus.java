package za.co.sheeptracker.enums;

/** Stored as text; must match ck_fhi_status in V18. */
public enum IncidentStatus {
    OPEN, UNDER_OBSERVATION, TREATMENT_IN_PROGRESS, FOLLOW_UP_REQUIRED, RESOLVED, CLOSED;

    /** RESOLVED and CLOSED end the incident lifecycle. */
    public boolean isFinal() {
        return this == RESOLVED || this == CLOSED;
    }
}
