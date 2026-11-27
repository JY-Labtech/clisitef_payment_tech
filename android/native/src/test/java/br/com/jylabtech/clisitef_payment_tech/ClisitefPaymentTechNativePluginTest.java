package br.com.jylabtech.clisitef_payment_tech;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import br.com.jylabtech.clisitef_payment_tech.core.ClisitefTech;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;

public class ClisitefPaymentTechNativePluginTest {

    /** Result que registra a única resposta enviada ao Dart. */
    private static final class RecordingResult implements MethodChannel.Result {
        int calls = 0;
        String kind;
        Object value;
        String errorCode;

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
        }

        @Override
        public void notImplemented() {
            calls++;
            kind = "notImplemented";
        }
    }

    private ClisitefPaymentTechNativePlugin plugin;

    @Before
    public void setUp() {
        plugin = new ClisitefPaymentTechNativePlugin();
    }

    private RecordingResult call(String method) {
        RecordingResult result = new RecordingResult();
        plugin.onMethodCall(new MethodCall(method, new HashMap<String, Object>()), result);
        return result;
    }

    @Test
    public void metodosSuportados() {
        assertTrue(ClisitefTech.isSupportedMethod("configureTef"));
        assertTrue(ClisitefTech.isSupportedMethod("startTransaction"));
        assertTrue(ClisitefTech.isSupportedMethod("transactionMsitef"));
        assertTrue(ClisitefTech.isSupportedMethod("menuMSitef"));
        assertFalse(ClisitefTech.isSupportedMethod("initialize"));
        assertFalse(ClisitefTech.isSupportedMethod("desconhecido"));
    }

    @Test
    public void metodoDesconhecidoRespondeNotImplemented() {
        RecordingResult result = call("desconhecido");
        assertEquals(1, result.calls);
        assertEquals("notImplemented", result.kind);
    }

    @Test
    public void initializeNaoExisteMais() {
        RecordingResult result = call("initialize");
        assertEquals(1, result.calls);
        assertEquals("notImplemented", result.kind);
    }

    @Test
    public void operacoesSemActivityRespondemNoActivityUmaVez() {
        for (String method : new String[]{"configureTef", "startTransaction", "transactionMsitef", "menuMSitef"}) {
            RecordingResult result = call(method);
            assertEquals(method, 1, result.calls);
            assertEquals(method, "error", result.kind);
            assertEquals(method, "NO_ACTIVITY", result.errorCode);
            assertNull(method, result.value);
        }
    }
}
