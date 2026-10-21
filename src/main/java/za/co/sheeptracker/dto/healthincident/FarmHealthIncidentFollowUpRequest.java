package za.co.sheeptracker.dto.healthincident;

import java.time.OffsetDateTime;

/** Record a follow-up that happened. nextFollowUpAt schedules the next one; leave it empty if none is needed. */
public record FarmHealthIncidentFollowUpRequest(String notes, OffsetDateTime occurredAt, OffsetDateTime nextFollowUpAt) {
}
