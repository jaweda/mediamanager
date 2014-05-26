package com.jamierf.mediamanager.views;

import io.dropwizard.views.View;

public class BackfillForm extends View {

    public BackfillForm() {
        super("backfill.mustache");
    }

    public String getTitle() {
        return "Backfill";
    }
}
