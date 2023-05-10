package com.example.taskrs;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;



//we use fragment to make it more easy and touch by user side
//this reference we used to know and declare(https://www.geeksforgeeks.org/fragment-lifecycle-in-android/)
public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    private TextView setDate;
    private EditText editText;
    private Button SaveBtn;
    private FirebaseFirestore firestore;
    private Context context;
    private String dueDate = "";
    private String id = "";
    private String dueDateUpdate = "";

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Nullable//so that can handle null values
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.add_new_task , container , false);//we used inflater to make it compatible with xml file

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setDate = view.findViewById(R.id.setDate);
        editText= view.findViewById(R.id.editText);
        SaveBtn = view.findViewById(R.id.save);

        firestore = FirebaseFirestore.getInstance();

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if (bundle != null){
            isUpdate = true;
            String task = bundle.getString("task");
            id = bundle.getString("id");
            dueDateUpdate = bundle.getString("due");

            editText.setText(task);
            setDate.setText(dueDateUpdate);

            if (task.length() > 0){
                SaveBtn.setEnabled(false);
                SaveBtn.setBackgroundColor(Color.GRAY);
            }
        }

        editText.addTextChangedListener(new TextWatcher() {
            /* here we make listener will focus about changing in the adding text
            if any text change(add char,remove etc..) will make save buttin on
            else:will still disable because not changing.
            */
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
               if (s.toString().equals("")){
                   SaveBtn.setEnabled(false);
                   SaveBtn.setBackgroundColor(Color.GRAY);
               }else{
                   SaveBtn.setEnabled(true);

               }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        setDate.setOnClickListener(new View.OnClickListener() {
            //this listener will be wait for any change in date will perform to set date
            //to show the dialog box have dialog date picker *Reference*(https://www.digitalocean.com/community/tutorials/android-date-time-picker-dialog)
            @Override
            public void onClick(View v) {
                Calendar calendar = Calendar.getInstance();

                int MONTH = calendar.get(Calendar.MONTH);
                int YEAR = calendar.get(Calendar.YEAR);
                int DAY = calendar.get(Calendar.DATE);

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        month =+ 1;
                        setDate.setText(dayOfMonth + "/" + month + "/" + year);
                        dueDate = dayOfMonth + "/" + month +"/"+year;

                    }
                } , YEAR , MONTH , DAY);

                datePickerDialog.show();
            }
        });

        boolean finalIsUpdate = isUpdate;
        SaveBtn.setOnClickListener(new View.OnClickListener() {
            /*this is listen for save button any change in length of task will
            go blue else will be the same to announce there is no added new change to
            task you try to add then if save will add to hash map called taskMap
            then will saved to the data base in firebase by firestore.collction
            before this will check if it is update (finalIsUpdate)?will update in the realtime database
            in firebase else will go else finally if successful will show alert is that go right way and done!
            */
            @Override
            public void onClick(View v) {

                String task = editText.getText().toString();

                if (finalIsUpdate){
                    firestore.collection("task").document(id).update("task" , task , "due" , dueDate);
                    Toast.makeText(context, "Task  is Updated", Toast.LENGTH_SHORT).show();

                }
                else {
                    if (task.isEmpty()) {
                        Toast.makeText(context, "sorry,Empty task not Allowed !", Toast.LENGTH_SHORT).show();
                    } else {

                        Map<String, Object> taskMap = new HashMap<>();

                        taskMap.put("task", task);
                        taskMap.put("due", dueDate);
                        taskMap.put("status", 0);
                        taskMap.put("time", FieldValue.serverTimestamp());

                        firestore.collection("task").add(taskMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(context, "Task is Saved", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                dismiss();
            }
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        // this method will execute fragment and link it with the activity we called context in it
        //and to access to theresourses and others
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        /*
        any dismiss action to the fragment activity will called this
        in onClick up will do job what is the  job? to notify
        the activity is the fragment dismissed
        */
        super.onDismiss(dialog);//called the parent class and pass dialog
        Activity activity = getActivity();//to get the current activity
        if (activity instanceof  OnDialogCloseListner){//to check current activity implement interface ondialogclose or not
            ((OnDialogCloseListner)activity).onDialogClose(dialog);//will implement and close
        }
    }
}
