package group.omarks.xeux;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

//import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FileUploadService extends IntentService {
    public static String HOSTNAME = "";
    public static String TYPE = "";
    public static String NBF = "";
    private File[] files;

    public FileUploadService() {
        super(FileUploadService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String username = "myName";
        String userToken = "some%token%data";
        HOSTNAME = intent.getExtras().getString("HOSTNAME");
        TYPE = intent.getExtras().getString("TYPE");
        NBF = intent.getExtras().getString("NBF");

        Log.i("Adresse",HOSTNAME);
        Log.i("Local",TYPE);


        try {
            String url = HOSTNAME+"/api/v1.0/upload_file";

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.connectTimeout(30, TimeUnit.SECONDS);
            builder.readTimeout(30, TimeUnit.SECONDS);

            if(TYPE.equals("local")) {
                files = getLocalFile();
            }else{
                files = getInternetFile();
            }
            for (int i =0; files.length>i;i++ ) {
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("file", files[i].getName(),
                                RequestBody.create(MediaType.parse("text/csv"), files[i]))
//                        .addFormDataPart("username", username)
                        .build();

                Request requestBuilder = new Request.Builder()
//                        .header("Authorization", userToken)
                        .url(url)
                        .post(requestBody)
                        .build();

                OkHttpClient client = builder.build();

                Response response = client.newCall(requestBuilder).execute();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File[] getLocalFile() {
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


        return DeleteFile(path);

    }

    private File[] getInternetFile() {
        Tools tools = new Tools();

        String path = Environment.getExternalStorageDirectory().toString()+ File.separator +"XeuxDATA"+ File.separator +"Internet";
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        Log.d("Files", "Size: "+ files.length);
        for (int i = 0; i < files.length; i++)
        {
            Log.d("Files", "FileName:" + files[i].getName());
        }


        return DeleteFile(path);

    }

    private File[] DeleteFile(String path) {
        Tools tools = new Tools();

        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        File[] files = directory.listFiles();
        int del_Val = files.length-Integer.parseInt(NBF);
        for(int i=0; i<del_Val;i++){
            files[i].delete();
        }
        files = directory.listFiles();

//        String localFile = Environment.getExternalStorageDirectory() + File.separator +"XeuxDATA"+ File.separator +"Local"+ File.separator +"Xeux-data-"+tools.getJour("ddMMyyyy")+".csv";
//        String internetFile = Environment.getExternalStorageDirectory() + File.separator + "XeuxDATA"+ File.separator +"Internet"+File.separator +"Xeux-data-"+tools.getJour("ddMMyyyy")+".csv";


        return files;

    }
}
