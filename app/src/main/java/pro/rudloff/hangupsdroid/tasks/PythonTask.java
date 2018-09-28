package pro.rudloff.hangupsdroid.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import com.chaquo.python.PyException;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import java.lang.ref.WeakReference;
import pro.rudloff.hangupsdroid.App;
import pro.rudloff.hangupsdroid.runnables.ToastRunnable;

public class PythonTask extends AsyncTask<PyObject, Integer, Boolean> {

    // Use a weak reference so the activity can be garbage collected.
    private WeakReference<Activity> activityReference;
    private boolean catchExceptions = false;

    public PythonTask(Activity activity) {
        activityReference = new WeakReference<Activity>(activity);
    }

    public PythonTask(Activity activity, boolean newCatchExceptions) {
        this(activity);
        catchExceptions = newCatchExceptions;
    }

    protected Boolean doInBackground(PyObject... tasks) {
        Python py = Python.getInstance();
        PyObject asyncio = py.getModule("asyncio");

        PyObject loop = asyncio.callAttr("new_event_loop");
        for (PyObject task : tasks) {
            try {
                loop.callAttr("run_until_complete", task);
            } catch (PyException error) {
                if (catchExceptions) {
                    Activity activity = activityReference.get();
                    App app = (App) activity.getApplicationContext();

                    Log.e("hangupsdroid", error.getMessage());
                    if (activity != null) {
                        app.progressDialog.dismiss();
                        activity.runOnUiThread(new ToastRunnable(activity, error.getMessage()));
                    }
                } else {
                    throw error;
                }
            }
        }
        loop.callAttr("close");

        return true;
    }
}
