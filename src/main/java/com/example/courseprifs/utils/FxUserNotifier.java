package com.example.courseprifs.utils;

import com.example.courseprifs.hibernateControl.UserNotifier;
import javafx.scene.control.Alert;

/**
 * {@link UserNotifier} realizacija, kuri rodo JavaFX langus per {@link FxUtils}.
 * Langų tekstai ir tipai tokie patys, kokie buvo naudojami DAO klasėse iki refaktoringo.
 */
public class FxUserNotifier implements UserNotifier {

    @Override
    public void error(String header, Exception exception) {
        FxUtils.generateDialogAlert(Alert.AlertType.ERROR, header, exception);
    }

    @Override
    public void error(String header, String message) {
        FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", header, message);
    }

    @Override
    public void warning(String header, String message) {
        FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", header, message);
    }
}
