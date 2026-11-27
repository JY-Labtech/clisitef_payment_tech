package br.com.jylabtech.clisitef_payment_tech.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class ClisitefTechTest {

    private static final class RecordingResult implements MethodChannel.Result {
        int calls = 0;
        String kind;
        Object value;
        String errorCode;
        String errorMessage;
        Object errorDetails;

        @Override
        public void success(@Nullable Object result) {
            calls++;
            kind = "success";
            value = result;
        }

        @Override
        public void error(@NonNull String code, @Nullable String message, @Nullable Object details) {
            calls++;
            kind = "error";
            errorCode = code;
            errorMessage = message;
            errorDetails = details;
        }

        @Override
        public void notImplemented() {
            calls++;
            kind = "notImplemented";
        }
    }

    private static final class FakeOperations implements PaymentOperations {
        int configureResult = 0;
        int transactionResult = 0;
        RuntimeException transactionError;
        final List<String> invoked = new ArrayList<>();

        @Override
        public int configureTef(MethodCall call) {
            invoked.add("configureTef");
            return configureResult;
        }

        @Override
        public int clisitefTransaction(MethodCall call) {
            invoked.add("clisitefTransaction");
            if (transactionError != null) throw transactionError;
            return transactionResult;
        }

        @Override
        public void mSitefTransaction(MethodCall call) {
            invoked.add("mSitefTransaction");
        }

        @Override
        public void mSitefMenu(MethodCall call) {
            invoked.add("mSitefMenu");
        }

        @Override
        public void updateActivity(Activity activity) {
        }

        @Override
        public void dispose() {
        }
    }

    private static MethodCall call(String method) {
        return new MethodCall(method, new HashMap<String, Object>());
    }

    @Test
    public void configureTefRespondeOCodigoDoSdk() throws Exception {
        FakeOperations ops = new FakeOperations();
        ops.configureResult = 2;
        RecordingResult result = new RecordingResult();

        boolean awaits = new ClisitefTech(ops).handleMethodCall(call("configureTef"), result);

        assertFalse(awaits);
        assertEquals(1, result.calls);
        assertEquals("success", result.kind);
        assertEquals(2, result.value);
    }

    @Test
    public void startTransactionComConfigureOkAguardaAActivity() throws Exception {
        FakeOperations ops = new FakeOperations();
        RecordingResult result = new RecordingResult();

        boolean awaits = new ClisitefTech(ops).handleMethodCall(call("startTransaction"), result);

        assertTrue(awaits);
        assertEquals(0, result.calls);
        assertEquals(1, ops.invoked.size());
        assertEquals("clisitefTransaction", ops.invoked.get(0));
    }

    @Test
    public void startTransactionComConfigureFalhoRespondeConfigureErrorUmaVez() throws Exception {
        FakeOperations ops = new FakeOperations();
        ops.transactionResult = 3;
        RecordingResult result = new RecordingResult();

        boolean awaits = new ClisitefTech(ops).handleMethodCall(call("startTransaction"), result);

        assertFalse(awaits);
        assertEquals(1, result.calls);
        assertEquals("error", result.kind);
        assertEquals(ClisitefTech.CONFIGURE_ERROR, result.errorCode);
        assertEquals(3, result.errorDetails);
        assertTrue(result.errorMessage.contains("3"));
    }

    @Test
    public void startTransactionComExcecaoNaoResponde() throws Exception {
        FakeOperations ops = new FakeOperations();
        ops.transactionError = new IllegalStateException("sdk");
        RecordingResult result = new RecordingResult();

        try {
            new ClisitefTech(ops).handleMethodCall(call("startTransaction"), result);
            fail("esperava exceção");
        } catch (IllegalStateException e) {
            assertSame(ops.transactionError, e);
        }
        // Quem responde (UNEXPECTED_ERROR) é o plugin, no catch.
        assertEquals(0, result.calls);
    }

    @Test
    public void mSitefAguardaAActivity() throws Exception {
        FakeOperations ops = new FakeOperations();
        RecordingResult r1 = new RecordingResult();
        RecordingResult r2 = new RecordingResult();
        ClisitefTech tech = new ClisitefTech(ops);

        assertTrue(tech.handleMethodCall(call("transactionMsitef"), r1));
        assertTrue(tech.handleMethodCall(call("menuMSitef"), r2));
        assertEquals(0, r1.calls);
        assertEquals(0, r2.calls);
        assertEquals("mSitefTransaction", ops.invoked.get(0));
        assertEquals("mSitefMenu", ops.invoked.get(1));
    }

    @Test
    public void metodoDesconhecidoRespondeNotImplemented() throws Exception {
        RecordingResult result = new RecordingResult();
        assertFalse(new ClisitefTech(new FakeOperations()).handleMethodCall(call("outro"), result));
        assertEquals(1, result.calls);
        assertEquals("notImplemented", result.kind);
        assertNull(result.value);
    }
}
