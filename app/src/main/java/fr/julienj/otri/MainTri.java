/*
A faire
- Ajouter un conteneur : prendre les différents infos disponibles et renseigner une URL
- Ajouter l'interaction avec OuRecycler
- Ajouter l'interaction Dechetteries.fr
- Nettoyer le code
- Ameliorer l'IHM
- Faire des Test

Pour OuRecycler et dechetterie.fr ==> BDD sqlite ??

 */

//Geocodage
//http://maps.googleapis.com/maps/api/geocode/json?latlng=45.3,5.03&sensor=true
//http://maps.googleapis.com/maps/api/geocode/json?address=grenoble


package fr.julienj.otri;

import android.Manifest;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import cz.msebera.android.httpclient.Header;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

public class MainTri extends AppCompatActivity implements View.OnClickListener  {

    private Button scanButton;
    private MaterialBetterSpinner  listItemSortingView;
    private MaterialBetterSpinner listItemSortingViewCustomDialog;
    private EditText seekProductView;
    private Button seekPoductConteneur;
    private TableRow tableRowBtnScan;
    private BottomNavigationView navigation;
    private Button seekKindConteneurFromListChoice;
    private Button hideShowWebViewBtn;

    //qr code scanner object
    private IntentIntegrator qrScan;
    private Context contextApp;

    public double theContainerlatitude;
    public double theContainerlongitude;

    private PointOfTri theNearestPoint;

    private ServiceGPS servGPS;
    private WebView webview;

    private android.support.v4.app.FragmentManager fragmentManager;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;
    private Fragment mapsGoogleFrag;
    private Fragment contirbutionFrag;

    private String keyworkSeek;
    private String packingsSeek;

    private String uriRestIndicationSortingFromKeywork;

    private boolean anUrlPageIsLoaded;

    private ProgressDialog progressDialog;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_scan:

                    scanButton.setVisibility(View.VISIBLE);
                    seekPoductConteneur.setVisibility(View.VISIBLE);
                    seekProductView.setVisibility(View.VISIBLE);
                    listItemSortingView.setVisibility(View.VISIBLE);
                    seekKindConteneurFromListChoice.setVisibility(View.VISIBLE);
                    tableRowBtnScan.setBackgroundResource(R.drawable.barcode);

                    //Si une page a été chargé alors on réaffiche le bouton
                    if(anUrlPageIsLoaded) {
                        hideShowWebViewBtn.setVisibility(View.VISIBLE);
                        webview.setVisibility(View.VISIBLE);
                    }

                    /*if(mapsGoogleFrag!=null) {
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();

                        mapsGoogleFrag=null;
                    }

                    if(contirbutionFrag!=null) {
                        fragmentManager = getSupportFragmentManager();
                        fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();

                        contirbutionFrag=null;
                    }*/

                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.remove(getSupportFragmentManager().findFragmentById(R.id.fragment_container)).commit();



                    return true;
                case R.id.navigation_mapsContainer:

                    seekPoductConteneur.setVisibility(View.GONE);
                    scanButton.setVisibility(View.GONE);
                    listItemSortingView.setVisibility(View.GONE);
                    seekProductView.setVisibility(View.GONE);
                    seekKindConteneurFromListChoice.setVisibility(View.GONE);
                    hideShowWebViewBtn.setVisibility(View.GONE);

                    tableRowBtnScan.setBackgroundResource(0);

                    if( webview!=null)
                        webview.setVisibility(View.GONE);

                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    mapsGoogleFrag = new MapsConteneur();
                    fragmentTransaction.replace(R.id.fragment_container,mapsGoogleFrag);
                    fragmentTransaction.commit();

                    return true;
                case R.id.navigation_notifications:

                    seekPoductConteneur.setVisibility(View.GONE);
                    scanButton.setVisibility(View.GONE);
                    listItemSortingView.setVisibility(View.GONE);
                    seekProductView.setVisibility(View.GONE);
                    seekKindConteneurFromListChoice.setVisibility(View.GONE);
                    hideShowWebViewBtn.setVisibility(View.GONE);

                    tableRowBtnScan.setBackgroundResource(0);

                    if( webview!=null)
                        webview.setVisibility(View.GONE);


                    fragmentManager = getSupportFragmentManager();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    contirbutionFrag = new ContributionFragment();
                    fragmentTransaction.replace(R.id.fragment_container,contirbutionFrag);
                    fragmentTransaction.commit();

                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contextApp=this.getApplicationContext();

        setContentView(R.layout.activity_main_tri);

        tableRowBtnScan=(TableRow) findViewById(R.id.tableRowBtnScan);


