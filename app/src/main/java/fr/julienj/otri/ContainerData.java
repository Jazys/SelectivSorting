package fr.julienj.otri;

import java.util.ArrayList;

/**
 * Created by JulienJ on 16/07/2017.
 */

public class ContainerData {
    private static final ContainerData ourInstance = new ContainerData();


    public String urlProductInfo;

    public double myLatitude;
    public double myLongitude;
    public String codeScanned;
    public String[] listePackaging;
    public ArrayList<PointOfTri> listOfPointOfTri;
    public boolean isInternetAlive;

    public static ContainerData getInstance() {
        return ourInstance;
    }

    private ContainerData() {
        urlProductInfo="https://fr.openfoodfacts.org/produit/";
        myLatitude=0;
        myLongitude=0;
        codeScanned="";
        listePackaging=null;
        isInternetAlive=false;
        listOfPointOfTri=new ArrayList<PointOfTri>();

    }

    private void clearListPointOfTri()
    {
        listOfPointOfTri.clear();
    }
}
