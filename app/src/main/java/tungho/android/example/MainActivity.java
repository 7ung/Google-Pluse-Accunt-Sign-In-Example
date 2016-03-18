package tungho.android.example;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusShare;

import java.util.Arrays;

public class MainActivity extends Activity implements
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks,
        View.OnClickListener
{
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int RC_SIGN_IN = 0;
    private static final int RC_SHARE = 1;
    private static final int RC_SHARE_INTERACTION = 2;
    private static final int RC_SHARE_MEDIA = 3;
    private static final int RC_PICK_PHOTO = 4;

    private static final int SignedIn = 1;
    private static final int SignedOut = 2;
    private int mStatus;

    // GoogleApiClient khởi động các dịch vụ cần kết nối google service.
    // Khởi tạo ở onCreate
    // Gọi connect ở onStart.
    //      Nếu kết nối thành công thì kích hoạt onConnected.
    //      Nếu đang kết nối mà bị gián đoạn thì kích hoạt onConnectpended. Trong đó ta nên gọi lại connect
    //      Nếu kết nối không thành công thì kích hoạt onConnectionFailed
    // Gọi disconnect ở onStop
    GoogleApiClient mGoogleApiClient;
    GoogleSignInAccount mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mStatus = SignedOut;
        updateUI();

        Scope scope_login = new Scope(Scopes.PLUS_LOGIN);
        GoogleSignInOptions ggSignInOption = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(scope_login)
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, ggSignInOption)
                /*.addApi(Plus.API)*/
                .build();


        SignInButton button = (SignInButton) findViewById(R.id.sign_in_button);
        button.setOnClickListener(this);
        button.setScopes(ggSignInOption.getScopeArray());

        findViewById(R.id.sign_out_btn).setOnClickListener(this);
        findViewById(R.id.share_button).setOnClickListener(this);
        findViewById(R.id.share_button_interactive).setOnClickListener(this);
        findViewById(R.id.share_media).setOnClickListener(this);
    }

    @Override
    protected void onStart(){
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if (mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }

    private void updateUI() {
        switch (mStatus){
            case SignedIn:
                findViewById(R.id.sign_in_button).setVisibility(View.GONE);
                findViewById(R.id.sign_out_btn).setVisibility(View.VISIBLE);
                findViewById(R.id.share_button).setVisibility(View.VISIBLE);
                findViewById(R.id.share_button_interactive).setVisibility(View.VISIBLE);
                findViewById(R.id.share_media).setVisibility(View.VISIBLE);
                break;
            case SignedOut:
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_btn).setVisibility(View.GONE);
                findViewById(R.id.share_button).setVisibility(View.GONE);
                findViewById(R.id.share_button_interactive).setVisibility(View.GONE);
                findViewById(R.id.share_media).setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN){
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else if (requestCode == RC_SHARE){

        }
        else if (requestCode == RC_PICK_PHOTO){
            //  pick photo
            if (resultCode == RESULT_OK){
                Uri selectedimg = data.getData();
                ContentResolver resolver = this.getContentResolver();
                String mime = resolver.getType(selectedimg);    // try to detect you select image or video or something like that

                PlusShare.Builder builder = new PlusShare.Builder(this)
                        .setStream(selectedimg)
                        .setType(mime)
                        //.setContentUrl("[photoURL or something like that]") - // Please don't use setContentUrl with setStream. Use text Url in set text instead.
                        .setText("I test my share photo function");
                startActivityForResult(builder.getIntent(), RC_SHARE_MEDIA);

            }
        }
        else if (requestCode == RC_SHARE_MEDIA){
            Toast.makeText(this, "Photo uploaded successed", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()){
            GoogleSignInAccount account = result.getSignInAccount();
            Toast.makeText(getApplicationContext(),
                    "Sign in successed. User name is: " + account.getDisplayName(),
                    Toast.LENGTH_LONG)
                    .show();
            mStatus = SignedIn;
            updateUI();
        }else {
            // do something.
            mStatus = SignedOut;
            updateUI();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Không kết nối được. Quăng lỗi ở đây.
        //
        Util.CompactLog(Util.LOG_DEBUG, TAG,
                "Connection Failed. Error message is " + connectionResult.getErrorMessage());
        if (connectionResult.getErrorCode() == ConnectionResult.INTERNAL_ERROR){
            // an internal error occurred. Retrying should resolve problem.
            mGoogleApiClient.connect();
        }
        else if (connectionResult.hasResolution()){
            // Nếu gg tự động có giải pháp thì ta khởi động màn hình khắc phục.
            //
            try {
                connectionResult.startResolutionForResult(this, 2);
            }
            catch(IntentSender.SendIntentException ex){
                // handle error here
            }
        }

    }

    @Override
    public void onConnectionSuspended(int cause){
        // Bị gián đoạn kết nối service do bị mất kết nối internet hoặc ứng dụng service bị kill.
        // Nên disable UI, và re-connect.
        Util.CompactLog(Util.LOG_DEBUG,TAG, "Connection Suspended");
        if (cause == CAUSE_NETWORK_LOST){

            Toast.makeText(this, "Warning: Connection is slow.", Toast.LENGTH_LONG)
                    .show();
        }
        Util.CompactLog(Util.LOG_DEBUG,TAG, "Try re-connect");
        //mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // đăng nhập thành công.
        getProfileInfo();

    }

    private void getProfileInfo() {
        // do some stuff
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sign_in_button:
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            case R.id.share_button:
                Intent shareIntent = new PlusShare.Builder(this)
                        .setType("text/plain")
                        .setText("Test share function")
                        .setContentUrl(Uri.parse("https://developers.google.com/+/"))
                        .getIntent();
                startActivityForResult(shareIntent, RC_SHARE);
                break;
            case R.id.share_media:
                Intent photo_picker = new Intent(Intent.ACTION_PICK);
                photo_picker.setType("video/*, image/*");
                startActivityForResult(photo_picker, RC_SHARE_MEDIA);
                break;
            case R.id.share_button_interactive:
                // just test. not success. May be cause of the url is wrong.
                PlusShare.Builder builder = new PlusShare.Builder(this);
                builder.addCallToAction(
                        "CREATE_ITEM", /**Call-action label. More label here: https://developers.google.com/+/features/call-to-action-labels*/
                        Uri.parse("http://plus.google.com/pages/create"),
                        "/page/create" );
                builder.setContentUrl(Uri.parse("https://plus.google.com/pages/")); // for desktop use
                builder.setContentDeepLinkId("/page/", null, null, null);           // for mobile use

                builder.setText("Create your Google+ Page too!");
                startActivityForResult(builder.getIntent(),RC_SHARE_INTERACTION);

                break;
            case R.id.sign_out_btn:

                Auth.GoogleSignInApi.signOut(mGoogleApiClient)
                    .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()){
                            Toast.makeText(getApplicationContext(),
                                    "Sign out successed",Toast.LENGTH_LONG)
                                    .show();
                            mStatus = SignedOut;
                            updateUI();
                        }
                        else {
                            Toast.makeText(getApplicationContext(),
                                    "Sign out failed",Toast.LENGTH_LONG)
                                    .show();
                            mStatus = SignedIn;
                            updateUI();
                        }
                    }
                });

        }
    }


}