        seekPoductConteneur=(Button) findViewById(R.id.btnseekConteneur);
        seekKindConteneurFromListChoice=(Button) findViewById(R.id.btnseekFromListChoice);
        scanButton=(Button) findViewById(R.id.btnscancode);
        hideShowWebViewBtn=(Button) findViewById(R.id.btnhideWebView);

        //attaching onclick listener
        scanButton.setOnClickListener(this);
        scanButton.setVisibility(View.VISIBLE);

        seekPoductConteneur.setOnClickListener(this);
        seekKindConteneurFromListChoice.setOnClickListener(this);
        hideShowWebViewBtn.setOnClickListener(this);

        listItemSortingView=(MaterialBetterSpinner ) findViewById(R.id.listItemSorting);
        seekProductView=(EditText)  findViewById(R.id.seekProduct);

        listItemSortingView.setVisibility(View.VISIBLE);
        seekProductView.setVisibility(View.VISIBLE);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.arrayKindOfSorting, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        listItemSortingView.setAdapter(adapter);


        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setVisibility(View.GONE);

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                ContainerData.getInstance().isInternetAlive=((getNetworkType(contextApp)!=null)?true:false);

            }
        }, 1000, 30000);

        init();
    }

    private void init()
    {

        //intializing scan object
        qrScan = new IntentIntegrator(this);

        mapsGoogleFrag=null;
        contirbutionFrag=null;


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
           // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + ContainerData.getInstance().myLatitude + "\nLong: " + ContainerData.getInstance().myLongitude, Toast.LENGTH_LONG).show();
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

                        if( ContainerData.getInstance().isInternetAlive)
                        {
                            getInformationFormProduct();
                            anUrlPageIsLoaded=true;
                            hideShowWebViewBtn.setVisibility(View.VISIBLE);
                            webview.loadUrl(ContainerData.getInstance().urlProductInfo + ContainerData.getInstance().codeScanned);
                        }
                        else
                            showToast("Pas de connexion Internet Disponible");

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
        switch(view.getId()) {
            case R.id.btnscancode:
                hideShowWebViewBtn.setVisibility(View.GONE);
                anUrlPageIsLoaded=false;
                qrScan.initiateScan();
                break;
            case R.id.btnseekConteneur:
                hideShowWebViewBtn.setVisibility(View.GONE);
                anUrlPageIsLoaded=false;
                if(ContainerData.getInstance().isInternetAlive)
                {
                    progressDialog = ProgressDialog.show(MainTri.this, "",
                            "Chargement", true);
                    getIndicationForProduct();
                }
                else
                    showToast("Pas de connexion Internet Disponible");
                break;
            case R.id.btnseekFromListChoice:
                hideShowWebViewBtn.setVisibility(View.GONE);
                anUrlPageIsLoaded=false;
                showToast("Fonctionnalite non disponible");
                break;
            case R.id.btnhideWebView:
                if( webview.getVisibility()==View.VISIBLE)
                    webview.setVisibility(View.GONE);
                else if(webview.getVisibility()==View.GONE)
                    webview.setVisibility(View.VISIBLE);
            break;

        }

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

    public void getIndicationForProduct()
    {
        uriRestIndicationSortingFromKeywork="https://ecoproxy.redshift.fr/api/v2/" +
                "Search?citycode=0&" +
                "e="+ContainerData.getInstance().myLongitude+"&" +
                "keyword=$KEYWORD$&" +
                "n="+ContainerData.getInstance().myLatitude+"&" +
                "packings=$PACKING$";


        //https://ecoproxy.redshift.fr/api/v2/AutoComplete?searchText=carton
        HttpUtils.get("https://ecoproxy.redshift.fr/api/v2/AutoComplete?searchText="+seekProductView.getText().toString().trim(), null, new JsonHttpResponseHandler(){
            //HttpUtils.get("3222476439574.json", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                try{
                    JSONArray listeItemSuggested= response.getJSONArray("keywords");

                    JSONObject anItem = listeItemSuggested.getJSONObject(0);

                    System.out.println(anItem.toString());

                    packingsSeek=(anItem.getString("packings")).trim();
                    keyworkSeek=(anItem.getString("keyword")).trim();

                    uriRestIndicationSortingFromKeywork=uriRestIndicationSortingFromKeywork.replace("$KEYWORD$",keyworkSeek);
                    uriRestIndicationSortingFromKeywork=uriRestIndicationSortingFromKeywork.replace("$PACKING$",packingsSeek);

                    System.out.println("URI SEEK Product "+uriRestIndicationSortingFromKeywork);

                    getonteneurFromIndicationProduct();

                }catch (JSONException e) {
                    e.printStackTrace();
                    packingsSeek="";
                    keyworkSeek="";
                    if(progressDialog!=null)
                    {
                        progressDialog.dismiss();
                        progressDialog=null;
                    }
                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }
        });
    }

    public void getonteneurFromIndicationProduct()
    {
        //https://ecoproxy.redshift.fr/api/v2/AutoComplete?searchText=carton
        HttpUtils.get(uriRestIndicationSortingFromKeywork, null, new JsonHttpResponseHandler(){
            //HttpUtils.get("3222476439574.json", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                boolean haveAItem=false;

                try{
                    JSONArray listeItemSuggested= response.getJSONArray("objects");
                    String[] listeLabelAndMaterial= new String[listeItemSuggested.length()];

                    for(int i=0; i<listeItemSuggested.length(); i++) {
                        haveAItem=true;
                        JSONObject aIndication = listeItemSuggested.getJSONObject(i);

                        System.out.println("*"+(aIndication.getString("label")).trim());
                        System.out.println("**"+(aIndication.getString("deposit")).trim());
                        System.out.println("***"+(aIndication.getString("material")).trim());

                        listeLabelAndMaterial[i]=(aIndication.getString("label")).trim()+ " -- Conteneur : ";

                        //Il existe un cas O
                        switch ( aIndication.getString("material").charAt(0)) {
                            case 'C':
                                listeLabelAndMaterial[i]+="Carton";
                                break;
                            case 'D':
                                listeLabelAndMaterial[i]+="déchetterie";
                                break;
                            case 'V':
                                listeLabelAndMaterial[i]+="Verre";
                                break;
                            case 'B':
                                listeLabelAndMaterial[i]+="Brique";
                                break;
                            case 'M':
                                listeLabelAndMaterial[i]+="Métal";
                                break;
                            case 'P':
                                listeLabelAndMaterial[i]+="Plastique";
                                break;
                            case 'J':
                                listeLabelAndMaterial[i]+="Papiers";
                                break;
                            default:
                                listeLabelAndMaterial[i]+=aIndication.getString("material");
                                break;
                        }


                    }

                    if(progressDialog!=null)
                    {
                        progressDialog.dismiss();
                        progressDialog=null;
                    }

                    if (haveAItem)
                        createCustomDialogForKeyword(listeLabelAndMaterial);
                    else
                        showToast("Aucun élément trouvé");
                }catch (JSONException e) {
                    e.printStackTrace();
                    if(progressDialog!=null)
                    {
                        progressDialog.dismiss();
                        progressDialog=null;
                    }

                }
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {

            }
        });
    }

    private void createCustomDialogForKeyword(String[] arrayList)
    {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.custom_alertdialog_keyword);

        //liste des choix disponibles en fonction de la recherche
        listItemSortingViewCustomDialog=(MaterialBetterSpinner) dialog.findViewById(R.id.listItemSortingAlertDialog);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, arrayList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);



        listItemSortingViewCustomDialog.setAdapter(adapter);

        //bouton et action
        Button dialogButtonCancel = (Button) dialog.findViewById(R.id.btnCancelAlertDialog);

        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });



        Button dialogButtonGoConteneur = (Button) dialog.findViewById(R.id.btnGoToConteneurAlertDialog);

        dialogButtonGoConteneur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Permet d'obtenir le type de conteneur sous forme d'un tableau de string
                ContainerData.getInstance().listePackaging=listItemSortingViewCustomDialog.getText().toString()
                        .replace(" -- Conteneur : ",";")
                        .split(";")[1].split(";;");

                dialog.dismiss();

                //Pour sélectionner le menu carte Conteneurs
                navigation.getMenu().findItem(R.id.navigation_mapsContainer).setChecked(true);

                //Pour faire appel à l'API de google Maps
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                mapsGoogleFrag = new MapsConteneur();
                fragmentTransaction.replace(R.id.fragment_container,mapsGoogleFrag);
                fragmentTransaction.commit();


            }
        });

        dialog.show();

        listItemSortingViewCustomDialog.setFocusable(true);

        //Pour séléctionner le premier Item
        listItemSortingViewCustomDialog.setText(arrayList[0]);

        //Permet d'obtenir le type de conteneur sous forme d'un tableau de string
        ContainerData.getInstance().listePackaging=arrayList[0]
                .replace(" -- Conteneur : ",";")
                .split(";")[1].split(";;");


        //toute la largeur et au mini 1/3 en hauteur
        dialog.getWindow().setLayout(getWindow().getDecorView().getRootView().getWidth(), getWindow().getDecorView().getRootView().getHeight()/3);
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

    public String getNetworkType(Context context){
        String networkType = null;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                networkType = "WiFi";
            } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                networkType = "Mobile";
            }
        } else {
            // not connected to the internet
        }
        return networkType;
    }

}
