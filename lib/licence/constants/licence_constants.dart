import 'package:flutter/services.dart';

/// URL (base64) da validação de licença no jylabtech_core:
/// `https://portal-api.jylabtech.com.br/pos-payments/licence/check`.
///
/// É o valor padrão do `licenceInternalKey` em `initPayment`. O nativo sempre
/// valida no servidor oficial; valores que apontem para outro servidor são ignorados.
const String licenceCheck = 'aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s=';

// ignore: constant_identifier_names
const CHANNEL_NAME = "clisitef_payment_tech";
const channel = MethodChannel(CHANNEL_NAME);
