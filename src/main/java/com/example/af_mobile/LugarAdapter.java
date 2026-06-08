package com.example.af_mobile;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LugarAdapter extends RecyclerView.Adapter<LugarAdapter.ViewHolder> {

    private List<Lugar> lista;

    public LugarAdapter(List<Lugar> lista) {
        this.lista = lista;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Lugar lugar = lista.get(position);

        holder.nome.setText(lugar.getNome());

        holder.info.setText(
                "Lat: " + lugar.getLatitude() +
                        " | Lon: " + lugar.getLongitude()
        );
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView nome;
        TextView info;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            nome = itemView.findViewById(android.R.id.text1);
            info = itemView.findViewById(android.R.id.text2);
        }
    }
}