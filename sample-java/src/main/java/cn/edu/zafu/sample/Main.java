package cn.edu.zafu.sample;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Scanner;



/**
 * 模拟登录知乎，抓取数据，测试cookie功能是否正常
 */
public class Main {
    private static OkHttpClient client = new OkHttpClient();
    private static String xsrf;
    private static String email="*******";
    private static String password="********";
    public static void main(String[] args) {
        //client.setCookieHandler(CookieHandlerFactory.get);
        while(true){
            System.out.println("=================================\n1：获取验证码及防止跨站攻击码；\n2：登录；\n3：获取信息\n=================================\n");
            Scanner sc = new Scanner(System.in);
            int input=sc.nextInt();

            switch(input){
                case 1:
                    try {
                        getCode();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 2:
                    System.out.println("请输入验证码");
                    String randCode=sc.next().trim();
                    try {
                        login(randCode,email,password);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    try {
                        getFollowers();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }


    public static void getCode() throws IOException{
        Request request = new Request.Builder()
                .url("http://www.zhihu.com/")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();

        Response response = client.newCall(request).execute();
        String result = response.body().string();

        Document parse = Jsoup.parse(result);
        System.out.println(parse + "");
        result = parse.select("input[type=hidden]").get(0).attr("value")
                .trim();
        xsrf=result;
        System.out.println("_xsrf:" + result);
        String codeUrl = "http://www.zhihu.com/captcha.gif?r=";
        codeUrl += System.currentTimeMillis();
        System.out.println("codeUrl:" + codeUrl);
        Request getcode = new Request.Builder()
                .url(codeUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();

        Response code = client.newCall(getcode).execute();

        byte[] bytes = code.body().bytes();
        saveCode(bytes, "code.png");
    }


    public static void login(String randCode,String email,String password) throws IOException{
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
                .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();


        Response execute = client.newCall(login).execute();
        System.out.println(decode(execute.body().string()));

    }

    public static void getFollowers() throws IOException{
        Request request = new Request.Builder()
                .url("http://www.zhihu.com/people/zord-vczh/followees")
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/43.0.2357.134 Safari/537.36")
                .build();
        Response response = client.newCall(request).execute();

        String result=response.body().string();

        Document parse = Jsoup.parse(result);

        Elements select = parse.select("div.zm-profile-card");
        StringBuilder builder=new StringBuilder();
        for (int i=0;i<select.size();i++){
            Element element = select.get(i);
            String name=element.select("h2").text();
            System.out.println(name+"");
            builder.append(name);
            builder.append("\n");
        }
    }



    public static void saveCode(byte[] bfile, String fileName) {
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
