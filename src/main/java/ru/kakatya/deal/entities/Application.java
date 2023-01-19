package ru.kakatya.deal.entities;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.Data;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import ru.kakatya.deal.entities.enums.ApplicationStatus;

import javax.persistence.*;
import java.sql.Timestamp;

@Data
@Entity
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class Application {
    @Id
    @Column(name = "application_id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;


    @JoinColumn(name = "client_id", referencedColumnName = "client_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Client client;


    @JoinColumn(name = "credit_id", referencedColumnName = "credit_id")
    @OneToOne(cascade = CascadeType.ALL)
    private Credit credit;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Column(name = "creation_date", nullable = false)
    private Timestamp creationDate;

    @Column(name = "ses_code", nullable = false)
    private String sesCode;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb")
    private StatusHistory statusHistory;
}
