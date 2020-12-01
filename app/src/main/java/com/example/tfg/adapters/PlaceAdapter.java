package com.example.tfg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.tfg.objects.Lugar;
import com.example.tfg.R;
import com.example.tfg.interfaces.ItemListener;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class PlaceAdapter extends RecyclerView.Adapter<PlaceAdapter.Vista> {

    private List<Lugar> lugares;
    private ItemListener listener;

    public PlaceAdapter(List<Lugar> lugares, ItemListener lugarListener){
        this.lugares = lugares;
        this.listener = lugarListener;
    }

    @Override
    @NonNull
    public Vista onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lugar, parent, false);
        return new Vista(v, listener);
    }

    @Override
    public void onBindViewHolder(Vista holder, int position) {
        holder.nombrePlace.setText(this.lugares.get(position).getTitulo());
    }

    @Override
    public int getItemCount() {
        return lugares.size();
    }


    protected static class Vista extends RecyclerView.ViewHolder {

        private TextView nombrePlace;
        private ItemListener listener;

        public Vista(View parent, final ItemListener listener){
            super(parent);
            this.nombrePlace = itemView.findViewById(R.id.nombrePlace);
            this.listener = listener;
            parent.setClickable(true);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listener.itemOnClick(getAdapterPosition());
                }
            });
        }
    }
}
