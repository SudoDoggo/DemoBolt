package com.example.courseprifs.hibernateControl;

/**
 * Abstrakcija, per kurią duomenų prieigos sluoksnis praneša vartotojui apie klaidas.
 * <p>
 * DAO klasės nebežino apie JavaFX (Dependency Inversion): realiai programai naudojama
 * {@link com.example.courseprifs.utils.FxUserNotifier}, o testuose galima paduoti mock'ą.
 */
public interface UserNotifier {

    /** Netikėta klaida (rodomas langas su išimties tekstu). */
    void error(String header, Exception exception);

    /** Žinoma klaida su paaiškinimu (pvz. „įrašas nerastas“). */
    void error(String header, String message);

    /** Įspėjimas, kai veiksmas neatliktas, bet tai nėra klaida. */
    void warning(String header, String message);
}
