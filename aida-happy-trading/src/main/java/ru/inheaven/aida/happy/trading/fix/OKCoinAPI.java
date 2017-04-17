package ru.inheaven.aida.happy.trading.fix;

import java.util.HashMap;
import java.util.Map;

/**
 * @author inheaven on 22.07.2016.
 */
class OKCoinAPI {
    final static Map<String, String> KEY = new HashMap<>();

    static {
        KEY.put("8b8620cf-83ed-46d8-91e6-41e5eb65f44f", "DBB5E3FAA26238E9613BD73A3D4ECEDC");
        KEY.put("8784b792-f956-4cc5-afb5-706d313b2bf3", "216D259AECB4DBE7E758FFF4E7E054DC");
        KEY.put("eecd6e5e-25a9-4b20-839b-dc97f02abe31", "EB906E792188A6CC8FF789F97DEB48B1");
        KEY.put("48a255bd-60a1-4d41-b1f8-38dd8b18f8cf", "52B653FAE3852414FB3912DE71D83B59");
        KEY.put("030af393-e14a-4069-a8c0-6955f11d1cab", "1C5B3609AAA2484D2380739CE6F3FF1C");
    }

    final static Map<String, Long> ACCOUNT_ID = new HashMap<>();

    static {
        ACCOUNT_ID.put("8b8620cf-83ed-46d8-91e6-41e5eb65f44f", 8L);
        ACCOUNT_ID.put("8784b792-f956-4cc5-afb5-706d313b2bf3", 8L);
        ACCOUNT_ID.put("eecd6e5e-25a9-4b20-839b-dc97f02abe31", 8L);
        ACCOUNT_ID.put("48a255bd-60a1-4d41-b1f8-38dd8b18f8cf", 8L);
        ACCOUNT_ID.put("030af393-e14a-4069-a8c0-6955f11d1cab", 8L);
    }
}
