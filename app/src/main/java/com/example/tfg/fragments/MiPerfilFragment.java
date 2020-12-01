package com.example.tfg.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.example.tfg.Login;
import com.example.tfg.R;
import com.example.tfg.db.DatabaseManager;
import com.example.tfg.MyRoutes;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class MiPerfilFragment extends Fragment {

    private DatabaseManager db = new DatabaseManager();
    private String provider;
    private String name;
    private String id;
    private String photoUrl;
    private AlertDialog.Builder alertDialogBuilder;
    private AlertDialog alertDialog;
    private SharedPreferences prefs;

    public MiPerfilFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        provider = prefs.getString("provider", "null");
        name = prefs.getString("userName", "John Doe");
        id = prefs.getString("userId", "null");
        photoUrl = prefs.getString("userPhoto", "null");
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageView photoContainer = view.findViewById(R.id.profileImage);
        Glide.with(getContext()).load(Uri.parse(photoUrl)).circleCrop().into(photoContainer);

        TextView nameContainer = view.findViewById(R.id.profileName);
        nameContainer.setText(name);

        Button misRutas = view.findViewById(R.id.botonMisRutas);
        misRutas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent misRutas = new Intent(getContext(), MyRoutes.class);
                startActivity(misRutas);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mi_perfil, container, false);
    }

    private void desloguea(){
        try{
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(getContext(), getString(R.string.sesion_cerrada), Toast.LENGTH_LONG).show();
            loginPage();
        }catch(Exception e){
            Toast.makeText(getContext(), getString(R.string.error_desloguear), Toast.LENGTH_LONG).show();
        }
    }

    private void borraDatos(){
        db.getRoutesRef().child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                db.getUsersRef().child(id).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        borraCuenta();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), getString(R.string.error_borrar_datos), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), getString(R.string.error_borrar_cuenta), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void borraCuenta(){
        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(getContext(), getString(R.string.datos_borrados), Toast.LENGTH_LONG).show();
                loginPage();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), getString(R.string.error_borrar_datos), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loginPage(){
        if(provider.equals("FACEBOOK")){
            LoginManager.getInstance().logOut();
        }
        prefs.edit().clear().apply();
        Intent login = new Intent(getContext(), Login.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().finishAndRemoveTask();
        startActivity(login);
    }

    private void confirmaBorrarDatos(){
        alertDialogBuilder = new AlertDialog.Builder(getContext());
        final View vistaBorraDatos = getLayoutInflater().inflate(R.layout.vista_confirma_signout, null);

        TextView texto = vistaBorraDatos.findViewById(R.id.borrarText);
        texto.setText(getString(R.string.borrar_todo)+"\n"+getString(R.string.no_puede_deshacer));

        final CheckBox checkBox = vistaBorraDatos.findViewById(R.id.checkControl);

        Button borraDatos = vistaBorraDatos.findViewById(R.id.borrarDatos);
        Button cancelar = vistaBorraDatos.findViewById(R.id.cancelarBorrarDatos);
        borraDatos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkBox.isChecked()){
                    alertDialog.dismiss();
                    borraDatos();
                }else{
                    Toast.makeText(getContext(), getString(R.string.error_marque_casilla_borrar), Toast.LENGTH_LONG).show();
                }
            }
        });
        cancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialogBuilder.setView(vistaBorraDatos);
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.logOut:
                desloguea();
                return true;
            case R.id.signOut:
                confirmaBorrarDatos();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
