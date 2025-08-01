import 'package:check_licence_tech/check_licence_tech.dart';
import 'package:clisitef_payment_tech/licence/constants/licence_constants.dart';
import 'package:clisitef_payment_tech/payments/payment.dart';

class ClisitefTech {
  ClisitefTech._internal();
  static ClisitefTech? _instance;

  static ClisitefTech get instance {
    _instance ??= ClisitefTech._internal();
    return _instance!;
  }

  Payment? _payment;

  final _licence = CheckLicenceTech.instance();

  Future<String> getSerialNumber() async => await _licence.getSerialNumber();

  Future<Payment> initPayment({required String licenceKey}) async {
    if (_payment != null) {
      return _payment!;
    }
    _payment = await Payment.create(channel: channel, licenceKey: licenceKey);
    return _payment!;
  }

  Payment get payment {
    if (_payment == null) {
      throw Exception('Payment não autorizado. Licença inválida.');
    }
    return _payment!;
  }
}
