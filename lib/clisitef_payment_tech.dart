import 'dart:convert';

import 'package:check_licence_tech/check_licence_tech.dart';
import 'package:clisitef_payment_tech/licence/constants/licence_constants.dart';
import 'package:clisitef_payment_tech/payments/enum/sitef_modality.dart';
import 'package:clisitef_payment_tech/payments/helper/tef_helper.dart';
import 'package:clisitef_payment_tech/payments/model/json/tef_transaction_model.dart';
import 'package:clisitef_payment_tech/payments/model/sitef_settings.dart';
import 'package:clisitef_payment_tech/payments/model/sitef_transaction.dart';
import 'package:clisitef_payment_tech/payments/model/tef_transaction_response.dart';

class ClisitefTech {
  static final ClisitefTech _instance = ClisitefTech._internal();
  static ClisitefTech get instance => _instance;

  _Payment? _payment;

  ClisitefTech._internal();

  final _licence = CheckLicenceTech();

  Future<String> getSerialNumber() async => await _licence.getSerialNumber();

  Future<bool> _checkLicence({
    required String licenceKey,
  }) async {
    // Simulate a licence check
    // In a real implementation, this would involve an API call or similar
    if (licenceKey.isEmpty) {
      return false; // Invalid licence key
    }

    final resultLicence = await _licence.checkLicence(
      licenceKey,
      url: licenceCheck,
    );
    if (!resultLicence) {
      throw Exception('Invalid licence key. Please check your licence key.');
    }

    // Simulate a successful licence check
    return resultLicence;
  }

  Future<_Payment> initPayment({required String licenceKey}) async {
    final isValid = await _checkLicence(licenceKey: licenceKey);
    if (!isValid) {
      _payment = null;
      throw Exception('Invalid licence key. Please check your licence key.');
    }
    _payment = _Payment(isValid);
    return _payment!;
  }

  _Payment get payment {
    if (_payment == null || !_payment!._isValidLicence) {
      throw Exception('Payment não autorizado. Licença inválida.');
    }
    return _payment!;
  }
}

class _Payment {
  final bool _isValidLicence;
  _Payment(this._isValidLicence);

  final Map<SitefModalityClisitef, String> _modalityMap = {
    SitefModalityClisitef.undefined: '0',
    SitefModalityClisitef.creditCard: '3',
    SitefModalityClisitef.debitCard: '2',
    SitefModalityClisitef.pix: '122',
    SitefModalityClisitef.digitalWallet: '122',
    SitefModalityClisitef.cancellation: '200',
  };

  void _ensureValid() {
    if (!_isValidLicence) {
      throw Exception('Licença inválida. Acesso bloqueado.');
    }
  }

  TefTransactionResponseClisitef _fetchTefTransactionResponse({
    required TefTransactionModel model,
    String? dateTime,
  }) {
    int? resultCode;
    if (model.resultCode != null) {
      resultCode = model.resultCode;
      if (model.resultCode == 0 && (model.tefNSUAuthorizing?.isEmpty ?? true)) {
        resultCode = 99999;
      }
    }
    final errorTransaction = TefHelper.checkErrorTransactionTEF(model.resultCode!);

    return TefTransactionResponseClisitef(
      id: model.id ?? 0,
      tefCodPayment: model.tefCodPayment ?? '',
      tefDescPayment: model.tefDescPayment ?? '',
      tefInstitution: model.tefInstitution ?? '',
      tefCodInstitution: model.tefCodInstitution ?? '',
      tefCardType: model.tefCardType ?? '',
      binCard: model.binCard ?? '',
      tefDigitalWalletId: model.tefDigitalWalletId ?? '',
      tefDigitalWalletType: model.tefDigitalWalletType ?? '',
      tefDigitalWalletName: model.tefDigitalWalletName ?? '',
      tefNSUSitef: model.tefNSUSitef ?? '',
      tefNSUAuthorizing: model.tefNSUAuthorizing ?? '',
      tefCodAuthorization: model.tefCodAuthorization ?? '',
      establishmentReceipt: model.establishmentReceipt ?? '',
      customerReceipt: model.customerReceipt ?? '',
      transactionConfirmation: model.transactionConfirmation ?? '',
      couponModality: model.couponModality ?? '',
      transactionDateTime: dateTime ?? model.transactionDateTime ?? '',
      cancellationMode: model.cancellationMode ?? '',
      amountToPay: model.amountToPay ?? '',
      amountReceived: model.amountReceived ?? '',
      returnValue: model.returnValue ?? '',
      paymentAmount: model.paymentAmount ?? '',
      codCancellationMode: model.codCancellationMode ?? '',
      resultCode: resultCode ?? 999999,
      compDadosConf: model.compDadosConf ?? '',
      codTrans: model.codTrans ?? '',
      redeAut: model.redeAut ?? '',
      codeDocumentCanceled: model.codDocumentCancelled ?? '',
      amountCancelled: model.amountCancelled ?? '',
      cardNumber: model.cardNumber ?? '',
      cardNumberLast: model.cardNumberLast ?? '',
      errorTransaction: resultCode == 0 ? null : errorTransaction[resultCode],
      typeFields: model.typeFields,
    );
  }

  Future<int> configureClisitef({required SitefSettingsClisitef sitefSettings}) async {
    _ensureValid();
    final params = {
      'ipSitef': sitefSettings.serverIp,
      'storeId': sitefSettings.storeCode,
      'terminalId': sitefSettings.posNumber,
      'parametrosAdc': sitefSettings.additionalParameters,
    };
    final response = await channel.invokeMethod('configureTef', params);
    return response;
  }

  Future<TefTransactionResponseClisitef> clisitefTransaction({
    required SitefSettingsClisitef sitefSettings,
    required SitefTransactionClisitef sitefTransaction,
  }) async {
    _ensureValid();
    final modality = int.tryParse(_modalityMap[sitefTransaction.sitefModality] ?? '0') ?? 0;
    final restrictions = (modality == 3 || modality == 2) ? sitefSettings.enabledTransactions : '';
    final paramsTransaction = {
      'modalidade': modality,
      'valor': sitefTransaction.valuePayment,
      'cupomFiscal': sitefTransaction.couponNumber,
      'dataFiscal': sitefTransaction.dateFiscal,
      'horario': sitefTransaction.hourFiscal,
      'operador': sitefTransaction.operatorPos,
      'restricoes': restrictions,
      'ipSitef': sitefSettings.serverIp,
      'storeId': sitefSettings.storeCode,
      'terminalId': sitefSettings.posNumber,
      'parametrosAdc': sitefSettings.additionalParameters,
    };

    final resultTransaction = await channel.invokeMethod('startTransaction', paramsTransaction);
    final resultJson = json.decode(resultTransaction);
    final model = TefTransactionModel.fromJson(resultJson);

    if (model.resultCode != 0 || model.tefNSUAuthorizing?.isEmpty == true) {
      if (model.resultCode == -1) {
        final configure = await configureClisitef(sitefSettings: sitefSettings);
        if (configure != 0) {
          return await clisitefTransaction(sitefSettings: sitefSettings, sitefTransaction: sitefTransaction);
        }
      }
    }

    return _fetchTefTransactionResponse(model: model);
  }
}
