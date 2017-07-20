package fr.julienj.otri;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.transition.Visibility;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class MainTri extends AppCompatActivity implements View.OnClickListener  {

    private Button scanButton;
    private Spinner listItemSortingView;
    private EditText seekProductView;

    //qr code scanner object
    private IntentIntegrator qrScan;

    public double theContainerlatitude;
    public double theContainerlongitude;

    private PointOfTri theNearestPoint;

    private ServiceGPS servGPS;
    private WebView webview;

    private android.support.v4.app.FragmentManager fragmentManager;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;
    private Fragment mapsGoogleFrag;


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:

                    scanButton.setVisibility(View.VISIBLE);

                    if( webview!=null)
                        webview.setVisibility(View.VISIBLE);

                    if(mapsGoogleFrag!=null) {
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();
                        //fragmentTransaction.commit();
                        mapsGoogleFrag=null;
                    }
                    return true;
                case R.id.navigation_mapsContainer:

                    scanButton.setVisibility(View.GONE);
                    listItemSortingView.setVisibility(View.GONE);
                    seekProductView.setVisibility(View.GONE);

                    if( webview!=null)
                        webview.setVisibility(View.GONE);

                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    mapsGoogleFrag = new MapsConteneur();
                    fragmentTransaction.replace(R.id.fragment_container,mapsGoogleFrag);
                    fragmentTransaction.commit();

                    return true;
                case R.id.navigation_notifications:

                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tri);


        scanButton=(Button) findViewById(R.id.btnscancode);

        //attaching onclick listener
        scanButton.setOnClickListener(this);
        scanButton.setVisibility(View.VISIBLE);

        listItemSortingView=(Spinner) findViewById(R.id.listItemSorting);
        seekProductView=(EditText)  findViewById(R.id.seekProduct);

        listItemSortingView.setVisibility(View.VISIBLE);
        seekProductView.setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.arrayKindOfSorting, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listItemSortingView.setAdapter(adapter);


        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setVisibility(View.GONE);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        init();
    }

    private void init()
    {

        //intializing scan object
        qrScan = new IntentIntegrator(this);

        mapsGoogleFrag=null;


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            //User permission needed
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    1);

        }


        servGPS = new ServiceGPS(MainTri.this);

        // check if GPS enabled
        if(servGPS.canGetLocation()){

            ContainerData.getInstance().myLatitude = servGPS.getLatitude();
            ContainerData.getInstance().myLongitude = servGPS.getLongitude();

            // \n is for new line
            Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + ContainerData.getInstance().myLatitude + "\nLong: " + ContainerData.getInstance().myLongitude, Toast.LENGTH_LONG).show();
        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            servGPS.showSettingsAlert();
        }
    }

    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            //if qrcode has nothing in it
            if (result.getContents() == null) {
                Toast.makeText(this, "Result Not Found", Toast.LENGTH_LONG).show();
            } else {
                //if qr contains data
                try {
                    //converting the data to json
                    JSONObject obj = new JSONObject(result.getContents());
                    //setting values to textviews

                } catch (JSONException e) {
                    e.printStackTrace();
                    //if control comes here
                    //that means the encoded format not matches
                    //in this case you can display whatever data is available on the qrcode
                    //to a toast
                    ContainerData.getInstance().codeScanned=result.getContents().toString();

                    webview.setVisibility(View.VISIBLE);




                    try {
                        getInformationFormProduct();
                        webview.loadUrl(ContainerData.getInstance().urlProductInfo+ContainerData.getInstance().codeScanned);
                        //getNearConteneurFromMyPosition(myLatitude,myLongitude);

                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    @Override
    public void onClick(View view) {
        //initiating the qr code scan
        qrScan.initiateScan();
    }



    public void getInformationFormProduct() throws JSONException {

        if(ContainerData.getInstance().codeScanned!="") {
            HttpUtils.get("https://fr.openfoodfacts.org/api/v0/produit/"+ContainerData.getInstance().codeScanned+".json", null, new JsonHttpResponseHandler(){
                //HttpUtils.get("3222476439574.json", null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try{
                        String productExist= response.getString("status_verbose");

                        if( productExist.equalsIgnoreCase("product found")) {
                            JSONObject product = response.getJSONObject("product");

                            if (product.has("packaging")) {
                                final String infoPackaging = product.getString("packaging");
                                ContainerData.getInstance().listePackaging = infoPackaging.split(",");
                                System.out.println(ContainerData.getInstance().listePackaging);
                                showToast("info "+infoPackaging);
                            }
                            else
                            {
                                showToast("L'emballage n'est pas renseigné");
                            }


                        }
                        else
                        {
                            showToast("Produit non disponible");
                        }


                    }catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

                }
            });
        }
    }

    public void showToast(final String toast)
    {
        runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(MainTri.this, toast, Toast.LENGTH_SHORT).show();



            }
        });
    }

}
