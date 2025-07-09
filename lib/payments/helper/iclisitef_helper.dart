import 'package:clisitef_payment_tech/payments/handler/iclisitef_handler.dart';
import 'package:flutter/services.dart';

import '../utils/payment_types.dart';

abstract class IClisitefHelper {
  static Future<dynamic> callHandler({
    required MethodCall call,
    required IClisitefHandler iClisitefHandler,
  }) async {
    switch (call.method.handler) {
      case PaymentTypeHandler.ON_TRANSACTION_SUCCESS:
        {}
        break;
      case PaymentTypeHandler.ON_ERROR:
        {
          iClisitefHandler.onError(call.arguments);
        }
        break;
      case PaymentTypeHandler.ON_MESSAGE:
        {
          iClisitefHandler.onMessage(call.arguments);
        }
        break;
      case PaymentTypeHandler.ON_FINISHED_RESPONSE:
        {
          iClisitefHandler.onFinishedResponse(call.arguments);
        }
        break;
      case PaymentTypeHandler.ON_CHANGED:
        {
          iClisitefHandler.onChanged(call.arguments);
        }
        break;
      case PaymentTypeHandler.ON_LOADING:
        {
          iClisitefHandler.onLoading(call.arguments);
        }
        break;
      case PaymentTypeHandler.ON_AUTH_PROGRESS:
        {
          iClisitefHandler.onAuthProgress(call.arguments);
        }
        break;
      default:
        throw "METHOD NOT IMPLEMENTED";
    }
    return true;
  }
}
