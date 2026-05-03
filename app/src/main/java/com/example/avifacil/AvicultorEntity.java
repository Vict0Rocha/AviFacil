package com.example.avifacil;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "avicultores")
public class AvicultorEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String nome;
    private String email;
    private boolean deleted = false;
    private boolean sincronizado = false;

    public AvicultorEntity(String nome, String email) {
        this.nome = nome;
        this.email = email;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
