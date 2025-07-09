// ignore_for_file: public_member_api_docs, sort_constructors_first
import 'dart:convert';

class SitefSettingsClisitef {
  final String serverIp;
  final String storeCode;
  final String posNumber;
  final String additionalParameters;
  final String operatorPos;
  final String companyDocument;
  final String lib;
  final String comExternal;
  final String isDoubleValidation;
  final String otp;
  final String enabledTransactions;
  final String automationDocument;

  const SitefSettingsClisitef({
    this.serverIp = '',
    this.storeCode = '',
    this.posNumber = '',
    this.additionalParameters = '',
    this.operatorPos = '',
    this.companyDocument = '',
    this.lib = '',
    this.comExternal = '',
    this.isDoubleValidation = '',
    this.otp = '',
    this.enabledTransactions = '',
    this.automationDocument = '',
  });

  SitefSettingsClisitef copyWith({
    String? serverIp,
    String? storeCode,
    String? posNumber,
    String? additionalParameters,
    String? operatorPos,
    String? companyDocument,
    String? lib,
    String? comExternal,
    String? isDoubleValidation,
    String? otp,
    String? enabledTransactions,
    String? automationDocument,
  }) {
    return SitefSettingsClisitef(
      serverIp: serverIp ?? this.serverIp,
      storeCode: storeCode ?? this.storeCode,
      posNumber: posNumber ?? this.posNumber,
      additionalParameters: additionalParameters ?? this.additionalParameters,
      operatorPos: operatorPos ?? this.operatorPos,
      companyDocument: companyDocument ?? this.companyDocument,
      lib: lib ?? this.lib,
      comExternal: comExternal ?? this.comExternal,
      isDoubleValidation: isDoubleValidation ?? this.isDoubleValidation,
      otp: otp ?? this.otp,
      enabledTransactions: enabledTransactions ?? this.enabledTransactions,
      automationDocument: automationDocument ?? this.automationDocument,
    );
  }

  Map<String, dynamic> toMap() {
    return <String, dynamic>{
      'serverIp': serverIp,
      'storeCode': storeCode,
      'posNumber': posNumber,
      'additionalParameters': additionalParameters,
      'operatorPos': operatorPos,
      'companyDocument': companyDocument,
      'lib': lib,
      'comExternal': comExternal,
      'isDoubleValidation': isDoubleValidation,
      'otp': otp,
      'enabledTransactions': enabledTransactions,
      'automationDocument': automationDocument,
    };
  }

  factory SitefSettingsClisitef.fromMap(Map<String, dynamic> map) {
    return SitefSettingsClisitef(
      serverIp: map['serverIp'] ?? '',
      storeCode: map['storeCode'] ?? '',
      posNumber: map['posNumber'] ?? '',
      additionalParameters: map['additionalParameters'] ?? '',
      operatorPos: map['operatorPos'] ?? '',
      companyDocument: map['companyDocument'] ?? '',
      lib: map['lib'] ?? '',
      comExternal: map['comExternal'] ?? '',
      isDoubleValidation: map['isDoubleValidation'] ?? '',
      otp: map['otp'] ?? '',
      enabledTransactions: map['enabledTransactions'] ?? '',
      automationDocument: map['automationDocument'] ?? '',
    );
  }

  String toJson() => json.encode(toMap());

  factory SitefSettingsClisitef.fromJson(String source) => SitefSettingsClisitef.fromMap(json.decode(source) as Map<String, dynamic>);
}
