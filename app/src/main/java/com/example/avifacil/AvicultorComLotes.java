package com.example.avifacil;

import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;

public class AvicultorComLotes {
    @Embedded
    public AvicultorEntity avicultor;

    @Relation(
            parentColumn = "id",
            entityColumn = "avicultorId"
    )
    public List<LoteEntity> lotes;
}
