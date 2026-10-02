package br.com.jylabtech.clisitef_payment_tech.licence;

import java.util.Date;

/** Relógio injetável (java.util.function.Supplier exige API 24). */
public interface LicenceClock {
    Date now();

    LicenceClock SYSTEM = new LicenceClock() {
        @Override
        public Date now() {
            return new Date();
        }
    };
}
