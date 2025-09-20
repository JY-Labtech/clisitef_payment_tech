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

  Payment initPayment() {
    if (_payment != null) {
      return _payment!;
    }
    _payment = Payment.create(channel: channel);
    return _payment!;
  }

  Payment get payment {
    if (_payment == null) {
      throw Exception('Payment não autorizado. Licença inválida.');
    }
    return _payment!;
  }
}
