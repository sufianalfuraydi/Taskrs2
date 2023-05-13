package com.example.taskrs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.taskrs.Adapter.ToDoAdapter;
import com.example.taskrs.Model.model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnDialogCloseListner{

    private RecyclerView recyclerView; // RecyclerView to display the to-do list
    private FloatingActionButton Fab; // Button to add new tasks
    private FirebaseFirestore firestore; // Firebase Firestore database instance
    private ToDoAdapter adapter; // Adapter for the RecyclerView
    private List<model> mList; // List of to-do items
    FirebaseAuth auth;
    Button button;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to UI in activity_main.xml elements
        recyclerView = findViewById(R.id.recycerlview);
        Fab = findViewById(R.id.floatingActionButton);

        auth = FirebaseAuth.getInstance();

        button = findViewById(R.id.logout);

        user = auth.getCurrentUser();

        if (user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });

        // Initialize Firebase Firestore database instance
        firestore = FirebaseFirestore.getInstance();

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // Set the layout manager
        adapter = new ToDoAdapter(this, mList = new ArrayList<>()); // Create a new adapter with an empty list
        recyclerView.setAdapter(adapter); // Set the adapter to the RecyclerView
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new TouchHelper(adapter)); // Set up swipe-to-delete
        itemTouchHelper.attachToRecyclerView(recyclerView);

        // Set up button to add new tasks
        Fab.setOnClickListener(view -> AddNewTask.newInstance().show(getSupportFragmentManager(), AddNewTask.TAG));

        // Load data from the Firebase Firestore database
        loadData();
    }

    // Method to load data from Firebase Firestore database
    private void loadData(){
        firestore.collection("task") // Get a reference to the "task" collection
                .orderBy("time", Query.Direction.DESCENDING) // Order the tasks by time, newest first
                .addSnapshotListener((value, error) -> { // Set up a listener for changes to the database
                    if (error != null) return; // If there was an error, do nothing
                    mList.clear(); // Clear the list of to-do items
                    for (QueryDocumentSnapshot doc : value) { // Loop through each document in the collection
                        model toDoModel = doc.toObject(model.class).withId(doc.getId()); // Convert the document to a ToDoModel object
                        mList.add(toDoModel); // Add the new to-do item to the list
                    }
                    adapter.notifyDataSetChanged(); // Notify the adapter that the data has changed
                });
    }

    // Method to reload data after a new task is added
    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        loadData();
    }
}