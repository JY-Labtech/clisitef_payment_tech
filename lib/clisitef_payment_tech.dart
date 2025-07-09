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

  static ClisitefTech? _instance;

//GET instance from ClisitefTech
  static ClisitefTech instance() {
    _instance ??= ClisitefTech();
    return _instance!;
  }

// Function to init payment and register handler from notify
  // Payment initPayment(IClisitefHandler handler) {
  //   _payment ??= Payment(iClisitefHandler: handler);
  //   return _payment!;
  // }

  Payment initPayment() {
    _payment ??= Payment();
    return _payment!;
  }

  Payment get payment {
    return _payment ?? initPayment();
  }
}
