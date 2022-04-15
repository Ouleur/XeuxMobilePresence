package group.omarks.xeux;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class UploadJobService extends JobService {
    private static final String TAG = "SyncService";
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onStartJob(JobParameters params) {
        Intent intent1 = new Intent(this, FileUploadService.class);
        intent1.putExtra(FileUploadService.HOSTNAME,"http://192.168.8.103:5000");
        intent1.putExtra(FileUploadService.TYPE,"locaL");
        startService(intent1);

        Intent intent2 = new Intent(this, FileUploadService.class);
        intent2.putExtra(FileUploadService.HOSTNAME,"http://www.xeux.vit.ci");
        intent2.putExtra(FileUploadService.TYPE,"internet");
        startService(intent2);

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }
}
