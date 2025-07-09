import '../enum/sitef_modality.dart';

class SitefTransactionClisitef {
  final String dateFiscal;
  final String hourFiscal;
  final String? couponNumber;
  final String valuePayment;
  final String operatorPos;
  final String additionalParameters;
  final SitefModalityClisitef sitefModality;

  const SitefTransactionClisitef({
    this.dateFiscal = '',
    this.hourFiscal = '',
    this.couponNumber = '',
    this.valuePayment = '',
    this.operatorPos = '',
    this.additionalParameters = '',
    this.sitefModality = SitefModalityClisitef.undefined,
  });

  SitefTransactionClisitef copyWith({
    String? dateFiscal,
    String? hourFiscal,
    String? couponNumber,
    String? valuePayment,
    String? operatorPos,
    String? additionalParameters,
    SitefModalityClisitef? sitefModality,
  }) =>
      SitefTransactionClisitef(
        dateFiscal: dateFiscal ?? this.dateFiscal,
        hourFiscal: hourFiscal ?? this.hourFiscal,
        couponNumber: couponNumber ?? this.couponNumber,
        valuePayment: valuePayment ?? this.valuePayment,
        operatorPos: operatorPos ?? this.operatorPos,
        additionalParameters: additionalParameters ?? this.additionalParameters,
        sitefModality: sitefModality ?? this.sitefModality,
      );
}
