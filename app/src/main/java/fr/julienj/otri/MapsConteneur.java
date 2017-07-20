package fr.julienj.otri;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public class MapsConteneur extends Fragment implements OnMapReadyCallback{

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


        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(ContainerData.getInstance().myLatitude, ContainerData.getInstance().myLongitude))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        try {
            getNearConteneurFromMyPosition(ContainerData.getInstance().myLongitude, ContainerData.getInstance().myLatitude);
        } catch (JSONException e) {
            e.printStackTrace();
        }





        //LatLng UCA = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(UCA).title("YOUR TITLE")).showInfoWindow();

        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(UCA,17));

    }

    private void displayConteneur()
    {
        //display all conteneur
        if( ContainerData.getInstance().listOfPointOfTri!=null)
        {
            double distance=-1;
            for (PointOfTri aPoint: ContainerData.getInstance().listOfPointOfTri)
            {
                if(aPoint.matchPackaging)
                {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(aPoint.latitude, aPoint.longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            .title(aPoint.road+","+aPoint.city))
                            .showInfoWindow();
                }
                else
                {
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(aPoint.latitude, aPoint.longitude))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                            .title(aPoint.road+","+aPoint.city))
                            .showInfoWindow();
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

                                for (int j = 0; j < listeMaterials.length(); j++) {
                                    JSONObject aMaterial = listeMaterials.getJSONObject(j);
                                    System.out.println(aMaterial.getString("label"));

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
}
