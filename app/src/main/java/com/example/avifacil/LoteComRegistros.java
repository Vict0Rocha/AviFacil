package com.example.avifacil;

import androidx.room.Embedded;
import androidx.room.Relation;
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
