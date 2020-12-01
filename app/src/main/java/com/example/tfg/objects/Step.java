package com.example.tfg.objects;

public class Step {

    private String instructions;

    public Step(){}

    public Step(String instructions){
        this.instructions = instructions;
    }

    public String getInstructions(){
        return instructions;
    }

    public void setInstructions(String instructions){
        this.instructions = instructions;
    }
}
