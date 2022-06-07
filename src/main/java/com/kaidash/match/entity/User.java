package com.kaidash.match.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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

    @Column(name = "age")
    private int age;

    @Column(name = "sex")
    private String sex;

    @Column(name = "opposite_sex")
    private String oppositeSex;

    @Column(name = "city")
    private String city;

    @Column(name = "description")
    private String description;

//    public User(long userId, String name, int age, String sex, String oppositeSex, String city, String description) {
//        this.userId = userId;
//        this.name = name;
//        this.age = age;
//        this.sex = sex;
//        this.oppositeSex = oppositeSex;
//        this.city = city;
//        this.description = description;
//    }
}
