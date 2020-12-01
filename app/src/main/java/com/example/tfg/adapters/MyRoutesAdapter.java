package com.example.tfg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.tfg.R;
import com.example.tfg.interfaces.ItemListener;
import com.example.tfg.objects.Route;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyRoutesAdapter extends RecyclerView.Adapter<MyRoutesAdapter.VistaRuta> {

    private List<Route> routesList;
    private ItemListener listener;

    public MyRoutesAdapter(List<Route> routes, ItemListener listener){
        this.routesList = routes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VistaRuta onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_route, parent, false);
        return new VistaRuta(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull VistaRuta holder, int position) {
        holder.nombreRuta.setText(routesList.get(position).getNombre());
    }

    @Override
    public int getItemCount() {
        return routesList.size();
    }


    protected static class VistaRuta extends RecyclerView.ViewHolder {

        private TextView nombreRuta;
        private ItemListener myListener;

        public VistaRuta(View parent, ItemListener listener){
            super(parent);
            this.nombreRuta = itemView.findViewById(R.id.nombreMiRuta);
            this.myListener = listener;
            parent.setClickable(true);
            parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myListener.itemOnClick(getAdapterPosition());
                }
            });
        }
    }
}
