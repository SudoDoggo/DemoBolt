package com.example.courseprifs.utils;

import javafx.scene.control.Alert;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.mockito.Mockito.mockStatic;

/** Patikrina, kad FxUserNotifier rodo tuos pačius langus, kuriuos DAO rodė prieš refaktoringą. */
class FxUserNotifierTest {

    private final FxUserNotifier notifier = new FxUserNotifier();

    @Test
    void delegatesToFxUtils() {
        Exception e = new RuntimeException("x");
        try (MockedStatic<FxUtils> fx = mockStatic(FxUtils.class)) {
            notifier.error("Header", e);
            notifier.error("Not found", "msg");
            notifier.warning("Careful", "msg2");

            fx.verify(() -> FxUtils.generateDialogAlert(Alert.AlertType.ERROR, "Header", e));
            fx.verify(() -> FxUtils.generateAlert(Alert.AlertType.ERROR, "Error", "Not found", "msg"));
            fx.verify(() -> FxUtils.generateAlert(Alert.AlertType.WARNING, "Warning", "Careful", "msg2"));
        }
    }
}
