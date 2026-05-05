package com.example.avifacil.ui.registro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.RegistroEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RegistroAdapter extends RecyclerView.Adapter<RegistroAdapter.RegistroViewHolder> {

    private List<RegistroEntity> registros = new ArrayList<>();
    private java.util.Date dataInicioLote;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public void setRegistros(List<RegistroEntity> registros, java.util.Date dataInicioLote) {
        this.registros = registros;
        this.dataInicioLote = dataInicioLote;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RegistroViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_registro, parent, false);
        return new RegistroViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegistroViewHolder holder, int position) {
        RegistroEntity registro = registros.get(position);
        holder.txtData.setText(dateFormat.format(registro.getDataRegistro()));
        holder.txtMortas.setText(holder.itemView.getContext().getString(R.string.label_mortas, registro.getAvesMortasPeriodo()));
        holder.txtConsumo.setText(holder.itemView.getContext().getString(R.string.label_consumo, registro.getConsumoRacaoPeriodo()));
        
        if (dataInicioLote != null) {
            long diff = registro.getDataRegistro().getTime() - dataInicioLote.getTime();
            long dias = java.util.concurrent.TimeUnit.DAYS.convert(diff, java.util.concurrent.TimeUnit.MILLISECONDS);
            holder.txtIdade.setText(holder.itemView.getContext().getString(R.string.label_idade_lote, Math.max(0, dias)));
            holder.txtIdade.setVisibility(View.VISIBLE);
        } else {
            holder.txtIdade.setVisibility(View.GONE);
        }

        if (registro.getObservacoes() != null && !registro.getObservacoes().isEmpty()) {
            holder.txtObs.setVisibility(View.VISIBLE);
            holder.txtObs.setText(registro.getObservacoes());
        } else {
            holder.txtObs.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return registros.size();
    }

    static class RegistroViewHolder extends RecyclerView.ViewHolder {
        TextView txtData, txtMortas, txtConsumo, txtObs, txtIdade;

        public RegistroViewHolder(@NonNull View itemView) {
            super(itemView);
            txtData = itemView.findViewById(R.id.txtDataRegistro);
            txtMortas = itemView.findViewById(R.id.txtMortas);
            txtConsumo = itemView.findViewById(R.id.txtConsumo);
            txtObs = itemView.findViewById(R.id.txtObservacoes);
            txtIdade = itemView.findViewById(R.id.txtIdadeLote);
        }
    }
}