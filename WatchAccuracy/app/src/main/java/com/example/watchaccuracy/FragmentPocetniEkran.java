package com.example.watchaccuracy;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class FragmentPocetniEkran extends Fragment {  //ovo je u stvari pocetni ekran
    private MojViewModel viewModel;

    public FragmentPocetniEkran() {
    }

    public static FragmentPocetniEkran newInstance() {
        FragmentPocetniEkran fragment = new FragmentPocetniEkran();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MojViewModel.class);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_pocetni_ekran, container, false);
        LinearLayout l = vv.findViewById(R.id.mainLytPocetniEkran);
        LinearLayout l2 = vv.findViewById(R.id.mainLyt2PocetniEkran);

        iscrtajDugmeDodajSat(l2);


        viewModel.getSatovi().observe(getViewLifecycleOwner(), new Observer<ArrayList<Sat>>() {
            @Override
            public void onChanged(ArrayList<Sat> satovi) {
                iscrtajSatove(l, satovi);                           //ici ce u observe za satove
            }
        });

        return vv;
    }

    private void iscrtajDugmeDodajSat(LinearLayout ll) {
        Button dodajSat = ll.findViewById(R.id.dugmeDodajNoviSat);                //dohvatanje dugmeta koje je u drugom layout-u
        dodajSat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.replace(R.id.fragmentView, FragmentFormaDodajSat.newInstance(), "fragmentDodajSat");
                ft.addToBackStack("fragmentDodajSat");  //u ovom slucaju mi ovo ne treba
                ft.commit();
            }
        });
    }

    private void iscrtajSatove(ViewGroup container, ArrayList<Sat> lista){
        container.removeAllViews();

        LayoutInflater inf = getLayoutInflater();
        for (Sat ss : lista){

            View red = inf.inflate(R.layout.layout_sat, null);
            red.findViewById(R.id.layoutSatRed);

            TextView labelaMarka = red.findViewById(R.id.labelaMarkaSat);
            TextView labelaModel = red.findViewById(R.id.labelaModelSat);
            TextView labelaPoslednjiCheck24h = red.findViewById(R.id.labelaPoslednjiCheck24h);
            Button izmeniSat = red.findViewById(R.id.dugmeIzmeniSat);
            ImageView obrisiSat = red.findViewById(R.id.dugmeObrisiSat);
            Button dugmeCheck = red.findViewById(R.id.buttonCheck);

            Baza baza = new Baza(getActivity());
            SQLiteDatabase db = baza.getWritableDatabase();
            baza.onCreate(db);
            BazaCheckpoint bazaCheckpoint = new BazaCheckpoint(baza);

            ArrayList<Checkpoint> listaCheckpointa = new ArrayList<>();
            listaCheckpointa = bazaCheckpoint.getAllCheckpointi(ss.getId());
            db.close();

            if(listaCheckpointa.size() >= 1){       //dohvatim poslednjeg, ako on nema konacno odstupanje vidim da li ima onaj pre njega i tu stajem. Mislim da je na kraju odradjeno pogledaj samo da li postoji poslednji checkpoint. Ako da prikazi ga, ako ne ne prikazuj ga
                Checkpoint cc = listaCheckpointa.get(listaCheckpointa.size() - 1);
                if(cc != null){
                    if(cc.getUkupnoODstupanje() != null){
                        labelaPoslednjiCheck24h.setText(cc.getOdstupanjeNa24h() + "spd");
                    }
                }
            }

            labelaMarka.setText(ss.getMarka());
            labelaModel.setText(ss.getModel());

            izmeniSat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewModel.setJedanSelektovanSat(ss);

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.fragmentView, FragmentFormaIzmeniSat.newInstance(), "fragmentIzmeniSat");
                    ft.addToBackStack("fragmentIzmeniSat");
                    ft.commit();
                }
            });
            obrisiSat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    Baza baza = new Baza(getActivity());
//                    SQLiteDatabase db = baza.getWritableDatabase();
//                    baza.onCreate(db);
//
//                    BazaSat bazaSat = new BazaSat(baza);
//                    BazaCheckpoint bazaCheckpoint = new BazaCheckpoint(baza);
//
//                    //delete vraca int ali mi apsolutno nije bitno gde cu smestiti to sto vrati, bitno je da se izvrsi ono unutar upita
//                    bazaCheckpoint.brisiSve(ss.getId());  //prov obrisemo sve njegove checkpointe, tek onda sat
//                    bazaSat.deleteSat(ss.getId());
//                    db.close();
//
//                    viewModel.setSatovi();  //osvezavam prikaz

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setMessage("Da li ste sigurni da želite obrisati?")
                            .setTitle("Potvrda brisanja")
                            .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Baza baza = new Baza(getActivity());
                                    SQLiteDatabase db = baza.getWritableDatabase();
                                    baza.onCreate(db);

                                    BazaSat bazaSat = new BazaSat(baza);
                                    BazaCheckpoint bazaCheckpoint = new BazaCheckpoint(baza);

                                    //delete vraca int ali mi apsolutno nije bitno gde cu smestiti to sto vrati, bitno je da se izvrsi ono unutar upita
                                    bazaCheckpoint.brisiSve(ss.getId());  //prov obrisemo sve njegove checkpointe, tek onda sat
                                    bazaSat.deleteSat(ss.getId());
                                    db.close();

                                    viewModel.setSatovi();  //osvezavam prikaz

                                    Toast.makeText(requireContext(), "Uspesno obrisano.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss(); // Zatvara dijalog
                                    //Toast.makeText(requireContext(), "Otkazano brisanje.", Toast.LENGTH_SHORT).show();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();


                }
            });
            dugmeCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    viewModel.setJedanSelektovanSat(ss);

                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.fragmentView, FragmentCheckpoint.newInstance(), "fragmentCheckpoint");
                    ft.addToBackStack("fragmentCheckpoint");
                    ft.commit();
                }
            });

            container.addView(red);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {                             //kreiranje menija, poziva se sam ali u oncreate metodi fragmenta moram da imam
        inflater.inflate(R.menu.meni, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                                       //za meni: kada se selektuje neki element iz menija, odnosno onclick za menu iteme
        // Handle item selection
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        switch (item.getItemId()) {
            case R.id.menuItemOAplikaciji:
                ft.replace(R.id.fragmentView, FragmentOAplikaciji.newInstance(), "fragmentOAplikaciji");
                ft.addToBackStack("fragmentOAplikaciji");
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);  //ovo vraca false
        }
    }


}
