import 'package:clisitef_payment_tech/clisitef_payment_tech.dart';
import 'package:flutter/material.dart';

class PaymentPage extends StatefulWidget {
  const PaymentPage({Key? key}) : super(key: key);

  @override
  State<PaymentPage> createState() => _PaymentPageState();
}

class _PaymentPageState extends State<PaymentPage> {
  final serverIpController = TextEditingController(text: '127.0.0.1');
  final storeCodeController = TextEditingController(text: '00000000');
  final posNumberController = TextEditingController(text: 'IP000001');
  final valueController = TextEditingController();

  bool busy = false;
  String output = '';

  SitefSettingsClisitef get settings => SitefSettingsClisitef(
        serverIp: serverIpController.text.trim(),
        storeCode: storeCodeController.text.trim(),
        posNumber: posNumberController.text.trim(),
      );

  /// Valor no formato do CliSiTef, com vírgula decimal (ex.: "10,50").
  String get value {
    final parsed = double.tryParse(valueController.text.replaceAll(',', '.')) ?? 0;
    return TefHelper.priceSanitized(parsed);
  }

  Future<void> run(String label, Future<String> Function() action) async {
    FocusScope.of(context).unfocus();
    setState(() {
      busy = true;
      output = '$label...';
    });
    try {
      final result = await action();
      setState(() => output = '$label: $result');
    } catch (error) {
      setState(() => output = '$label falhou: $error');
    } finally {
      setState(() => busy = false);
    }
  }

  Future<String> configure() async {
    final code = await ClisitefTech.I.payment.configureClisitef(sitefSettings: settings);
    return code == 0 ? 'configurado (0)' : 'erro de configuração ($code)';
  }

  Future<String> pay(SitefModalityClisitef modality) async {
    final now = DateTime.now();
    String two(int v) => v.toString().padLeft(2, '0');
    final response = await ClisitefTech.I.payment.clisitefTransaction(
      sitefSettings: settings,
      sitefTransaction: SitefTransactionClisitef(
        sitefModality: modality,
        valuePayment: value,
        couponNumber: now.millisecondsSinceEpoch.toString(),
        dateFiscal: '${now.year}${two(now.month)}${two(now.day)}',
        hourFiscal: '${two(now.hour)}${two(now.minute)}${two(now.second)}',
        operatorPos: 'operador',
      ),
    );
    if (response.resultCode == 0) {
      return 'aprovada (NSU ${response.tefNSUSitef}, autorização ${response.tefCodAuthorization})';
    }
    return 'não aprovada (${response.resultCode}) ${response.errorTransaction ?? ''}';
  }

  @override
  void dispose() {
    serverIpController.dispose();
    storeCodeController.dispose();
    posNumberController.dispose();
    valueController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return ListView(
      padding: const EdgeInsets.all(16),
      children: <Widget>[
        TextField(
          controller: serverIpController,
          decoration: const InputDecoration(labelText: 'IP do servidor SiTef'),
        ),
        TextField(
          controller: storeCodeController,
          decoration: const InputDecoration(labelText: 'Código da loja (8 dígitos)'),
        ),
        TextField(
          controller: posNumberController,
          decoration: const InputDecoration(labelText: 'Terminal (ex.: IP000001)'),
        ),
        TextField(
          controller: valueController,
          keyboardType: const TextInputType.numberWithOptions(decimal: true),
          decoration: const InputDecoration(labelText: 'Valor (R\$)'),
        ),
        const SizedBox(height: 20),
        Wrap(
          spacing: 10,
          runSpacing: 10,
          children: <Widget>[
            ElevatedButton(
              onPressed: busy ? null : () => run('Configuração', configure),
              child: const Text('Configurar TEF'),
            ),
            ElevatedButton(
              onPressed: busy ? null : () => run('Crédito', () => pay(SitefModalityClisitef.creditCard)),
              child: const Text('Crédito'),
            ),
            ElevatedButton(
              onPressed: busy ? null : () => run('Débito', () => pay(SitefModalityClisitef.debitCard)),
              child: const Text('Débito'),
            ),
            ElevatedButton(
              onPressed: busy ? null : () => run('PIX', () => pay(SitefModalityClisitef.pix)),
              child: const Text('PIX'),
            ),
            ElevatedButton(
              onPressed: busy ? null : () => run('Cancelamento', () => pay(SitefModalityClisitef.cancellation)),
              child: const Text('Cancelamento'),
            ),
          ],
        ),
        const SizedBox(height: 20),
        Text(output),
      ],
    );
  }
}
