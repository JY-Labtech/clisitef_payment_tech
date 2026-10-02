package br.com.jylabtech.clisitef_payment_tech.licence;

/**
 * Consulta online da licença. Abstraído para permitir testes sem rede.
 * Implementações devem ser chamadas fora da main thread.
 */
public interface LicenceNetwork {
    LicenceResult checkLicence(String url, String licenceKey) throws Exception;
}
