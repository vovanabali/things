package com.owl.thingsbot.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

import java.net.URI;
import java.net.URISyntaxException;

import static org.apache.logging.log4j.util.Strings.EMPTY;

@Data
@Entity
@NoArgsConstructor
@Slf4j
public class SiteInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String uri = EMPTY;
    @Column(length = 100000)
    private String description;

    @Override
    public String toString() {
        return uri + " - " +  description;
    }
}
