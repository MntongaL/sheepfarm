package za.co.sheeptracker.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.sheeptracker.dto.healthincident.*;
import za.co.sheeptracker.entity.FarmEvent;
import za.co.sheeptracker.entity.FarmHealthIncident;
import za.co.sheeptracker.entity.Sheep;
import za.co.sheeptracker.enums.FarmEventType;
import za.co.sheeptracker.enums.HealthSeverity;
import za.co.sheeptracker.enums.IncidentStatus;
import za.co.sheeptracker.repository.FarmHealthIncidentRepository;
import za.co.sheeptracker.repository.SheepRepository;
import za.co.sheeptracker.security.CurrentUser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class FarmHealthIncidentService {

    private final FarmHealthIncidentRepository incidents;
    private final SheepRepository sheepRepository;
    private final FarmEventService events;

    public FarmHealthIncidentService(FarmHealthIncidentRepository incidents, SheepRepository sheepRepository,
                                     FarmEventService events) {
        this.incidents = incidents;
        this.sheepRepository = sheepRepository;
        this.events = events;
    }

    @Transactional
    public FarmHealthIncidentResponse create(FarmHealthIncidentRequest r) {
        Sheep sheep = sheepRepository.findById(r.sheepId())
                .orElseThrow(() -> new NoSuchElementException("Sheep not found: " + r.sheepId()));
        if (!sheep.getFarmId().equals(r.farmId()))
            throw new IllegalArgumentException("Sheep does not belong to supplied farm");

        OffsetDateTime observedAt = r.observedAt() != null ? r.observedAt() : now();
        requireNotBefore(r.followUpAt(), observedAt, "followUpAt");

        FarmHealthIncident i = new FarmHealthIncident();
        i.setFarmId(r.farmId());
        i.setSheepId(r.sheepId());
        i.setIncidentNumber(blankToNull(r.incidentNumber()));
        i.setTitle(r.title().trim());
        i.setIncidentType(r.incidentType());
        if (r.severity() != null) i.setSeverity(r.severity());
        i.setObservedAt(observedAt);
        i.setReportedBy(CurrentUser.idOrNull());
        i.setAssignedTo(r.assignedTo());
        i.setSymptoms(blankToNull(r.symptoms()));
        i.setDiagnosis(blankToNull(r.diagnosis()));
        i.setNotes(blankToNull(r.notes()));
        i.setFollowUpAt(r.followUpAt());
        FarmHealthIncident saved = incidents.save(i);

        FarmEvent reported = FarmEvent.of(saved.getFarmId(), FarmEventType.HEALTH_INCIDENT_REPORTED,
                "Health incident reported: " + saved.getTitle());
        reported.setSheepId(saved.getSheepId());
        reported.setHealthIncidentId(saved.getId());
        reported.setOccurredAt(saved.getObservedAt());
        reported.setDescription(saved.getSymptoms());
        reported.setMetadata(meta("incidentType", saved.getIncidentType(), "severity", saved.getSeverity().name()));
        events.record(reported);

        if (saved.getDiagnosis() != null) recordDiagnosis(saved);
        return FarmHealthIncidentResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public FarmHealthIncidentResponse get(UUID id) {
        return FarmHealthIncidentResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public List<FarmHealthIncidentResponse> list(UUID farmId, UUID sheepId, IncidentStatus status,
                                                 HealthSeverity severity, OffsetDateTime followUpBefore) {
        Specification<FarmHealthIncident> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("farmId"), farmId));
            if (sheepId != null) p.add(cb.equal(root.get("sheepId"), sheepId));
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (severity != null) p.add(cb.equal(root.get("severity"), severity));
            if (followUpBefore != null) {
                // follow-up queue: only incidents that are still open and due
                p.add(cb.lessThanOrEqualTo(root.<OffsetDateTime>get("followUpAt"), followUpBefore));
                p.add(cb.not(root.get("status").in(IncidentStatus.RESOLVED, IncidentStatus.CLOSED)));
            }
            return cb.and(p.toArray(new Predicate[0]));
        };
        return incidents.findAll(spec, Sort.by(Sort.Direction.DESC, "observedAt")).stream()
                .map(FarmHealthIncidentResponse::from).toList();
    }

    @Transactional
    public FarmHealthIncidentResponse update(UUID id, FarmHealthIncidentUpdateRequest r) {
        FarmHealthIncident i = find(id);
        if (r.status() == IncidentStatus.RESOLVED)
            throw new IllegalArgumentException("Use the resolve endpoint to resolve an incident");

        if (r.title() != null) {
            if (r.title().isBlank()) throw new IllegalArgumentException("title must not be blank");
            i.setTitle(r.title().trim());
        }
        if (r.severity() != null) i.setSeverity(r.severity());
        if (r.assignedTo() != null) i.setAssignedTo(r.assignedTo());
        if (r.symptoms() != null) i.setSymptoms(blankToNull(r.symptoms()));
        if (r.notes() != null) i.setNotes(blankToNull(r.notes()));

        boolean newDiagnosis = r.diagnosis() != null && !r.diagnosis().isBlank()
                && !r.diagnosis().trim().equals(i.getDiagnosis());
        if (r.diagnosis() != null) i.setDiagnosis(blankToNull(r.diagnosis()));

        if (r.followUpAt() != null) {
            requireNotBefore(r.followUpAt(), i.getObservedAt(), "followUpAt");
            i.setFollowUpAt(r.followUpAt());
        }
        if (r.status() != null) {
            if (i.getStatus().isFinal() && !r.status().isFinal()) { // reopening clears the resolution (ck_fhi_resolved_at)
                i.setResolvedAt(null);
                i.setResolvedBy(null);
                i.setOutcome(null);
            }
            i.setStatus(r.status());
        }
        if (i.getStatus() == IncidentStatus.FOLLOW_UP_REQUIRED && i.getFollowUpAt() == null)
            throw new IllegalArgumentException("followUpAt is required when status is FOLLOW_UP_REQUIRED");

        FarmHealthIncident saved = incidents.save(i);
        if (newDiagnosis) recordDiagnosis(saved);
        return FarmHealthIncidentResponse.from(saved);
    }

    @Transactional
    public FarmHealthIncidentResponse resolve(UUID id, FarmHealthIncidentResolveRequest r) {
        FarmHealthIncident i = find(id);
        if (i.getStatus().isFinal())
            throw new IllegalArgumentException("Incident is already " + i.getStatus());

        OffsetDateTime now = now();
        i.setStatus(IncidentStatus.RESOLVED);
        i.setResolvedAt(now.isBefore(i.getObservedAt()) ? i.getObservedAt() : now); // ck_fhi_resolved_dt
        i.setResolvedBy(CurrentUser.idOrNull());
        if (r != null) {
            i.setOutcome(blankToNull(r.outcome()));
            if (r.notes() != null && !r.notes().isBlank())
                i.setNotes(i.getNotes() == null ? r.notes().trim() : i.getNotes() + "\n" + r.notes().trim());
        }
        FarmHealthIncident saved = incidents.save(i);

        FarmEvent e = FarmEvent.of(saved.getFarmId(), FarmEventType.INCIDENT_RESOLVED,
                "Health incident resolved: " + saved.getTitle());
        e.setSheepId(saved.getSheepId());
        e.setHealthIncidentId(saved.getId());
        e.setOccurredAt(saved.getResolvedAt());
        e.setDescription(saved.getOutcome());
        events.record(e);
        return FarmHealthIncidentResponse.from(saved);
    }

    @Transactional
    public FarmHealthIncidentResponse recordFollowUp(UUID id, FarmHealthIncidentFollowUpRequest r) {
        FarmHealthIncident i = find(id);
        if (i.getStatus().isFinal())
            throw new IllegalArgumentException("Cannot record a follow-up on a " + i.getStatus() + " incident");

        OffsetDateTime when = r != null && r.occurredAt() != null ? r.occurredAt() : now();
        OffsetDateTime next = r == null ? null : r.nextFollowUpAt();
        requireNotBefore(next, i.getObservedAt(), "nextFollowUpAt");
        i.setFollowUpAt(next);
        // FOLLOW_UP_REQUIRED needs a date (ck_fhi_followup_req); with none scheduled the animal is simply observed
        if (next == null && i.getStatus() == IncidentStatus.FOLLOW_UP_REQUIRED)
            i.setStatus(IncidentStatus.UNDER_OBSERVATION);
        FarmHealthIncident saved = incidents.save(i);

        FarmEvent e = FarmEvent.of(saved.getFarmId(), FarmEventType.FOLLOW_UP_RECORDED,
                "Follow-up recorded: " + saved.getTitle());
        e.setSheepId(saved.getSheepId());
        e.setHealthIncidentId(saved.getId());
        e.setOccurredAt(when);
        e.setDescription(r == null ? null : blankToNull(r.notes()));
        if (next != null) e.setMetadata(meta("nextFollowUpAt", next.toString()));
        events.record(e);
        return FarmHealthIncidentResponse.from(saved);
    }

    private void recordDiagnosis(FarmHealthIncident i) {
        FarmEvent e = FarmEvent.of(i.getFarmId(), FarmEventType.DIAGNOSIS_RECORDED,
                "Diagnosis recorded: " + i.getTitle());
        e.setSheepId(i.getSheepId());
        e.setHealthIncidentId(i.getId());
        e.setDescription(i.getDiagnosis());
        events.record(e);
    }

    private FarmHealthIncident find(UUID id) {
        return incidents.findById(id).orElseThrow(() -> new NoSuchElementException("Health incident not found: " + id));
    }

    private static void requireNotBefore(OffsetDateTime value, OffsetDateTime observedAt, String field) {
        if (value != null && value.isBefore(observedAt))
            throw new IllegalArgumentException(field + " must not be before observedAt");
    }

    private static OffsetDateTime now() {
        return OffsetDateTime.now(ZoneOffset.UTC);
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static Map<String, Object> meta(String... keyValues) {
        Map<String, Object> m = new LinkedHashMap<>();
        for (int k = 0; k + 1 < keyValues.length; k += 2) m.put(keyValues[k], keyValues[k + 1]);
        return m;
    }
}
