import 'package:json_annotation/json_annotation.dart';

part 'tef_transaction_model.g.dart';

@JsonSerializable()
class TefTransactionModel {
  @JsonKey(name: 'id')
  final int? id;
  @JsonKey(name: 'tefCodPayment')
  final String? tefCodPayment;
  @JsonKey(name: 'tefDescPayment')
  final String? tefDescPayment;
  @JsonKey(name: 'tefInstitution')
  final String? tefInstitution;
  @JsonKey(name: 'tefCodInstitution')
  final String? tefCodInstitution;
  @JsonKey(name: 'tefCardType')
  final String? tefCardType;
  @JsonKey(name: 'binCard')
  final String? binCard;
  @JsonKey(name: 'tefDigitalWalletId')
  final String? tefDigitalWalletId;
  @JsonKey(name: 'tefDigitalWalletType')
  final String? tefDigitalWalletType;
  @JsonKey(name: 'tefDigitalWalletName')
  final String? tefDigitalWalletName;
  @JsonKey(name: 'tefNSUSitef')
  final String? tefNSUSitef;
  @JsonKey(name: 'tefNSUAuthorizing')
  final String? tefNSUAuthorizing;
  @JsonKey(name: 'tefCodAuthorization')
  final String? tefCodAuthorization;
  @JsonKey(name: 'establishmentReceipt')
  final String? establishmentReceipt;
  @JsonKey(name: 'customerReceipt')
  final String? customerReceipt;
  @JsonKey(name: 'transactionConfirmation')
  final String? transactionConfirmation;
  @JsonKey(name: 'couponModality')
  final String? couponModality;
  @JsonKey(name: 'transactionDateTime')
  final String? transactionDateTime;
  @JsonKey(name: 'cancellationMode')
  final String? cancellationMode;
  @JsonKey(name: 'amountToPay')
  final String? amountToPay;
  @JsonKey(name: 'amountReceived')
  final String? amountReceived;
  @JsonKey(name: 'returnValue')
  final String? returnValue;
  @JsonKey(name: 'paymentAmount')
  final String? paymentAmount;
  @JsonKey(name: 'codCancellationMode')
  final String? codCancellationMode;
  @JsonKey(name: 'resultCode')
  final int? resultCode;
  @JsonKey(name: 'compDadosConf')
  final String? compDadosConf;
  @JsonKey(name: 'codTrans')
  final String? codTrans;
  @JsonKey(name: 'redeAut')
  final String? redeAut;
  @JsonKey(name: 'codResposta')
  final String? codResposta;
  @JsonKey(name: 'typeFields')
  final String? typeFields;
  @JsonKey(name: 'codDocumentCancelled')
  final String? codDocumentCancelled;
  @JsonKey(name: 'amountCancelled')
  final String? amountCancelled;
  @JsonKey(name: 'cardNumber')
  final String? cardNumber;
  @JsonKey(name: 'cardNumberLast')
  final String? cardNumberLast;

  TefTransactionModel({
    this.id,
    this.tefCodPayment,
    this.tefDescPayment,
    this.tefInstitution,
    this.tefCodInstitution,
    this.tefCardType,
    this.binCard,
    this.tefDigitalWalletId,
    this.tefDigitalWalletType,
    this.tefDigitalWalletName,
    this.tefNSUSitef,
    this.tefNSUAuthorizing,
    this.tefCodAuthorization,
    this.establishmentReceipt,
    this.customerReceipt,
    this.transactionConfirmation,
    this.couponModality,
    this.transactionDateTime,
    this.cancellationMode,
    this.amountToPay,
    this.amountReceived,
    this.returnValue,
    this.paymentAmount,
    this.codCancellationMode,
    this.resultCode,
    this.compDadosConf,
    this.redeAut,
    this.codTrans,
    this.codResposta,
    this.typeFields,
    this.codDocumentCancelled,
    this.amountCancelled,
    this.cardNumber,
    this.cardNumberLast,
  });

  factory TefTransactionModel.fromJson(Map<String, dynamic> json) =>
      _$TefTransactionModelFromJson(json);

  Map<String, dynamic> toJson() => _$TefTransactionModelToJson(this);
}
