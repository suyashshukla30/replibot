package com.suyashshukla.replibot;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // on below line we are creating
    // a list with Firebase Text Message.
    List<FirebaseTextMessage> messageList;

    // on below line we are creating variables for
    // our edittext, recycler view, floating action button,
    // array list for storing our message
    // and creating a variable for our adapter class.
    private EditText userMsgEdt;
    private RecyclerView msgRV;
    private FloatingActionButton sendFAB;
    private ArrayList<ChatMsgModal> chatMsgModalArrayList;
    private ChatRVAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing all our variables.
        userMsgEdt = findViewById(R.id.idEdtUserMsg);
        msgRV = findViewById(R.id.idRVMessage);
        sendFAB = findViewById(R.id.idBtnFAB);

        // initializing our array list.
        messageList = new ArrayList<>();
        chatMsgModalArrayList = new ArrayList<>();

        // initializing our adapter.
        adapter = new ChatRVAdapter(MainActivity.this, chatMsgModalArrayList);

        // layout manager for our recycler view.
        LinearLayoutManager manager = new LinearLayoutManager(MainActivity.this);

        // setting layout manager
        // for our recycler view.
        msgRV.setLayoutManager(manager);

        // setting adapter to our recycler view.
        msgRV.setAdapter(adapter);

        // adding on click listener for our floating action button
        sendFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // validating if the edit text is empty or not.
                if (TextUtils.isEmpty(userMsgEdt.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter your message..", Toast.LENGTH_SHORT).show();
                    return;
                }
                // calling a method to send our message
                // and getting the response.
                sendMessage();
                userMsgEdt.setText("");
            }
        });
    }

    private void sendMessage() {
        // on below line we are creating a variable for our Firebase text message
        // and passing our user input message to it along with time.
        FirebaseTextMessage message = FirebaseTextMessage.createForRemoteUser(
                userMsgEdt.getText().toString(), // Content of the message
                System.currentTimeMillis(),	 // Time at which the message was sent
                "uid"						 // This has to be unique for every other person involved
                // in the chat who is not your user
        );
        // on below line we are adding
        // our message to our message list.
        messageList.add(message);

        // on below line we are adding our edit text field
        // value to our array list and setting type as 0.
        // as we are using type as 0 for our user message
        // and 1 for our message from Firebase.
        chatMsgModalArrayList.add(new ChatMsgModal(userMsgEdt.getText().toString(), 0));

        // on below line we are calling a method
        // to notify data change in adapter.
        adapter.notifyDataSetChanged();

        // on below line we are creating a variable for our Firebase
        // smart reply and getting instance of it.
        FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();

        // on below line we are calling a method to suggest reply for our user message.
        smartReply.suggestReplies(messageList).addOnSuccessListener(new OnSuccessListener<SmartReplySuggestionResult>() {
            @Override
            public void onSuccess(SmartReplySuggestionResult smartReplySuggestionResult) {
                // inside on success listener method we are checking if the language is not supported.
                if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_NOT_SUPPORTED_LANGUAGE) {
                    // displaying toast message if the language is not supported.
                    Toast.makeText(MainActivity.this, "Language not supported..", Toast.LENGTH_SHORT).show();
                } else if (smartReplySuggestionResult.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {
                    // if we get a successful status message.
                    // we are adding that message to our
                    // array list and notifying our adapter
                    // that data has been changed
                    chatMsgModalArrayList.add(new ChatMsgModal(smartReplySuggestionResult.getSuggestions().get(0).getText(), 1));
                    adapter.notifyDataSetChanged();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // inside on failure method we are displaying a toast message
                Toast.makeText(MainActivity.this, "Fail to get data.." + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
