package com.example.tfg.objects;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

public class Lugar implements Parcelable {

    private String id;
    private LatLng posicion;
    private String titulo;

    public Lugar(String id, LatLng posicion, String titulo){
        this.id = id;
        this.posicion = posicion;
        this.titulo = titulo;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public LatLng getPosicion(){
        return posicion;
    }

    public void setPosicion(LatLng posicion){
        this.posicion = posicion;
    }

    public String getTitulo(){
        return titulo;
    }

    public void setTitulo(String titulo){
        this.titulo = titulo;
    }

    protected Lugar(Parcel in) {
        id = in.readString();
        posicion = in.readParcelable(LatLng.class.getClassLoader());
        titulo = in.readString();
    }

    public static final Creator<Lugar> CREATOR = new Creator<Lugar>() {
        @Override
        public Lugar createFromParcel(Parcel in) {
            return new Lugar(in);
        }

        @Override
        public Lugar[] newArray(int size) {
            return new Lugar[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeParcelable(posicion, i);
        parcel.writeString(titulo);
    }
}
