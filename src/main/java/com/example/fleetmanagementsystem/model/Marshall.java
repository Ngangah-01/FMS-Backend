package com.example.fleetmanagementsystem.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data
@Entity
@Table(name = "marshalls")
public class Marshall {

    @Id
    @Column(name = "marshall_Id")
    private Long marshallId;

    @NotBlank
    @Column(nullable = false)
    private String firstname;

    @NotBlank
    @Column(nullable = false)
    private String lastname;

    //email should be unique
    @Column(unique = true, nullable = false)
    private String Email;

    @Column
    private String PhoneNumber;

    @Column(nullable = false)
    private String Stage;

    @ManyToOne
    private Route route;

//    @OneToOne
//    @JoinColumn(name = "user_id", unique = true)
//    private Users user; // Assuming Users is a class that represents the user details

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "marshall_Id", referencedColumnName = "id_Number", insertable = false, updatable = false, unique = true)
    @JsonBackReference
    private Users user; // Assuming Users is a class that represents the user details

    @Override
    public String toString() {
        return "Marshall{" +
                "marshallId=" + marshallId +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", Email='" + Email + '\'' +
                ", PhoneNumber='" + PhoneNumber + '\'' +
                ", Stage='" + Stage + '\'' +
                '}';
    }

    // Additional fields can be added as needed

}
