package com.cudpast.app.patientApp.Service;

import com.cudpast.app.patientApp.Common.Common;
import com.cudpast.app.patientApp.helper.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class MyFirebaseIdService {



  //  extends FirebaseInstanceIdService

//    @Override
//    public void onTokenRefresh() {
//        super.onTokenRefresh();
//        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        updateTokenToServer(refreshedToken);
//    }
//
//
//    private void updateTokenToServer(String refreshedToken) {
//        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference(Common.token_tbl);
//        Token token = new Token(refreshedToken);
//        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
//            tokens.child(FirebaseAuth
//                    .getInstance()
//                    .getCurrentUser()
//                    .getUid())
//                    .setValue(token);
//        }
//    }
}
