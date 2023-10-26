package uci.localproxy.profilescreens.profiledetails;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;

import uci.localproxy.proxydata.profile.Profile;
import uci.localproxy.proxydata.profile.source.ProfilesDataSource;
import uci.localproxy.proxydata.profile.source.ProfilesLocalDataSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by daniel on 18/09/17.
 */

public class ProfileDetailsPresenter implements ProfileDetailsContract.Presenter{

    private final ProfilesLocalDataSource mProfilesLocalDataSource;

    @NonNull
    private final ProfileDetailsContract.View mProfilesDetailsView;

    @Nullable
    private String mProfileId;

    public ProfileDetailsPresenter(@NonNull ProfileDetailsContract.View profilesDetailsView,
                                   @Nullable String profileId) {
        mProfilesLocalDataSource = ProfilesLocalDataSource.newInstance();
        mProfilesDetailsView = checkNotNull(profilesDetailsView);
        mProfileId = profileId;

        mProfilesDetailsView.setPresenter(this);
    }


    @Override
    public void start() {
        openProfile();
    }

    private void openProfile(){
        if (Strings.isNullOrEmpty(mProfileId)){
            mProfilesDetailsView.showMissingProfile();
            return;
        }

        mProfilesLocalDataSource.getProfile(mProfileId, new ProfilesDataSource.GetProfileCallback() {
            @Override
            public void onProfileLoaded(Profile profile) {
                if (!mProfilesDetailsView.isActive()){
                    return;
                }
                showProfile(profile);
            }

            @Override
            public void onDataNoAvailable() {
                if (!mProfilesDetailsView.isActive()){
                    return;
                }
                mProfilesDetailsView.showMissingProfile();
            }
        });
    }

    private void showProfile(@NonNull Profile profile){
        if (!mProfilesDetailsView.isActive()){
            return;
        }

        mProfilesDetailsView.showName(profile.getName());
        mProfilesDetailsView.showServer(profile.getHost());
        mProfilesDetailsView.showInPort(profile.getInPort());
        mProfilesDetailsView.showBypass(profile.getBypass());
//        mProfilesDetailsView.showDomain(profile.getDomain());
    }

    @Override
    public void editProfile() {
        if (Strings.isNullOrEmpty(mProfileId)){
            mProfilesDetailsView.showMissingProfile();
            return;
        }
        mProfilesDetailsView.showEditProfile(mProfileId);
    }

    @Override
    public void deleteProfile() {
        if (Strings.isNullOrEmpty(mProfileId)){
            mProfilesDetailsView.showMissingProfile();
            return;
        }
        mProfilesLocalDataSource.deleteProfile(mProfileId);
        mProfilesDetailsView.showProfileDeleted();
    }

    @Override
    public void result(int requestCode, int resultCode) {
        if (requestCode == ProfileDetailsFragment.REQUEST_EDIT_PROFILE &&
                resultCode == Activity.RESULT_OK){
            mProfilesDetailsView.showSuccessfullyUpdatedMessage();
        }
    }

    @Override
    public void onDestroy() {
        mProfilesLocalDataSource.releaseResources();
    }

}
