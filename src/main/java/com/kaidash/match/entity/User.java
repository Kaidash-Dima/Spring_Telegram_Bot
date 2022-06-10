package com.kaidash.match.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "user_id")
    private long userId;

    @Column(name = "name")
    private String name;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "age")
    private int age;

    @Column(name = "sex")
    private int sex;

    @Column(name = "opposite_sex")
    private int oppositeSex;

    @Column(name = "city")
    private String city;

    @Column(name = "description")
    private String description = "";

    @Column(name = "opposite_sex_id")
    private long oppositeSexId = 1;

    @OneToMany(mappedBy = "oppositeUserId")
    private List<Match> matches;
}
