package za.co.sheeptracker.entity;
import jakarta.persistence.*; import java.math.BigDecimal; import java.time.*; import java.util.UUID;
@Entity @Table(name="treatments") public class Treatment {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
 @Column(name="farm_id",nullable=false) private UUID farmId;
 @Column(name="sheep_id",nullable=false) private UUID sheepId;
 @Column(name="medication_id",nullable=false) private UUID medicationId;
 @Column(name="administered_at",nullable=false) private LocalDate administeredAt;
 @Column(name="dose_amount",precision=12,scale=3) private BigDecimal doseAmount;
 @Column(name="dose_unit",length=30) private String doseUnit;
 @Column(name="withdrawal_days_applied") private Integer withdrawalDaysApplied;
 @Column(name="withdrawal_until") private LocalDate withdrawalUntil;
 @Column(name="administered_by",length=160) private String administeredBy;
 @Column(columnDefinition="text") private String notes;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt=OffsetDateTime.now(ZoneOffset.UTC);
 public UUID getId(){return id;} public UUID getFarmId(){return farmId;} public void setFarmId(UUID v){farmId=v;} public UUID getSheepId(){return sheepId;} public void setSheepId(UUID v){sheepId=v;} public UUID getMedicationId(){return medicationId;} public void setMedicationId(UUID v){medicationId=v;} public LocalDate getAdministeredAt(){return administeredAt;} public void setAdministeredAt(LocalDate v){administeredAt=v;} public BigDecimal getDoseAmount(){return doseAmount;} public void setDoseAmount(BigDecimal v){doseAmount=v;} public String getDoseUnit(){return doseUnit;} public void setDoseUnit(String v){doseUnit=v;} public Integer getWithdrawalDaysApplied(){return withdrawalDaysApplied;} public void setWithdrawalDaysApplied(Integer v){withdrawalDaysApplied=v;} public LocalDate getWithdrawalUntil(){return withdrawalUntil;} public void setWithdrawalUntil(LocalDate v){withdrawalUntil=v;} public String getAdministeredBy(){return administeredBy;} public void setAdministeredBy(String v){administeredBy=v;} public String getNotes(){return notes;} public void setNotes(String v){notes=v;}
}
