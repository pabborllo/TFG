package com.example.tfg;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.example.tfg.db.DatabaseManager;
import com.example.tfg.objects.Lugar;
import com.example.tfg.objects.Route;
import com.example.tfg.utils.Functions;
import com.example.tfg.utils.GetMapsInformation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import java.util.List;

public class DisplayRoute extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap myMap;
    private String routeUrl;
    private Boolean currentPos;
    private List<Lugar> places;
    private List<String> placesNames;
    private Route route = new Route();
    private GetMapsInformation getMapsInformation;
    private RecyclerView buttonsView;
    private RecyclerView stepsView;
    private RecyclerView headerView;
    private Button tope;

    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    private ProgressBar progressBar;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_route);

        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        progressBar = findViewById(R.id.progressBarDisplay);
        progressBar.setClickable(true);

        tope = findViewById(R.id.tope);
        stepsView = findViewById(R.id.listaPasos);
        buttonsView = findViewById(R.id.listaButtons);
        headerView = findViewById(R.id.legHeader);

        routeUrl = getIntent().getExtras().getString("routeUrl");
        currentPos = getIntent().getExtras().getBoolean("currentPos");
        places = getIntent().getExtras().getParcelableArrayList("places");
        placesNames = Functions.getPlacesNames(places);
        if(currentPos && !placesNames.get(0).equals(getString(R.string.mi_ubicacion))){
            placesNames.add(0, getString(R.string.mi_ubicacion));
        }
        cargaMapa();
    }

    private void cargaMapa() {
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapRoute);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        lanzaRuta();
    }

    private void lanzaRuta(){
        Object[] objects = new Object[9];
        objects[0] = myMap;
        objects[1] = routeUrl;
        objects[2] = DisplayRoute.this;
        objects[3] = buttonsView;
        objects[4] = stepsView;
        objects[5] = route;
        objects[6] = placesNames;
        objects[7] = headerView;
        objects[8] = tope;

        getMapsInformation = new GetMapsInformation(objects, GetMapsInformation.MapDataType.ROUTE);
        getMapsInformation.execute();
    }

    private void confirmaGuardarRuta(){
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View vistaGuardar = getLayoutInflater().inflate(R.layout.vista_guardar_ruta_favoritos, null);

        final EditText nombre = vistaGuardar.findViewById(R.id.nombreRuta);

        Button guardar = vistaGuardar.findViewById(R.id.guardarRuta);
        Button cancelar = vistaGuardar.findViewById(R.id.cancelarGuardar);
        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!nombre.getText().toString().trim().isEmpty()){
                    alertDialog.dismiss();
                    subirRuta(nombre.getText().toString());
                }else{
                    Toast.makeText(DisplayRoute.this, getString(R.string.error_nombre_ruta), Toast.LENGTH_LONG).show();
                }
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaGuardar);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void subirRuta(String nombre){
        progressBar.setVisibility(View.VISIBLE);
        route = getMapsInformation.getRoute();
        route.setCurrentPos(currentPos);
        route.setPlaces(places);
        String userId = prefs.getString("userId", "null");
        DatabaseReference ref = new DatabaseManager().getRoutesRef();
        String routeId = getIntent().getExtras().getString("id", ref.child(userId).push().getKey());
        route.setId(routeId);
        route.setNombre(nombre);
        ref.child(userId)
                .child(routeId)
                .setValue(route)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getApplicationContext(), getString(R.string.ruta_guardada), Toast.LENGTH_LONG).show();
                Intent inicio = new Intent(DisplayRoute.this, Home.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(inicio);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), getString(R.string.error_guardar_ruta), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Devuelvo false/true dependiendo de si vengo de createRoute o MyRoutes para muestrar o no el menú de subir ruta
        Boolean fromCreateRoute = getIntent().getExtras().getBoolean("flag");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_save_route, menu);
        return fromCreateRoute;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.saveRoute) {
            confirmaGuardarRuta();
            return true;
        }else {
            return super.onOptionsItemSelected(item);
        }
    }
}