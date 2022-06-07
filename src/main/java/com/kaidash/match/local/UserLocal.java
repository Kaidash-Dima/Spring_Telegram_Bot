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
    private String sex = "";
    private String oppositeSex = "";
    private String city = "";
    private String description = "";

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
