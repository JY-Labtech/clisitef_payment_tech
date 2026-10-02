import 'package:clisitef_payment_tech/licence/constants/licence_constants.dart';
import 'package:flutter/foundation.dart';
import 'package:clisitef_payment_tech/payments/payment.dart';

class ClisitefTech {
  ClisitefTech._internal();
  static ClisitefTech? _instance;

  static ClisitefTech get I {
    _instance ??= ClisitefTech._internal();
    return _instance!;
  }

  Payment? _payment;

  /// Limpa o estado do plugin (uso em testes).
  @visibleForTesting
  static void resetForTesting() {
    _instance = null;
    // ignore: invalid_use_of_visible_for_testing_member
    Payment.resetForTesting();
  }

  /// Valida a licença e libera o plugin.
  ///
  /// [licenceInternalKey] é opcional (padrão: [licenceCheck], servidor oficial
  /// da JY Labtech). Lança `PlatformException(code: 'LICENCE_ERROR')` se a
  /// licença for inválida, vencida, suspensa ou cancelada.
  Future<Payment> initPayment({
    required String licenceKey,
    String licenceInternalKey = licenceCheck,
  }) async {
    final paymentInstance = Payment.create(channel: channel);
    await paymentInstance.ensureInitialized(
      licenceKey: licenceKey,
      licenceInternalKey: licenceInternalKey,
    );
    _payment = paymentInstance;
    return paymentInstance;
  }

  Payment get payment {
    if (_payment == null) {
      throw Exception('Payment não autorizado. Chame initPayment com as chaves de licença válidas.');
    }
    return _payment!;
  }
}
