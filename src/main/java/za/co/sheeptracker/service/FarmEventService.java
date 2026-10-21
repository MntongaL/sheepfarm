package za.co.sheeptracker.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.sheeptracker.dto.farmevent.FarmEventRequest;
import za.co.sheeptracker.dto.farmevent.FarmEventResponse;
import za.co.sheeptracker.entity.FarmEvent;
import za.co.sheeptracker.entity.FarmHealthIncident;
import za.co.sheeptracker.entity.Sheep;
import za.co.sheeptracker.enums.FarmEventType;
import za.co.sheeptracker.repository.FarmEventRepository;
import za.co.sheeptracker.repository.FarmHealthIncidentRepository;
import za.co.sheeptracker.repository.SheepRepository;
import za.co.sheeptracker.security.CurrentUser;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class FarmEventService {

    private static final int MAX_LIMIT = 500;

    private final FarmEventRepository repository;
    private final SheepRepository sheepRepository;
    private final FarmHealthIncidentRepository incidentRepository;

    public FarmEventService(FarmEventRepository repository, SheepRepository sheepRepository,
                            FarmHealthIncidentRepository incidentRepository) {
        this.repository = repository;
        this.sheepRepository = sheepRepository;
        this.incidentRepository = incidentRepository;
    }

    /** Used by the incident and treatment flows. Joins the caller's transaction. */
    @Transactional
    public FarmEvent record(FarmEvent event) {
        if (event.getOccurredAt() == null) event.setOccurredAt(OffsetDateTime.now(ZoneOffset.UTC));
        if (event.getRecordedBy() == null) event.setRecordedBy(CurrentUser.idOrNull());
        if (event.getMetadata() == null) event.setMetadata(new LinkedHashMap<>());
        event.setTitle(limit(event.getTitle(), 200));
        return repository.save(event);
    }

    @Transactional
    public FarmEventResponse create(FarmEventRequest r) {
        if (!r.eventType().isManual()) {
            throw new IllegalArgumentException("Event type " + r.eventType()
                    + " is created by its own flow; allowed here: HEALTH_OBSERVED, SHEEP_MOVED, VACCINATION, WEIGHT_RECORDED, GENERAL_NOTE");
        }
        if (r.sheepId() != null) {
            Sheep sheep = sheepRepository.findById(r.sheepId())
                    .orElseThrow(() -> new NoSuchElementException("Sheep not found: " + r.sheepId()));
            if (!sheep.getFarmId().equals(r.farmId()))
                throw new IllegalArgumentException("Sheep does not belong to supplied farm");
        }
        if (r.healthIncidentId() != null) {
            FarmHealthIncident incident = incidentRepository.findById(r.healthIncidentId())
                    .orElseThrow(() -> new NoSuchElementException("Health incident not found: " + r.healthIncidentId()));
            if (!incident.getFarmId().equals(r.farmId()))
                throw new IllegalArgumentException("Health incident does not belong to supplied farm");
        }
        FarmEvent e = FarmEvent.of(r.farmId(), r.eventType(), r.title().trim());
        e.setSheepId(r.sheepId());
        e.setHealthIncidentId(r.healthIncidentId());
        e.setDescription(r.description());
        e.setOccurredAt(r.occurredAt());
        if (r.metadata() != null) e.setMetadata(new LinkedHashMap<>(r.metadata()));
        return FarmEventResponse.from(record(e));
    }

    /** Newest first. */
    @Transactional(readOnly = true)
    public List<FarmEventResponse> list(UUID farmId, UUID sheepId, UUID healthIncidentId, UUID treatmentId,
                                        FarmEventType eventType, OffsetDateTime from, OffsetDateTime to, int limit) {
        Specification<FarmEvent> spec = (root, query, cb) -> {
            List<Predicate> p = new ArrayList<>();
            p.add(cb.equal(root.get("farmId"), farmId));
            if (sheepId != null) p.add(cb.equal(root.get("sheepId"), sheepId));
            if (healthIncidentId != null) p.add(cb.equal(root.get("healthIncidentId"), healthIncidentId));
            if (treatmentId != null) p.add(cb.equal(root.get("treatmentId"), treatmentId));
            if (eventType != null) p.add(cb.equal(root.get("eventType"), eventType));
            if (from != null) p.add(cb.greaterThanOrEqualTo(root.<OffsetDateTime>get("occurredAt"), from));
            if (to != null) p.add(cb.lessThanOrEqualTo(root.<OffsetDateTime>get("occurredAt"), to));
            return cb.and(p.toArray(new Predicate[0]));
        };
        Pageable page = PageRequest.of(0, Math.min(Math.max(limit, 1), MAX_LIMIT),
                Sort.by(Sort.Order.desc("occurredAt"), Sort.Order.desc("id")));
        return repository.findAll(spec, page).getContent().stream().map(FarmEventResponse::from).toList();
    }

    static String limit(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, max - 1) + "…";
    }
}
