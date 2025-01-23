package com.example.watchaccuracy;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class FragmentCheckpoint extends Fragment {
    MojViewModel viewModel;
    public int selektovaniSat;      //selektovano iz timepicker-a
    public int selektovaniMinut;

    public Date sistemskoVreme;
    public Date vremeNaSatu;
    public DateFormat dateFormat;

    public FragmentCheckpoint() {
    }

    public static FragmentCheckpoint newInstance() {
        FragmentCheckpoint fragment = new FragmentCheckpoint();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(MojViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_checkpoint, container, false);
        LinearLayout l = vv.findViewById(R.id.mainLytCheckpoint);
        LinearLayout l2 = vv.findViewById(R.id.mainLytCheckpoint2);

        viewModel.getJedanSelektovaniSat().observe(getViewLifecycleOwner(), new Observer<Sat>() {
            @Override
            public void onChanged(Sat sat) {
                iscrtajDodavanjeCheckpointa(sat, l2, l);
                iscrtajCheckpointe(sat, l);
            }
        });

        return vv;
    }


    private void iscrtajDodavanjeCheckpointa(Sat sat, LinearLayout ll, ViewGroup container){
        TextView tv = ll.findViewById(R.id.tv1);
//        EditText inputOpis = ll.findViewById(R.id.inputOpisCheckpointa);
        Button kreirajCheck = ll.findViewById(R.id.dugmeKreirajCheck);

        //kada korisnik pritisne ovo dugme to znaci da je sekundna kazaljka dosegla marker 12, i mi prvo treba da uzmemo:
        //sistemsko vreme (trenutno tacno) i da sacuvamo. Nakon toga od korisnika trazimo unos tacnog vremena na njegovom
        //satu da bi smo mogli dalje da pravimo checkpoint (racunamo odstupanja i setujemo u bazu)
        kreirajCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");   // 2022/03/29 23:54:24
                sistemskoVreme = new Date();   //dohvata trenutno sistemsko vreme, vreme kad je kreiran check, odnosno kad je kliknuto na dugme


                TimePickerDialog.OnTimeSetListener onTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {             // i je sat, i1 minut
                        Date d = new Date();                                                  //ponovo uzimamo trenutno vreme
                        long rezultat = d.getTime() - sistemskoVreme.getTime();               //razlika izmedju ovog i gore sistemskog vremena su sekunde koje treba da setujemo na vreme koje je na satu, jer sekunde se pocinju meriti od klika na dugme do setovanja i potvrde setovanja vremena na satu
                        long rezultatUSekundama = (rezultat / (1000)) % 60;                   //ovo je vreme koje je proso od klika na dugme check do zavrsetka odabira vremena na satu
                                                                                              // da bi smo znali koje sekunde da setujemo, jer od klika na dugme do odabira vremena isto prodje vreme koje su nam u stvari sekunde, ne verujem da ce neko za vise od minuta potrositi da upise vreme koje vidi na satu, ali moze da se ishendluje
                                                                                              //mada mozda bi ovo i radilo bez obzira da li je proslo vise od 60 sekundi
                        DateFormat dateFormat3 = new SimpleDateFormat("yyyy/MM/dd");      //odavde mi treba samo datum
                        String datumStr = dateFormat3.format(d);                                 //jako mi je bitno da pamtim datum kod check-a jer je moguce da drugi check bude odradjen za par dana kasnije
                        String selektovaniSatStr = String.valueOf(i);                           // da li je bitan datum?? Nije jer u sistemskom vremenu i na satu je isti datum, propverava se samo odstupanje u sekundama, minutma, satima
                        String selektovaniMinutStr = String.valueOf(i1);
                        String sekundeStr = String.valueOf(rezultatUSekundama);

                        //   2022/03/29 23:54:24, ovkao izgleda vremeStr kada dodam sve potrebne atribute u datom formatu
                        String vremeStr = datumStr + " " + selektovaniSatStr + ":" + selektovaniMinutStr + ":" + sekundeStr;
                        try {
                            vremeNaSatu = dateFormat.parse(vremeStr);       //koristimo funkciju parse da dobijemo objekat date, da radimo dalja racunanja
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        ///////////////////////////////////
                        Baza baza = new Baza(getActivity());
                        SQLiteDatabase db = baza.getWritableDatabase();
                        baza.onCreate(db);
                        BazaCheckpoint bazaCheckpoint = new BazaCheckpoint(baza);

                        ArrayList<Checkpoint> lista = new ArrayList<>();
                        lista = bazaCheckpoint.getAllCheckpointi(sat.getId());
                        //db.close();

                        //bez da rucno sve pretvaram u sekunde, mogao sam ovo uraditi:
                        //import java.util.concurrent.TimeUnit;
                        //long odstupanje = sistemskoVreme.getTime() - vremeNaSatu.getTime();  //u milisekundama
                        //long ukupneSekunde = TimeUnit.MILLISECONDS.toSeconds(odstupanje);
                        //String rezultatUSekundamaStr = String.valueOf(ukupneSekunde);

                        //proveravam samo poslednjeg ucitanog iz liste jer ne moze se praviti novi checkpoint ako sposlednji nema setovano konacno odstupanje
                        long odstupanje = sistemskoVreme.getTime() - vremeNaSatu.getTime();  //u milisekundama

                        System.out.println( odstupanje);

                        long razlikaUSekundama = (odstupanje / (1000)) % 60;
                        long razlikaUMinutima = (odstupanje / (1000 * 60)) % 60;
                        long razlikaUSatima = (odstupanje / (1000 * 60 * 60)) % 24;
                        //long razlikaUGodinama = (odstupanje / (1000l * 60 * 60 * 24 * 365));  //ne bi bas trebalo da se checkpoint uradi za godinu dana haha
                        long razlikaUDanima = (odstupanje / (1000 * 60 * 60 * 24)) % 365;

                        //posto odstupanje priakzujem u sekundama, ove razlike u minutima, satima, danima pretvaram u sekunde
                        long razlikaUMinutimaUSekunde = razlikaUMinutima * 60;
                        long razlikaUSatimaUSekunde = razlikaUSatima * 3600;
                        //long razlikaUGodinamaUSekunde = razlikaUGodinama * 31536000;  //nece valjda checkpointi biti stariji od godinu dana?
                        long razlikaUDanimaUSekunde = razlikaUDanima * 86400;

                        long ukupneSekunde = razlikaUSekundama + razlikaUMinutimaUSekunde + razlikaUSatimaUSekunde + razlikaUDanimaUSekunde;
                        String rezultatUSekundamaStr = String.valueOf(ukupneSekunde);

                        //ovde bih trebao dalje da vrsim operacije izmedju datuma i kreiranje checkpointa u bazu
                        //prvo moram dohvatiti checkpointe i na poslednjem proveriti da li su neke od vrednostri null da bih znao sta da setujem (novi checkpoint ili menjam postojeci)
                        Checkpoint poslednji;
                        if(!lista.isEmpty()){     //ako lista nije prazna
                            poslednji = lista.get(lista.size() - 1);

                            if(poslednji.getUkupnoODstupanje() == null){      //ako poslednji nema konacno odstupanje, onda mu setujemo prvo vreme sistemsko i prvo vreme na satu i guramo u bazu, ostalo na null
                                int prvoOdstupanjeInt = Integer.parseInt(poslednji.getPrvoOdstupanje());
                                int drugoOdstupanje = Integer.parseInt(rezultatUSekundamaStr);
                                int ukupno = prvoOdstupanjeInt - drugoOdstupanje;

                                //////////////
                                //ovde hocu da konacno odstupanje izmedju dva check-pointa pretvorim u jedinicu
                                //spd - odnosno odstupanje na 24h
                                Date vreme1 = null;  //prvo sistemsko vreme kada se radio prvi cek
                                try {
                                    vreme1 = dateFormat.parse(poslednji.getPrvoVremeSistemsko());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                long razlika_milisec = sistemskoVreme.getTime() - vreme1.getTime();  //drugo - prvo sistemsko vreme u milisekundama, uvek oduzimam od drugog prvo da bi dobio
                                int razlika_u_sekundama = (int) (razlika_milisec / 1000);
                                //Razlika do 24 sata u sekundama
                                int sekundi_u_24h = 24 * 60 * 60; // 24 sata u sekundama

                                //konacno odstupanje (odstupanje koje je bilo izmedju dva check-a) * sekundi u 24h / vreme koje je proteklo izmedju 2 check-a
                                //imam odstupanje za neki period i onda racunam koliko bi to odsupanje bilo na 24h?
                                long odstupanje_na_24h = ukupno * sekundi_u_24h / razlika_u_sekundama;


                                //upis svega u bazu
                                bazaCheckpoint.editCheckpoint(poslednji.getCheckpointId(), poslednji.getPrvoVremeSistemsko(), poslednji.getPrvoVremeNaSatu(),
                                        poslednji.getPrvoOdstupanje(), dateFormat.format(sistemskoVreme), dateFormat.format(vremeNaSatu),
                                        rezultatUSekundamaStr, String.valueOf(ukupno), String.valueOf(odstupanje_na_24h), sat.getId());  //opis se ne setuje ovde
                                db.close();

                            }else{   //ako poslednji ima konacno odstupanje onda dodajemo novi objekat sa pocetnim vrednostima prvog cekpointa i prvih vremena na satu
                                bazaCheckpoint.addCheckpoint(dateFormat.format(sistemskoVreme), dateFormat.format(vremeNaSatu), rezultatUSekundamaStr,
                                        null, null, null, null, null, null, sat.getId());  //opis se setuje posle
                                db.close();
                            }
                        }else{       //ako je lista == null, tj. i poslednji == null, znaci da u bazi nemamo nista i automatski dodajemo novog
                            bazaCheckpoint.addCheckpoint(dateFormat.format(sistemskoVreme), dateFormat.format(vremeNaSatu), rezultatUSekundamaStr,
                                    null, null, null, null, null, null, sat.getId());  //opis se setuje na dugme dodaj opis, po defaultu stavljam null
                            db.close();
                        }
                        iscrtajCheckpointe(sat, container);   //posle operacije sa bazom ponovo pozivam iscrtavanje checkpoint-a
                        db.close();

                    }
                };
                TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), AlertDialog.THEME_HOLO_LIGHT ,onTimeSetListener, selektovaniSat, selektovaniMinut, true);
                timePickerDialog.setTitle("Odaberite trenutno vreme na vasem satu:");
                timePickerDialog.show();

                //Postavljanje Handlera za automatsko zatvaranje nakon 58 sekundi
                //Ovo radim jer ne mogu da pratim da li je proslo vise od minuta od kada se time picker otvorio jer bi onda morao da uvecavam odabrano vreme za vise od tog minuta
                //Implementirano je da se na dodato vreme dodaju sekunde, ali ne vise od 60.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (timePickerDialog.isShowing()) {
                            timePickerDialog.dismiss();
                        }
                    }
                }, 56000); // 56 sekundi

            }

        });

    }


    private void iscrtajCheckpointe(Sat sat, ViewGroup container){
        container.removeAllViews();

        Baza baza = new Baza(getActivity());
        SQLiteDatabase db = baza.getWritableDatabase();
        baza.onCreate(db);
        BazaCheckpoint bazaCheckpoint = new BazaCheckpoint(baza);

        ArrayList<Checkpoint> lista = new ArrayList<>();
        lista = bazaCheckpoint.getAllCheckpointi(sat.getId());

        //db.close();

        LayoutInflater inf = getLayoutInflater();
        for(Checkpoint cc : lista){
            View red = inf.inflate(R.layout.layout_checkpoint, null);
            red.findViewById(R.id.layoutCheckpointRed);

            TextView labelaPrviCheckVreme = red.findViewById(R.id.labelaPrviCheck);
            TextView labelaDrugiCheckVreme = red.findViewById(R.id.labelaDrugiCheck);
            TextView labelaUkupnoOdstupanje = red.findViewById(R.id.labelaUkupnoOdstupanje);
            TextView labelaOdstupanjeNa24h = red.findViewById(R.id.labelaOdstupanjeNa24h);
            TextView labelaOpis = red.findViewById(R.id.tvOpis);
            ImageView slikaObrisi = red.findViewById(R.id.slikaObrisiCheckpoint);
            Button dugmeDodajOpis = red.findViewById(R.id.dugmeDodajOpis);
            ConstraintLayout layoutZaObojiti = red.findViewById(R.id.redZaObojiti);

            labelaPrviCheckVreme.setText(cc.getPrvoVremeSistemsko());   //ovo ne bi trebalo biti null nikad ako postoji checkpoint

            if(cc.getDrugoVremeSistemsko() == null){              //mogao sam ovo isto da odradim i za proveru konacnog odstupanja, svejedno ispadne
                labelaDrugiCheckVreme.setText("Drugi check odraditi nakon 6 sati!");
                labelaDrugiCheckVreme.setTextColor(Color.parseColor("#FFAC0606"));  //stara boja: #FFAC0606
                layoutZaObojiti.setBackgroundColor(Color.parseColor("#4DD55656"));   //stara boja: #4DD55656
            }else{
                labelaDrugiCheckVreme.setText(cc.getDrugoVremeSistemsko());
            }

            if(cc.getUkupnoODstupanje() == null){
                labelaUkupnoOdstupanje.setText("");
                labelaOdstupanjeNa24h.setText("");
            }else{
                labelaUkupnoOdstupanje.setText("Ukupno: " + cc.getUkupnoODstupanje() + "s");
                labelaOdstupanjeNa24h.setText(cc.getOdstupanjeNa24h() + "spd");
            }

            if(cc.getOpis() == null){
                labelaOpis.setText("");
            }else{
                labelaOpis.setText(cc.getOpis());
            }

            slikaObrisi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
//                    bazaCheckpoint.deleteCheckpoint(cc.getCheckpointId());
//                    iscrtajCheckpointe(sat, container);

                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    builder.setMessage("Da li ste sigurni da želite obrisati?")
                            .setTitle("Potvrda brisanja")
                            .setPositiveButton("Da", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    bazaCheckpoint.deleteCheckpoint(cc.getCheckpointId());
                                    iscrtajCheckpointe(sat, container);
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


            dugmeDodajOpis.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                    LayoutInflater inf = getLayoutInflater();
                    View dialogView = inf.inflate(R.layout.dijalog_unos_texta, null);
                    EditText unosOpisa = dialogView.findViewById(R.id.unosOpisa);

                    if (cc.getOpis() != null) {
                        unosOpisa.setText(cc.getOpis());
                    }

                    builder.setView(dialogView)
                            .setTitle("Unos teksta")
                            .setPositiveButton("Potvrdi", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    String unetiTekst = unosOpisa.getText().toString();
                                    bazaCheckpoint.izmeniOpis(cc.getCheckpointId(), unetiTekst);
                                    iscrtajCheckpointe(sat, container);
                                    //Toast.makeText(requireContext(), "Uneli ste: " + unetiTekst, Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Otkaži", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

            //////////
            container.addView(red);
        }
        db.close();

    }

//    private void prikaziDijalogZaPotvrduBrisanja() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
//        builder.setMessage("Da li ste sigurni da želite obrisati?")
//                .setTitle("Potvrda brisanja")
//                .setPositiveButton("Da", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // Implementacija brisanja
//                        // Ovde možete dodati logiku za brisanje podataka
//                        Toast.makeText(requireContext(), "Podaci su obrisani.", Toast.LENGTH_SHORT).show();
//                    }
//                })
//                .setNegativeButton("Ne", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        // Opcionalno, ovde možete dodati neku akciju ako korisnik odustane od brisanja
//                        dialog.dismiss(); // Zatvara dijalog
//                        Toast.makeText(requireContext(), "Otkazano brisanje.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//        AlertDialog dialog = builder.create();
//        dialog.show();
//    }
//}


}
