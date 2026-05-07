package com.example.avifacil.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.avifacil.R;
import com.example.avifacil.data.local.entity.LoteEntity;
import com.example.avifacil.data.local.entity.StatusLote;
import java.util.ArrayList;
import java.util.List;

public class LoteResumoAdapter extends RecyclerView.Adapter<LoteResumoAdapter.ViewHolder> {

    private List<LoteEntity> lotes = new ArrayList<>();
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
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lote_resumo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoteEntity lote = lotes.get(position);
        holder.txtNumero.setText("Lote: " + lote.getNumeroLote());
        holder.txtLinhagem.setText(lote.getLinhagem());
        
        if (lote.getStatus() == StatusLote.ATIVO) {
            holder.txtStatus.setText("ATIVO");
            holder.txtStatus.setBackgroundResource(R.drawable.bg_badge_ativo);
        } else {
            holder.txtStatus.setText("ENCERRADO");
            holder.txtStatus.setBackgroundResource(R.drawable.bg_badge_encerrado);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onLoteClick(lote);
        });
    }

    @Override
    public int getItemCount() {
        return lotes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNumero, txtLinhagem, txtStatus;

        ViewHolder(View itemView) {
            super(itemView);
            txtNumero = itemView.findViewById(R.id.txtNumeroLoteResumo);
            txtLinhagem = itemView.findViewById(R.id.txtLinhagemResumo);
            txtStatus = itemView.findViewById(R.id.txtStatusBadge);
        }
    }
}
