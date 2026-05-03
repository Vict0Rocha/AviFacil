package com.example.avifacil.data.local.model;

import androidx.room.Embedded;
import androidx.room.Relation;
import com.example.avifacil.data.local.entity.AvicultorEntity;
import com.example.avifacil.data.local.entity.LoteEntity;
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
