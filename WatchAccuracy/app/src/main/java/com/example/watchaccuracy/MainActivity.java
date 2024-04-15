package com.example.watchaccuracy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ///////////////////////////////////////

        //prvi (pocenti) fragment se dodaje sa add, ostali se dodaju sa replace, i ako stavimo add to back stack onda se sa prikazanog fragmenta vraca na prethodni
        //main activity treba samo da barata fragmentima i nista drugo
        //preko view modela se iscrtava fragment kada se nesto promeni. Dakle fragment radi observe nad podacima iz view modela.
        //ovde sam mogao dodati upit za dozvolu za internet i unutrasnje skladiste??
        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragmentView, FragmentPocetniEkran.newInstance(), "fragmentPocetniEkran")
                .commit();

        //trebao bih dodati ako zuri + u prikazu sekundi, mozda????
        //dodati proveru dozvola
        //da li ce biti u redu prikaz za tablet?
        //proveriti gde su stare baze


    }


    //za diplomski
    //word!!!
    //istorijat androida, koji problem resava aplikacija? pojasnjenje sa sscreenshot kod (screenshot na emulatoru se radi na crtl + s)

}


