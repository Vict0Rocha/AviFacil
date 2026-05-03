package com.example.avifacil;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
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
    private Date dataInicio;
    private int quantidadeAvesInicial;
    private StatusLote status = StatusLote.ATIVO;
    private boolean deleted = false;
    private boolean sincronizado = false;

    public LoteEntity(long avicultorId, String numeroLote, Date dataInicio, int quantidadeAvesInicial) {
        this.avicultorId = avicultorId;
        this.numeroLote = numeroLote;
        this.dataInicio = dataInicio;
        this.quantidadeAvesInicial = quantidadeAvesInicial;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getAvicultorId() { return avicultorId; }
    public void setAvicultorId(long avicultorId) { this.avicultorId = avicultorId; }
    public String getNumeroLote() { return numeroLote; }
    public void setNumeroLote(String numeroLote) { this.numeroLote = numeroLote; }
    public Date getDataInicio() { return dataInicio; }
    public void setDataInicio(Date dataInicio) { this.dataInicio = dataInicio; }
    public int getQuantidadeAvesInicial() { return quantidadeAvesInicial; }
    public void setQuantidadeAvesInicial(int quantidadeAvesInicial) { this.quantidadeAvesInicial = quantidadeAvesInicial; }
    public StatusLote getStatus() { return status; }
    public void setStatus(StatusLote status) { this.status = status; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public boolean isSincronizado() { return sincronizado; }
    public void setSincronizado(boolean sincronizado) { this.sincronizado = sincronizado; }
}
