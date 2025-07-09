// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'dart:convert';

import 'package:flutter/services.dart';

import 'enum/sitef_modality.dart';
import 'helper/tef_helper.dart';
import 'model/json/tef_transaction_model.dart';
import 'model/sitef_settings.dart';
import 'model/sitef_transaction.dart';
import 'model/tef_transaction_response.dart';

//Fixed channel name
// ignore: constant_identifier_names
const CHANNEL_NAME = "clisitef_payment_tech";

class Payment {
  // final IClisitefHandler iClisitefHandler;

  final channel = const MethodChannel(CHANNEL_NAME);

  // Payment({
  // }) {
  //   // channel.setMethodCallHandler((e) => IClisitefHelper.callHandler(
  //   //       call: e,
  //   //       iClisitefHandler: iClisitefHandler,
  //   //     ));
  // }

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
      //resultCode: model.resultCode ?? 0,
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
    int aux = 0;
    Map<String, dynamic> lineConfigureTef({
      String? ipSitef,
      String? storeId,
      String? terminalId,
      String? paramsAdd,
    }) {
      return {
        'ipSitef': ipSitef,
        'storeId': storeId,
        'terminalId': terminalId,
        'parametrosAdc': paramsAdd,
      };
    }

    final params = lineConfigureTef(
      ipSitef: sitefSettings.serverIp,
      storeId: sitefSettings.storeCode,
      terminalId: sitefSettings.posNumber,
      paramsAdd: sitefSettings.additionalParameters,
    );

    try {
      final int response = await channel.invokeMethod(
        'configureTef',
        params,
      );

      aux = response;
      return aux;
    } catch (e) {
      rethrow;
    }
  }

  Future<TefTransactionResponseClisitef> clisitefTransaction({
    required SitefSettingsClisitef sitefSettings,
    required SitefTransactionClisitef sitefTransaction,
  }) async {
    try {
      Map<String, dynamic> lineToMethodChannel({
        int? modalidade,
        String? value,
        String? couponNumber,
        String? dateFiscal,
        String? hourFiscal,
        String? operador,
        String? params,
        String? ipSitef,
        String? storeId,
        String? terminalId,
        String? paramsAdd,
      }) {
        return {
          'modalidade': modalidade,
          'valor': value,
          'cupomFiscal': couponNumber,
          'dataFiscal': dateFiscal,
          'horario': hourFiscal,
          'operador': operador,
          'restricoes': params,
          'ipSitef': ipSitef,
          'storeId': storeId,
          'terminalId': terminalId,
          'parametrosAdc': paramsAdd,
        };
      }

      final modality = int.tryParse(_modalityMap[sitefTransaction.sitefModality] ?? '0') ?? 0;

      String restrictions = '';

      switch (modality) {
        case 3:
          restrictions = sitefSettings.enabledTransactions;
          break;
        case 2:
          restrictions = sitefSettings.enabledTransactions;
          break;
        default:
          restrictions = '';
      }

      final paramsTransaction = lineToMethodChannel(
        modalidade: modality,
        value: sitefTransaction.valuePayment,
        couponNumber: sitefTransaction.couponNumber,
        dateFiscal: sitefTransaction.dateFiscal,
        hourFiscal: sitefTransaction.hourFiscal,
        operador: sitefTransaction.operatorPos,
        params: restrictions,
        ipSitef: sitefSettings.serverIp,
        storeId: sitefSettings.storeCode,
        terminalId: sitefSettings.posNumber,
        paramsAdd: sitefSettings.additionalParameters,
      );

      String resultTransaction = await channel.invokeMethod(
        'startTransaction',
        paramsTransaction,
      );

      final resultJson = json.decode(resultTransaction);

      final model = TefTransactionModel.fromJson(resultJson);

      if (model.resultCode != 0 || model.tefNSUAuthorizing == '') {
        final resultCode = model.resultCode;

        if (resultCode == -1) {
          final configure = await configureClisitef(sitefSettings: sitefSettings);
          if (configure != 0) {
            return await clisitefTransaction(sitefSettings: sitefSettings, sitefTransaction: sitefTransaction);
          }
        }
      }

      return _fetchTefTransactionResponse(
        model: model,
      );
    } catch (e) {
      rethrow;
    }
  }
}
