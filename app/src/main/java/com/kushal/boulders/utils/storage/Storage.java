package com.kushal.boulders.utils.storage;

import com.kushal.boulders.models.User;

public interface Storage {

    User getUser();

    void saveUserName(User user);

    void resetUserName();

    void resetUser();

}
