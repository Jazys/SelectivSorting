package fr.julienj.otri;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class ContributionFragment extends Fragment {

    private Button addPointTri;
    private Button addProductInfomration;
    private EditText contribLat;
    private EditText contribLong;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_contribution, container, false);

        addPointTri=(Button) view.findViewById(R.id.btnAddPointTri) ;
        addProductInfomration=(Button) view.findViewById(R.id.btnAddProductInfo);
        contribLat=(EditText) view.findViewById(R.id.contribLat) ;
        contribLong=(EditText) view.findViewById(R.id.contribLong) ;

        contribLat.setText(String.valueOf(ContainerData.getInstance().myLatitude));
        contribLong.setText(String.valueOf(ContainerData.getInstance().myLongitude));

        contribLat.setEnabled(false);
        contribLong.setEnabled(false);

        https://play.google.com/store/apps/details?id=org.openfoodfacts.scanner


        addPointTri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("Votre point de tri sera validé prochainnement, merci pour votre contribution");
            }
        });

        addProductInfomration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = getActivity().getPackageManager().getLaunchIntentForPackage(getResources().getString(R.string.appOpenFoodFacts));
                if (intent != null) {
                    // We found the activity now start the activity
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } else {
                    // Bring user to the market or let them choose an app?
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse("market://details?id=" + getResources().getString(R.string.appOpenFoodFacts)));
                    startActivity(intent);
                }

            }
        });


        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onDetach() {
        super.onDetach();
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

    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getApplicationInfo(packageName, 0);
            return true;
        }
        catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }


}
