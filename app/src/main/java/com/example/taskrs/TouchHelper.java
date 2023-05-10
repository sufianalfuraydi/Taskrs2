package com.example.taskrs;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.taskrs.Adapter.ToDoAdapter;

/* TouchHelper is a class that extends the SimpleCallback class from ItemTouchHelper.
 This allows us to handle swipe actions on items in a RecyclerView.

 in touch helper we do more assist from google like stackoverflow and others this refernce/
 (https://stackoverflow.com/questions/40240307/correct-way-to-implement-item-touch-helper-on-recyclerview-in-android)
 */
public class TouchHelper extends ItemTouchHelper.SimpleCallback {
    private ToDoAdapter adapter;

    // The constructor takes a ToDoAdapter object as an argument.
    public TouchHelper(ToDoAdapter adapter) {
        // The superclass constructor is called with two arguments:
        // 1. 0 indicates that no drag-and-drop functionality is supported.
        // 2. LEFT | RIGHT indicates that swipe left and swipe right actions are supported.
        super(0 , ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    // The onMove() method is not used, so it just returns false.
    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    // The onSwiped() method is called when an item in the RecyclerView is swiped.
    // The direction argument indicates whether the swipe was to the left or right.
    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        if (direction == ItemTouchHelper.RIGHT){
            // If the swipe was to the right, show a dialog box to confirm deletion.
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setMessage("the message will deleted!")
                    .setTitle("Delete Task")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            adapter.deleteTask(position);
                        }
                    }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // If the user cancels the deletion, notify the adapter that the item should be updated.
                            adapter.notifyItemChanged(position);
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // If the swipe was to the left, edit the item.
            adapter.editTask(position);
        }
    }
}
