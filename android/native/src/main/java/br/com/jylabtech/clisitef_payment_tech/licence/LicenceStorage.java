package br.com.jylabtech.clisitef_payment_tech.licence;

/**
 * Cache local da licença. Abstraído para permitir testes sem Android.
 */
public interface LicenceStorage {
    CheckLicenceEntity load();

    boolean save(CheckLicenceEntity entity);

    void clear();
}
