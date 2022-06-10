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
    private long oppositeSexId;
    private String userName;
}
