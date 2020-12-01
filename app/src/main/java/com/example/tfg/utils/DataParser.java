package com.example.tfg.utils;

import android.util.Pair;
import com.example.tfg.objects.Leg;
import com.example.tfg.objects.Route;
import com.example.tfg.objects.Step;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DataParser {

    private List<String> placesNames;
    private Integer[] waypointsOrder;
    private List<String> waypointsNames;

    public DataParser(){}

    public DataParser(List<String> names){
        this.placesNames = names;
        if(names.size()>2){
            this.waypointsOrder = new Integer[placesNames.size()-2];
            this.waypointsNames = placesNames.subList(1, placesNames.size()-1);
        }
    }

    //Convierto la String a un JSON y mando el JSONArray "results",
    // que contiene los resultados de todos los lugares encontrados, para parsearlo
    public List<HashMap<String, String>> parsePlacesData(String jsonData){
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonData);
            if(!jsonObject.isNull("results")) {
                jsonArray = jsonObject.getJSONArray("results");
            }
            if(!jsonObject.isNull("result")) {
                JSONObject result = jsonObject.getJSONObject("result");
                jsonArray.put(result);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPlaces(jsonArray);
    }

    //Cada JSON (lugar) dentro del JSONArray "results", lo mando a convertir en un map y lo meto en una lista
    private List<HashMap<String, String>> getPlaces(JSONArray jsonArray){

        int count = jsonArray.length();
        List<HashMap<String, String>> placesList = new ArrayList<>();
        HashMap<String, String> placeMap;

        for(int i=0; i<count; i++){
            try {
                //placeMap = getPlace((JSONObject) jsonArray.get(i));
                placeMap = getPlace(jsonArray.getJSONObject(i));
                placesList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placesList;
    }

    //Extraigo la información del JSON y lo convierto en un map con las propiedades del lugar
    private HashMap<String, String> getPlace(JSONObject googlePlaceJson){
        HashMap<String, String> googlePlaceMap = new HashMap<>();
        String placeName = "-NA-";
        String vicinity = "-NA-";
        String place_id = "-NA-";
        String latitude = "-NA-";
        String longitude = "-NA-";
        String reference = "-NA-";
        String open = "-NA-";
        String photoReference = "-NA-";
        String photoHeight = "-NA-";
        String photoWidth = "-NA-";

        try{
            if(!googlePlaceJson.isNull("name")){
                placeName = googlePlaceJson.getString("name");
            }
            if(!googlePlaceJson.isNull("vicinity")){
                vicinity = googlePlaceJson.getString("vicinity");
            }
            if(!googlePlaceJson.isNull("opening_hours")){
                open = googlePlaceJson.getJSONObject("opening_hours").getString("open_now");
            }
            if(!googlePlaceJson.isNull("photos")){
                JSONArray photoArray = googlePlaceJson.getJSONArray("photos");
                JSONObject photoObject = (JSONObject) photoArray.get(0);
                photoReference = photoObject.getString("photo_reference");
                photoHeight = photoObject.getString("height");
                photoWidth = photoObject.getString("width");
            }
            if(!googlePlaceJson.isNull("place_id")){
                place_id = googlePlaceJson.getString("place_id");
            }
            if(!googlePlaceJson.isNull("geometry")){
                latitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
                longitude = googlePlaceJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            }
            if(!googlePlaceJson.isNull("reference")){
                reference = googlePlaceJson.getString("reference");
            }

            googlePlaceMap.put("place_name", placeName);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("place_id", place_id);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);
            googlePlaceMap.put("open", open);
            googlePlaceMap.put("photoReference", photoReference);
            googlePlaceMap.put("photoHeight", photoHeight);
            googlePlaceMap.put("photoWidth", photoWidth);
        }catch (JSONException e) {
            e.printStackTrace();
        }

        return googlePlaceMap;
    }

    public Route parseRoute(String stringJson, String url){
        JSONObject jsonObject;
        JSONObject jsonRoute = new JSONObject();
        try{
            jsonObject = new JSONObject(stringJson);
            if(!jsonObject.isNull("routes")) {
                jsonRoute = jsonObject.getJSONArray("routes").getJSONObject(0);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        Route route = getRoute(url, jsonRoute);
        return route;
    }

    private Route getRoute(String url, JSONObject jsonRoute){
        JSONArray legsArray = new JSONArray();
        List<Leg> legs = new ArrayList<>();
        JSONArray waypoints;
        try{
            if(!jsonRoute.isNull("legs")){
                legsArray = jsonRoute.getJSONArray("legs");
            }
            if(!jsonRoute.isNull("waypoint_order")){
                waypoints = jsonRoute.getJSONArray("waypoint_order");
                for(int j=0; j<waypoints.length(); j++){
                    waypointsOrder[j] = Integer.parseInt(String.valueOf(waypoints.get(j)));
                }
            }
            for(int i=0; i<legsArray.length(); i++){
                legs.add(getLeg(legsArray.getJSONObject(i), i));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return new Route(url, legs);
    }

    private Leg getLeg(JSONObject jsonLeg, int pos){
        JSONArray stepsArray = new JSONArray();
        String distance = "-NA-";
        String duration = "-NA-";
        List<Step> steps = new ArrayList<>();
        String cabecera;
        if(placesNames.size() == 2){
            cabecera = placesNames.get(0) + " -> " + placesNames.get(1);
        }else{
            if(pos == 0){
                cabecera = placesNames.get(0) + " -> " + waypointsNames.get(waypointsOrder[0]);
            }else if(pos == placesNames.size() - 2){
                cabecera = waypointsNames.get(waypointsOrder[pos-1]) + " -> " + placesNames.get(placesNames.size()-1);
            }else{
                cabecera = waypointsNames.get(waypointsOrder[pos-1]) + " -> " + waypointsNames.get(waypointsOrder[pos]);
            }
        }
        try{
            if(!jsonLeg.isNull("distance")){
                distance = jsonLeg.getJSONObject("distance").getString("text");
            }
            if(!jsonLeg.isNull("duration")){
                duration = jsonLeg.getJSONObject("duration").getString("text");
            }
            if(!jsonLeg.isNull("steps")){
                stepsArray = jsonLeg.getJSONArray("steps");
            }

            for(int i=0; i<stepsArray.length(); i++){
                steps.add(getStep(stepsArray.getJSONObject(i)));
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        cabecera = cabecera + " (" + distance + " / " + duration + ")";
        return new Leg(cabecera, steps);
    }

    private Step getStep(JSONObject jsonStep){
        String distance = "-NA-";
        String duration = "-NA-";
        String htmlInstruction = "-NA-";
        try{
            if(!jsonStep.isNull("distance")){
                distance = jsonStep.getJSONObject("distance").getString("text");
            }
            if(!jsonStep.isNull("duration")){
                duration = jsonStep.getJSONObject("duration").getString("text");
            }
            if(!jsonStep.isNull("html_instructions")){
                htmlInstruction = jsonStep.getString("html_instructions");
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        String instruction = parseInstruction(htmlInstruction) + " (" + distance + " / " + duration + ")";
        return new Step(instruction);
    }

    private String parseInstruction(String htmlInstruction){
        StringBuilder res = new StringBuilder();
        for(int i=0; i<htmlInstruction.length(); i++){
            if(htmlInstruction.charAt(i) != '<'){
                res.append(htmlInstruction.charAt(i));
            }else{
                for(int j=i; j<htmlInstruction.length(); j++){
                    if(htmlInstruction.charAt(j)=='>'){
                        i = j;
                        if(i<htmlInstruction.length()-1){
                            if(htmlInstruction.charAt(i-1) == '"'){
                                res.append(". ");
                            }
                        }
                        break;
                    }
                }
            }
        }
        return res.toString().replaceAll("/", " ");
    }

    public List<Pair<Integer, List<LatLng>>> getRoutePositions(String stringJson){
        JSONObject jsonObject;
        JSONObject jsonRoute = new JSONObject();
        try{
            jsonObject = new JSONObject(stringJson);
            if(!jsonObject.isNull("routes")) {
                jsonRoute = jsonObject.getJSONArray("routes").getJSONObject(0);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }
        return getPairsList(jsonRoute);
    }

    private List<Pair<Integer, List<LatLng>>> getPairsList(JSONObject jsonRoute){
        JSONArray legsArray;
        List<Pair<Integer, List<LatLng>>> listaPares = new ArrayList<>();
        if(!jsonRoute.isNull("legs")){
            try {
                legsArray = jsonRoute.getJSONArray("legs");
                for(int i=0; i<legsArray.length(); i++){
                    List<LatLng> lista = getPositionsList(legsArray.getJSONObject(i));
                    Pair<Integer, List<LatLng>> par = new Pair<>(i, lista);
                    listaPares.add(par);

                    if(i == legsArray.length() - 1){
                        JSONObject step = legsArray.getJSONObject(legsArray.length() - 1);
                        Double endLat = .0;
                        Double endLng = .0;
                        if(!step.isNull("end_location")){
                            endLat = step.getJSONObject("end_location").getDouble("lat");
                            endLng = step.getJSONObject("end_location").getDouble("lng");
                        }
                        LatLng endLatLng = new LatLng(endLat, endLng);
                        lista.add(endLatLng);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return listaPares;
    }

    private List<LatLng> getPositionsList(JSONObject jsonLeg){
        JSONArray stepsArray;
        List<LatLng> lista = new ArrayList<>();
        Double startLat = .0;
        Double startLng = .0;
        if(!jsonLeg.isNull("steps")){
            try {
                stepsArray = jsonLeg.getJSONArray("steps");
                for(int i=0; i<stepsArray.length(); i++){
                    JSONObject step = stepsArray.getJSONObject(i);
                    if(!step.isNull("start_location")){
                        startLat = step.getJSONObject("start_location").getDouble("lat");
                        startLng = step.getJSONObject("start_location").getDouble("lng");
                    }
                    LatLng startLatLng = new LatLng(startLat, startLng);
                    lista.add(startLatLng);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return lista;
    }
}
