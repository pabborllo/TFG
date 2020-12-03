package com.example.tfg.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.tfg.CreateRouteMap;
import com.example.tfg.CreateRouteAutocomplete;
import com.example.tfg.R;
import com.example.tfg.objects.Lugar;

import java.util.ArrayList;

public class BuscarRutasFragment extends Fragment implements View.OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button mapButton = view.findViewById(R.id.mapButton);
        Button listButton = view.findViewById(R.id.listButton);

        mapButton.setOnClickListener(this);
        listButton.setOnClickListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_buscar_rutas, container, false);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.mapButton:
                loadIntent(new Intent(getContext(), CreateRouteMap.class));
                break;
            case R.id.listButton:
                loadIntent(new Intent(getContext(), CreateRouteAutocomplete.class));
                break;
            default:
                break;
        }
    }

    private void loadIntent(Intent showMap){
        Bundle bundle = new Bundle();
        bundle.putBoolean("flag", true);
        showMap.putExtras(bundle);
        startActivity(showMap);
    }
}
