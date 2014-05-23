package com.mendeley.api.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.mendeley.api.R;
import com.mendeley.api.network.interfaces.AuthenticationInterface;
import com.mendeley.api.util.Utils;

public class AuthentictionManager {
	
	WebView webView;	
	String authorizationCode;	
	Handler refreshHandler;	
	Dialog loginDialog;
	
	final static String TOKENS_URL = "https://api-oauth2.mendeley.com/oauth/token";
	final static String OUATH2_URL = "https://api-oauth2.mendeley.com/oauth/authorize";
	final static String GRANT_TYPE_AUTH = "authorization_code";
	final static String GRANT_TYPE_REFRESH = "refresh_token";
	final static String REDIRECT_URI = "http://localhost/auth_return";
	final static String SCOPE = "all";
	final static String RESPONSE_TYPE = "code";
	
	Context context;
	CredentialsManager credentialsManager;	
	AuthenticationInterface authInterface;
	
	protected AuthentictionManager (Context context, AuthenticationInterface authInterface) {
		this.context = context;
		this.authInterface = authInterface;
		credentialsManager = new CredentialsManager(context);
	}
	
	protected boolean hasCredentials() {
		return credentialsManager.hasCredentials();
	}
	
	protected void clearCredentials() {
		credentialsManager.clearCredentials();
	}
	
	public void authenticate() {
		if (hasCredentials()) {
			createRefreshHandler(true);
		} else {
			createDialog();		
			this.webView.setWebViewClient(new MendeleyWebViewClient());
			this.webView.loadUrl(getOauth2URL());
	
		    loginDialog.show();
		}
	}
		
	private void createDialog() {
		loginDialog = new Dialog(context);
		loginDialog.requestWindowFeature(Window.FEATURE_LEFT_ICON);
		loginDialog.setContentView(R.layout.dialog_layout);
		loginDialog.setTitle("Log in Mendeley");
		loginDialog.setCancelable(true);

		loginDialog.setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.ic_launcher);

    	webView = (WebView) loginDialog.findViewById(R.id.dialogWebView);

