package com.example.watchaccuracy;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class MojViewModel extends AndroidViewModel {               //da bih mogao da dobavim kontekst
    private MutableLiveData<ArrayList<Sat>> satovi;
    private MutableLiveData<Sat> jedanSelektovaniSat;
    ///////////////

    public MojViewModel(@NonNull Application application) {
        super(application);
    }

    //parametar korisnik kk mi sluzi da bih ga u fragmentu pocetni ekran preneo u funkciju koja ovo observe-uje
    public LiveData<ArrayList<Sat>> getSatovi(){  //ako je null onda ga instanciraj i pozovi njegov seter
        if(satovi == null){
            satovi = new MutableLiveData<>();
            setSatovi();
        }
        return satovi;
    }

    public void setSatovi(){                //dodavanje satova iz baze i setovanje atributa view modela
        Baza baza = new Baza(getApplication().getApplicationContext());
        SQLiteDatabase db = baza.getWritableDatabase();
        baza.onCreate(db);

        BazaSat bazaSat = new BazaSat(baza);

        ArrayList<Sat> lista = new ArrayList<>();
        lista = bazaSat.getAllSatovi();
        db.close();

        this.satovi.setValue(lista);
    }

    public LiveData<Sat> getJedanSelektovaniSat(){
        return this.jedanSelektovaniSat;
    }

    public void setJedanSelektovanSat(Sat sat){
        this.jedanSelektovaniSat = new MutableLiveData<>();        //u android view modelu moram imati deklaracije u setovanju!!!
        this.jedanSelektovaniSat.setValue(sat);
    }





}
