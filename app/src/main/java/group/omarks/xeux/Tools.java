package group.omarks.xeux;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by adou on 02/10/17.
 */

public class Tools {

    MainActivity activity ;

    Button connexion= null;
    EditText pseudo = null;
    EditText pass = null;

    Dialog dialog = null;
    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS =0 ;
    public String phoneNo="";
    public String message="";
    public static final String PREFS_NAME = "PdvPrefFile";
    public SharedPreferences settings;


    public MainActivity getActivity() {
        return activity;

    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
        settings = PreferenceManager.getDefaultSharedPreferences(activity);
        setPhoneNo(settings.getString("numero_boss",""));
    }

    public Tools(){

    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = "Le point de l'agence ["+settings.getString("name","")+"] :\n"+message;
    }

    public String getJour(String format){
        long dv = Long.valueOf(System.currentTimeMillis());// its need to be in milisecond
        Date df = new Date(dv);
        String jour = new SimpleDateFormat(format).format(df);
        return jour;
    }

    public String getJourHeure(){
        long dv = Long.valueOf(System.currentTimeMillis());// its need to be in milisecond
        Date df = new Date(dv);
        String jour = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(df);
        return jour;
    }

    public String getTime(){
        return String.valueOf(System.currentTimeMillis());// its need to be in milisecond

    }

    public String getDateFin(){
        long dv = Long.valueOf(System.currentTimeMillis());// its need to be in milisecond
        Date df = new Date(dv);
        String annee = new SimpleDateFormat("yyyy-MM-dd").format(df);
        return annee;
    }

    public String getMois(){
        long dv = Long.valueOf(System.currentTimeMillis());// its need to be in milisecond
        Date df = new Date(dv);
        String jour = new SimpleDateFormat("MM").format(df);
        return jour;
    }

//    public Boolean etat(){
//        Boolean result = false;
//
//        Periode_manager pm = new Periode_manager(activity);
//        pm.open();
//
//        long dv = Long.valueOf(System.currentTimeMillis());// its need to be in milisecond
//        Date df = new Date(dv);
//        String jour = new SimpleDateFormat("dd/MM/yyyy").format(df);
//
//        Periode p = pm.getPeriode(jour);
//        pm.close();
//        Log.i("periode etat","test"+p.toString());
//
////        Toast.makeText(activity,"etat"+p.getEtat(),Toast.LENGTH_LONG).show();
//        if (p.getEtat().equals("")){
//            result = false;
//        }else {
//            result = true;
//        }
//        pm.close();
//        return result;
//    }



    public static String MD5_Hash(String s) {
        MessageDigest m = null;

        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        Log.i("ETAT",s);
        m.update(s.getBytes(),0,s.length());
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash;
    }


//    public void connexion(){
//        this.dialog = new Dialog(activity);
//        dialog.setContentView(R.layout.connect);
//        this.connexion = (Button)dialog.findViewById(R.id.connexion);
//        this.pseudo = (EditText)dialog.findViewById(R.id.pseudo);
//        this.pass = (EditText)dialog.findViewById(R.id.pass);
//
//
//    }

    protected void sendSMSMessage() {

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + this.getPhoneNo()));
        intent.putExtra("sms_body", this.getMessage());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().getBaseContext().startActivity(intent);
    }

}
