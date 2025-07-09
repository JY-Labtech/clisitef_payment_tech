import 'package:clisitef_payment_tech/licence/licence_service.dart';
import 'payments/payment.dart';

class ClisitefTech {
  // Instância única (singleton)
  static final ClisitefTech _instance = ClisitefTech._internal();

  // Getter para acesso externo
  static ClisitefTech get instance => _instance;

  Payment? _payment;

  // Construtor privado
  ClisitefTech._internal();

  /// Inicializa o pagamento e valida a licença
  Future<Payment> initPayment({
    required String licenceKey,
  }) async {
    try {
      final isValid = await LicenceService().checkLicence(licenceKey: licenceKey);
      if (!isValid) {
        throw Exception('Invalid licence key. Please check your licence key.');
      }

      _payment = Payment();
      return _payment!;
    } catch (e) {
      print('Error initializing payment: $e');
      rethrow;
    }
  }

  /// Acesso ao pagamento já inicializado
  Payment get payment {
    if (!_isPaymentInitialized) {
      throw Exception(
        'Payment not initialized. Call initPayment() before accessing payment.',
      );
    }
    return _payment!;
  }

  bool get _isPaymentInitialized => _payment != null;
}
