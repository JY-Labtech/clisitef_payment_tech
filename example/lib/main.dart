import 'package:flutter/material.dart';

import 'payment/payment_page.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('Clisitef Payment Tech')),
        body: const PaymentPage(),
      ),
    );
  }
}
