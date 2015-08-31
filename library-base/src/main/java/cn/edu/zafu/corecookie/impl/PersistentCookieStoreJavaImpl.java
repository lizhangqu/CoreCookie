package cn.edu.zafu.corecookie.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.edu.zafu.corecookie.PersistentCookieStore;
import cn.edu.zafu.corecookie.SerializableHttpCookie;

/**
 * Cookie处理Java实现类
 * User:lizhangqu(513163535@qq.com)
 * Date:2015-08-31
 * Time: 10:04
 */
public class PersistentCookieStoreJavaImpl extends PersistentCookieStore {
    private static final String COOKIE_FILE_NAME = "cookie.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private Map<String, String> cookieMap;

    public PersistentCookieStoreJavaImpl() {
        String cookieJson = readFile(COOKIE_FILE_NAME);
        Map<String, String> fromJson = gson.fromJson(cookieJson, new TypeToken<Map<String, String>>() {
        }.getType());
        if (fromJson != null) {
            cookieMap = fromJson;
            System.out.println(fromJson);
        } else {
            cookieMap = new HashMap<String, String>();
        }
        // Load any previously stored cookies into the store
        for (Map.Entry<String, ?> entry : cookieMap.entrySet()) {
            if (((String) entry.getValue()) != null && !((String) entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                String[] cookieNames = split((String) entry.getValue(), ",");
                for (String name : cookieNames) {
                    String encodedCookie = cookieMap.get(COOKIE_NAME_PREFIX + name);
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
    public void add(URI uri, HttpCookie cookie, String name) {
        cookieMap.put(uri.getHost(), join(",", cookies.get(uri.getHost()).keySet()));
        cookieMap.put(COOKIE_NAME_PREFIX + name, encodeCookie(new SerializableHttpCookie(cookie)));

        String json = gson.toJson(cookieMap);
        saveFile(json.getBytes(), COOKIE_FILE_NAME);
    }

    @Override
    public boolean removeOther() {
        cookieMap.clear();
        return true;
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie, String name) {
        if (cookieMap.containsKey(COOKIE_NAME_PREFIX + name)) {
            cookieMap.remove(COOKIE_NAME_PREFIX + name);
        }
        cookieMap.put(uri.getHost(), join(",", cookies.get(uri.getHost()).keySet()));
        return true;
    }

    public static String join(CharSequence delimiter, Iterable tokens) {
        StringBuilder sb = new StringBuilder();
        boolean firstTime = true;
        for (Object token : tokens) {
            if (firstTime) {
                firstTime = false;
            } else {
                sb.append(delimiter);
            }
            sb.append(token);
        }
        return sb.toString();
    }

    public static String[] split(String text, String expression) {
        if (text.length() == 0) {
            return new String[]{};
        } else {
            return text.split(expression, -1);
        }
    }

    public static void saveFile(byte[] bfile, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            file = new File(fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(bfile);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public static String readFile(String fileName) {
        BufferedInputStream bis = null;
        FileInputStream fis = null;
        File file = null;
        try {
            file = new File(fileName);
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);

            int available = bis.available();
            byte[] bytes = new byte[available];
            bis.read(bytes);
            String str = new String(bytes);
            return str;
        } catch (FileNotFoundException e){
            System.out.println("cookie file not exists ,it will create soon. ");
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        return "";
    }
}
