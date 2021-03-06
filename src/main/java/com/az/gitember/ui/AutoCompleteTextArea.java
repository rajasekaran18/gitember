package com.az.gitember.ui;

import javafx.scene.control.TextArea;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Thanks to https://gist.github.com/floralvikings for AutoComplete
 * Igor_Azarny on 28 - Jan -2017.
 */
public class AutoCompleteTextArea extends TextArea {

    /** The existing autocomplete entries. */
    private final SortedSet<String> entries = new TreeSet<>();
    private final ChangeListenerHistoryHint changeListenerHistoryHint;


    public AutoCompleteTextArea() {
        this("");
    }

    public AutoCompleteTextArea(String text) {
        super(text);

        changeListenerHistoryHint = new ChangeListenerHistoryHint(this, entries);

    }

    public SortedSet<String> getEntries() {
        return entries;
    }
}
