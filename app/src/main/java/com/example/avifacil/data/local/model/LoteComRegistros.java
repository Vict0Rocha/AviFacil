package com.example.avifacil.data.local.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.RegistroEntity;
import java.util.List;

public class LoteComRegistros {
    @Embedded
    public LoteEntity lote;

    @Relation(
            parentColumn = "id",
            entityColumn = "loteId"
    )
    public List<RegistroEntity> registros;
}
