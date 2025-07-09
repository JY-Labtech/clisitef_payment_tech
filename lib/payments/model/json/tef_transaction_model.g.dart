// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'tef_transaction_model.dart';

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TefTransactionModel _$TefTransactionModelFromJson(Map<String, dynamic> json) =>
    TefTransactionModel(
      id: json['id'] as int?,
      tefCodPayment: json['tefCodPayment'] as String?,
      tefDescPayment: json['tefDescPayment'] as String?,
      tefInstitution: json['tefInstitution'] as String?,
      tefCodInstitution: json['tefCodInstitution'] as String?,
      tefCardType: json['tefCardType'] as String?,
      binCard: json['binCard'] as String?,
      tefDigitalWalletId: json['tefDigitalWalletId'] as String?,
      tefDigitalWalletType: json['tefDigitalWalletType'] as String?,
      tefDigitalWalletName: json['tefDigitalWalletName'] as String?,
      tefNSUSitef: json['tefNSUSitef'] as String?,
      tefNSUAuthorizing: json['tefNSUAuthorizing'] as String?,
      tefCodAuthorization: json['tefCodAuthorization'] as String?,
      establishmentReceipt: json['establishmentReceipt'] as String?,
      customerReceipt: json['customerReceipt'] as String?,
      transactionConfirmation: json['transactionConfirmation'] as String?,
      couponModality: json['couponModality'] as String?,
      transactionDateTime: json['transactionDateTime'] as String?,
      cancellationMode: json['cancellationMode'] as String?,
      amountToPay: json['amountToPay'] as String?,
      amountReceived: json['amountReceived'] as String?,
      returnValue: json['returnValue'] as String?,
      paymentAmount: json['paymentAmount'] as String?,
      codCancellationMode: json['codCancellationMode'] as String?,
      resultCode: json['resultCode'] as int?,
      compDadosConf: json['compDadosConf'] as String?,
      redeAut: json['redeAut'] as String?,
      codTrans: json['codTrans'] as String?,
      codResposta: json['codResposta'] as String?,
      typeFields: json['typeFields'] as String?,
      codDocumentCancelled: json['codDocumentCancelled'] as String?,
      amountCancelled: json['amountCancelled'] as String?,
      cardNumber: json['cardNumber'] as String?,
      cardNumberLast: json['cardNumberLast'] as String?,
    );

Map<String, dynamic> _$TefTransactionModelToJson(
        TefTransactionModel instance) =>
    <String, dynamic>{
      'id': instance.id,
      'tefCodPayment': instance.tefCodPayment,
      'tefDescPayment': instance.tefDescPayment,
      'tefInstitution': instance.tefInstitution,
      'tefCodInstitution': instance.tefCodInstitution,
      'tefCardType': instance.tefCardType,
      'binCard': instance.binCard,
      'tefDigitalWalletId': instance.tefDigitalWalletId,
      'tefDigitalWalletType': instance.tefDigitalWalletType,
      'tefDigitalWalletName': instance.tefDigitalWalletName,
      'tefNSUSitef': instance.tefNSUSitef,
      'tefNSUAuthorizing': instance.tefNSUAuthorizing,
      'tefCodAuthorization': instance.tefCodAuthorization,
      'establishmentReceipt': instance.establishmentReceipt,
      'customerReceipt': instance.customerReceipt,
      'transactionConfirmation': instance.transactionConfirmation,
      'couponModality': instance.couponModality,
      'transactionDateTime': instance.transactionDateTime,
      'cancellationMode': instance.cancellationMode,
      'amountToPay': instance.amountToPay,
      'amountReceived': instance.amountReceived,
      'returnValue': instance.returnValue,
      'paymentAmount': instance.paymentAmount,
      'codCancellationMode': instance.codCancellationMode,
      'resultCode': instance.resultCode,
      'compDadosConf': instance.compDadosConf,
      'codTrans': instance.codTrans,
      'redeAut': instance.redeAut,
      'codResposta': instance.codResposta,
      'typeFields': instance.typeFields,
      'codDocumentCancelled': instance.codDocumentCancelled,
      'amountCancelled': instance.amountCancelled,
      'cardNumber': instance.cardNumber,
      'cardNumberLast': instance.cardNumberLast,
    };
