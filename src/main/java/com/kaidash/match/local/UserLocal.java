package com.kaidash.match.local;

import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class UserLocal {

    private long userId;
    private String name = "";
    private int age = 0;
    private int sex;
    private int oppositeSex;
    private String city = "";
    private String description = "";
    private long oppositeSexId = 1;

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
                ", oppositeSexId='" + oppositeSexId + '\'' +
                '}';
    }
}
