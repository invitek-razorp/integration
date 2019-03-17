package com.invitekaro;

import android.util.Base64;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import javax.net.ssl.HttpsURLConnection;

public class Voucher {

    private boolean isInitialized;
    private String merchantId, apiKey, basicAuth;
    private static Voucher voucher = new Voucher();

    public static Voucher getInstance(){
        return voucher;
    }

    public void initialize(String merchantId, String apiKey){
        this.merchantId = merchantId;
        this.apiKey = apiKey;
        isInitialized = merchantId == null || merchantId.isEmpty() || apiKey == null || apiKey.isEmpty();
        basicAuth = "Basic " + Base64.encodeToString((this.merchantId + ":" + this.apiKey).getBytes(), Base64.NO_WRAP);
    }

    public boolean isInitialized(){
        return isInitialized;
    }


    public void login(final String teamIdOrEmail, final String password){

        OutputStream out = null;
        HttpsURLConnection urlConnection = null;
        BufferedReader responseStreamReader = null;
        try{
            JSONObject json = new JSONObject();
            json.put("username", teamIdOrEmail);
            json.put("password", password);

            URL url = new URL("https://api.vauchar.com/teams/login_validate");
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty ("Authorization", basicAuth);
            urlConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");

            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(json.toString().getBytes(Charset.forName("UTF-8")));
            out.flush();
            Log.e("VQHLog", "Output Flushed : " + json.toString());
            int responseCode = urlConnection.getResponseCode();
            Log.e("VQHLog", "responseCode : " + responseCode);
            if (responseCode >= 200 && responseCode < 300){
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                    Log.e("VQHLog", "response loop : " + stringBuilder.toString());
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onResult(LOGIN, responseCode, responseString);
            }else{
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getErrorStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                    Log.e("VQHLog", "response loop : " + stringBuilder.toString());
                }
                String responseString = stringBuilder.toString();
                if(mListener != null) mListener.onError(LOGIN, responseCode, responseString);
                Log.e("VQHLog", "errorResponseString : " + responseString);
            }

        } catch (MalformedURLException e) {
            if(mListener != null)mListener.onException(LOGIN, e, "MalformedURLException caught : " + e.getMessage());
        } catch (IOException e) {
            if(mListener != null)mListener.onException(LOGIN, e, "IOException caught : " + e.getMessage());
        } catch (JSONException e) {
            if(mListener != null) mListener.onException(LOGIN, e, "JSONException caught : " + e.getMessage());
        }finally {
            try{
                if(responseStreamReader != null) responseStreamReader.close();
                if(out != null)out.close();
            } catch (IOException e) {}
            if(urlConnection != null) urlConnection.disconnect();
        }
    }


    //aka createTeam
    public void signUp(final String name, final String email, final String password){

        OutputStream out = null;
        HttpsURLConnection urlConnection = null;
        BufferedReader responseStreamReader = null;

        try {
            JSONObject json = new JSONObject();
            json.put("name", name);
            json.put("email", email);
            json.put("password", password);

            JSONObject permissionJson = new JSONObject();
            permissionJson.put("view_vouchers", 1);
            permissionJson.put("view_redemptions", 1);
            json.put("permissions", permissionJson);

            URL url = new URL("https://api.vauchar.com/teams");
            urlConnection = (HttpsURLConnection) url.openConnection();

            urlConnection.setRequestProperty ("Authorization", basicAuth);
            urlConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");
            //urlConnection.setRequestProperty("data", json.toString());

            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);

            out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(json.toString().getBytes(Charset.forName("UTF-8")));
            out.flush();
            Log.e("VQHLog", "Output Flushed : " + json.toString());
            int responseCode = urlConnection.getResponseCode();
            Log.e("VQHLog", "responseCode : " + responseCode);
            if (responseCode >= 200 && responseCode < 300){
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onResult(SIGN_UP, responseCode, responseString);
            }else{
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getErrorStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onError(SIGN_UP, responseCode, responseString);
            }

        } catch (MalformedURLException e) {
            if(mListener != null)mListener.onException(SIGN_UP, e, "MalformedURLException caught : " + e.getMessage());
        } catch (IOException e) {
            if(mListener != null)mListener.onException(SIGN_UP, e, "IOException caught : " + e.getMessage());
        } catch (JSONException e) {
            if(mListener != null) mListener.onException(SIGN_UP, e, "JSONException caught : " + e.getMessage());
        } finally {
            try{
                if(responseStreamReader != null) responseStreamReader.close();
                if(out != null) out.close();
            } catch (IOException e) {}
            if(urlConnection != null) urlConnection.disconnect();
        }

    }


    public void createVoucher(int numVoucher, int voucherVal, String valUnit, String currencyCode, String tag){

        OutputStream out = null;
        HttpsURLConnection urlConnection = null;
        BufferedReader responseStreamReader = null;

        try{
            JSONObject json = new JSONObject();
            json.put("count", numVoucher);
            json.put("value", voucherVal);
            json.put("value_unit", valUnit);
            json.put("currency", currencyCode);
            json.put("tag", tag);

            URL url = new URL("https://api.vauchar.com/vouchers");
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty ("Authorization", basicAuth);
            urlConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setChunkedStreamingMode(0);

            out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(json.toString().getBytes(Charset.forName("UTF-8")));
            out.flush();
            Log.e("VQHLog", "Output Flushed : " + json.toString());
            int responseCode = urlConnection.getResponseCode();
            Log.e("VQHLog", "responseCode : " + responseCode);
            if (responseCode >= 200 && responseCode < 300){
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onResult(CREATE_VOUCHER, responseCode, responseString);
            }else{
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getErrorStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                    Log.e("VQHLog", "response loop : " + stringBuilder.toString());
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onError(CREATE_VOUCHER, responseCode, responseString);
            }

        } catch (MalformedURLException e) {
            if(mListener != null)mListener.onException(CREATE_VOUCHER, e, "MalformedURLException caught : " + e.getMessage());
        } catch (IOException e) {
            if(mListener != null)mListener.onException(CREATE_VOUCHER, e, "IOException caught : " + e.getMessage());
        } catch (JSONException e) {
            if(mListener != null) mListener.onException(CREATE_VOUCHER, e, "JSONException caught : " + e.getMessage());
        }finally {
            try{
                if(responseStreamReader != null) responseStreamReader.close();
                if(out != null)out.close();
            } catch (IOException e) {}
            if(urlConnection != null) urlConnection.disconnect();
        }
    }


    public void redeemVoucher(String voucherId, String userId){
        OutputStream out = null;
        HttpsURLConnection urlConnection = null;
        BufferedReader responseStreamReader = null;

        try{
            JSONObject json = new JSONObject();
            json.put("user_id", userId);

            URL url = new URL("https://api.vauchar.com/vouchers/" + voucherId + "/redemptions");
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty ("Authorization", basicAuth);
            urlConnection.setRequestProperty("Content-Type","application/json; charset=UTF-8");

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod("POST");
            urlConnection.setChunkedStreamingMode(0);

            out = new BufferedOutputStream(urlConnection.getOutputStream());
            out.write(json.toString().getBytes(Charset.forName("UTF-8")));
            out.flush();
            Log.e("VQHLog", "Output Flushed : " + json.toString());
            int responseCode = urlConnection.getResponseCode();
            Log.e("VQHLog", "responseCode : " + responseCode);
            if (responseCode >= 200 && responseCode < 300){
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                    Log.e("VQHLog", "response loop : " + stringBuilder.toString());
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onResult(REDEEM_VOUCHER, responseCode, responseString);
            }else{
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getErrorStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                    Log.e("VQHLog", "response loop : " + stringBuilder.toString());
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onError(REDEEM_VOUCHER, responseCode, responseString);
            }

        } catch (MalformedURLException e) {
            if(mListener != null)mListener.onException(REDEEM_VOUCHER, e, "MalformedURLException caught : " + e.getMessage());
        } catch (IOException e) {
            if(mListener != null)mListener.onException(REDEEM_VOUCHER, e, "IOException caught : " + e.getMessage());
        } catch (JSONException e) {
            if(mListener != null) mListener.onException(REDEEM_VOUCHER, e, "JSONException caught : " + e.getMessage());
        }finally {
            try{
                if(responseStreamReader != null) responseStreamReader.close();
                if(out != null)out.close();
            } catch (IOException e) {}
            if(urlConnection != null) urlConnection.disconnect();
        }
    }


    public void checkValidity(String voucherCode){

        HttpsURLConnection urlConnection = null;
        BufferedReader responseStreamReader = null;

        try{

            URL url = new URL("https://api.vauchar.com/vouchers/validate?voucher_code=" + voucherCode);
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty ("Authorization", basicAuth);

            int responseCode = urlConnection.getResponseCode();
            Log.e("VQHLog", "responseCode : " + responseCode);
            if (responseCode >= 200 && responseCode < 300){
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onResult(CHECK_VOUCHER_VALIDITY, responseCode, responseString);
            }else{
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getErrorStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onError(CHECK_VOUCHER_VALIDITY, responseCode, responseString);
            }

        } catch (MalformedURLException e) {
            if(mListener != null)mListener.onException(CHECK_VOUCHER_VALIDITY, e, "MalformedURLException caught : " + e.getMessage());
        } catch (IOException e) {
            if(mListener != null)mListener.onException(CHECK_VOUCHER_VALIDITY, e, "IOException caught : " + e.getMessage());
        }finally {
            try{
                if(responseStreamReader != null) responseStreamReader.close();
            } catch (IOException e) {}
            if(urlConnection != null) urlConnection.disconnect();
        }
    }


    public void verify(){
        OutputStream out = null;
        HttpsURLConnection urlConnection = null;
        BufferedReader responseStreamReader = null;

        try{
            URL url = new URL("https://api.vauchar.com/verifycredentials");
            urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty ("Authorization", basicAuth);

            int responseCode = urlConnection.getResponseCode();
            if (responseCode == 200){
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getInputStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onResult(VERIFY_API_CREDENTIAL, responseCode, responseString);
            }else{
                Log.e("VQHLog", "responseCode : " + responseCode);
                responseStreamReader =
                        new BufferedReader(new InputStreamReader(new BufferedInputStream(urlConnection.getErrorStream())));
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = responseStreamReader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                String responseString = stringBuilder.toString();
                if(mListener != null)mListener.onError(VERIFY_API_CREDENTIAL, responseCode, responseString);
            }

        } catch (MalformedURLException e) {
            if(mListener != null)mListener.onException(VERIFY_API_CREDENTIAL, e, "MalformedURLException caught : " + e.getMessage());
        } catch (IOException e) {
            if(mListener != null)mListener.onException(VERIFY_API_CREDENTIAL, e, "IOException caught : " + e.getMessage());
        }finally {
            try{
                if(responseStreamReader != null) responseStreamReader.close();
                if(out != null)out.close();
            } catch (IOException e) {}
            if(urlConnection != null) urlConnection.disconnect();
        }
    }


    private OnVoucherResultListener mListener;

    public void setOnVoucherResultListener(OnVoucherResultListener listener){
        mListener = listener;
    }

    public interface OnVoucherResultListener {
        void onResult(int requestType, int responseCode, String response);
        void onError(int requestType, int responseCode, String errorMessage);
        void onException(int requestType, Exception e, String message);
    }


    public static final int VERIFY_API_CREDENTIAL  = 2;
    public static final int CHECK_VOUCHER_VALIDITY = 3;
    public static final int REDEEM_VOUCHER         = 5;
    public static final int LOGIN                  = 7;
    public static final int CREATE_VOUCHER         = 11;
    public static final int SIGN_UP                = 13;

    public static final String VOUCHERVALID        = "1000";
}
