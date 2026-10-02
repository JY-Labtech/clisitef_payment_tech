// ignore_for_file: constant_identifier_names, public_member_api_docs, sort_constructors_first

import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import '../licence/constants/licence_constants.dart' show licenceCheck;

import 'enum/sitef_modality.dart';
import 'helper/tef_helper.dart';
import 'model/json/tef_transaction_model.dart';
import 'model/sitef_settings.dart';
import 'model/sitef_transaction.dart';
import 'model/tef_transaction_response.dart';

const CHANNEL_NAME = "clisitef_payment_tech";

final class Payment {
  final MethodChannel channel;

  static Payment? _instance;

  bool _licenceValidated = false;
  String? _lastLicenceKey;
  String? _lastLicenceInternalKey;
  Future<void>? _initializing;

  Payment._({
    required this.channel,
  });

  static Payment create({
    required MethodChannel channel,
  }) {
    if (_instance != null) {
      return _instance!;
    }

    final payment = Payment._(channel: channel);
    _instance = payment;
    return payment;
  }

  /// Limpa a instância compartilhada e o estado da licença (uso em testes).
  @visibleForTesting
  static void resetForTesting() {
    _instance = null;
  }

  Future<void> ensureInitialized({
    required String licenceKey,
    String licenceInternalKey = licenceCheck,
  }) async {
    if (_licenceValidated &&
        _lastLicenceKey == licenceKey &&
        _lastLicenceInternalKey == licenceInternalKey) {
      return;
    }

    if (_initializing != null) {
      await _initializing;
      if (_licenceValidated &&
          _lastLicenceKey == licenceKey &&
          _lastLicenceInternalKey == licenceInternalKey) {
        return;
      }
    }

    _initializing = _initializeLicence(
      licenceKey: licenceKey,
      licenceInternalKey: licenceInternalKey,
    );

    _licenceValidated = false;
    try {
      await _initializing;
      _licenceValidated = true;
      _lastLicenceKey = licenceKey;
      _lastLicenceInternalKey = licenceInternalKey;
    } finally {
      _initializing = null;
    }
  }

  Future<void> _initializeLicence({
    required String licenceKey,
    required String licenceInternalKey,
  }) async {
    final bool success = await channel.invokeMethod<bool>(
          'initialize',
          {
            'licenceKey': licenceKey,
            'licenceInternalKey': licenceInternalKey,
          },
        ) ??
        false;

    if (!success) {
      throw PlatformException(
        code: 'LICENCE_ERROR',
        message: 'Falha ao validar licença no dispositivo.',
      );
    }
  }

  final Map<SitefModalityClisitef, String> _modalityMap = {
    SitefModalityClisitef.undefined: '0',
    SitefModalityClisitef.creditCard: '3',
    SitefModalityClisitef.debitCard: '2',
    SitefModalityClisitef.pix: '122',
    SitefModalityClisitef.digitalWallet: '122',
    SitefModalityClisitef.cancellation: '200',
  };

  TefTransactionResponseClisitef _fetchTefTransactionResponse({
    required TefTransactionModel model,
    String? dateTime,
  }) {
    int? resultCode;
    if (model.resultCode != null) {
      resultCode = model.resultCode;
      if (model.resultCode == 0 && (model.tefNSUAuthorizing == '' || model.tefNSUAuthorizing == null)) {
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

  Future<int> configureClisitef({
    required SitefSettingsClisitef sitefSettings,
  }) async {
    if (!_licenceValidated) {
      throw PlatformException(
        code: 'LICENCE_NOT_INITIALIZED',
        message: 'Chame ClisitefTech.I.initPayment antes de configurar o TEF.',
      );
    }
    Map<String, dynamic> params = {
      'ipSitef': sitefSettings.serverIp,
      'storeId': sitefSettings.storeCode,
      'terminalId': sitefSettings.posNumber,
      'parametrosAdc': sitefSettings.additionalParameters,
    };
    try {
      final int response = await channel.invokeMethod('configureTef', params);
      return response;
    } catch (e) {
      rethrow;
    }
  }

  Future<TefTransactionResponseClisitef> clisitefTransaction({
    required SitefSettingsClisitef sitefSettings,
    required SitefTransactionClisitef sitefTransaction,
  }) async {
    if (!_licenceValidated) {
      throw PlatformException(
        code: 'LICENCE_NOT_INITIALIZED',
        message: 'Chame ClisitefTech.I.initPayment antes de iniciar transações.',
      );
    }
    final modality = int.tryParse(_modalityMap[sitefTransaction.sitefModality] ?? '0') ?? 0;

    String restrictions = '';
    if (modality == 3 || modality == 2) {
      restrictions = sitefSettings.enabledTransactions;
    }

    Map<String, dynamic> paramsTransaction = {
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

    try {
      String resultTransaction = await channel.invokeMethod('startTransaction', paramsTransaction);
      final resultJson = json.decode(resultTransaction);
      final model = TefTransactionModel.fromJson(resultJson);

      if (model.resultCode != 0 || model.tefNSUAuthorizing == '') {
        if (model.resultCode == -1) {
          final configure = await configureClisitef(sitefSettings: sitefSettings);
          if (configure != 0) {
            return await clisitefTransaction(sitefSettings: sitefSettings, sitefTransaction: sitefTransaction);
          }
        }
      }
      return _fetchTefTransactionResponse(model: model);
    } catch (e) {
      rethrow;
    }
  }
}
