package com.example.avifacil.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import java.util.Date;

@Entity(tableName = "lotes",
        foreignKeys = @ForeignKey(
                entity = AvicultorEntity.class,
                parentColumns = "id",
                childColumns = "avicultorId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {
                @Index("avicultorId"),
                @Index(value = {"avicultorId", "numeroLote"}, unique = true)
        })
public class LoteEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private long avicultorId;
    private String numeroLote;
    private String linhagem;
    private String galpao;
    private Date dataInicio;
    private int quantidadeAvesInicial;
    private double pesoInicial;
    private String observacoes;
    private StatusLote status = StatusLote.ATIVO;
    private boolean deleted = false;
    private boolean sincronizado = false;
    private long updatedAt;

    public LoteEntity(long avicultorId, String numeroLote, String linhagem, String galpao, Date dataInicio, int quantidadeAvesInicial, double pesoInicial, String observacoes) {
        this.avicultorId = avicultorId;
        this.numeroLote = numeroLote;
        this.linhagem = linhagem;
        this.galpao = galpao;
        this.dataInicio = dataInicio;
        this.quantidadeAvesInicial = quantidadeAvesInicial;
        this.pesoInicial = pesoInicial;
        this.observacoes = observacoes;
        this.updatedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getAvicultorId() { return avicultorId; }
    public void setAvicultorId(long avicultorId) { this.avicultorId = avicultorId; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    public String getLinhagem() { return linhagem; }
    public void setLinhagem(String linhagem) { this.linhagem = linhagem; }
    public String getGalpao() { return galpao; }
    public void setGalpao(String galpao) { this.galpao = galpao; }
    public Date getDataInicio() { return dataInicio; }
    public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }
    public int getQuantidadeAvesInicial() { return quantidadeAvesInicial; }
    public void setQuantidadeAvesInicial(int quantidadeAvesInicial) { this.quantidadeAvesInicial = quantidadeAvesInicial; }
    public double getPesoInicial() { return pesoInicial; }
    public void setPesoInicial(double pesoInicial) { this.pesoInicial = pesoInicial; }
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    public StatusLote getStatus() { return status; }
    public void setStatus(StatusLote status) { this.status = status; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
