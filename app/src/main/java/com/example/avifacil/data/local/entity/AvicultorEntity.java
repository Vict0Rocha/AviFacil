package com.example.avifacil.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "avicultores",
        indices = {@Index(value = {"uuid"}, unique = true)})
public class AvicultorEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String uuid; // ID estável para Firebase
    private String nome;
    private String email;
    private String nomePropriedade;
    private boolean deleted = false;
    private boolean sincronizado = false;
    private long updatedAt;

    public AvicultorEntity() {
        // Necessário para o Firebase
    }

    public AvicultorEntity(String nome, String email, String nomePropriedade) {
        this.uuid = java.util.UUID.randomUUID().toString();
        this.nome = nome;
        this.email = email;
        this.nomePropriedade = nomePropriedade;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getNomePropriedade() { return nomePropriedade; }
    public void setNomePropriedade(String nomePropriedade) { this.nomePropriedade = nomePropriedade; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
