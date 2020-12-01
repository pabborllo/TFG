package com.example.tfg.utils;

import com.example.tfg.R;
import com.example.tfg.objects.Lugar;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

public class Functions {

    //Nombres de los lugares para crear la ruta
    public static List<String> getPlacesNames(List<Lugar> places){
        List<String> names = new ArrayList<>();
        for(Lugar lugar: places){
            names.add(lugar.getTitulo());
        }
        return names;
    }

    //IDs de los lugares para crear la ruta
    public static String[] getPlacesIDs(List<Lugar> places){
        String[] ids = new String[places.size()];
        for(int i=0; i<places.size(); i++){
            ids[i] = places.get(i).getId();
        }
        return ids;
    }

    //Comprobar si un lugar ya ha sido agregado a la ruta
    public static Boolean compruebaExistencia(LatLng posicion, List<Lugar> places){
        Boolean res = false;
        for(Lugar lugar: places){
            res = lugar.getPosicion().equals(posicion);
            if(res){
                break;
            }
        }
        return res;
    }

    //Comprobar inicio de ruta y optimización de ruta
    public static Boolean[] checkConfiguracion(int posicionDeInicio, int optimizada){
        Boolean[] res = new Boolean[2];
        switch (posicionDeInicio){
            case R.id.posicionActual:
                res[0] = true;
                break;
            case R.id.primerLugar:
                res[0] = false;
            default:
                break;
        }
        switch (optimizada){
            case R.id.optimizarSi:
                res[1] = true;
                break;
            case R.id.optimizarNo:
                res[1] = false;
                break;
            default:
                break;
        }
        return res;
    }
}
