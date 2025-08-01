// ignore_for_file: constant_identifier_names, public_member_api_docs, sort_constructors_first

import 'dart:convert';

import 'package:check_licence_tech/check_licence_tech.dart';
import 'package:flutter/services.dart';

import 'package:clisitef_payment_tech/licence/constants/licence_constants.dart';

import 'enum/sitef_modality.dart';
import 'helper/tef_helper.dart';
import 'model/json/tef_transaction_model.dart';
import 'model/sitef_settings.dart';
import 'model/sitef_transaction.dart';
import 'model/tef_transaction_response.dart';

const CHANNEL_NAME = "clisitef_payment_tech";

final class Payment {
  final MethodChannel channel;
  final String licenceKey;

  final CheckLicenceTech _licence = CheckLicenceTech.instance();

  // Cache da instância criada
  static Payment? _instance;
  static bool _isLicenceValidated = false;

  Payment._({
    required this.channel,
    required this.licenceKey,
  });

  static Future<Payment> create({
    required MethodChannel channel,
    required String licenceKey,
  }) async {
    // Se já existe instância e licença validada, retorna ela diretamente
    if (_instance != null && _isLicenceValidated) {
      return _instance!;
    }

    final payment = Payment._(
      channel: channel,
      licenceKey: licenceKey,
    );

    _isLicenceValidated = await payment._checkLicence();
    if (!_isLicenceValidated) {
      _instance = null; // Limpa a instância se a licença não for válida
      _isLicenceValidated = false;
      throw Exception('Invalid licence key. Please check your licence key.');
    }
    _instance = payment;
    _isLicenceValidated = true;
    return payment;
  }

  Future<bool> _checkLicence() async {
    if (licenceKey.isEmpty) return false;
    return await _licence.checkLicence(licenceKey, url: licenceCheck);
  }

  void _ensureLicence() {
    if (!_isLicenceValidated) {
      throw Exception('Invalid licence key. Please check your licence key.');
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
    _ensureLicence();
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
    _ensureLicence();
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
