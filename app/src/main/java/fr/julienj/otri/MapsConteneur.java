package fr.julienj.otri;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;


public class MapsConteneur extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;

    public MapsConteneur() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.activity_maps_conteneur, container, false);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return view;
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Efface tout
        mMap.clear();

        //efface la liste des points si existants
        if( !ContainerData.getInstance().listOfPointOfTri.isEmpty())
            ContainerData.getInstance().listOfPointOfTri.clear();


        System.out.println("Liste packaging : "+ Arrays.toString(ContainerData.getInstance().listePackaging));


        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(ContainerData.getInstance().myLatitude, ContainerData.getInstance().myLongitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        try {
            if (ContainerData.getInstance().isInternetAlive)
                getNearConteneurFromMyPosition(ContainerData.getInstance().myLongitude, ContainerData.getInstance().myLatitude);
            else
                showToast("Pas de connexion Internet Disponible");
        } catch (JSONException e) {
            e.printStackTrace();
        }




        getOuRecyclerData();
        //LatLng UCA = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(UCA).title("YOUR TITLE")).showInfoWindow();

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UCA,17));

    }

    private void displayConteneur()
    {
        String addInfoConteneur="";
        Marker marker;

        //display all conteneur
        if( ContainerData.getInstance().listOfPointOfTri!=null)
        {
            double distance=-1;
            for (PointOfTri aPoint: ContainerData.getInstance().listOfPointOfTri)
            {

                if(aPoint.listeConteneur!=null)
                {

                    addInfoConteneur = Arrays.toString(aPoint.listeConteneur);
                }
                else
                    addInfoConteneur="Aucune information disponible";

                if(aPoint.matchPackaging)
                {
                    marker=mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(aPoint.latitude, aPoint.longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title(aPoint.road+","+aPoint.city));
                    marker.setTag(aPoint.distance);
                    marker.setSnippet(addInfoConteneur);
                    marker.showInfoWindow();
                }
                else
                {
                    marker=mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(aPoint.latitude, aPoint.longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .title(aPoint.road+","+aPoint.city));
                    marker.setTag(aPoint.distance);
                    marker.setSnippet(addInfoConteneur);
                    marker.showInfoWindow();

                }

                if(distance==-1 || distance>=aPoint.distance)
                {
                    distance=aPoint.distance;
                    //theContainerlatitude=aPoint.latitude;
                    //theContainerlongitude=aPoint.longitude;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(aPoint.latitude, aPoint.longitude),13));
                }
            }
        }
    }

    public void getNearConteneurFromMyPosition(double longitude, double latitude) throws JSONException {
        {
            https://ecoproxy.redshift.fr/api/v2/PickupPoint?e=5&materials=&maxPoints=20&n=45.393794


            HttpUtils.get("https://ecoproxy.redshift.fr/api/v2/PickupPoint?e=" + longitude + "&materials=&maxPoints=20&n="+latitude, null, new JsonHttpResponseHandler() {
                //HttpUtils.get("3222476439574.json", null, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                    try{


                        JSONArray listeOfPdc= response.getJSONArray("pdc");

                        for(int i=0; i<listeOfPdc.length(); i++){
                            JSONObject aPdc = listeOfPdc.getJSONObject(i);

                            PointOfTri aPointOfTri=new PointOfTri();

                            JSONObject aCoordinate= aPdc.getJSONObject("coordinates");
                            aPointOfTri.latitude=aCoordinate.getDouble("N");
                            aPointOfTri.longitude=aCoordinate.getDouble("E");;
                            aPointOfTri.distance=aCoordinate.getDouble("distance");

                            JSONObject aCity= aPdc.getJSONObject("city");
                            aPointOfTri.road=aCity.getString("street");
                            aPointOfTri.city=aCity.getString("cityname");

                            if (aPdc.has("materials")) {

                                JSONArray listeMaterials = aPdc.getJSONArray("materials");

                                aPointOfTri.listeConteneur=new String[listeMaterials.length()];

                                for (int j = 0; j < listeMaterials.length(); j++) {
                                    JSONObject aMaterial = listeMaterials.getJSONObject(j);
                                    System.out.println(aMaterial.getString("label"));
                                    aPointOfTri.listeConteneur[j]=aMaterial.getString("label");

                                    if (ContainerData.getInstance().listePackaging != null && ContainerData.getInstance().listePackaging.length >= 1) {

                                        for (int z = 0; z < ContainerData.getInstance().listePackaging.length; z++) {

                                            if (ContainerData.getInstance().listePackaging[z].equalsIgnoreCase(aMaterial.getString("label"))) {
                                                aPointOfTri.matchPackaging = true;

                                            }
                                        }
                                    }
                                }
                            }

                            ContainerData.getInstance().listOfPointOfTri.add(i,aPointOfTri);


                        }

                        //getTheNearestPoint();
                        //displayMaps(theContainerlatitude,theContainerlongitude);


                    }catch (JSONException e) {
                        e.printStackTrace();
                    }

                    displayConteneur();

                }

                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONArray timeline) {


                }
            });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        int position = (int)(marker.getTag());
        return true;
    }

    public void getOuRecyclerData()
    {
        HttpUtils.get(OuRecyclerManager.getUrlOuRecyclcer(ContainerData.getInstance().myLongitude, ContainerData.getInstance().myLatitude,""), null,  new TextHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, String res) {
                        // called when response HTTP status is "200 OK"
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                        // called when response HTTP status is "4XX" (eg. 401, 403, 404)
                    }
                }
        );

    }

    public void showToast(final String toast)
    {
        getActivity().runOnUiThread(new Runnable() {
            public void run()
            {
                Toast.makeText(getActivity(), toast, Toast.LENGTH_SHORT).show();



            }
        });
    }


}
