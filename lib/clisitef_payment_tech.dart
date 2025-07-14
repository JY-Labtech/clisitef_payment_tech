import 'package:check_licence_tech/check_licence_tech.dart';
import 'package:clisitef_payment_tech/licence/constants/licence_constants.dart';
import 'package:clisitef_payment_tech/payments/payment.dart';

class ClisitefTech {
  static final ClisitefTech _instance = ClisitefTech._internal();
  static ClisitefTech get instance => _instance;

  Payment? _payment;

  ClisitefTech._internal();

  final _licence = CheckLicenceTech.instance();

  Future<String> getSerialNumber() async => await _licence.getSerialNumber();

  Future<Payment> initPayment({required String licenceKey}) async {
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
