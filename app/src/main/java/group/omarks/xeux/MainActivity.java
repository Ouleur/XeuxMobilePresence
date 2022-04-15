package group.omarks.xeux;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;

import com.budiyev.android.codescanner.AutoFocusMode;
import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.budiyev.android.codescanner.DecodeCallback;
import com.budiyev.android.codescanner.ScanMode;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.zxing.Result;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


        Context context;
        private View mView;
        private ImageButton qrCodeFoundButton;
        private String qrCode = "";

        private static final int PERMISSION_REQUEST_CAMERA = 0;


        private PreviewView previewView;
        private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;


        NfcAdapter nfcAdapter;
        PendingIntent pendingIntent;
        IntentFilter writeTagFilters[];
        Tag myTag;
        private Button lier_carte;
        boolean writeMode;

        private FileWriter mFileWriter;
        private FileWriter mFileWriterInt;
        private CodeScannerView scannerView;
        private CodeScanner mCodeScanner;
        private Boolean condition;
        ProgressDialog pd;
        Tools tools = new Tools();
        JobScheduler mJobScheduler;
        private static final String TAG = "MainActivity";
        Timer timer;
        public SharedPreferences prefs;
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(this));

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


            context = this;

            prefs = PreferenceManager.getDefaultSharedPreferences(context);

            condition=true;



            initStorage();
            scannerView = findViewById(R.id.scanner_view);

            qrCodeFoundButton = findViewById(R.id.activity_main_qrCodeFoundButton);
//            qrCodeFoundButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    if(checkInternetConenction()) {
//                        mCodeScanner.stopPreview();
//
//                        mCodeScanner.startPreview();
//                        scannerView.setVisibility(View.VISIBLE);
//                        qrCodeFoundButton.setVisibility(View.INVISIBLE);
//                    }
//                }
//            });

            nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            if (nfcAdapter == null) {
                // Stop here, we definitely need NFC
                Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
                finish();
            }

            try {
                readFromIntent(getIntent());
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
            pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
            tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
            writeTagFilters = new IntentFilter[] { tagDetected };


//            new .JsonGetInfoTask().execute("http://192.168.8.100:5000/items/"+code);


            scannerView.setVisibility(View.GONE);
            mCodeScanner = new CodeScanner(this, scannerView);
            // Parameters (default values)

            mCodeScanner.setCamera(CodeScanner.CAMERA_FRONT); // or CAMERA_FRONT or specific camera id
            mCodeScanner.setFormats(CodeScanner.ALL_FORMATS); // list of type BarcodeFormat,
            // ex. listOf(BarcodeFormat.QR_CODE)
            mCodeScanner.setAutoFocusMode(AutoFocusMode.SAFE);
            // or CONTINUOUS
            mCodeScanner.setScanMode(ScanMode.SINGLE); // or CONTINUOUS or PREVIEW
            mCodeScanner.setAutoFocusEnabled(true);// Whether to enable auto focus or not
            mCodeScanner.setFlashEnabled(false); // Whether to enable flash or not

            mCodeScanner.setDecodeCallback(new DecodeCallback() {
                @Override
                public void onDecoded(@NonNull final Result result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            qrCode = result.getText();
//                                Toast.makeText(getApplicationContext(), qrCode, Toast.LENGTH_SHORT).show();

                                //finish();
                            mCodeScanner.stopPreview();
                            scannerView.setVisibility(View.INVISIBLE);
                            qrCodeFoundButton.setVisibility(View.VISIBLE);
                                ViewDialog alert = new ViewDialog();
                            try {
                                createCsvFile(new String[]{"",qrCode,tools.getJourHeure()});
                                    alert.showDialog(MainActivity.this, null);
                                } catch (InterruptedException | IOException e) {
                                    e.printStackTrace();
                                }
                                alert.dismissDialog();


                        }
                    });
                }
            });

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String time = prefs.getString("pref_time_val", "1000");
            Log.i("time",time);

            timer = new Timer();
            timer.schedule(new MyTimerTask(getApplicationContext()), 1000, Integer.parseInt(time));

        }

    // create an action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // R.menu.mymenu is a reference to an xml file named mymenu.xml which should be inside your res/menu directory.
        // If you don't have res/menu, just create a directory named "menu" inside res
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // handle button activities
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.mybutton) {
            // do something here
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(i);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == PERMISSION_REQUEST_CAMERA) {
                if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
                    requestCamera();
                }
            }
        }

        private void requestCamera() {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
                    }
                }
            }

    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) throws InterruptedException, IOException {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//            showAlert();
//            new SweetAlertDialog(MainActivity.this).setCustomView(mView).show();
            createCsvFile(new String[]{StringUtil.toHexStr(myTag.getId()),"",tools.getJourHeure()});

            mCodeScanner.stopPreview();
            scannerView.setVisibility(View.INVISIBLE);
            qrCodeFoundButton.setVisibility(View.VISIBLE);

            ViewDialog alert = new ViewDialog();
            alert.showDialog(this, null);
            alert.dismissDialog();


        }
    }

    public static String readTag(Tag tag, Intent intent) {
        if (tag != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ReadTime:").append(TimeUtil.curTime(System.currentTimeMillis()))
                     .append("\n")
                    .append("ID:").append(StringUtil.toHexStr(tag.getId())).append("\n");
            return StringUtil.toHexStr(tag.getId()).toString();
        }
        return null;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        try {
            readFromIntent(intent);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())|| NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            Log.i("TAG",myTag.toString());
        }
        Log.i("TAG",myTag.toString());
    }

    @Override
    public void onPause(){
        super.onPause();
        WriteModeOff();
        mCodeScanner.stopPreview();
    }

    @Override
    public void onResume(){
        super.onResume();
        WriteModeOn();
        mCodeScanner.stopPreview();
        mCodeScanner.startPreview();
    }



    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }
    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }

    private class JsonUpdateInfoTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);   //here u ll get whole response...... :-)
                }

                return buffer.toString();

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()){
                pd.dismiss();
            }

            try {
                JSONObject jsonObject = new JSONObject(result);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    // Scane de carte

    // Affichage Bonne Journée à vous [Vert- Blanc]/ Erreur sur la carte [Roge - BLanc]

    /*** sauvegarde local
     * ID_carte / Matricule
     * date et heure
     *
     * SQlite / CSV
     * */

    // envoie sur wiffi
    /**
     * Si l'envoie est correcte alors
     * marque WIFFI OK / NOK
     */

    // envoie par internet
    /**
     * Si l'envoie est correcte alors
     * marque INTERNET OK / NOK
     */




    public void showAlert(){
        AlertDialog.Builder alertDialog2 = new AlertDialog.Builder(
                this);

        // Setting Dialog Title
        alertDialog2.setTitle("Confirmation Operation...");

        // Setting Dialog Message
        alertDialog2.setMessage("Voulez vous lier cette carte à cet(te) Etudiant(e) ?");

        // Setting Icon to Dialog
        //        alertDialog2.setIcon(R.drawable.delete);

        // Setting Positive "Yes" Btn
        alertDialog2.setPositiveButton("OUI",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        new JsonUpdateInfoTask().execute("http://192.168.8.100:5000/api/v1.0/presence/"+StringUtil.toHexStr(myTag.getId()));
                        Toast.makeText(getApplicationContext(),
                                "Operation en cours", Toast.LENGTH_SHORT)
                                .show();
                        qrCode = "";

                    }
                });
        // Setting Negative "NO" Btn
        alertDialog2.setNegativeButton("NON",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        Toast.makeText(getApplicationContext(),
                                "Operation annulée", Toast.LENGTH_SHORT)
                                .show();
                        dialog.cancel();
                        qrCode = "";

                    }
                });

        // Showing Alert Dialog
        alertDialog2.show();
    }


    public void creatFile(){


    }

    public void addData(){
        /**
         * carte_id / matricule
         * date_heure
         */

    }

