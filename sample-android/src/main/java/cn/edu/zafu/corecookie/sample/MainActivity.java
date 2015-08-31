package cn.edu.zafu.corecookie.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;

import cn.edu.zafu.corecookie.factory.CookieHandlerFactory;


public class MainActivity extends AppCompatActivity {
    private static OkHttpClient client = new OkHttpClient();
    private static String xsrf;
    private static String email = "**************";
    private static String password = "********";

    private static ImageView code;
    private static Button getCode, login, info;
    private static EditText input;
    private static TextView tv;
    private static Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    code.setImageBitmap((Bitmap) msg.obj);
                    break;
                case 2:
                    tv.setText((String) msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client.setCookieHandler(CookieHandlerFactory.getCookieHandler(this));
        initView();
    }

    private void initView() {
        code = (ImageView) findViewById(R.id.code);
        getCode = (Button) findViewById(R.id.get_code);
        login = (Button) findViewById(R.id.login);
        info = (Button) findViewById(R.id.info);
        input = (EditText) findViewById(R.id.input);
        tv = (TextView) findViewById(R.id.tv);
        getCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getCode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String randCode = input.getText().toString();
                try {
                    login(randCode,email,password);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    getFollowers();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    public static void getCode() throws IOException {
        Request request = new Request.Builder()
                .url("http://www.zhihu.com/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();

                Document parse = Jsoup.parse(result);
                System.out.println(parse + "");
                result = parse.select("input[type=hidden]").get(0).attr("value")
                        .trim();
                xsrf = result;
                System.out.println("_xsrf:" + result);
                String codeUrl = "http://www.zhihu.com/captcha.gif?r=";
                codeUrl += System.currentTimeMillis();
                System.out.println("codeUrl:" + codeUrl);
                Request getcode = new Request.Builder()
                        .url(codeUrl)
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                        .build();
                client.newCall(getcode).enqueue(new Callback() {
                    @Override
                    public void onFailure(Request request, IOException e) {

                    }

                    @Override
                    public void onResponse(Response response) throws IOException {
                        saveCode(response.body().byteStream());
                    }
                });



            }
        });

    }

    public static void saveCode(InputStream is) {
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        Message obtain = Message.obtain();
        obtain.obj = bitmap;
        obtain.what = 1;
        mHandler.sendMessage(obtain);
    }

    public static void login(String randCode, String email, String password) throws IOException {

        RequestBody formBody = new FormEncodingBuilder()
                .add("_xsrf", xsrf)
                .add("captcha", randCode)
                .add("email", email)
                .add("password", password)
                .add("remember_me", "true")
                .build();
        Request login = new Request.Builder()
                .url("http://www.zhihu.com/login/email")
                .post(formBody)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();



        client.newCall(login).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = decode(response.body().string());
                Message obtain = Message.obtain();
                obtain.obj = result;
                obtain.what = 2;
                mHandler.sendMessage(obtain);
            }
        });

    }

    public static void getFollowers() throws IOException {
        Request request = new Request.Builder()
                .url("http://www.zhihu.com/people/zord-vczh/followees")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {

            }

            @Override
            public void onResponse(Response response) throws IOException {
                String result = response.body().string();

                Document parse = Jsoup.parse(result);

                Elements select = parse.select("div.zm-profile-card");
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < select.size(); i++) {
                    Element element = select.get(i);
                    String name = element.select("h2").text();
                    System.out.println(name + "");
                    builder.append(name);
                    builder.append("\n");
                }

                Message obtain = Message.obtain();
                obtain.obj =  builder.toString();
                obtain.what = 2;
                mHandler.sendMessage(obtain);
            }
        });


    }

    public static String decode(String unicodeStr) {
        if (unicodeStr == null) {
            return null;
        }
        StringBuffer retBuf = new StringBuffer();
        int maxLoop = unicodeStr.length();
        for (int i = 0; i < maxLoop; i++) {
            if (unicodeStr.charAt(i) == '\\') {
                if ((i < maxLoop - 5)
                        && ((unicodeStr.charAt(i + 1) == 'u') || (unicodeStr
                        .charAt(i + 1) == 'U')))
                    try {
                        retBuf.append((char) Integer.parseInt(
                                unicodeStr.substring(i + 2, i + 6), 16));
                        i += 5;
                    } catch (NumberFormatException localNumberFormatException) {
                        retBuf.append(unicodeStr.charAt(i));
                    }
                else
                    retBuf.append(unicodeStr.charAt(i));
            } else {
                retBuf.append(unicodeStr.charAt(i));
            }
        }
        return retBuf.toString();
    }
}
