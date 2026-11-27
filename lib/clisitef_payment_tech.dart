/// Plugin Flutter para o TEF CliSiTef (Software Express / Fiserv) em Android.
library clisitef_payment_tech;

import 'package:flutter/foundation.dart';

import 'payments/payment.dart';

export 'payments/enum/sitef_modality.dart';
export 'payments/handler/iclisitef_handler.dart';
export 'payments/helper/tef_helper.dart';
export 'payments/model/sitef_settings.dart';
export 'payments/model/sitef_transaction.dart';
export 'payments/model/tef_transaction_response.dart';
export 'payments/payment.dart';

/// Ponto de entrada do plugin. Use a instância única [ClisitefTech.I].
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

  /// Cria (ou devolve) a instância de [Payment] ligada ao canal nativo.
  ///
  /// Não faz chamada ao nativo nem exige chave: basta chamar uma vez antes das
  /// operações, ou usar direto o getter [payment].
  Future<Payment> initPayment() async => payment;

  /// Instância de [Payment] usada nas operações de TEF.
  Payment get payment => _payment ??= Payment.create(channel: channel);
}