//    public static class AsyncHttpPostTask extends AsyncTask<File, Void, String> {
//
//        private static final String TAG = AsyncHttpPostTask.class.getSimpleName();
//        private String server;
//
//        public AsyncHttpPostTask(final String server) {
//            this.server = server;
//        }
//
//        @Override
//        protected String doInBackground(File... params) {
//            Log.d(TAG, "doInBackground");
//            HttpClient http = AndroidHttpClient.newInstance("MyApp");
//            HttpPost method = new HttpPost(this.server);
//            method.setEntity(new FileEntity(params[0], "text/plain"));
//            try {
//                HttpResponse response = http.execute(method);
//                BufferedReader rd = new BufferedReader(new InputStreamReader(
//                        response.getEntity().getContent()));
//                final StringBuilder out = new StringBuilder();
//                String line;
//                try {
//                    while ((line = rd.readLine()) != null) {
//                        out.append(line);
//                    }
//                } catch (Exception e) {}
//                // wr.close();
//                try {
//                    rd.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                // final String serverResponse = slurp(is);
//                Log.d(TAG, "serverResponse: " + out.toString());
//            } catch (ClientProtocolException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }

    public void initStorage(){
        Log.i("file",Environment.getExternalStorageDirectory().toString());
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "XeuxDATA");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            // Do something on success
            new File(Environment.getExternalStorageDirectory() +File.separator + "XeuxDATA"+File.separator +"Local").mkdirs();
            new File(Environment.getExternalStorageDirectory() +File.separator + "XeuxDATA"+File.separator +"Internet").mkdirs();
        } else {
            // Do something else on failure
        }
    }

    public void createCsvFile(String[] data) throws IOException {
        tools = new Tools();
        String fileName = "Xeux-data-"+tools.getJour("ddMMyyyy")+"-"+ prefs.getString("nom_device", "")+".csv";
        String localFile = Environment.getExternalStorageDirectory() + File.separator +"XeuxDATA"+ File.separator +"Local"+ File.separator +fileName;
        String internetFile = Environment.getExternalStorageDirectory() + File.separator + "XeuxDATA"+ File.separator +"Internet"+File.separator +fileName;

        File lf = new File(localFile);
        File inf = new File(internetFile);

        CSVWriter writer = null;
        CSVWriter writerIn = null;

        if(lf.exists() && !lf.isDirectory() && inf.exists() && !inf.isDirectory()){
            try {
                mFileWriter = new FileWriter(localFile , true);
                mFileWriterInt = new FileWriter(internetFile , true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            writer = new CSVWriter(mFileWriter);
            writerIn = new CSVWriter(mFileWriterInt);
        }else {
            try {
                writer = new CSVWriter(new FileWriter(localFile));
                writer.writeNext(new String[]{"ID_card","Matricule", "Date"});

                writerIn = new CSVWriter(new FileWriter(internetFile));
                writerIn.writeNext(new String[]{"ID_card", "Matricule","Date"});
                writer.flush();
                writerIn.flush();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        writer.writeNext(data);
        writerIn.writeNext(data);

        writer.flush();
        writerIn.flush();
    }


    private File getLogFile() {
        Tools tools = new Tools();

        String path = Environment.getExternalStorageDirectory().toString()+ File.separator +"XeuxDATA"+ File.separator +"Local";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }
//        String localFile = Environment.getExternalStorageDirectory() + File.separator +"XeuxDATA"+ File.separator +"Local"+ File.separator +"Xeux-data-"+tools.getJour("ddMMyyyy")+".csv";
//        String internetFile = Environment.getExternalStorageDirectory() + File.separator + "XeuxDATA"+ File.separator +"Internet"+File.separator +"Xeux-data-"+tools.getJour("ddMMyyyy")+".csv";


        return new File(files[0].getName());

    }


    public void sendToLocal(){

    }

    public void sendToInternet(){

    }


    private boolean checkInternetConenction() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec
                =(ConnectivityManager)getSystemService(getBaseContext().CONNECTIVITY_SERVICE);

        // Check for network connections
        if ( connec.getNetworkInfo(0).getState() ==
                android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() ==
                        android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED ) {
//            Toast.makeText(this, " Connected ", Toast.LENGTH_LONG).show();
            return true;
        }else if (
                connec.getNetworkInfo(0).getState() ==
                        android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() ==
                                android.net.NetworkInfo.State.DISCONNECTED  ) {
            Toast.makeText(this, " Not Connected ", Toast.LENGTH_LONG).show();
            return false;
        }
        return false;
    }

}




class TimeUtil {
    public static String curTime(long time) {
        return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH).format(new Date(time));
    }
}

