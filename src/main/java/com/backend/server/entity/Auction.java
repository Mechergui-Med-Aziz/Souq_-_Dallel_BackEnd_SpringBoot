package com.backend.server.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Auction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @ManyToOne
    private User seller;
    private String title;
    private String discription;
    private float start_price;
    private float current_price;
    private Date start_time;
    private Date end_time;
    public enum Status {
        OPEN,
        CLOSED,
        CANCELED
    }

    private Status status;
}