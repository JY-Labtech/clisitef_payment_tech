import 'package:clisitef_payment_tech/licence/constants/licence_constants.dart';
import 'package:clisitef_payment_tech/payments/payment.dart';

class ClisitefTech {
  ClisitefTech._internal();
  static ClisitefTech? _instance;

  static ClisitefTech get I {
    _instance ??= ClisitefTech._internal();
    return _instance!;
  }

  Payment? _payment;

  Future<Payment> initPayment({
    required String licenceKey,
    required String licenceInternalKey,
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
