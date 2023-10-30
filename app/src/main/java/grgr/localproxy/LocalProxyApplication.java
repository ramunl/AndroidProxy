package grgr.localproxy;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import io.realm.Realm;

/**
 * Created by daniel on 17/09/17.
 */

public class LocalProxyApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        MultiDex.install(this);
        Realm.init(this);
    }
}
