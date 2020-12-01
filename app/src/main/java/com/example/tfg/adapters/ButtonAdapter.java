package com.example.tfg.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.tfg.R;
import com.example.tfg.interfaces.ItemListener;
import com.example.tfg.objects.Leg;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ButtonAdapter extends RecyclerView.Adapter<ButtonAdapter.LegButton> {

    private List<Leg> legs;
    private ItemListener listener;
    private Context context;

    public ButtonAdapter(Context context, List<Leg> myLegs, ItemListener listener){
        this.context = context;
        this.legs = myLegs;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LegButton onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_button, parent, false);
        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topToTop = R.id.parent;
        params.leftToLeft = R.id.parent;
        int pxMargins = (int) (5 * parent.getResources().getDisplayMetrics().density);
        int height = (int) (35 * parent.getResources().getDisplayMetrics().density);
        params.rightMargin = pxMargins;
        params.height = height;
        v.setLayoutParams(params);
        return new LegButton(v, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull LegButton holder, int position) {
        holder.button.setText(position+1+context.getResources().getString(R.string.tramo));
    }

    @Override
    public int getItemCount() {
        return legs.size();
    }

    protected static class LegButton extends RecyclerView.ViewHolder {

        private Button button;
        private ItemListener myListener;

        public LegButton(View parent, ItemListener listener){
            super(parent);
            this.button = parent.findViewById(R.id.legButton);
            this.myListener = listener;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    myListener.itemOnClick(getAdapterPosition());
                }
            });
        }
    }
}
