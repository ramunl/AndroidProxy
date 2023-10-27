package uci.localproxy;

import android.app.Application;
import android.os.Build;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import io.realm.Realm;

/**
 * Created by daniel on 17/09/17.
 */

public class LocalProxyApplication extends MultiDexApplication {
    public static final int MAX_SDK_SUPPORTED_FOR_WIFI_CONF = Build.VERSION_CODES.LOLLIPOP_MR1;
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);

        Realm.init(this);
    }
}
