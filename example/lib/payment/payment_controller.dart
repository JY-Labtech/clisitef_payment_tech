import 'package:clisitef_payment_tech/payments/handler/iclisitef_handler.dart';

class PaymentController extends IClisitefHandler {
  int saleValue = 0;
  bool enable = false;
  bool clickPayment = false;
  bool enableRefund = false;
  String? transactionCode;
  String? transactionId;
  String? response;

  void setSaleValue(double value) {
    if (value > 0.0) {
      saleValue = (value * 100).toInt();
      clickPayment = false;
      enable = true;
    } else {
      clickPayment = false;
      enable = false;
    }
  }

  void disposeDialog() {
    //BotToast.cleanAll();
  }

  void onAbortedSuccessfully() {}

  void onActivationDialog() {}

  @override
  void onAuthProgress(String message) {
    print('***onAuthProgress: $message');
  }

  @override
  void onError(String message) {
    print('***onError: $message');
  }

  @override
  void onMessage(String message) {
    print('***********onMessage: $message');
  }

  @override
  void onFinishedResponse(String message) {
    print('***onFinishedResponse: $message');
  }

  void onTransactionSuccess() {}

  void writeToFile({
    String? transactionCode,
    String? transactionId,
    String? response,
  }) {}

  @override
  void onLoading(bool show) {}

  void onTransactionInfo({
    String? transactionCode,
    String? transactionId,
    String? response,
  }) {
    this.transactionCode = transactionCode;
    this.transactionId = transactionId;
    this.response = response;

    enableRefund = true;
  }

  @override
  void onChanged(String message) {
    // TODO: implement onChanged
  }
}
