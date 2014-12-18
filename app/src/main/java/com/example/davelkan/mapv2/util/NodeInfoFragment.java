package com.example.davelkan.mapv2.util;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.davelkan.mapv2.R;

public class NodeInfoFragment extends Fragment {
    View rootView;
    Context context;

    private OnFragmentInteractionListener mListener;

    public NodeInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_node_info, container, false);

        Button gatherIntelButton = (Button) rootView.findViewById(R.id.gather_intel_button);
        Button decryptIntelButton = (Button) rootView.findViewById(R.id.decrypt_intel_button);
        Button deliverIntelButton = (Button) rootView.findViewById(R.id.deliver_intel_button);

        decryptIntelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mapState = 4;
//                displayButtons(mapState);
//                popUp(intel.decryptIntel(activeNode, user));
            }
        });

        deliverIntelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mapState = 3;
//                displayButtons(mapState);
//                returnIntel();
            }
        });

        gatherIntelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mapState = 3;
//                displayButtons(mapState);
//                popUp(intel.gatherIntel(activeNode, user));
            }
        });

        return rootView;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        context = activity;
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
