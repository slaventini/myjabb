package com.myjabb;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.roster.Roster;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddContackFragment extends Fragment {

    private EditText mUseridView;

    public AddContackFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_contack, container, false);

        mUseridView = (EditText) view.findViewById(R.id.userid);

        Button mAddContact = (Button) view.findViewById(R.id.add_button);
        mAddContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContact();
            }
        });

        getActivity().setTitle("Добавить контакт");

        // Inflate the layout for this fragment
        return view;
    }

    private void addContact(){

        String userid = mUseridView.getText().toString();

        if (TextUtils.isEmpty(userid)) {
            mUseridView.setError(getString(R.string.error_field_required));
            mUseridView.requestFocus();
        } else {
            Roster roster = Roster.getInstanceFor(getApp().getConnection());
            try {
                roster.createEntry(userid, userid, null);
            } catch (SmackException.NotLoggedInException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "ОШИБКА! " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (SmackException.NoResponseException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "ОШИБКА! " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (XMPPException.XMPPErrorException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "ОШИБКА! " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
                Toast.makeText(getActivity(), "ОШИБКА! " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mUseridView = null;
    }

    public MyJabbApp getApp(){
        return ((ChatActivity)getActivity()).getApp();
    }



}
