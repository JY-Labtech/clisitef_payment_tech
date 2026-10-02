import 'package:flutter/services.dart';

/// URL (base64) da validação de licença no jylabtech_core:
/// `https://licence-server.invalid/pos-payments/licence/check`.
///
/// É o valor padrão do `licenceInternalKey` em `initPayment`. O nativo sempre
/// valida no servidor oficial; valores que apontem para outro servidor são ignorados.
const String licenceCheck = '***REMOVIDO***';

// ignore: constant_identifier_names
const CHANNEL_NAME = "clisitef_payment_tech";
const channel = MethodChannel(CHANNEL_NAME);
