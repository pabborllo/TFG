package com.example.tfg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.tfg.adapters.MyRoutesAdapter;
import com.example.tfg.db.DatabaseManager;
import com.example.tfg.interfaces.ItemListener;
import com.example.tfg.objects.Leg;
import com.example.tfg.objects.Lugar;
import com.example.tfg.objects.Route;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MyRoutes extends AppCompatActivity {

    private DatabaseReference ref;
    private String id;
    private List<Route> routes = new ArrayList<>();

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialogHandle;
    private AlertDialog alertDialogElijeMapa;
    private AlertDialog alertDialogConfirmaElimina;

    private ImageView myRoutes;
    private RecyclerView listaRutas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_routes);

        myRoutes = findViewById(R.id.myRoutes_bg);
        Glide.with(MyRoutes.this).load(R.drawable.mis_rutas).into(myRoutes);
        listaRutas = findViewById(R.id.myRoutesList);

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyRoutes.this);
        id = prefs.getString("userId", "null");
        ref = new DatabaseManager().getUsersRef().child(id).child("routes");

        getMyRoutes();
        myRoutes.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                myRoutes.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setListView(){
        int density = (int) MyRoutes.this.getResources().getDisplayMetrics().density;
        int listInicio = (int) listaRutas.getY();
        int heighUsable = MyRoutes.this.getResources().getDisplayMetrics().heightPixels - listInicio;
        int listTAM = Math.min(routes.size()*60*density, 60*heighUsable/100);
        ConstraintLayout.LayoutParams constraints = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, listTAM);
        constraints.topToBottom = R.id.cabeceraMisRutas;
        constraints.leftToLeft = R.id.parent;
        constraints.rightToRight = R.id.parent;
        constraints.setMargins(15*density, 25*density, 15*density, 25*density);
        listaRutas.setBackground(MyRoutes.this.getDrawable(R.drawable.borders));
        listaRutas.setLayoutParams(constraints);
    }

    private void getMyRoutes(){
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                routes.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    ArrayList<Lugar> lugares = new ArrayList<>();
                    Iterator<DataSnapshot> it = ds.child("places").getChildren().iterator();
                    while(it.hasNext()){
                        DataSnapshot placeSnapshot = it.next();
                        String id = placeSnapshot.child("id").getValue(String.class);
                        Double latitude = placeSnapshot.child("posicion").child("latitude").getValue(Double.class);
                        Double longitude = placeSnapshot.child("posicion").child("longitude").getValue(Double.class);
                        LatLng posicion = new LatLng(latitude, longitude);
                        String titulo = placeSnapshot.child("titulo").getValue(String.class);
                        lugares.add(new Lugar(id, posicion, titulo));
                    }

                    List<Leg> legs = new ArrayList<>();
                    for(DataSnapshot dsLeg: ds.child("legs").getChildren()) {
                        Leg leg = dsLeg.getValue(Leg.class);
                        legs.add(leg);
                    }

                    String id = ds.getKey();
                    Boolean currenPos = ds.child("currentPos").getValue(Boolean.class);
                    String nombre = ds.child("nombre").getValue(String.class);
                    String url = ds.child("url").getValue(String.class);

                    Route route = new Route(url, legs);
                    route.setId(id);
                    route.setCurrentPos(currenPos);
                    route.setNombre(nombre);
                    route.setPlaces(lugares);
                    routes.add(route);
                }
                mostrarRutas(routes);
                setListView();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_recuperar_datos), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void mostrarRutas(final List<Route> mRoutes){
        MyRoutesAdapter myRoutesAdapter = new MyRoutesAdapter(mRoutes, new ItemListener() {
            @Override
            public void itemOnClick(int posicion) {
                handleRoute(mRoutes.get(posicion));
            }
        });
        listaRutas.setLayoutManager(new LinearLayoutManager(MyRoutes.this));
        listaRutas.setAdapter(myRoutesAdapter);
    }

    private void handleRoute(final Route route){
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View vistaMiRuta = getLayoutInflater().inflate(R.layout.vista_my_fav_route, null);

        final RadioGroup accion = vistaMiRuta.findViewById(R.id.accionRutaFav);
        Button listo = vistaMiRuta.findViewById(R.id.listoButton);
        Button cancelar = vistaMiRuta.findViewById(R.id.cancelarButton);

        listo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int check = accion.getCheckedRadioButtonId();
                if(check == -1){
                    Toast.makeText(getApplicationContext(), getString(R.string.elija_opcion), Toast.LENGTH_LONG).show();
                }else{
                    switch (check){
                        case R.id.cargarRuta:
                            alertDialogHandle.dismiss();
                            loadIntent(route, new Intent(MyRoutes.this, DisplayRoute.class));
                            break;
                        case R.id.editarRuta:
                            chooseMap(route);
                            break;
                        case R.id.borrarRuta:
                            confirmDelete(route);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogHandle.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaMiRuta);
        alertDialogHandle = alertDialogBuilder.create();
        alertDialogHandle.show();
    }

    private void chooseMap(final Route route){
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View vistaElijeMapa = getLayoutInflater().inflate(R.layout.vista_tipo_mapa, null);

        final RadioGroup accion = vistaElijeMapa.findViewById(R.id.mapType);
        Button listo = vistaElijeMapa.findViewById(R.id.listoButton2);
        Button cancelar = vistaElijeMapa.findViewById(R.id.cancelarButton2);

        listo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int check = accion.getCheckedRadioButtonId();
                if(check == -1){
                    Toast.makeText(getApplicationContext(), getString(R.string.elija_opcion), Toast.LENGTH_LONG).show();
                }else{
                    switch (check){
                        case R.id.mapa:
                            alertDialogElijeMapa.dismiss();
                            alertDialogHandle.dismiss();
                            loadIntent(route, new Intent(MyRoutes.this, CreateRouteMap.class));
                            break;
                        case R.id.autocomplete:
                            alertDialogElijeMapa.dismiss();
                            alertDialogHandle.dismiss();
                            loadIntent(route, new Intent(MyRoutes.this, CreateRouteAutocomplete.class));
                            break;
                        default:
                            break;
                    }
                }
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogElijeMapa.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaElijeMapa);
        alertDialogElijeMapa = alertDialogBuilder.create();
        alertDialogElijeMapa.show();
    }

    private void confirmDelete(final Route route){
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View vistaBorraRuta = getLayoutInflater().inflate(R.layout.vista_eliminar_ruta, null);

        TextView cabecera = vistaBorraRuta.findViewById(R.id.cabeceraBorrarRuta);
        String nombre = route.getNombre();
        if(nombre.length()>9){
            nombre = nombre.substring(0, 6) + "...";
        }
        cabecera.setText(getString(R.string.alert_seguro)+"\n"+nombre+"?");
        Button aceptar = vistaBorraRuta.findViewById(R.id.eliminarRutaButton);
        Button cancelar = vistaBorraRuta.findViewById(R.id.noEliminarRutaButton);

        aceptar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogConfirmaElimina.dismiss();
                alertDialogHandle.dismiss();
                deleteRoute(route);
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialogConfirmaElimina.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaBorraRuta);
        alertDialogConfirmaElimina = alertDialogBuilder.create();
        alertDialogConfirmaElimina.show();
    }

    private void deleteRoute(Route route){
        ref.child(route.getId()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), getString(R.string.ruta_borrada), Toast.LENGTH_LONG).show();

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.error_borrar_ruta), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadIntent(Route route, Intent showRoute){
        Bundle bundle = new Bundle();
        ArrayList<Lugar> places = new ArrayList<>(route.getPlaces());
        bundle.putString("routeUrl", route.getUrl());
        bundle.putString("id", route.getId());
        bundle.putBoolean("currentPos", route.getCurrentPos());
        bundle.putParcelableArrayList("places", places);
        bundle.putBoolean("flag", false);
        showRoute.putExtras(bundle);
        startActivity(showRoute);
    }
}