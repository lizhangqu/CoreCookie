package cn.edu.zafu.corecookie.factory;

import android.content.Context;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import cn.edu.zafu.corecookie.impl.PersistentCookieStoreAndroidImpl;
import cn.edu.zafu.corecookie.impl.PersistentCookieStoreJavaImpl;

/**
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-08-31
 * Time: 11:10
 */
public class CookieHandlerFactory {
    private static CookieManager cookieManager = null;

    public static CookieHandler getCookieHandler(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context can't be null.");
        }
        if (cookieManager == null) {
            cookieManager = new CookieManager(new PersistentCookieStoreAndroidImpl(context.getApplicationContext()), CookiePolicy.ACCEPT_ALL);
        }
        return cookieManager;
    }

    public static CookieHandler getCookieHandler() {
        if (cookieManager == null) {
            cookieManager = new CookieManager(new PersistentCookieStoreJavaImpl(), CookiePolicy.ACCEPT_ALL);
        }
        return cookieManager;
    }
}
