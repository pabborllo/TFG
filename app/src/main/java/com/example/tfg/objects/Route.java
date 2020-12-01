package com.example.tfg.objects;

import java.util.List;

public class Route {

    private String url;
    private List<Leg> legs;
    private String id;
    private String nombre;
    private List<Lugar> places;
    private Boolean currentPos;

    public Route(){}

    public Route(String url, List<Leg> legsList){
        this.url = url;
        this.legs = legsList;
        this.id = "-NA-";
        this.nombre = "-NA-";
    }

    public String getUrl(){
        return url;
    }

    public void setUrl(String url){
        this.url = url;
    }

    public List<Leg> getLegs(){
        return legs;
    }

    public void setLegs(List<Leg> legs){
        this.legs = legs;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public List<Lugar> getPlaces(){
        return places;
    }

    public void setPlaces(List<Lugar> places){
        this.places = places;
    }

    public Boolean getCurrentPos(){
        return currentPos;
    }

    public void setCurrentPos(Boolean currentPos){
        this.currentPos = currentPos;
    }
}
