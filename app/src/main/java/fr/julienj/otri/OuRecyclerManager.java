package fr.julienj.otri;

/**
 * Created by JulienJ on 07/08/2017.
 */

public class OuRecyclerManager {

    private static String baseUrl="https://ourecycler.fr/generateurseul.php?";
    private static String filtreLatLong="SO_Lt=%latMin%&SO_Lg=%longMin%&NE_Lt=%latMax%&NE_Lg=%longMax%";
    private static String otherFilter="&typ=%type%&asso=-999&Pts-apport=1&dech=1&filtre=%filtre%&typact=1";

    private static String[] verre={"typ=1", "filtre=7"};
    private String[] bouchon={"typ=2", "filtre=22"};
    private String[] capsuleCafe={"typ=3", "filtre=31"};
    private String[] ampoule={"typ=4", "filtre=11"};
    private String[] tel={"typ=5", "filtre=5"};
    private String[] dechetVert={"typ=6", "filtre=13"};
    private String[] compost={"typ=6", "filtre=34"};
    private String[] tissu={"typ=7", "filtre=17"};
    private String[] pile={"typ=9", "filtre=9"};
    private String[] jouet={"typ=11", "filtre=16"};
    private String[] cartoucheEncre={"typ=11", "filtre=17"};
    private String[] chaussures={"typ=13", "filtre=18"};
    private String[] cdDvd={"typ=14", "filtre=19"};




    public OuRecyclerManager()
    {

    }

    public static String getUrlOuRecyclcer(double longitude, double latitude, String type)
    {
        return (baseUrl
                +filtreLatLong.replace("%latMin%",String.valueOf(latitude-0.1))
                .replace("%latMax%",String.valueOf(latitude+0.1))
                .replace("%longMin%",String.valueOf(longitude-0.1))
                .replace("%longMax%",String.valueOf(longitude+0.1))
                +otherFilter.replace("typ=%type%",verre[0])
                .replace("filtre=%filtre%",verre[1]));
    }

}
