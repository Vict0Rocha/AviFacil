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
        void onLoteLongClick(LoteEntity lote);
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
        holder.txtLinhagem.setText(lote.getLinhagem());
        holder.txtData.setText(dateFormat.format(lote.getDataInicio()));
        holder.txtQtd.setText(holder.itemView.getContext().getString(R.string.label_quantidade, lote.getQuantidadeAvesInicial()));
        
        // Exibir Galpão no item da lista para facilitar identificação
        if (lote.getGalpao() != null && !lote.getGalpao().isEmpty()) {
            holder.txtLinhagem.setText(holder.itemView.getContext().getString(R.string.label_linhagem_galpao, lote.getLinhagem(), lote.getGalpao()));
        }

        if (lote.getStatus() != null) {
            holder.txtStatus.setText(lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ATIVO ? 
                holder.itemView.getContext().getString(R.string.status_ativo) : 
                holder.itemView.getContext().getString(R.string.status_encerrado));
            
            holder.txtStatus.setBackgroundResource(lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ATIVO ? 
                R.drawable.bg_status_active : 
                R.drawable.bg_status_encerrado);
                
            holder.txtStatus.setTextColor(lote.getStatus() == com.example.avifacil.data.local.entity.StatusLote.ATIVO ? 
                holder.itemView.getContext().getResources().getColor(R.color.primary_dark_blue) : 
                holder.itemView.getContext().getResources().getColor(R.color.secondary_text));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onLoteClick(lote);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onLoteLongClick(lote);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return lotes.size();
    }

    static class LoteViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumero, txtLinhagem, txtData, txtQtd, txtStatus;

        public LoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNumero = itemView.findViewById(R.id.txtNumeroLote);
            txtLinhagem = itemView.findViewById(R.id.txtLinhagemLote);
            txtData = itemView.findViewById(R.id.txtDataInicio);
            txtQtd = itemView.findViewById(R.id.txtQtdAves);
            txtStatus = itemView.findViewById(R.id.txtStatusLote);
        }
    }
}
