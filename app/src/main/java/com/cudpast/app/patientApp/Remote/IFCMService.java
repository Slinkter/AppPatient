package com.cudpast.app.patientApp.Remote;

import com.cudpast.app.patientApp.helper.FCMResponse;
import com.cudpast.app.patientApp.helper.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAqb495J4:APA91bG4syzoU6QuWt5GvyIu3FUoPS9UPMNYWmeFZUutzYQZ1rPtou54UOTwxd-eumWuqdaCAoNJ1MOQJ6FXGZQnOW66KbH1YvsfGWZlyUjHvZdeH8iP6arioetA1VCCycVDDJiaSkge"

    })

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}
