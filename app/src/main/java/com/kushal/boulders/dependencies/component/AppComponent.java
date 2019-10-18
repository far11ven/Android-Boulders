package com.kushal.boulders.dependencies.component;

import com.kushal.boulders.activities.AboutActivity;
import com.kushal.boulders.activities.AddMemberActivity;
import com.kushal.boulders.activities.AuthenticatedActivity;
import com.kushal.boulders.activities.LoginActivity;
import com.kushal.boulders.activities.EditMemberProfileActivity;
import com.kushal.boulders.activities.MainActivity;
import com.kushal.boulders.activities.MemberProfileActivity;
import com.kushal.boulders.activities.RegistrationActivity;
import com.kushal.boulders.activities.UserProfileActivity;
import com.kushal.boulders.dependencies.module.ConfigStoragePrefModule;
import com.kushal.boulders.dependencies.module.ImageStoragePrefModule;
import com.kushal.boulders.dependencies.module.NetworkModule;
import com.kushal.boulders.dependencies.module.SharedPrefStorageModule;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {NetworkModule.class, SharedPrefStorageModule.class, ImageStoragePrefModule.class, ConfigStoragePrefModule.class})
public interface AppComponent {

    void inject(RegistrationActivity activity);

    void inject(MainActivity activity);

    void inject(LoginActivity activity);

    void inject(AuthenticatedActivity activity);

    void inject(AddMemberActivity activity);

    void inject(EditMemberProfileActivity activity);

    void inject(MemberProfileActivity activity);

    void inject(UserProfileActivity activity);

    void inject(AboutActivity activity);

}
