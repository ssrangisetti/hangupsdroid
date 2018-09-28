package pro.rudloff.hangupsdroid.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import pro.rudloff.hangupsdroid.App;
import pro.rudloff.hangupsdroid.CredentialsPrompt;
import pro.rudloff.hangupsdroid.R;
import pro.rudloff.hangupsdroid.RefreshTokenCache;
import pro.rudloff.hangupsdroid.listeners.LoginButtonListener;
import pro.rudloff.hangupsdroid.runnables.ProgressDialogRunnable;
import pro.rudloff.hangupsdroid.tasks.PythonTask;

public class LoginActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        App app = (App) getApplicationContext();
        Button loginButton = findViewById(R.id.btn_login);
        EditText passwordText = findViewById(R.id.input_password);
        RefreshTokenCache cache = new RefreshTokenCache(this);
        LoginButtonListener listener = new LoginButtonListener(this);

        loginButton.setOnClickListener(listener);
        passwordText.setOnEditorActionListener(listener);

        if (app.pythonApp.get("client") != null) {
            onConnected();
        } else if (cache.get() != null) {
            login();
        }
    }

    public void login() {
        App app = (App) getApplicationContext();

        Python py = Python.getInstance();
        PyObject hangups = py.getModule("hangups");
        PyObject hangupsdroid = py.getModule("hangupsdroid");

        runOnUiThread(new ProgressDialogRunnable(this, getString(R.string.auth_dialog)));

        new PythonTask(this, true)
                .execute(
                        app.pythonApp.callAttr(
                                "getAuth",
                                this,
                                new CredentialsPrompt(this),
                                new RefreshTokenCache(this)));
    }

    public void onConnected() {
        Intent intent = new Intent(this, ConversationListActivity.class);
        startActivity(intent);
    }

    public void onAuth(PyObject cookies) {
        App app = (App) getApplicationContext();

        runOnUiThread(new ProgressDialogRunnable(this, getString(R.string.connect_dialog)));
        new PythonTask(this).execute(app.pythonApp.callAttr("connect", this, cookies));
    }
}
