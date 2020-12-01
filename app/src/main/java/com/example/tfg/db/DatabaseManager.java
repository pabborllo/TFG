package com.example.tfg.db;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class DatabaseManager {

    private DatabaseReference usersRef;
    private DatabaseReference routesRef;

    public DatabaseManager()
    {
        this.usersRef = FirebaseDatabase.getInstance().getReference("users");
        this.routesRef = FirebaseDatabase.getInstance().getReference("routes");
    }

    public DatabaseReference getUsersRef(){
        return usersRef;
    }

    public DatabaseReference getRoutesRef(){
        return routesRef;
    }
}
