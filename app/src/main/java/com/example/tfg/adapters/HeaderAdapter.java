package com.example.tfg.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.tfg.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HeaderAdapter extends RecyclerView.Adapter<HeaderAdapter.Header> {

    private String head;

    public HeaderAdapter(String head){
        this.head = head;
    }

    @NonNull
    @Override
    public Header onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_header, parent, false);
        return new Header(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Header holder, int position) {
        holder.header.setText(head);
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    protected static class Header extends RecyclerView.ViewHolder{

        private TextView header;

        public Header(View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.headerText);
        }
    }
}
