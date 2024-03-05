package com.example.shortlink.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "links")
public class Link {
    @Id
    private String endOfShortLink;
    private String longLink;
    private Timestamp ttl;
}
