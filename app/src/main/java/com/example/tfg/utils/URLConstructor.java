package com.example.tfg.utils;

import com.google.android.gms.maps.model.LatLng;

public class URLConstructor {

    public static String getUrlNearbyPlaces(LatLng posicion, String placeType, Double radio, String key){
        StringBuilder baseURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
        baseURL.append("location=").append(posicion.latitude).append(",").append(posicion.longitude);
        baseURL.append("&radius=").append(radio*1000);
        baseURL.append("&type=").append(placeType);
        baseURL.append("&key=").append(key);
        return baseURL.toString();
    }

    public static String getUrlPlaceDetails(String place_id, String key){
        StringBuilder baseURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?place_id=").append(place_id);
        baseURL.append("&fields=place_id,geometry,name,photo,vicinity,opening_hours");
        baseURL.append("&key=").append(key);
        return baseURL.toString();
    }

    public static String getUrlPhoto(String[] photo, String key){
        StringBuilder baseURL = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        baseURL.append("maxwidth=" + photo[2]);
        baseURL.append("&photoreference=" + photo[0]);
        baseURL.append("&key=").append(key);
        return baseURL.toString();
    }

    public static String getUrlRoute(Boolean[] configuracion, LatLng posicion, String[] ids, String key, String language){
        Boolean desdePosicionActual = configuracion[0];
        Boolean optimizada = configuracion[1];
        StringBuilder baseUrl = new StringBuilder("https://maps.googleapis.com/maps/api/directions/json?language="+language+"&");
        if(desdePosicionActual){
            baseUrl.append("origin=").append(posicion.latitude).append(",").append(posicion.longitude);
            baseUrl.append("&destination=place_id:"+ids[ids.length-1]+"&mode=walking");
            if(ids.length<3){
                baseUrl.append("&waypoints=place_id:").append(ids[0]);
            }else{
                if(optimizada){
                    baseUrl.append("&waypoints=optimize:true|");
                }else{
                    baseUrl.append("&waypoints=");
                }
                for(int i=0; i<ids.length-1; i++){
                    baseUrl.append("place_id:").append(ids[i]);
                    if(i<ids.length-2){
                        baseUrl.append("|");
                    }
                }
            }
        }else{
            baseUrl.append("origin=place_id:").append(ids[0]);
            baseUrl.append("&destination=place_id:"+ids[ids.length-1]+"&mode=walking");
            if(ids.length==3){
                baseUrl.append("&waypoints=place_id:").append(ids[1]);
            }else if(ids.length>3){
                if(optimizada){
                    baseUrl.append("&waypoints=optimize:true|");
                }else{
                    baseUrl.append("&waypoints=");
                }
                for(int i=1; i<ids.length-1; i++){
                    baseUrl.append("place_id:").append(ids[i]);
                    if(i<ids.length-2){
                        baseUrl.append("|");
                    }
                }
            }
        }
        baseUrl.append("&key=").append(key);
        return baseUrl.toString();
    }
}
