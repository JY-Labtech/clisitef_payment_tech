package br.com.jylabtech.clisitef_payment_tech;

import android.content.Intent;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * Classe de ponte que delega toda a lógica para {@link ClisitefPaymentTechNativePlugin},
 * permitindo distribuir apenas o binário AAR/JAR no pub.dev.
 */
public final class ClisitefPaymentTechPlugin implements
        FlutterPlugin,
        MethodChannel.MethodCallHandler,
        ActivityAware,
        PluginRegistry.ActivityResultListener {

    private final ClisitefPaymentTechNativePlugin delegate = new ClisitefPaymentTechNativePlugin();

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        delegate.onAttachedToEngine(binding);
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        delegate.onDetachedFromEngine(binding);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        delegate.onMethodCall(call, result);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        delegate.onAttachedToActivity(binding);
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        delegate.onDetachedFromActivityForConfigChanges();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        delegate.onReattachedToActivityForConfigChanges(binding);
    }

    @Override
    public void onDetachedFromActivity() {
        delegate.onDetachedFromActivity();
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return delegate.onActivityResult(requestCode, resultCode, data);
    }
}

