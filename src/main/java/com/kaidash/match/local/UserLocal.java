package com.kaidash.match.local;

import lombok.*;
import org.springframework.stereotype.Component;


@Getter
@Setter
@Component
@AllArgsConstructor
@NoArgsConstructor
public class UserLocal {

    private long userId;
    private String name = "";
    private int age = 0;
    private String sex = "";
    private String oppositeSex = "";
    private String city = "";
    private String description = "";

//    public UserLocal(){}
//
//    public UserLocal(long userId, String name, int age, String sex, String oppositeSex, String city, String description) {
//        this.userId = userId;
//        this.name = name;
//        this.age = age;
//        this.sex = sex;
//        this.oppositeSex = oppositeSex;
//        this.city = city;
//        this.description = description;
//    }
//
//    public UserLocal (long userId){
//        this.userId = userId;
//    }
//
//    public UserLocal(String name) {
//        this.name = name;
//    }

    @Override
    public String toString() {
        return "UserLocal{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", oppositeSex='" + oppositeSex + '\'' +
                ", city='" + city + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
