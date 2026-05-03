package com.example.avifacil;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String email;

    public User(String name, String email) {
        this.name = name;
        this.email = email;
    }
}