    	((Button)loginDialog.findViewById(R.id.cancelButton)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				loginDialog.hide();
			}
		});
    } 
	  
	private void createRefreshHandler(final boolean notify) {

		Runnable runnableNotify = new Runnable() {	
			@Override
			public void run() {
				refreshToken(notify);
			}
		};
		
		long delayMillis = notify ? 0 : (long)((NetworkProvider.expiresIn * 0.9) * 1000);
		refreshHandler = new Handler();
		
		refreshHandler.postDelayed(runnableNotify, delayMillis);
	}
	
	
	public void refreshToken(boolean notify) {
		new RefreshTokenTask().execute(notify);
	}
		
	private String getOauth2URL() {
		
		StringBuffer urlString = new StringBuffer(OUATH2_URL);
		
		urlString
		.append("?").append("grant_type=").append(GRANT_TYPE_AUTH)
		.append("&").append("redirect_uri=").append(REDIRECT_URI)
		.append("&").append("scope=").append(SCOPE)
		.append("&").append("response_type=").append(RESPONSE_TYPE)
		.append("&").append("client_id=").append(credentialsManager.getClientID());
		
		return urlString.toString();
	}
		
    private class MendeleyWebViewClient extends WebViewClient {
    	
    	@Override
    	public boolean shouldOverrideUrlLoading (WebView view, String url) {

    		new AuthenticateTask().execute(url);
			return true;    		
    	}
    }
        
	private void getTokenDetails(String tokenString) throws JSONException {
		
		JSONObject tokenObject = new JSONObject(tokenString);

		String accessToken = tokenObject.getString("access_token");
		String refreshToken = tokenObject.getString("refresh_token");	
		String tokenType = tokenObject.getString("token_type");
		int expiresIn = tokenObject.getInt("expires_in");

		credentialsManager.setTokens(accessToken, refreshToken, tokenType, expiresIn);	
	}
	    
    class AuthenticateTask extends AsyncTask<String, Void, String> {

    	protected String getJSONTokenString(String authorizationCode) throws ClientProtocolException, IOException {
    		HttpResponse response = doPost(TOKENS_URL, GRANT_TYPE_AUTH, authorizationCode);
    		String data = getJsonString(response.getEntity().getContent());
	           
    		return data;
		}
    	
    	protected String getAuthorizationCode(String authReturnUrl) {
    		
    		String AuthorizationCode = null;
			int index = authReturnUrl.indexOf("code=");	       			
	        if (index != -1) {
	        	index += 5;
	        	AuthorizationCode = authReturnUrl.substring(index);
	        }
			
			return AuthorizationCode;
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			
			String result = null;
			
			authorizationCode = getAuthorizationCode(params[0]);			
			if (authorizationCode != null) {
				try {
					String jsonTokenString = getJSONTokenString(authorizationCode);
					getTokenDetails(jsonTokenString);
					result = "ok";
					
				} catch (IOException e) {
					Log.e("", "", e);

				} catch (JSONException e) {
					Log.e("", "", e);
				}
			}
			
			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				authInterface.onAuthenticationFail();
			}
			else {
				loginDialog.hide();
				authInterface.onAuthenticated();
				createRefreshHandler(false);
			}
		}    
    }
    
    
    class RefreshTokenTask extends AsyncTask<Boolean, Void, String> {

    	boolean notify = false;
    	
    	protected String getJSONTokenString() throws ClientProtocolException, IOException {
	           HttpResponse response = doPost(TOKENS_URL, GRANT_TYPE_REFRESH);
	           String data = getJsonString(response.getEntity().getContent());
	           return data;
		}

		@Override
		protected String doInBackground(Boolean... params) {

			if (params.length > 0) {
				notify = params[0];
			}

			String result = null;
				try {
					String jsonTokenString = getJSONTokenString();
					getTokenDetails(jsonTokenString);
					result = "ok";
				} 
				catch (JSONException e) {
					Log.e("", "", e);
					return result;
				} catch (ClientProtocolException e) {
					Log.e("", "", e);
					return result;
				} catch (IOException e) {
					Log.e("", "", e);
					return result;
				}

			return result;
		}
		
		@Override
		protected void onPostExecute(String result) {			
			createRefreshHandler(false);
			if (notify) {
				authInterface.onAuthenticated();
			}
		}
    }	
    
	String getJsonString(InputStream stream) throws IOException {
		
		StringBuffer data = new StringBuffer();
		InputStreamReader isReader = null;
		BufferedReader br = null;
		
		try {
			
			isReader = new InputStreamReader(stream); 
            br = new BufferedReader(isReader);
            String brl = ""; 
            while ((brl = br.readLine()) != null) {
        	    data.append(brl);
            }
            
		} finally {
			stream.close();
            isReader.close();
            br.close();
		}
		
		return data.toString();
	}
	
	HttpResponse doPost(String url, String grantType, String authorizationCode) throws ClientProtocolException, IOException {
		
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
        nameValuePairs.add(new BasicNameValuePair("grant_type", grantType));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri", "http://localhost/auth_return"));
        nameValuePairs.add(new BasicNameValuePair("code", authorizationCode));
        nameValuePairs.add(new BasicNameValuePair("client_id", credentialsManager.getClientID()));
        nameValuePairs.add(new BasicNameValuePair("client_secret", credentialsManager.getClientSecret()));
        
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);
		  
		return response;  
	}
	
	HttpResponse doPost(String url, String grantType) throws ClientProtocolException, IOException {
		
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(url);
        
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(5);
        nameValuePairs.add(new BasicNameValuePair("grant_type", grantType));
        nameValuePairs.add(new BasicNameValuePair("redirect_uri", "http://localhost/auth_return"));
        nameValuePairs.add(new BasicNameValuePair("code", authorizationCode));
        nameValuePairs.add(new BasicNameValuePair("client_id", credentialsManager.getClientID()));
        nameValuePairs.add(new BasicNameValuePair("client_secret", credentialsManager.getClientSecret()));
        nameValuePairs.add(new BasicNameValuePair("refresh_token", NetworkProvider.refreshToken));
        
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        HttpResponse response = httpclient.execute(httppost);
		  
		return response;  
	}
}
