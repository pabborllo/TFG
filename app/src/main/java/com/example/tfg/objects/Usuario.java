package com.example.tfg.objects;

public class Usuario {

    private String id;
    private String nombre;
    private String email;
    private String photoUrl;

    public Usuario(){}

    public Usuario(String id, String nombre, String email, String photoUrl){
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.photoUrl = photoUrl;
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

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getPhotoUrl(){
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl){
        this.photoUrl = photoUrl;
    }
}
