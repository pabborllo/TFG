package com.example.tfg;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.tfg.adapters.PlaceAdapter;
import com.example.tfg.utils.GetMapsInformation;
import com.example.tfg.utils.Functions;
import com.example.tfg.utils.URLConstructor;
import com.example.tfg.interfaces.ItemListener;
import com.example.tfg.objects.Lugar;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CreateRouteAutocomplete extends AppCompatActivity implements
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, View.OnClickListener{

    private Button buscador;
    private static int AUTOCOMPLETE_REQUEST_CODE = 1001;
    private final int LOCATION_REQUEST_CODE = 1002;
    private List<Place.Field> campos = Arrays.asList(Place.Field.NAME, Place.Field.ID, Place.Field.LAT_LNG,
            Place.Field.PHONE_NUMBER, Place.Field.WEBSITE_URI);
    private Place place;

    private FusedLocationProviderClient myFusedLocationProviderClient;
    private Location myLastKnownLocation;
    private LatLng miPosicionOriginal;

    private GoogleMap myMap;
    private HashMap<LatLng, String[]> placeMap = new HashMap<>();
    private HashMap<String, String[]> photoMap = new HashMap<>();

    //Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    //Route
    private ArrayList<Lugar> places = new ArrayList<>();
    private Button crearRuta;
    private RecyclerView vista;
    private FrameLayout mapInicio;

    //Vista del lugar elegido
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;

    private Boolean fromFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Restaurar posición actual y cámara si entro por vez > 1ª
        if (savedInstanceState != null) {
            myLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        setContentView(R.layout.activity_create_route_autocomplete);

        Places.initialize(CreateRouteAutocomplete.this, getString(R.string.GOOGLE_MAPS_API_KEY));
        myFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CreateRouteAutocomplete.this);

        //Botón para lanzar Intent y crear ruta
        crearRuta = findViewById(R.id.createRouteAutocomplete);
        crearRuta.setOnClickListener(this);
        fromFragment = getIntent().getExtras().getBoolean("flag");

        buscador = findViewById(R.id.buscador);
        buscador.setOnClickListener(this);
        mapInicio = findViewById(R.id.mapFrameAutocomplete);
        vista = findViewById(R.id.listaPlacesAutocomplete);

        //Comprobar permisos y construir mapa
        if (ContextCompat.checkSelfPermission(
                CreateRouteAutocomplete.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    CreateRouteAutocomplete.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            cargaMapa();
        }
        buscador.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setMapView();
                buscador.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setMapView(){
        int pxMargins = (int) (10 * CreateRouteAutocomplete.this.getResources().getDisplayMetrics().density);
        int buscadorInicio = (int) buscador.getY();
        int buscadorTAM = buscador.getHeight();
        int crearInicio = (int) crearRuta.getY();
        int usableScreen = crearInicio - (buscadorInicio + buscadorTAM);
        int mapScreen = 60*usableScreen/100;
        ConstraintLayout.LayoutParams constraintsParamsMAP = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mapScreen);
        constraintsParamsMAP.topToBottom = R.id.buscador;
        constraintsParamsMAP.leftToLeft = R.id.parent;
        constraintsParamsMAP.topMargin = pxMargins / 2;
        mapInicio.setBackground(CreateRouteAutocomplete.this.getDrawable(R.drawable.borders));
        mapInicio.setLayoutParams(constraintsParamsMAP);

        mapInicio.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setListView();
                mapInicio.getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    private void setListView(){
        if(!fromFragment){
            places = getIntent().getExtras().getParcelableArrayList("places");
            mostrarDatos(places);
        }
        int pxMargins = (int) (10 * CreateRouteAutocomplete.this.getResources().getDisplayMetrics().density);
        int listInicio = (int) vista.getY();
        int buttonInicio = (int) crearRuta.getY();
        int usableSpace = buttonInicio - listInicio - pxMargins;
        ConstraintLayout.LayoutParams constraintsParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, usableSpace);
        constraintsParams.topToBottom = R.id.mapFrameAutocomplete;
        constraintsParams.leftToLeft = R.id.parent;
        constraintsParams.topMargin = pxMargins / 2;
        vista.setBackground(CreateRouteAutocomplete.this.getDrawable(R.drawable.borders));
        vista.setLayoutParams(constraintsParams);
    }

    //Guardar posición actual y cámara al acceder por 1ª vez
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        if (myMap != null) {
            outState.putParcelable(KEY_LOCATION, myLastKnownLocation);
            outState.putParcelable(KEY_CAMERA_POSITION, myMap.getCameraPosition());
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cargaMapa();
            } else {
                Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_ubicacion), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void cargaMapa() {
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapBuscadorAutocomplete);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        myMap = googleMap;
        if (ContextCompat.checkSelfPermission(
                CreateRouteAutocomplete.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    CreateRouteAutocomplete.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        myMap.setMyLocationEnabled(true);
        myMap.setOnMarkerClickListener(this);

        getPosicionActual();
        setListView();
    }

    private void getPosicionActual(){
        if (ContextCompat.checkSelfPermission(
                CreateRouteAutocomplete.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    CreateRouteAutocomplete.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        Task<Location> locationResult = myFusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Get the current location of the device.
                    if (task.getResult() != null) {
                        myLastKnownLocation = task.getResult();
                        miPosicionOriginal = new LatLng(myLastKnownLocation.getLatitude(), myLastKnownLocation.getLongitude());
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                miPosicionOriginal, 15));
                        myMap.addMarker(new MarkerOptions().position(miPosicionOriginal)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    } else {
                        Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_obt_ubicacion), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_obt_ubicacion), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(!Functions.compruebaExistencia(marker.getPosition(), places)){
            if(!marker.getPosition().equals(miPosicionOriginal)){
                String place_id = placeMap.get(marker.getPosition())[0];
                String place_name = placeMap.get(marker.getPosition())[1];
                String place_address = placeMap.get(marker.getPosition())[2];
                String open = "Sin horario";
                if(!placeMap.get(marker.getPosition())[3].equals("-NA-")){
                    if(placeMap.get(marker.getPosition())[3].equals("true")){
                        open = "Abierto ahora";
                    }else if(placeMap.get(marker.getPosition())[3].equals("false")){
                        open = "Cerrado ahora";
                    }
                }
                String[] photo = photoMap.get(place_id);
                String photoUrl;
                if(photo[0].equals("-NA-")){
                    photoUrl = "NOPHOTO";
                }else{
                    photoUrl = URLConstructor.getUrlPhoto(photo, getString(R.string.GOOGLE_MAPS_API_KEY));
                }
                mostrarLugar(marker.getPosition(), place_name, place_id, place_address, open, photoUrl);
            }
        }else{
            Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_lugar_ya_agregado), Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                place = Autocomplete.getPlaceFromIntent(data);
                recuperaDetallesLugar();
            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_enc_lugar), Toast.LENGTH_LONG).show();
                Status status = Autocomplete.getStatusFromIntent(data);
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
            }
            return;
        }
    }

    private void recuperaDetallesLugar(){
        myMap.clear();
        myMap.addMarker(new MarkerOptions().position(miPosicionOriginal)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        String placeDetailsUrl = URLConstructor.getUrlPlaceDetails(place.getId(), getString(R.string.GOOGLE_MAPS_API_KEY));
        Object[] dataTransfer = new Object[2];
        dataTransfer[0] = myMap;
        dataTransfer[1] = placeDetailsUrl;
        GetMapsInformation getMapsInformation = new GetMapsInformation(dataTransfer, GetMapsInformation.MapDataType.PLACE);
        getMapsInformation.execute();
        placeMap = getMapsInformation.getPlacesMap();
        photoMap = getMapsInformation.getPhotosMap();
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15));
    }

    //Carga la vista con los datos del lugar
    private void mostrarLugar(final LatLng posicion, final String title, final String id, String direccion, String abierto, String photoUrl){
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View vistaLugar = getLayoutInflater().inflate(R.layout.vista_agregar_lugar, null);

        ImageView fotoLugar = vistaLugar.findViewById(R.id.fotoLugar);
        TextView nombreLugar = vistaLugar.findViewById(R.id.nombreLugar);
        TextView direccionLugar = vistaLugar.findViewById(R.id.direccionLugar);
        TextView open = vistaLugar.findViewById(R.id.estaAbierto);
        ImageView tlf = vistaLugar.findViewById(R.id.imagenTlf);
        tlf.setClickable(true);
        tlf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(place.getPhoneNumber()!=null){
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:"+place.getPhoneNumber()));
                    startActivity(callIntent);
                }else{
                    Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_sin_numero_tlf), Toast.LENGTH_SHORT).show();
                }
            }
        });

        ImageView web = vistaLugar.findViewById(R.id.imagenWeb);
        web.setClickable(true);
        web.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(place.getWebsiteUri()!=null){
                    Intent webIntent = new Intent(Intent.ACTION_VIEW);
                    webIntent.setData(place.getWebsiteUri());
                    startActivity(webIntent);
                }else{
                    Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_sin_sitio_web), Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(photoUrl.equals("NOPHOTO")){
            Glide.with(CreateRouteAutocomplete.this)
                    .load(R.drawable.no_image)
                    .into(fotoLugar);
        }else{
            Glide.with(CreateRouteAutocomplete.this)
                    .load(Uri.parse(photoUrl))
                    .into(fotoLugar);
        }
        nombreLugar.setText(title);
        direccionLugar.setText(direccion);
        open.setText(abierto);

        Button añadirLugar = vistaLugar.findViewById(R.id.agregarLugar);
        Button cancelarLugar = vistaLugar.findViewById(R.id.cancelarLugar);
        añadirLugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(places.size()<10){
                    places.add(new Lugar(id, posicion, title));
                    mostrarDatos(places);
                    Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.lugar_agregado), Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }else{
                    Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.error_10_lugares), Toast.LENGTH_LONG).show();
                    alertDialog.dismiss();
                }
            }
        });
        cancelarLugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaLugar);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //Confirmar eliminar lugar de ruta
    private void confirmaEliminarLugar(String nombre, final int posicion){
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View vistaLugar = getLayoutInflater().inflate(R.layout.vista_eliminar_lugar, null);

        TextView tituloLugar = vistaLugar.findViewById(R.id.tituloLugar);
        tituloLugar.setText(nombre);

        Button elimina = vistaLugar.findViewById(R.id.eliminarLugar);
        Button cancelar = vistaLugar.findViewById(R.id.cancelar);
        elimina.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                places.remove(posicion);
                mostrarDatos(places);
                Toast.makeText(CreateRouteAutocomplete.this, getString(R.string.lugar_eliminado), Toast.LENGTH_LONG).show();
                alertDialog.dismiss();
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaLugar);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //Elegir inicio y optimización de ruta
    private void configuraRuta(){
        alertDialogBuilder = new AlertDialog.Builder(this);
        final View vistaConfigura = getLayoutInflater().inflate(R.layout.vista_configura_ruta, null);

        final Boolean[] optionsChecked = new Boolean[2];

        final RadioGroup comenzarGroup = vistaConfigura.findViewById(R.id.comenzarDesdeGroup);
        final RadioGroup optimizarGroup = vistaConfigura.findViewById(R.id.optimizarGroup);
        Button aceptarRuta = vistaConfigura.findViewById(R.id.aceptarRuta);
        Button cancelarRuta = vistaConfigura.findViewById(R.id.cancelarRuta);

        aceptarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int inicioDeRuta = comenzarGroup.getCheckedRadioButtonId();
                int optimizarRuta = optimizarGroup.getCheckedRadioButtonId();
                if(inicioDeRuta != -1 && optimizarRuta != -1){
                    Boolean[] configuracion = Functions.checkConfiguracion(inicioDeRuta, optimizarRuta);
                    optionsChecked[0] = configuracion[0];
                    optionsChecked[1] = configuracion[1];
                    String routeUrl = URLConstructor.getUrlRoute(
                            optionsChecked, miPosicionOriginal, Functions.getPlacesIDs(places),
                            getString(R.string.GOOGLE_MAPS_API_KEY), getString(R.string.idioma));
                    alertDialog.dismiss();
                    loadDisplayRoute(routeUrl, configuracion[0]);
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.error_configure), Toast.LENGTH_LONG).show();
                }
            }
        });

        cancelarRuta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaConfigura);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void loadDisplayRoute(String routeUrl, Boolean currentPos){
        Intent creaRuta = new Intent(CreateRouteAutocomplete.this, DisplayRoute.class);
        Bundle bundle = new Bundle();
        if(!fromFragment){
            bundle.putString("id", getIntent().getExtras().getString("id"));
        }
        bundle.putString("routeUrl", routeUrl);
        bundle.putBoolean("currentPos", currentPos);
        bundle.putParcelableArrayList("places", places);
        bundle.putBoolean("flag", true);
        creaRuta.putExtras(bundle);
        startActivity(creaRuta);
    }

    //Renderiza lista con lugares agregados a ruta
    private void mostrarDatos(final List<Lugar> places){
        PlaceAdapter adaptador = new PlaceAdapter(places, new ItemListener() {
            public void itemOnClick(int posicion) {
                Lugar lugar = places.get(posicion);
                confirmaEliminarLugar(lugar.getTitulo(), posicion);
            }
        });
        vista.setLayoutManager(new LinearLayoutManager(CreateRouteAutocomplete.this));
        vista.setAdapter(adaptador);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.createRouteAutocomplete:
                if(places.size()>=2){
                    configuraRuta();
                }else{
                    Toast.makeText(getApplicationContext(), getString(R.string.error_2_lugares),Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.buscador:
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, campos)
                        .build(getApplicationContext());
                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);
                break;
            default:
                break;
        }
    }
}
