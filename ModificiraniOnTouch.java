package com.example.denis.PlanedIcom;

import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Denis on 7/3/2015.
 * Klasa omogucuje proslijeđivanje onTouch eventa s jednog na drugi View
 */
public class ModificiraniOnTouchListener implements View.OnTouchListener {

    private final String TAG = ModificiraniOnTouchListener.class.getSimpleName();

    private enum Mode{
        oblakListener, textListener
    }

    private ImageView oblak;
    private DanPrikaz dan;

    ImageView opcija1;

    private RelativeLayout.LayoutParams defaultni_parametri;
    private int default_left;
    private int default_right;
    private int default_top;
    private int default_bottom;

    int moveX;
    int moveY;

    int KO = 0;

    private Mode mode;


    public ModificiraniOnTouchListener(ImageView oblak){
        this.oblak = oblak;
        mode = Mode.textListener;
    }

    public ModificiraniOnTouchListener(ImageView oblak, DanPrikaz _dan, int broj){
        dan = _dan;
        this.oblak = oblak;
        this.KO = broj;
        mode = Mode.oblakListener;
    }

    public ModificiraniOnTouchListener(ImageView oblak, DanPrikaz _dan, int broj, ImageView opcija){
        dan = _dan;
        this.oblak = oblak;
        this.KO = broj;
        mode = Mode.oblakListener;
        opcija1 = opcija;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event){

        switch (mode){
            case textListener:

                return tekstListenerFunkcija();
            case oblakListener:
                return oblakListenerFunkcija(oblak, event);
        }

        return false;
    }

    /**
     * metoda tekstListenerFunkcija
    *kada je tekst registrirao onTouch treba ga proslijediti na drugi View koji se animira
    *animacije su asinkrone i traju sve dok traje onTouch event
     * @return bool event_flag
    */

    private boolean tekstListenerFunkcija() {
        postaviDefaultneParametre(oblak);

        System.out.println("\n-------\ntekstListener");

        oblak.callOnClick();
        return false;
    }

    /**
     * oblakListenerFunkcija
     * metoda omogucuje onDrag za pojedini View
     * onTouch se primjenjuje izravnim dodirom Oblaka ili se proslijeđuje iz Teksta
     * @param v objekt koji se pomice i animira
     * @param event tip događaja koji se odvija ili je proslijeđen
     * @return bool event flag
     */
    private boolean oblakListenerFunkcija(View v, MotionEvent event){

        int X = (int)event.getRawX();
        int Y = (int)event.getRawY();

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                System.out.println("ACTION DOWN_________");
                postaviDefaultneParametre(oblak);
                oblak.bringToFront();


                moveX = (int)event.getRawX();
                moveY = (int)event.getRawY();

                break;
            case MotionEvent.ACTION_UP:
                System.out.println("ACTION UP__________"+X+"/"+ Y);



                defaultni_parametri.rightMargin = default_right;
                defaultni_parametri.leftMargin = default_left;
                defaultni_parametri.topMargin = default_top;
                defaultni_parametri.bottomMargin = default_bottom;

                oblak.setImageResource(R.drawable.ob_dolje_lijevo);
                oblak.setLayoutParams(defaultni_parametri);

                dan.prikaziSve();

                break;
            case MotionEvent.ACTION_MOVE:

                RelativeLayout.LayoutParams param2 = (RelativeLayout.LayoutParams) v.getLayoutParams();
                param2.leftMargin = (int)event.getRawX() - moveX;
                param2.topMargin = (int)event.getRawY() - moveY;
                param2.bottomMargin = moveY -(int) event.getRawY();
                param2.rightMargin = moveX - (int) event.getRawX();

                oblak.setLayoutParams(param2);


                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                Log.i(TAG,"Hovered event");
                break;
        }
        oblak.invalidate();
        return true;
    }

    private void postaviDefaultneParametre(ImageView oblak){
        defaultni_parametri = (RelativeLayout.LayoutParams)oblak.getLayoutParams();

        default_right = defaultni_parametri.rightMargin;
        default_left = defaultni_parametri.leftMargin;
        default_top = defaultni_parametri.topMargin;
        default_bottom = defaultni_parametri.bottomMargin;
    }

}
