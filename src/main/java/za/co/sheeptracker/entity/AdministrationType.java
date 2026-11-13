package za.co.sheeptracker.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "administration_type")
public class AdministrationType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Short  id;
    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "sort_order", nullable = false)
    private Short  sortOrder;
    @Column(name = "is_active", nullable = false)
    private Boolean active;

}