class StringUtil {
    /**
     * 将字节数组转化成16进制字符串
     *Convertir le tableau d'octets en chaîne hexadécimale
     * @param bytes
     * @return
     */
    public static String toHexStr(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder iStringBuilder = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String vStr = Integer.toHexString(v);
            if (vStr.length() < 2) {
                iStringBuilder.append(0);
            }
            iStringBuilder.append(vStr);
        }
        return iStringBuilder.toString().toUpperCase();
    }

    /**
     * 将16位的short转换成byte数组
     * @param s
     * @return
     */
    public static byte[] shortToByteArray(short s) {
        byte[] targets = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = (targets.length - 1 - i) * 8;
            targets[i] = (byte) ((s >>> offset) & 0xff);
        }
        return targets;
    }


}

class MyTimerTask extends TimerTask {

    Context mContext;
    public MyTimerTask(Context context) {
        mContext = context;
    }
    @Override
    public void run() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        String addres_local = prefs.getString("adress_local", "");
        String addres_internet = prefs.getString("adress_internet", "");
        String nbf = prefs.getString("nombre_fichiers", "1000");


        Intent intent1 = new Intent(mContext, FileUploadService.class);
                intent1.putExtra("HOSTNAME",addres_local);
                intent1.putExtra("TYPE","local");
                intent1.putExtra("NBF",nbf.split(" ")[0]);
                mContext.startService(intent1);

                Intent intent2 = new Intent(mContext, FileUploadService.class);
                intent2.putExtra("HOSTNAME",addres_internet);
                intent2.putExtra("TYPE","internet");
                intent2.putExtra("NBF",nbf.split(" ")[0]);
                mContext.startService(intent2);



    }

}

class ViewDialog {
    public  Dialog dialog;
    public void showDialog(Activity activity, String msg) throws InterruptedException {
        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.succeslayout);

        TextView text = (TextView) dialog.findViewById(R.id.text_dialog);
        if (msg != null) {
            text.setText(msg);
        }
//        Button dialogButton = (Button) dialog.findViewById(R.id.btn_dialog);
//        dialogButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });

        dialog.show();
    }

    public void dismissDialog(){
            Runnable dismissRunner = new Runnable() {
                public void run() {
                    if (dialog != null)
                        dialog.dismiss();
                }

                ;
            };
            new Handler().postDelayed( dismissRunner, 2000 );



    }
}

