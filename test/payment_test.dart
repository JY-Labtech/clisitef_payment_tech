import 'dart:convert';

import 'package:clisitef_payment_tech/clisitef_payment_tech.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

const _settings = SitefSettingsClisitef(
  serverIp: '127.0.0.1',
  storeCode: '00000000',
  posNumber: 'IP000001',
  additionalParameters: '[TipoPinPad=Android_AUTO]',
  enabledTransactions: '16;26',
);

const _credit = SitefTransactionClisitef(
  sitefModality: SitefModalityClisitef.creditCard,
  valuePayment: '10,00',
  couponNumber: '1',
  dateFiscal: '20261001',
  hourFiscal: '120000',
  operatorPos: 'op',
);

String _approved({int resultCode = 0, String nsuHost = '123456'}) => json.encode({
      'id': 1,
      'resultCode': resultCode,
      'tefNSUSitef': '000111',
      'tefNSUAuthorizing': nsuHost,
      'tefCodAuthorization': 'A1B2C3',
      'tefInstitution': 'VISA',
      'customerReceipt': 'via cliente',
      'establishmentReceipt': 'via loja',
    });

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  final calls = <MethodCall>[];

  void mockNative(Future<Object?>? Function(MethodCall call) handler) {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (call) async {
        calls.add(call);
        return handler(call);
      },
    );
  }

  setUp(() {
    calls.clear();
    ClisitefTech.resetForTesting();
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  test('TefHelper.priceSanitized usa vírgula decimal', () {
    expect(TefHelper.priceSanitized(10.5), '10,50');
  });

  group('initPayment', () {
    test('não chama o nativo e devolve a mesma instância de payment', () async {
      mockNative((call) async => null);

      final payment = await ClisitefTech.I.initPayment();

      expect(calls, isEmpty);
      expect(identical(payment, ClisitefTech.I.payment), isTrue);
      expect(payment.channel.name, 'clisitef_payment_tech');
    });

    test('payment funciona sem initPayment', () async {
      mockNative((call) async => 0);

      expect(await ClisitefTech.I.payment.configureClisitef(sitefSettings: _settings), 0);
      expect(calls.single.method, 'configureTef');
    });
  });

  group('configureClisitef', () {
    test('envia os parâmetros e devolve o código do SDK', () async {
      mockNative((call) async => 0);

      final result = await ClisitefTech.I.payment.configureClisitef(sitefSettings: _settings);

      expect(result, 0);
      expect(calls.single.method, 'configureTef');
      expect(calls.single.arguments, {
        'ipSitef': '127.0.0.1',
        'storeId': '00000000',
        'terminalId': 'IP000001',
        'parametrosAdc': '[TipoPinPad=Android_AUTO]',
      });
    });

    test('devolve código de erro do SDK', () async {
      mockNative((call) async => -5);
      expect(await ClisitefTech.I.payment.configureClisitef(sitefSettings: _settings), -5);
    });

    test('propaga PlatformException do nativo', () async {
      mockNative((call) async => throw PlatformException(code: 'NO_ACTIVITY'));

      await expectLater(
        ClisitefTech.I.payment.configureClisitef(sitefSettings: _settings),
        throwsA(isA<PlatformException>().having((e) => e.code, 'code', 'NO_ACTIVITY')),
      );
    });
  });

  group('clisitefTransaction', () {
    test('crédito: envia modalidade 3, restrições e devolve a resposta aprovada', () async {
      mockNative((call) async => _approved());

      final response = await ClisitefTech.I.payment.clisitefTransaction(
        sitefSettings: _settings,
        sitefTransaction: _credit,
      );

      expect(calls.single.method, 'startTransaction');
      expect(calls.single.arguments, {
        'modalidade': 3,
        'valor': '10,00',
        'cupomFiscal': '1',
        'dataFiscal': '20261001',
        'horario': '120000',
        'operador': 'op',
        'restricoes': '16;26',
        'ipSitef': '127.0.0.1',
        'storeId': '00000000',
        'terminalId': 'IP000001',
        'parametrosAdc': '[TipoPinPad=Android_AUTO]',
      });
      expect(response.resultCode, 0);
      expect(response.tefNSUSitef, '000111');
      expect(response.tefNSUAuthorizing, '123456');
      expect(response.tefCodAuthorization, 'A1B2C3');
      expect(response.customerReceipt, 'via cliente');
      expect(response.errorTransaction, isNull);
    });

    test('débito usa modalidade 2 com restrições', () async {
      mockNative((call) async => _approved());

      await ClisitefTech.I.payment.clisitefTransaction(
        sitefSettings: _settings,
        sitefTransaction: _credit.copyWith(sitefModality: SitefModalityClisitef.debitCard),
      );

      final args = calls.single.arguments as Map;
      expect(args['modalidade'], 2);
      expect(args['restricoes'], '16;26');
    });

    final otherModalities = {
      SitefModalityClisitef.pix: 122,
      SitefModalityClisitef.digitalWallet: 122,
      SitefModalityClisitef.cancellation: 200,
      SitefModalityClisitef.undefined: 0,
    };
    otherModalities.forEach((modality, code) {
      test('$modality usa modalidade $code sem restrições', () async {
        mockNative((call) async => _approved());

        await ClisitefTech.I.payment.clisitefTransaction(
          sitefSettings: _settings,
          sitefTransaction: _credit.copyWith(sitefModality: modality),
        );

        final args = calls.single.arguments as Map;
        expect(args['modalidade'], code);
        expect(args['restricoes'], '');
      });
    });

    test('aprovada sem NSU do host vira 99999', () async {
      mockNative((call) async => _approved(nsuHost: ''));

      final response = await ClisitefTech.I.payment.clisitefTransaction(
        sitefSettings: _settings,
        sitefTransaction: _credit,
      );

      expect(response.resultCode, 99999);
    });

    test('negada traz resultCode e errorTransaction', () async {
      mockNative((call) async => _approved(resultCode: 5));

      final response = await ClisitefTech.I.payment.clisitefTransaction(
        sitefSettings: _settings,
        sitefTransaction: _credit,
      );

      expect(response.resultCode, 5);
      expect(response.errorTransaction, isNotNull);
    });

    test('resultCode -1 reconfigura e tenta de novo', () async {
      var attempts = 0;
      mockNative((call) async {
        if (call.method == 'configureTef') return 1;
        attempts++;
        return attempts == 1 ? _approved(resultCode: -1) : _approved();
      });

      final response = await ClisitefTech.I.payment.clisitefTransaction(
        sitefSettings: _settings,
        sitefTransaction: _credit,
      );

      expect(calls.map((c) => c.method), ['startTransaction', 'configureTef', 'startTransaction']);
      expect(response.resultCode, 0);
    });

    test('propaga CONFIGURE_ERROR quando o configure falha no nativo', () async {
      mockNative((call) async => throw PlatformException(code: 'CONFIGURE_ERROR', message: 'código 3', details: 3));

      await expectLater(
        ClisitefTech.I.payment.clisitefTransaction(sitefSettings: _settings, sitefTransaction: _credit),
        throwsA(isA<PlatformException>()
            .having((e) => e.code, 'code', 'CONFIGURE_ERROR')
            .having((e) => e.details, 'details', 3)),
      );
      expect(calls, hasLength(1));
    });

    test('propaga PlatformException do nativo', () async {
      mockNative((call) async => throw PlatformException(code: 'UNEXPECTED_ERROR', message: 'falha'));

      await expectLater(
        ClisitefTech.I.payment.clisitefTransaction(sitefSettings: _settings, sitefTransaction: _credit),
        throwsA(isA<PlatformException>().having((e) => e.code, 'code', 'UNEXPECTED_ERROR')),
      );
    });
  });
}
