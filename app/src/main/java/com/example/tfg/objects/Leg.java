package com.example.tfg.objects;

import java.util.List;

public class Leg {

    private String cabecera;
    private List<Step> steps;

    public Leg(){}

    public Leg(String cabecera, List<Step> stepsList){
        this.cabecera = cabecera;
        this.steps = stepsList;
    }

    public String getCabecera(){
        return cabecera;
    }

    public void setCabecera(String cabecera){
        this.cabecera = cabecera;
    }

    public List<Step> getSteps(){
        return steps;
    }

    public void setSteps(List<Step> steps){
        this.steps = steps;
    }
}
