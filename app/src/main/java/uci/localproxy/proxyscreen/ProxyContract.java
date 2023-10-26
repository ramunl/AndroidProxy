package uci.localproxy.proxyscreen;

import androidx.annotation.NonNull;

import java.util.List;

import uci.localproxy.BasePresenter;
import uci.localproxy.BaseView;
import uci.localproxy.data.profile.Profile;
import uci.localproxy.data.user.User;

/**
 * Created by daniel on 15/09/17.
 */

public interface ProxyContract {

    interface Presenter extends BasePresenter {

        void startProxyFromFabButton(@NonNull String user, @NonNull String pass, @NonNull String profileID,
                        @NonNull String localPort, @NonNull boolean rememberPass,
                        @NonNull boolean setGlobalProxy);

        void startProxyFromHelpDialog(@NonNull String user, @NonNull String pass, @NonNull String profileID,
                                     @NonNull String localPort, @NonNull boolean rememberPass,
                                     @NonNull boolean setGlobalProxy, @NonNull boolean dontShowAgain);

        void stopCredentialCheckTask();

        void stopProxy();

        void onDestroy();

        void addNewProfile();

        void goToWifiConfDialog();

        void goToWifiSettings(boolean dontShowAgain);

        void filterUsers(String constraint);

        void loadUsers();

        void startServiceResult(int message);
    }

    interface View extends BaseView<Presenter> {

        void enableAllViews();

        void disableAllViews();

        void setPlayView();

        void setStopView();

        void setUsername(String username);

        void setPassword(String password);

        void setRememberPassword(boolean remember);

        void setLocalPort(String localPort);

        void setGlobalProxyChecked(boolean checked);

        void setSpinnerProfiles(List<Profile> profiles);

        void setSpinnerProfileSelected(String profileId);

        void showNoProfilesView();

        void setUsernameEmptyError();

        void setPasswordEmptyError();

        void setProfileNoSelectedError();

        void setLocalPortEmptyError();

        void setLocalPortOutOfRangeError();

        void setLocalPortNotAvailable();

        void setUsers(@NonNull List<User> users);

        void showAddProfile();

//        boolean isProxyServiceRunning();

        boolean isConnectedToAWifi();

        void stopProxyService();

        void startProxyService(String username, String password, String server,
                               int inputport, int outputport, String bypass,
                               String domain, boolean setGlobProxy);

        void showWifiConfDialog();

        void startWifiConfActivity();

        void showProgressDialog(boolean show);

        void showWrongCredentialsDialog();

        void showNetworkError();

        void showConnectionTimeOutError();

        void showUnknownHostError();

        void showConnectionError();

        void showErrorStartingService();

    }
}
