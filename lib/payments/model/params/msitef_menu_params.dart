// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'dart:convert';

class MSitefMenuParams {
  final String? ipSitef;
  final String? storeId;
  final String? terminalId;
  final String? paramsAdd;
  final String? companyDocument;
  final String? comExternal;
  final String? isDoubleValidation;
  final String? otp;
  final String? enabledTransactions;
  final String? automationDocument;
  final String? tokenRegistroTls;

  MSitefMenuParams({
    this.ipSitef,
    this.storeId,
    this.terminalId,
    this.paramsAdd,
    this.companyDocument,
    this.comExternal,
    this.isDoubleValidation,
    this.otp,
    this.enabledTransactions,
    this.automationDocument,
    this.tokenRegistroTls,
  });

  MSitefMenuParams copyWith({
    String? ipSitef,
    String? storeId,
    String? terminalId,
    String? paramsAdd,
    String? companyDocument,
    String? comExternal,
    String? isDoubleValidation,
    String? otp,
    String? enabledTransactions,
    String? automationDocument,
    String? tokenRegistroTls,
  }) {
    return MSitefMenuParams(
      ipSitef: ipSitef ?? this.ipSitef,
      storeId: storeId ?? this.storeId,
      terminalId: terminalId ?? this.terminalId,
      paramsAdd: paramsAdd ?? this.paramsAdd,
      companyDocument: companyDocument ?? this.companyDocument,
      comExternal: comExternal ?? this.comExternal,
      isDoubleValidation: isDoubleValidation ?? this.isDoubleValidation,
      otp: otp ?? this.otp,
      enabledTransactions: enabledTransactions ?? this.enabledTransactions,
      automationDocument: automationDocument ?? this.automationDocument,
      tokenRegistroTls: tokenRegistroTls ?? this.tokenRegistroTls,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'ipSitef': ipSitef,
      'storeId': storeId,
      'terminalId': terminalId,
      'paramsAdd': paramsAdd,
      'companyDocument': companyDocument,
      'comExternal': comExternal,
      'isDoubleValidation': isDoubleValidation,
      'otp': otp,
      'enabledTransactions': enabledTransactions,
      'automationDocument': automationDocument,
      'tokenRegistroTls': tokenRegistroTls,
    };
  }

  factory MSitefMenuParams.fromMap(Map<String, dynamic> map) {
    return MSitefMenuParams(
      ipSitef: map['ipSitef'] != null ? map['ipSitef'] as String : null,
      storeId: map['storeId'] != null ? map['storeId'] as String : null,
      terminalId: map['terminalId'] != null ? map['terminalId'] as String : null,
      paramsAdd: map['paramsAdd'] != null ? map['paramsAdd'] as String : null,
      companyDocument: map['companyDocument'] != null ? map['companyDocument'] as String : null,
      comExternal: map['comExternal'] != null ? map['comExternal'] as String : null,
      isDoubleValidation: map['isDoubleValidation'] != null ? map['isDoubleValidation'] as String : null,
      otp: map['otp'] != null ? map['otp'] as String : null,
      enabledTransactions: map['enabledTransactions'] != null ? map['enabledTransactions'] as String : null,
      automationDocument: map['automationDocument'] != null ? map['automationDocument'] as String : null,
      tokenRegistroTls: map['tokenRegistroTls'] != null ? map['tokenRegistroTls'] as String : null,
    );
  }

  String toJson() => json.encode(toMap());

  factory MSitefMenuParams.fromJson(String source) => MSitefMenuParams.fromMap(json.decode(source) as Map<String, dynamic>);

  @override
  String toString() {
    return 'MSitefMenuParams(ipSitef: $ipSitef, storeId: $storeId, terminalId: $terminalId, paramsAdd: $paramsAdd, companyDocument: $companyDocument, comExternal: $comExternal, isDoubleValidation: $isDoubleValidation, otp: $otp, enabledTransactions: $enabledTransactions, automationDocument: $automationDocument, tokenRegistroTls: $tokenRegistroTls)';
  }

  @override
  bool operator ==(covariant MSitefMenuParams other) {
    if (identical(this, other)) return true;

    return other.ipSitef == ipSitef && other.storeId == storeId && other.terminalId == terminalId && other.paramsAdd == paramsAdd && other.companyDocument == companyDocument && other.comExternal == comExternal && other.isDoubleValidation == isDoubleValidation && other.otp == otp && other.enabledTransactions == enabledTransactions && other.automationDocument == automationDocument && other.tokenRegistroTls == tokenRegistroTls;
  }

  @override
  int get hashCode {
    return ipSitef.hashCode ^ storeId.hashCode ^ terminalId.hashCode ^ paramsAdd.hashCode ^ companyDocument.hashCode ^ comExternal.hashCode ^ isDoubleValidation.hashCode ^ otp.hashCode ^ enabledTransactions.hashCode ^ automationDocument.hashCode ^ tokenRegistroTls.hashCode;
  }
}
