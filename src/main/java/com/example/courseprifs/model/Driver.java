package com.example.courseprifs.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Driver extends BasicUser{
    private String licence;
    private LocalDate bDate;
    @Enumerated(EnumType.STRING)
    private VehicleType vehicleType;
    private String vehicleInfo;

    public Driver(String login, String password, String name, String surname, String phoneNumber, String address, String licence, LocalDate bDate, VehicleType vehicleType, String vehicleInfo) {
        super(login, password, name, surname, phoneNumber, address);
        this.licence = licence;
        this.bDate = bDate;
        this.vehicleType = vehicleType;
        this.vehicleInfo = vehicleInfo;
    }

    public Driver(String login, String password, String name, String surname, String phoneNumber, LocalDateTime dateCreated, String address, String licence, LocalDate bDate, VehicleType vehicleType, String vehicleInfo) {
        super(login, password, name, surname, phoneNumber, dateCreated, address);
        this.licence = licence;
        this.bDate = bDate;
        this.vehicleType = vehicleType;
        this.vehicleInfo = vehicleInfo;
    }
}
