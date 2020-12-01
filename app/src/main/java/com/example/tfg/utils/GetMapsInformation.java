package com.example.tfg.utils;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.Button;
import com.example.tfg.R;
import com.example.tfg.adapters.ButtonAdapter;
import com.example.tfg.adapters.HeaderAdapter;
import com.example.tfg.adapters.RouteAdapter;
import com.example.tfg.interfaces.ItemListener;
import com.example.tfg.objects.Leg;
import com.example.tfg.objects.Route;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class GetMapsInformation extends AsyncTask<Void, String, String> {

    public enum MapDataType {PLACE, ROUTE}

    //Propiedades compartidas
    private String downloadedData;
    private GoogleMap map;
    private String url;
    private MapDataType enumType;

    //Propiedades Places
    private HashMap<LatLng, String[]> placesMap = new HashMap<>();
    private HashMap<String, String[]> photosMap = new HashMap<>();

    //Propiedades Route
    private Route route;
    private List<String> placesNames;
    private Context context;
    private RecyclerView buttonsView;
    private RecyclerView stepsView;
    private RecyclerView headerView;
    private Button tope;

    public GetMapsInformation(Object[] objects, MapDataType enumType){
        this.enumType = enumType;
        this.map = (GoogleMap) objects[0];
        this.url = (String) objects[1];

        if(enumType.equals(MapDataType.ROUTE)){
            this.context = (Context) objects[2];
            this.buttonsView = (RecyclerView) objects[3];
            this.stepsView = (RecyclerView) objects[4];
            this.route = (Route) objects[5];
            this.placesNames = (List<String>) objects[6];
            this.headerView = (RecyclerView) objects[7];
            this.tope = (Button) objects[8];
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if(enumType.equals(MapDataType.ROUTE)){
            showHeader("");
        }
    }

    //Se ejecuta en 2º plano, hace la petición HTTP y devuelve un JSON en formato String
    @Override
    protected String doInBackground(Void... voids) {
        DownloadURL downloadURL = new DownloadURL();
        try {
            this.downloadedData = downloadURL.readUrl(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return downloadedData;
    }

    //Usa el JSON en formato String para crear la lista de maps con lugares asociados y los manda para representarlos en el GoogleMap
    @Override
    protected void onPostExecute(String s) {
        switch (enumType){
            case PLACE:
                showPlacesData(s);
                break;
            case ROUTE:
                showButtons(s);
                break;
            default:
                break;
        }
    }

    private void showPlacesData(String s){
        List<HashMap<String, String>> placesData;
        DataParser dataParser = new DataParser();
        placesData = dataParser.parsePlacesData(s);
        drawPlaceMarkers(placesData);
    }

    //De la lista de maps de lugares obtenida, construyo un mapa conteniendo sólo la posición y la ID de cada lugar
    //Creo marcadores con esa información y los posiciono en el GoogleMap
    private void drawPlaceMarkers(List<HashMap<String, String>> placesData){

        int count = placesData.size();
        for(int i=0; i<count; i++){
            MarkerOptions markerOptions = new MarkerOptions();
            HashMap<String, String> googlePlace = placesData.get(i);

            String place_id = googlePlace.get("place_id");
            String placeName = googlePlace.get("place_name");
            String vicinity = googlePlace.get("vicinity");
            String open = googlePlace.get("open");
            String photoReference = googlePlace.get("photoReference");
            String photoHeight = googlePlace.get("photoHeight");
            String photoWidth = googlePlace.get("photoWidth");
            double lat = Double.parseDouble(googlePlace.get("lat"));
            double lng = Double.parseDouble(googlePlace.get("lng"));

            LatLng latLng = new LatLng(lat, lng);
            markerOptions.position(latLng);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            map.addMarker(markerOptions);

            String[] placeDetails = new String[4];
            placeDetails[0] = place_id;
            placeDetails[1] = placeName;
            placeDetails[2] = vicinity;
            placeDetails[3] = open;
            placesMap.put(latLng, placeDetails);

            String[] photoDetails = new String[3];
            photoDetails[0] = photoReference;
            photoDetails[1] = photoHeight;
            photoDetails[2] = photoWidth;
            photosMap.put(place_id, photoDetails);
        }
    }

    public HashMap<LatLng, String[]> getPlacesMap(){
        return placesMap;
    }

    public HashMap<String, String[]> getPhotosMap(){
        return photosMap;
    }

    private void showButtons(String s) {
        //Cargar Ruta de vista DisplayRoute y mostrar tantos botones como tramos haya
        DataParser dataParser = new DataParser(placesNames);
        route = dataParser.parseRoute(s, url);
        final List<Leg> legs = route.getLegs();
        ButtonAdapter adapter = new ButtonAdapter(context, legs, new ItemListener() {
            @Override
            public void itemOnClick(int posicion) {
                showHeader(legs.get(posicion).getCabecera());
                showRouteLeg(legs.get(posicion));
            }
        });
        buttonsView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        buttonsView.setAdapter(adapter);
        setListView();
        drawPolylines(s);
    }

    private void showHeader(String header){
        HeaderAdapter adapter = new HeaderAdapter(header);
        headerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        headerView.setAdapter(adapter);
    }

    private void showRouteLeg(Leg leg){
        setListView();
        RouteAdapter adapter = new RouteAdapter(leg.getSteps());
        stepsView.setLayoutManager(new LinearLayoutManager(context));
        stepsView.setAdapter(adapter);
    }

    private void setListView() {
        int headerInicio = (int) headerView.getY();
        int headerTam = headerView.getHeight();
        int headerBottom = headerInicio + headerTam;
        int topeInicio = (int) tope.getY();
        int pxMargins = (int) (10 * context.getResources().getDisplayMetrics().density);
        int usableSpace = topeInicio - headerBottom - pxMargins;
        ConstraintLayout.LayoutParams constraintsParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, usableSpace);
        constraintsParams.bottomToTop = R.id.tope;
        constraintsParams.leftToLeft = R.id.parent;
        constraintsParams.bottomMargin = pxMargins / 2;
        stepsView.setLayoutParams(constraintsParams);
    }

    private void drawPolylines(String s){
        DataParser dataParser = new DataParser();
        List<Pair<Integer, List<LatLng>>> lista = dataParser.getRoutePositions(s);
        //Añado marcador a inicio de ruta
        MarkerOptions startMarker = new MarkerOptions().position(lista.get(0).second.get(0))
                .title(context.getResources().getString(R.string.inicio))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        map.addMarker(startMarker);
        //Si sólo hay un tramo, dibujo el último punto y los uno
        if(lista.size()==1){
            LatLng latLngInicio = lista.get(0).second.get(0);
            LatLng latLngFinal = lista.get(0).second.get(lista.get(0).second.size()-1);
            MarkerOptions marker = new MarkerOptions().position(latLngFinal)
                    .title(context.getResources().getString(R.string.fin))
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
            map.addMarker(marker);
            map.addPolyline(new PolylineOptions().add(latLngInicio, latLngFinal)
                    .color(Color.RED).width(12).startCap(new RoundCap()));
        }else{
            //Dibujo lineas y añado marcador a lugares restantes (final de cada tramo)
            for(int i=0; i<lista.size()-1; i++){
                LatLng inicioTramo = lista.get(i).second.get(0);
                LatLng finalTramo = lista.get(i+1).second.get(0);
                map.addPolyline(new PolylineOptions().add(inicioTramo, finalTramo)
                        .color(Color.RED).width(12).startCap(new RoundCap()));

                MarkerOptions endMarker = new MarkerOptions().position(finalTramo)
                        .title(String.format("%s", i+1)+context.getResources().getString(R.string.parada))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                map.addMarker(endMarker);

                if(i == lista.size()-2){
                    inicioTramo = finalTramo;
                    LatLng finalTramoFinal = lista.get(i+1).second.get(lista.get(i+1).second.size()-1);
                    finalTramo = finalTramoFinal;
                    map.addPolyline(new PolylineOptions().add(inicioTramo, finalTramo).color(Color.RED).width(12).startCap(new RoundCap()));
                    endMarker = new MarkerOptions().position(finalTramo).title(context.getResources().getString(R.string.fin))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                    map.addMarker(endMarker);
                }
            }
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                lista.get(0).second.get(0), 13));
    }

    public Route getRoute(){
        return route;
    }
}
