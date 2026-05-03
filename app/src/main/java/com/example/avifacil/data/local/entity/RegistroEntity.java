package com.example.avifacil.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(tableName = "registros",
        foreignKeys = @ForeignKey(
                entity = LoteEntity.class,
                parentColumns = "id",
                childColumns = "loteId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("loteId"),
                @Index(value = {"loteId", "dataRegistro"}, unique = true)
        })
public class RegistroEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long loteId;
    private Date dataRegistro;
    private int avesMortasPeriodo;
    private double consumoRacaoPeriodo;
    private String observacoes;
    private boolean deleted = false;
    private boolean sincronizado = false;
    private long updatedAt;

    public RegistroEntity(long loteId, Date dataRegistro, int avesMortasPeriodo, double consumoRacaoPeriodo) {
        this.loteId = loteId;
        this.dataRegistro = dataRegistro;
        this.avesMortasPeriodo = avesMortasPeriodo;
        this.consumoRacaoPeriodo = consumoRacaoPeriodo;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getLoteId() { return loteId; }
    public void setLoteId(long loteId) { this.loteId = loteId; }
    public Date getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(Date dataRegistro) { this.dataRegistro = dataRegistro; }
    public int getAvesMortasPeriodo() { return avesMortasPeriodo; }
    public void setAvesMortasPeriodo(int avesMortasPeriodo) { this.avesMortasPeriodo = avesMortasPeriodo; }
    public double getConsumoRacaoPeriodo() { return consumoRacaoPeriodo; }
    public void setConsumoRacaoPeriodo(double consumoRacaoPeriodo) { this.consumoRacaoPeriodo = consumoRacaoPeriodo; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
