package za.co.sheeptracker.enums;

/** Stored as text; must match the CHECK constraint in the Flyway migrations. */
public enum PlanSheepStatus {
    PENDING, IN_PROGRESS, COMPLETED, REMOVED
}
