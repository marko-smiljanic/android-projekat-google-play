package com.example.watchaccuracy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

public class FragmentOAplikaciji extends Fragment {

    public FragmentOAplikaciji(){

    }
    public static FragmentOAplikaciji newInstance(){
        FragmentOAplikaciji fragment = new FragmentOAplikaciji();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View vv = inflater.inflate(R.layout.fragment_o_aplikaciji, container, false);
        ConstraintLayout l = vv.findViewById(R.id.oAplikacijiMainLyt);

        iscrtaj(l);

        return vv;
    }

    private void iscrtaj(ConstraintLayout ll){
        TextView glavnaLabela = ll.findViewById(R.id.glavnaLabela);
        glavnaLabela.setText("Aplikacija je razvijena sa namerom da isprati odstupanja u radu mehanickih satova " +
                "i da bi korisniku omogucila jednostavnu evidenciju. " +
                "\n \n" +
                "Odstupanja kod mehanickih satova su veoma bitna jer nam ona " +
                "pomazu da procenimo opste stanje sata. Prevelika odstupanja u radu mogu da nam naznace da je vreme za servis mehanizma." +
                "\n \n" +
                "Aplikaciju mogu da koriste svi ljubitelji satova sa mehanickim mehanizmom. Aplikacija je posebno korisna casovnicarima koji vrse popravku ili " +
                "stelovanje satova. " +
                "Ukoliko ne posedujete timegrapher ili neki drugi moderni uredjaj za pracenje odstupanja mehanickih satova ovakva " +
                "aplikaciija je verovatno najbolje resenje. " +
                "\n \n" +
                "Ukoliko u aplikaciiju pravilno unesete vreme na koje je na satu, aplikacija ce vam priakzati ukupno odstupanje i odstupanje na 24h (spd)." +
                "Veoma je bitno da izmedju dva checkpoint-a prodje dovoljno " +
                "vremena (minimum 6 sati) da bi rezultat bio precizniji. " +
                "Da bi ste dobili najjasniju sliku o stanju sata, treba meriti u razlicitim situacijama (Kada je skroz navijen ili malo navijen. " +
                "Takodje treba meriti i u razlicitim polozajima, " +
                "u kojima sat moze da se nadje preko noci, dnevno nosenje na ruci, itd) " +
                "\n \n" +
                "Ukoliko imate pitanja, nedomice ili zelite da prijavite problem u aplikaciji obratite mi se na mejl: markosmiljanic99@gmail.com");

    }

}
