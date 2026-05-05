package com.example.avifacil.ui.lote;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.LoteEntity;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LoteAdapter extends RecyclerView.Adapter<LoteAdapter.LoteViewHolder> {

    private List<LoteEntity> lotes = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private OnLoteClickListener listener;

    public interface OnLoteClickListener {
        void onLoteClick(LoteEntity lote);
    }

    public void setLotes(List<LoteEntity> lotes) {
        this.lotes = lotes;
        notifyDataSetChanged();
    }

    public void setOnLoteClickListener(OnLoteClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public LoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lote, parent, false);
        return new LoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoteViewHolder holder, int position) {
        LoteEntity lote = lotes.get(position);
        holder.txtNumero.setText(lote.getNumeroLote());
        holder.txtData.setText(holder.itemView.getContext().getString(R.string.label_data_inicio, dateFormat.format(lote.getDataInicio())));
        holder.txtQtd.setText(holder.itemView.getContext().getString(R.string.label_quantidade, lote.getQuantidadeAvesInicial()));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLoteClick(lote);
            }
        });
    }

    @Override
    public int getItemCount() {
        return lotes.size();
    }

    static class LoteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumero, txtData, txtQtd;

        public LoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNumero = itemView.findViewById(R.id.txtNumeroLote);
            txtData = itemView.findViewById(R.id.txtDataInicio);
            txtQtd = itemView.findViewById(R.id.txtQtdAves);
        }
    }
}
