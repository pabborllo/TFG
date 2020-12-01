package com.example.tfg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.tfg.R;
import com.example.tfg.objects.Step;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.Vista> {

    private List<Step> steps;

    public RouteAdapter(List<Step> route){
        this.steps = route;
    }

    @NonNull
    @Override
    public Vista onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_lugar, parent, false);
        return new Vista(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Vista holder, int position) {
        holder.nombrePlace.setText(this.steps.get(position).getInstructions());
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    protected static class Vista extends RecyclerView.ViewHolder {

        private TextView nombrePlace;

        public Vista(View parent){
            super(parent);
            this.nombrePlace = itemView.findViewById(R.id.nombrePlace);
        }
    }
}
