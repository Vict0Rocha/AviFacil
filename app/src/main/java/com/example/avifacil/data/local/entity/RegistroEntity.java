package com.example.avifacil.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import com.google.firebase.firestore.Exclude;
import java.util.Date;

/**
 * Entidade que representa um lançamento diário de dados técnicos.
 * Contém informações sobre mortalidade, consumo de ração e pesagem.
 */
@Entity(tableName = "registros",
        foreignKeys = @ForeignKey(
                entity = LoteEntity.class,
                parentColumns = "id",
                childColumns = "loteId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("loteId"),
                @Index(value = {"loteId", "dataRegistro"}),
                @Index(value = {"uuid"}, unique = true)
        })
public class RegistroEntity {
    @PrimaryKey(autoGenerate = true)
    @Exclude
    private long id;
    
    private String uuid; // ID estável para Firebase
    private long loteId;
    private String loteUuid; // UUID do pai para sync
    private Date dataRegistro;
    private int avesMortasPeriodo;
    private double consumoRacaoPeriodo;
    private double pesoAtualMedio;
    private double precoKgInsumo;
    private String tipoInsumo; // "milho", "soja", "núcleo", "outro"
    private String observacoes;
    private boolean deleted = false;
    private boolean sincronizado = false;
    private long updatedAt;

    public RegistroEntity() {
        // Necessário para o Firebase
    }

    public RegistroEntity(long loteId, String loteUuid, Date dataRegistro, int avesMortasPeriodo, double consumoRacaoPeriodo, double pesoAtualMedio, double precoKgInsumo, String tipoInsumo) {
        this.uuid = java.util.UUID.randomUUID().toString();
        this.loteId = loteId;
        this.loteUuid = loteUuid;
        this.dataRegistro = dataRegistro;
        this.avesMortasPeriodo = avesMortasPeriodo;
        this.consumoRacaoPeriodo = consumoRacaoPeriodo;
        this.pesoAtualMedio = pesoAtualMedio;
        this.precoKgInsumo = precoKgInsumo;
        this.tipoInsumo = tipoInsumo;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public long getLoteId() { return loteId; }
    public void setLoteId(long loteId) { this.loteId = loteId; }
    public String getLoteUuid() { return loteUuid; }
    public void setLoteUuid(String loteUuid) { this.loteUuid = loteUuid; }
    public Date getDataRegistro() { return dataRegistro; }
    public void setDataRegistro(Date dataRegistro) { this.dataRegistro = dataRegistro; }
    public int getAvesMortasPeriodo() { return avesMortasPeriodo; }
    public void setAvesMortasPeriodo(int avesMortasPeriodo) { this.avesMortasPeriodo = avesMortasPeriodo; }
    public double getConsumoRacaoPeriodo() { return consumoRacaoPeriodo; }
    public void setConsumoRacaoPeriodo(double consumoRacaoPeriodo) { this.consumoRacaoPeriodo = consumoRacaoPeriodo; }
    public double getPesoAtualMedio() { return pesoAtualMedio; }
    public void setPesoAtualMedio(double pesoAtualMedio) { this.pesoAtualMedio = pesoAtualMedio; }
    
    public double getPrecoKgInsumo() { return precoKgInsumo; }
    public void setPrecoKgInsumo(double precoKgInsumo) { this.precoKgInsumo = precoKgInsumo; }
    
    public String getTipoInsumo() { return tipoInsumo; }
    public void setTipoInsumo(String tipoInsumo) { this.tipoInsumo = tipoInsumo; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
