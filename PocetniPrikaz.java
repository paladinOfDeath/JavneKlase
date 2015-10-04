package com.example.denis.PlanedIcom;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.Visibility;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
*Created by Denis 7/5/2015
*detailed comments are deleted due code privacy agreement
*Main Activity 
*/

public class DanPrikaz extends Activity {

    public static Context kontekst;
    public static final String TAG = DanPrikaz.class.getSimpleName();
    //public static KalendarObaveze k_obaveze;

    Bundle stanje = new Bundle();

    private int tocka_oslonca;

    private RelativeLayout layout;

    private IkonaObaveze[] polje_icona = new IkonaObaveze[8];
    private Doblak header;

    private Calendar datum = new GregorianCalendar();

    //private Alarm alarm;
    private List<ObavezaB> lista_obaveza;

    private ImageView dan;
    private TextView txt_dan_broj, txt_dan_mjesec;

    private ImageView dodaj_obavezu;
    private ImageView kal;

    private Fragment_listView frag_list;
    private Fragment_statistika frag_stat;
    private boolean frag_list_postavljen = false;

    private ServiceConnection s_conn= new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName ime, IBinder service) {
            k_obaveze = KalendarObavezeInterface.Stub.asInterface(service);

            inicijalizirajPrikaze();
            osvjezi();

        }

        @Override
        public void onServiceDisconnected(ComponentName ime) {

        }
    };
    //za service
    private KalendarObavezeInterface k_obaveze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dan);
        ucitajDatumDefault();
        Log.i(TAG,"onCreate");
        Intent i = new Intent(KalendarObavezeService.class.getName());
        startService(i);


    }
    @Override
    public void onResume(){
        super.onResume();
        Log.i(TAG,"onResume");
        Intent i = new Intent(KalendarObavezeService.class.getName());
        bindService(i,s_conn,0);
    }

    @Override
    public void onPause(){
        super.onPause();
        Log.i(TAG,"onPause");
        try {
            unbindService(s_conn);
        }catch(Throwable t){
            Log.w(TAG, "nisam uspio unbindati service");
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.i(TAG,"onStop");

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.i(TAG,"onDestroy");


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        outState.putLong("datum", this.datum.getTimeInMillis());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        this.datum.setTimeInMillis(savedInstanceState.getLong("datum"));

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        try{
            zatvoriSveFragmente();
        }catch(Exception e){}

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.dan, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.razvojReset:
                try {
                    k_obaveze.resetiraj();
                }catch (RemoteException e){
                    reeHelper(e);
                }
                lista_obaveza = new ArrayList<ObavezaB>();
                osvjezi();
        }
        return super.onOptionsItemSelected(item);
    }



    private Long slijedecaObavezaVrijeme() throws NullPointerException{
        //Obaveze ob = new Obaveze(this);
        Calendar c = new GregorianCalendar();
        Long vrijeme = (long) 0;
        try {
            vrijeme = k_obaveze.dajSlijedecuObavezu(c.getTimeInMillis()).dajVrijeme();
        }catch(NullPointerException e) {
        }catch(RemoteException e){
            reeHelper(e);
        }

        return vrijeme;
    }

    private ObavezaB slijedecaObaveza(){
        //Obaveze ob = new Obaveze(this);
        Calendar c = new GregorianCalendar();
        ObavezaB _ob = null;
        try {
            _ob = k_obaveze.dajSlijedecuObavezu(c.getTimeInMillis());
        }catch(NullPointerException e) {
        }catch(RemoteException e){
            reeHelper(e);
        }

        return _ob;
    }

    private ObavezaB dajObavezu(Long vrijeme){
        //Obaveze ob = new Obaveze(this);
        ObavezaB _ob = null;
        try{
            _ob = k_obaveze.dajSlijedecuObavezu(new Long(vrijeme-30*1000));
        }catch(NullPointerException e){
        }catch (RemoteException e){
            reeHelper(e);
        }

        return _ob;
    }

    public void sakrijSve(){
        this.postaviVidljivost(View.INVISIBLE);
    }

    private void inicijalizirajPrikaze(){
        kontekst = getApplicationContext();

        dan = (ImageView)findViewById(R.id.danCenter);
        dan.setImageResource(R.drawable.day_background);

        kal = (ImageView) findViewById(R.id.kalendar);
        kal.setImageResource(R.drawable.calendar);
        kal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                pokreniKalendar();
            }
        });

        txt_dan_broj = (TextView) findViewById(R.id.txtDanCenter);
        txt_dan_mjesec = (TextView) findViewById(R.id.txtDanCenter2);
        int temp_dan = datum.get(Calendar.DAY_OF_MONTH);
        int temp_mjesec = datum.get(Calendar.MONTH)+1;
        txt_dan_broj.setText("" + temp_dan );
        txt_dan_mjesec.setText(""+Alati.mjesecSlovima(temp_mjesec-1));

        txt_dan_broj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pokreniFragmentList(lista_obaveza);

            }
        });
        txt_dan_mjesec .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pokreniFragmentList(lista_obaveza);

            }
        });

        kal.setVisibility(View.VISIBLE);
        txt_dan_broj.setVisibility(View.VISIBLE);
        txt_dan_mjesec.setVisibility(View.VISIBLE);
        dan.setVisibility(View.VISIBLE);

        this.dodaj_obavezu = (ImageView) findViewById(R.id.opcijaDodajObavezu);
        this.dodaj_obavezu.setImageResource(R.drawable.plus_mark_black);
        this.dodaj_obavezu.setVisibility(View.VISIBLE);

        dodaj_obavezu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dodajObavezu();
            }
        });

        for(int i = 0; i < 8; i ++){
            this.polje_icona[i] = new IkonaObaveze(i+1, this);
        }

        this.header = new Doblak(this);

        layout = (RelativeLayout) findViewById(R.id.activity_dan_main);
        layout.setOnTouchListener(new OnSwipeTouchListener() {
            public boolean onSwipeTop() {
                if (tocka_oslonca > 0) {
                    pomakniDolje();
                }
                return true;
            }

            public boolean onSwipeBottom() {
                if(lista_obaveza.size() - tocka_oslonca > 8) {
                    pomakniGore();
                }
                return true;
            }

            public boolean onSwipeLeft() {
                pomakniDesno();
                return true;
            }

            public boolean onSwipeRight() {
                pomakniLijevo();
                return true;
            }
        });

    }

    private void pomakniGore(){
        this.tocka_oslonca++;
        veziObaveze();
        nacrtajObaveze();
    }

    private void pomakniDolje(){
        this.tocka_oslonca--;
        veziObaveze();
        nacrtajObaveze();
    }

    private void pomakniLijevo(){
        this.datum.add(Calendar.DAY_OF_MONTH, -1);
        sakrijSve();
        inicijalizirajPrikaze();
        testUcitajObaveze();

    }

    private void pomakniDesno(){
        this.datum.add(Calendar.DAY_OF_MONTH, 1);
        sakrijSve();
        inicijalizirajPrikaze();
        testUcitajObaveze();
    }

    public void prikaziSve(){
        postaviVidljivost(View.VISIBLE);
    }

    private void postaviVidljivost(int jeli){

        this.dan.setVisibility(jeli);
        this.kal.setVisibility(jeli);
        this.txt_dan_broj.setVisibility(jeli);
        this.txt_dan_mjesec.setVisibility(jeli);
        this.dodaj_obavezu.setVisibility(jeli);

        for(int i = 0; i < this.polje_icona.length; i++) {
            this.polje_icona[i].setVidljiv(jeli);
        }

        this.header.setVidljiv(jeli);
    }

    public void ispisiObavezuPrivremeno(ObavezaB __ob){

        this.header.postaviHeader(__ob);
    }

    public void osvjeziPrikazNakonIpsisa(){
        osvjezi();
    }

    private void testUcitajObaveze(){

        try {
            this.lista_obaveza = k_obaveze.dajDan(new Long(datum.getTimeInMillis()));
        }catch (RemoteException e){
            reeHelper(e);
        }
        prikaziObaveze();
    }

    private void pokreniKalendar(){
        Intent i = new Intent(this, Master.class);
        i.putExtra("datum", this.datum.getTimeInMillis());
        startActivityForResult(i, 2);
    }

    public void pokreniFragmentList(List<ObavezaB> pojedine){

        this.stanje.putString("fragmentList", "fragmentList");

        frag_list = new Fragment_listView(this);
        frag_list.postaviListu(pojedine);

        //sakrijSve();
        frag_list_postavljen = true;

        FragmentManager fm = getFragmentManager();

        fm.beginTransaction().add(R.id.activity_dan_main, frag_list).addToBackStack("fragList").commit();


    }

    public void okoncajFragmentList(){
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().remove(frag_list).commit();
        try {
            this.stanje.remove("fragmentList");
        }catch(Exception e){}
        osvjezi();
    }

    public void pokreniFragmentStatistika(List<ObavezaB> pojedine){

        this.stanje.putString("fragmentStatistika", "fragmentStatistika");

        frag_stat = new Fragment_statistika(this);
        frag_stat.postaviListuObaveza(pojedine);

        FragmentManager fm = getFragmentManager();

        fm.beginTransaction().add(R.id.activity_dan_main, frag_stat).addToBackStack("fragStats").commit();

    }

    public void okoncajFragmentStatistika(){
        try {
            this.stanje.remove("fragmentStatistika");
        }catch(Exception e){}
        FragmentManager fm = getFragmentManager();
        fm.beginTransaction().remove(frag_stat).commit();
    }

    public void zatvoriSveFragmente(){
        try{
            okoncajFragmentStatistika();
        }catch(Exception e){}
        try {
            okoncajFragmentList();
        }catch(Exception e){}
    }

    private void osvjezi(){
        sakrijSve();
        try {
            k_obaveze.init();
        }catch (RemoteException e){
            reeHelper(e);
        }
        inicijalizirajPrikaze();
        testUcitajObaveze();
    }

    private void resetirajPrikaze(){
        sakrijSve();
        inicijalizirajPrikaze();
        testUcitajObaveze();
    }

    private void ucitajDatumDefault() {
            this.datum.setTimeInMillis(System.currentTimeMillis());
    }

    private void ucitajSacuvanoStanje(){

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("datum")) {
                this.datum.setTimeInMillis(extras.getLong("datum"));
                osvjezi();
            }
            if(extras.containsKey("fragmentLista")){
                pokreniFragmentList(this.lista_obaveza);
            }
            if(extras.containsKey("fragmentStatistika")){
                pokreniFragmentStatistika(this.lista_obaveza);
            }
        }



    }

    private void prikaziObaveze(){

        this.napraviSucelje();

    }

    public void dodajObavezu(){
        Intent i = new Intent(this, ObavezaKreacija.class);

        i.putExtra("datum", datum.getTimeInMillis());
        i.putExtra("mode", "edit");
        startActivityForResult(i, 2);
    }

    public void dodajObavezu(ObavezaB obav){
        Intent i = new Intent(this, ObavezaKreacija.class);

        i.putExtra("datum", obav.dajVrijeme());
        i.putExtra("opis", obav.dajPoruku());
        i.putExtra("vrsta", (int) obav.dajVrstu());
        i.putExtra("mode", "prikaz");
        i.putExtra("trajanje", obav.dajTrajanje());

        startActivityForResult(i, 1);
    }

    protected void onActivityResult(int reqCode, int resCode, Intent data) {
        super.onActivityResult(reqCode, resCode, data);
        if(resCode == RESULT_OK){
            Log.i(TAG,"rezultat je tuuuuuu");

            if(reqCode == 2){


                Log.i(TAG,"rezultat je tuuuuuu    2");


                Bundle extras = data.getExtras();
                if(extras.containsKey("datum")){

                    Log.i(TAG,"rezultat je tuuuuuu  datm");
                    this.datum.setTimeInMillis(extras.getLong("datum"));
                }
            }

            osvjezi();
        }

    }

    private void setTocka_oslonca(){
        Long vrijeme = Alati.trenutnoVrijeme();

        int rez = Alati.danIzDatuma(vrijeme).compareTo(Alati.danIzDatuma(this.datum.getTimeInMillis()));
        if(rez != 0){
            this.tocka_oslonca = 0;
        }else {
            this.tocka_oslonca = tocka_oslonca_iz_obaveza(vrijeme, this.lista_obaveza, 0, this.lista_obaveza.size()-1);
        }
    }

    private int tocka_oslonca_iz_obaveza(Long vrijeme, List<ObavezaB> lista_ob, int pocetni, int zadnji){
        //return 0; /*
        int broj_elem = lista_ob.size();
        if(broj_elem==0){
            return 0;
        }
        int razlika = zadnji-pocetni;
        int tmp = pocetni + (razlika) / 2;

        if (razlika < 0 ){
            return zadnji + 1;
        }
        if (tmp < 0){
            return 0;
        }

        ObavezaB o = lista_ob.get(tmp);
        Long v_ob = o.dajVrijeme()+o.dajTrajanje()*60000;
        int rez = v_ob.compareTo(vrijeme);

        if(rez == 0){
            return tmp;
        }else if(rez < 0){
            return tocka_oslonca_iz_obaveza(vrijeme, lista_ob, tmp+1, zadnji);
        }else{
            return tocka_oslonca_iz_obaveza(vrijeme, lista_ob, pocetni, tmp-1);

        }//*/
    }

    private void veziObaveze(){
        int top = this.lista_obaveza.size()-this.tocka_oslonca;
        if(top > 8){
            top = 8;
        }
        for(int i = this.tocka_oslonca, j=0; j < top; i++,j++){
            this.polje_icona[j].setObaveza(this.lista_obaveza.get(i));
        }
        if(lista_obaveza.size() - tocka_oslonca > 0) {
            this.header.setObaveza(this.lista_obaveza.get(tocka_oslonca));
        }
    }

    private void nacrtajObaveze(){
        for(int i = 0; i < 8; i++){
            this.polje_icona[i].nacrtajIconu();
        }
    }

    private void napraviSucelje(){
        setTocka_oslonca();
        veziObaveze();
        nacrtajObaveze();
    }

    public List<ObavezaB> listaOdredjenih(int vrsta){
        List<ObavezaB> odredjene = new ArrayList<ObavezaB>();
        for(int i = 0; i < this.lista_obaveza.size(); i++){
            if(this.lista_obaveza.get(i).dajVrstu() == vrsta){
                odredjene.add(this.lista_obaveza.get(i));
            }
        }
        return odredjene;
    }



    private void reeHelper(RemoteException e){
        Log.w(TAG,"srvice ima problem " + e.getMessage() + " " + e.getStackTrace()[2].getLineNumber() );
    }
}
