import 'dart:convert';

import 'package:clisitef_payment_tech/clisitef_payment_tech.dart';
import 'package:clisitef_payment_tech/licence/constants/licence_constants.dart';
import 'package:clisitef_payment_tech/payments/enum/sitef_modality.dart';
import 'package:clisitef_payment_tech/payments/model/sitef_settings.dart';
import 'package:clisitef_payment_tech/payments/model/sitef_transaction.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

const _testKey = 'JYL-TEST-0000-0000';

const _settings = SitefSettingsClisitef(
  serverIp: '127.0.0.1',
  storeCode: '00000000',
  posNumber: 'IP000001',
);

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  final calls = <MethodCall>[];
  Future<Object?>? Function(MethodCall call)? nativeHandler;

  void mockNative(Future<Object?>? Function(MethodCall call) handler) {
    nativeHandler = handler;
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(
      channel,
      (call) async {
        calls.add(call);
        return nativeHandler!(call);
      },
    );
  }

  PlatformException licenceError([String message = 'Licença vencida']) =>
      PlatformException(code: 'LICENCE_ERROR', message: message);

  setUp(() {
    calls.clear();
    ClisitefTech.resetForTesting();
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger.setMockMethodCallHandler(channel, null);
  });

  group('licenceCheck', () {
    test('decodifica para a URL do servidor oficial (core)', () {
      final url = utf8.decode(base64.decode(licenceCheck));
      expect(url, startsWith('https://licence-server.invalid/pos-payments/'));
      expect(url, 'https://licence-server.invalid/pos-payments/licence/check');
      final uri = Uri.parse(url);
      expect(uri.scheme, 'https');
      expect(uri.host, 'licence-server.invalid');
    });
  });

  group('initPayment', () {
    test('envia licenceKey e licenceInternalKey padrão (core) quando omitido', () async {
      mockNative((call) async => true);

      await ClisitefTech.I.initPayment(licenceKey: _testKey);

      expect(calls, hasLength(1));
      expect(calls.single.method, 'initialize');
      expect(calls.single.arguments, {
        'licenceKey': _testKey,
        'licenceInternalKey': licenceCheck,
      });
    });

    test('envia o licenceInternalKey informado', () async {
      mockNative((call) async => true);
      const custom = '***REMOVIDO***';

      await ClisitefTech.I.initPayment(licenceKey: _testKey, licenceInternalKey: custom);

      expect((calls.single.arguments as Map)['licenceInternalKey'], custom);
    });

    test('propaga LICENCE_ERROR do nativo e não libera o plugin', () async {
      mockNative((call) async => throw licenceError());

      await expectLater(
        ClisitefTech.I.initPayment(licenceKey: _testKey),
        throwsA(isA<PlatformException>().having((e) => e.code, 'code', 'LICENCE_ERROR')),
      );
      expect(() => ClisitefTech.I.payment, throwsException);
    });

    test('resposta diferente de true é tratada como licença inválida', () async {
      mockNative((call) async => false);

      await expectLater(
        ClisitefTech.I.initPayment(licenceKey: _testKey),
        throwsA(isA<PlatformException>().having((e) => e.code, 'code', 'LICENCE_ERROR')),
      );
    });

    test('nova validação com falha bloqueia operações seguintes', () async {
      mockNative((call) async => true);
      final payment = await ClisitefTech.I.initPayment(licenceKey: _testKey);

      mockNative((call) async => throw licenceError('Licença cancelada'));
      await expectLater(
        ClisitefTech.I.initPayment(licenceKey: 'OUTRA-CHAVE'),
        throwsA(isA<PlatformException>()),
      );

      calls.clear();
      await expectLater(
        payment.configureClisitef(sitefSettings: _settings),
        throwsA(isA<PlatformException>().having((e) => e.code, 'code', 'LICENCE_NOT_INITIALIZED')),
      );
      expect(calls, isEmpty);
    });
  });

  group('operações', () {
    test('antes de initPayment, payment não está disponível', () {
      expect(() => ClisitefTech.I.payment, throwsException);
    });

    test('configureClisitef propaga LICENCE_ERROR do gate nativo', () async {
      mockNative((call) async {
        if (call.method == 'initialize') return true;
        throw licenceError();
      });
      final payment = await ClisitefTech.I.initPayment(licenceKey: _testKey);

      await expectLater(
        payment.configureClisitef(sitefSettings: _settings),
        throwsA(isA<PlatformException>()
            .having((e) => e.code, 'code', 'LICENCE_ERROR')
            .having((e) => e.message, 'message', 'Licença vencida')),
      );
      expect(calls.last.method, 'configureTef');
    });

    test('clisitefTransaction propaga LICENCE_ERROR do gate nativo', () async {
      mockNative((call) async {
        if (call.method == 'initialize') return true;
        throw licenceError();
      });
      final payment = await ClisitefTech.I.initPayment(licenceKey: _testKey);

      await expectLater(
        payment.clisitefTransaction(
          sitefSettings: _settings,
          sitefTransaction: const SitefTransactionClisitef(
            sitefModality: SitefModalityClisitef.creditCard,
            valuePayment: '1000',
            couponNumber: '1',
            dateFiscal: '20261001',
            hourFiscal: '120000',
            operatorPos: 'op',
          ),
        ),
        throwsA(isA<PlatformException>().having((e) => e.code, 'code', 'LICENCE_ERROR')),
      );
      expect(calls.last.method, 'startTransaction');
    });

    test('com licença válida, configureClisitef chama o nativo', () async {
      mockNative((call) async {
        if (call.method == 'initialize') return true;
        if (call.method == 'configureTef') return 0;
        return null;
      });
      final payment = await ClisitefTech.I.initPayment(licenceKey: _testKey);

      expect(await payment.configureClisitef(sitefSettings: _settings), 0);
      expect(calls.map((c) => c.method), ['initialize', 'configureTef']);
    });
  });
}
