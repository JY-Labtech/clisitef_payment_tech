import 'package:clisitef_payment_tech/licence/licence_service.dart';

import 'payments/payment.dart';

// export 'payments/payment.dart';
// export 'payments/enum/sitef_modality.dart';
// export 'payments/helper/tef_helper.dart';
// export 'payments/model/json/tef_transaction_model.dart';
// export 'payments/model/params/msitef_params.dart';
// export 'payments/model/sitef_settings.dart';
// export 'payments/model/sitef_transaction.dart';
// export 'payments/model/tef_transaction_response.dart';
// export 'payments/model/params/msitef_type.dart';

class ClisitefTech {
  Payment? _payment;
  ClisitefTech._();

  static ClisitefTech? _instance;

  static ClisitefTech instance() {
    _instance ??= ClisitefTech._();
    return _instance!;
  }

  Future<Payment> initPayment({
    required String licenceKey,
  }) async {
    try {
      final result = await LicenceService().checkLicence(licenceKey: licenceKey);
      if (!result) {
        throw Exception('Invalid licence key. Please check your licence key.');
      }
      _payment ??= Payment();
      return _payment!;
    } catch (e) {
      print('Error initializing payment: $e');
      rethrow;
    }
  }

  Payment get payment {
    if (_payment == null) {
      throw Exception('Payment not initialized. Call initPayment() first.');
    }
    return _payment!;
  }
}
