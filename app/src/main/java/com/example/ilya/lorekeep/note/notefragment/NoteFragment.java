package com.example.ilya.lorekeep.note.notefragment;


import android.graphics.Color;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.ilya.lorekeep.note.dao.Note;

public class NoteFragment {

    private LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );

    private LinearLayout layout;

    @Nullable
    public View CreateView(ViewGroup parent, Note note) {

        layout = new LinearLayout(parent.getContext());
        layout.setBackgroundColor(Color.GRAY);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView content = new TextView(parent.getContext());
        content.setLayoutParams(params);

        if (!note.getNoteComment().isEmpty()) {
            TextView comment = new TextView(parent.getContext());
            comment.setLayoutParams(params);
            comment.setText(note.getNoteComment());
            comment.setGravity(Gravity.CENTER);
            comment.setTextColor(Color.WHITE);
            layout.addView(comment);
        }

        TextView link = new TextView(parent.getContext());
        link.setLayoutParams(params);

        params.setMargins(20, 20, 20, 20);
        layout.setLayoutParams(params);

        content.setText(note.getNoteContent());
        content.setGravity(Gravity.CENTER);
        layout.addView(content);

        link.setText(note.getNoteContent());
        link.setGravity(Gravity.CENTER);
        layout.addView(link);

        return layout;
    }

    private int getChildNumber() {
        return ((ViewGroup) layout.getParent()).getChildCount();
    }
}
