package cn.edu.zafu.corecookie.impl;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import java.net.HttpCookie;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.zafu.corecookie.PersistentCookieStore;
import cn.edu.zafu.corecookie.SerializableHttpCookie;

/**
 * Cookie处理Android实现类
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-08-31
 * Time: 10:04
 */
public class PersistentCookieStoreAndroidImpl extends PersistentCookieStore {
    private final SharedPreferences cookiePrefs;
    private static final String LOG_TAG = "PersistentCookieStore";
    private static final String COOKIE_PREFS = "CookiePrefsFile";


    /**
     * Construct a persistent cookie store.
     *
     * @param context Context to attach cookie store to
     */
    public PersistentCookieStoreAndroidImpl(Context context) {
        cookiePrefs = context.getSharedPreferences(COOKIE_PREFS, 0);
        Map<String, ?> prefsMap = cookiePrefs.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            if (((String) entry.getValue()) != null && !((String) entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                String[] cookieNames = TextUtils.split((String) entry.getValue(), ",");
                for (String name : cookieNames) {
                    String encodedCookie = cookiePrefs.getString(COOKIE_NAME_PREFIX + name, null);
                    if (encodedCookie != null) {
                        HttpCookie decodedCookie = decodeCookie(encodedCookie);
                        if (decodedCookie != null) {
                            if (!cookies.containsKey(entry.getKey()))
                                cookies.put(entry.getKey(), new ConcurrentHashMap<String, HttpCookie>());
                            cookies.get(entry.getKey()).put(name, decodedCookie);
                        }
                    }
                }

            }
        }
    }


    @Override
    public void add(URI uri, HttpCookie cookie,String name) {
        // Save cookie into persistent store
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.putString(uri.getHost(), TextUtils.join(",", cookies.get(uri.getHost()).keySet()));
        prefsWriter.putString(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableHttpCookie(cookie)));
        prefsWriter.commit();
    }


    @Override
    public boolean remove(URI uri, HttpCookie cookie, String name) {
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        if(cookiePrefs.contains(COOKIE_NAME_PREFIX + name)) {
            prefsWriter.remove(COOKIE_NAME_PREFIX + name);
        }
        prefsWriter.putString(uri.getHost(), TextUtils.join(",", cookies.get(uri.getHost()).keySet()));
        boolean commit = prefsWriter.commit();
        return commit;
    }

    @Override
    public boolean removeOther() {
        SharedPreferences.Editor prefsWriter = cookiePrefs.edit();
        prefsWriter.clear();
        boolean commit = prefsWriter.commit();
        return commit;
    }
}
