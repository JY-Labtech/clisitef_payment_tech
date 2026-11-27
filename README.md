# clisitef_payment_tech

> Open source Flutter plugin (MIT) for the CliSiTef TEF SDK (Software Express / Fiserv) on Android: configure the TEF and run credit, debit, PIX and cancellation transactions.

[![pub package](https://img.shields.io/pub/v/clisitef_payment_tech.svg)](https://pub.dev/packages/clisitef_payment_tech)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

Plugin Flutter para integrar o app ao TEF **CliSiTef** da Software Express (Fiserv) em Android. O plugin configura a CliSiTef, abre o fluxo de transação do SDK (telas de menu, perguntas, PIN pad e QR Code) e devolve ao Dart a resposta da transação, com NSU, código de autorização e comprovantes.

O projeto é open source, sob a licença MIT. Não há chave de licença, cobrança nem validação em servidor.

## Funcionalidades

- Configuração da CliSiTef (`configureClisitef`): servidor SiTef, loja, terminal e parâmetros adicionais.
- Transação TEF (`clisitefTransaction`) nas modalidades:
  - crédito (à vista ou parcelado: a forma de pagamento é escolhida nas telas do CliSiTef);
  - débito;
  - PIX e carteira digital;
  - cancelamento (estorno);
  - menu geral do CliSiTef (`undefined`), onde o operador escolhe a operação que o SiTef liberar para o terminal (por exemplo, reimpressão).
- Restrição de transações habilitadas no crédito e no débito (`enabledTransactions`).
- Nova tentativa automática: se a transação voltar com `resultCode == -1` (CliSiTef não configurada), o plugin reconfigura e tenta de novo.
- Mensagens de erro em português para os códigos de retorno (`errorTransaction` e `TefHelper`).

O módulo nativo também tem os métodos `transactionMsitef` e `menuMSitef` (integração com o app m-SiTef por intent), e os modelos `MSitefParams`/`MSitefMenuParams` existem em `lib/payments/model/params/`. Esses métodos ainda não são expostos pela API Dart.

## Compatibilidade e requisitos

| Item | Requisito |
| --- | --- |
| Plataforma | Android (não há implementação iOS) |
| minSdk | 23 |
| compileSdk do plugin | 34 |
| Dart | `>=3.0.0 <4.0.0` |
| Flutter | `>=3.0.0` |
| SDK do fabricante | CliSiTef (`clisitef-android`), Gertec GEDI (`libgedi`, GPOS700) e PPComp, já inclusos em `android/libs` |
| Servidor | Cadastro na Software Express/Fiserv, com IP do servidor SiTef, código da loja e número do terminal |

O plugin não funciona atrás de **proxy**: a CliSiTef precisa de acesso direto ao servidor SiTef.

### Permissões

O manifest do plugin é mesclado ao do app e declara:

- `USB_PERMISSION`, `uses-feature android.hardware.usb.host` (obrigatório) para o PIN pad USB;
- `NFC` e `uses-feature android.hardware.nfc`;
- `BLUETOOTH`, `BLUETOOTH_ADMIN` (até API 30), `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT` e `uses-feature android.hardware.bluetooth_le` (opcional) para PIN pad Bluetooth;
- `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`, `MANAGE_EXTERNAL_STORAGE` (pasta de trabalho da CliSiTef);
- `SET_TIME`.

As permissões de execução (Bluetooth, armazenamento) devem ser pedidas pelo app quando o Android exigir.

## Instalação

```bash
flutter pub add clisitef_payment_tech
```

Ou no `pubspec.yaml`:

```yaml
dependencies:
  clisitef_payment_tech: ^4.0.0
```

## Configuração Android

Não é preciso repositório Maven nem credencial extra: o SDK da CliSiTef e as bibliotecas da Gertec/PPComp vêm no pacote (`android/libs`). As bibliotecas nativas da CliSiTef (`libclisitef.so`, `libssl.so`, `libcrypto.so`, `libtlswnb.so`, `libgsurf_rsa.so`, para `arm64-v8a`, `armeabi-v7a`, `x86` e `x86_64`), os assets (`CLSIT`, `Mens.txt`) e os layouts das telas entram no APK automaticamente. As activities do fluxo de transação (`TransactionActivity`, `MenuActivity`, `DialogActivity`, `YesNoActivity`, `MessageActivity`, `MyActivityQRCode`, `ConfigureActivity`) já estão declaradas no manifest do plugin.

1. Garanta `minSdkVersion 23` (ou maior) em `android/app/build.gradle`:

   ```groovy
   android {
       defaultConfig {
           minSdkVersion 23
       }
   }
   ```

2. Se o projeto usa `android.enableJetifier=true`, adicione ao `android/gradle.properties` a lista abaixo. Ela evita que o Jetifier tente transformar os jars do engine do Flutter, que já são AndroidX:

   ```properties
   android.jetifier.ignorelist=armeabi_v7a_,arm64_v8a_,x86_64_,flutter_embedding_
   ```

3. Em terminais com app assinado pelo fabricante (por exemplo Gertec), assine o APK com a chave fornecida pelo fabricante.

## Uso rápido

```dart
import 'package:clisitef_payment_tech/clisitef_payment_tech.dart';
import 'package:flutter/services.dart';

const settings = SitefSettingsClisitef(
  serverIp: '192.168.0.10', // IP do servidor SiTef
  storeCode: '00000000', // código da loja (8 dígitos)
  posNumber: 'IP000001', // número do terminal
);

Future<void> vender() async {
  final payment = await ClisitefTech.I.initPayment();

  final config = await payment.configureClisitef(sitefSettings: settings);
  if (config != 0) {
    print(TefHelper.checkErrorConfigTEF(config)[config]);
    return;
  }

  try {
    final response = await payment.clisitefTransaction(
      sitefSettings: settings,
      sitefTransaction: const SitefTransactionClisitef(
        sitefModality: SitefModalityClisitef.creditCard,
        valuePayment: '10,00',
        couponNumber: '123',
        dateFiscal: '20261127', // AAAAMMDD
        hourFiscal: '153000', // HHMMSS
        operatorPos: 'caixa1',
      ),
    );

    if (response.resultCode == 0) {
      print('Aprovada: NSU ${response.tefNSUSitef}, autorização ${response.tefCodAuthorization}');
      print(response.customerReceipt);
    } else {
      print('Não aprovada (${response.resultCode}): ${response.errorTransaction}');
    }
  } on PlatformException catch (e) {
    print('Erro no plugin: ${e.code} ${e.message}');
  }
}
```

## Referência da API

Tudo é exportado por `package:clisitef_payment_tech/clisitef_payment_tech.dart`.

### `ClisitefTech`

Instância única em `ClisitefTech.I`.

| Membro | Retorno | Descrição |
| --- | --- | --- |
| `initPayment()` | `Future<Payment>` | Cria (ou devolve) a instância de `Payment`. Não chama o nativo nem recebe parâmetros. |
| `payment` | `Payment` | A mesma instância de `Payment`, criada sob demanda. Pode ser usada sem `initPayment`. |

```dart
final payment = await ClisitefTech.I.initPayment();
// ou
final payment = ClisitefTech.I.payment;
```

### `Payment`

#### `configureClisitef`

```dart
Future<int> configureClisitef({required SitefSettingsClisitef sitefSettings})
```

Chama o `configure` da CliSiTef com `serverIp`, `storeCode`, `posNumber` e `additionalParameters`. Retorna o código do SDK: `0` é sucesso; os demais estão em [Tratamento de erros](#tratamento-de-erros).

```dart
final code = await ClisitefTech.I.payment.configureClisitef(sitefSettings: settings);
```

#### `clisitefTransaction`

```dart
Future<TefTransactionResponseClisitef> clisitefTransaction({
  required SitefSettingsClisitef sitefSettings,
  required SitefTransactionClisitef sitefTransaction,
})
```

Configura a CliSiTef com `sitefSettings` e abre o fluxo de transação na modalidade de `sitefTransaction`. O `Future` completa quando o fluxo termina, com a resposta da transação. Em crédito e débito, `sitefSettings.enabledTransactions` é enviado como restrição de transações.

```dart
final response = await ClisitefTech.I.payment.clisitefTransaction(
  sitefSettings: settings,
  sitefTransaction: transaction,
);
```

### Modelos

#### `SitefSettingsClisitef`

| Campo | Uso |
| --- | --- |
| `serverIp` | IP (ou nome) do servidor SiTef. |
| `storeCode` | Código da loja (8 dígitos). |
| `posNumber` | Número do terminal (ex.: `IP000001`). |
| `additionalParameters` | Parâmetros adicionais da CliSiTef (ex.: `[TipoPinPad=Android_AUTO]`). |
| `enabledTransactions` | Restrições de transações, usadas no crédito e no débito. |
| `operatorPos`, `companyDocument`, `lib`, `comExternal`, `isDoubleValidation`, `otp`, `automationDocument` | Dados do estabelecimento e do m-SiTef. Não são usados por `configureClisitef`/`clisitefTransaction`. |

Também tem `copyWith`, `toMap`/`fromMap` e `toJson`/`fromJson`.

#### `SitefTransactionClisitef`

| Campo | Uso |
| --- | --- |
| `sitefModality` | `SitefModalityClisitef` da transação. |
| `valuePayment` | Valor com vírgula decimal (ex.: `'10,00'`). Use `TefHelper.priceSanitized(10.0)`. |
| `couponNumber` | Número do cupom fiscal / documento. |
| `dateFiscal` | Data fiscal `AAAAMMDD`. |
| `hourFiscal` | Hora fiscal `HHMMSS`. |
| `operatorPos` | Operador. |
| `additionalParameters` | Reservado (não é enviado ao nativo). |

#### `TefTransactionResponseClisitef`

Principais campos:

| Campo | Conteúdo |
| --- | --- |
| `resultCode` | `0` = aprovada. `99999` = retorno `0` sem NSU do autorizador. `999999` = sem resposta. Outros valores: código do CliSiTef. |
| `errorTransaction` | Mensagem do código de retorno (nula quando aprovada). |
| `tefNSUSitef`, `tefNSUAuthorizing` | NSU do SiTef e do autorizador. |
| `tefCodAuthorization` | Código de autorização. |
| `tefInstitution`, `tefCodInstitution` | Bandeira/instituição. |
| `tefCodPayment`, `tefDescPayment`, `tefCardType`, `binCard`, `cardNumber`, `cardNumberLast` | Forma de pagamento e dados do cartão. |
| `tefDigitalWalletId`, `tefDigitalWalletType`, `tefDigitalWalletName` | Carteira digital / PIX. |
| `customerReceipt`, `establishmentReceipt` | Comprovantes (via do cliente e do estabelecimento). |
| `transactionDateTime`, `paymentAmount`, `amountCancelled`, `codeDocumentCanceled`, `cancellationMode` | Data, valores e dados de cancelamento. |
| `transactionConfirmation`, `compDadosConf`, `codTrans`, `redeAut`, `typeFields` | Dados de confirmação e campos brutos do SDK. |

### Enums

#### `SitefModalityClisitef`

| Valor | Modalidade CliSiTef |
| --- | --- |
| `creditCard` | `3` |
| `debitCard` | `2` |
| `pix` | `122` |
| `digitalWallet` | `122` |
| `cancellation` | `200` |
| `undefined` | `0` (menu geral) |

### Utilitários e contratos

- `TefHelper.priceSanitized(double)`: formata o valor com vírgula decimal (`10.5` → `'10,50'`).
- `TefHelper.checkErrorConfigTEF(int)` / `TefHelper.checkErrorTransactionTEF(int)`: `Map<int, String>` com a mensagem do código.
- `TefHelper.sanitizeSitefSettingsClisitef(settings)`: deixa só os dígitos de loja e terminal, completa com zeros à esquerda até 8 posições e limpa os documentos.
- `IClisitefHandler`: contrato com `onError`, `onMessage`, `onChanged`, `onFinishedResponse`, `onLoading` e `onAuthProgress` para tratar mensagens do canal. O fluxo atual de `clisitefTransaction` mostra as mensagens nas telas nativas e não depende dele.

## Exemplos por operação

As configurações usadas abaixo:

```dart
const settings = SitefSettingsClisitef(
  serverIp: '192.168.0.10',
  storeCode: '00000000',
  posNumber: 'IP000001',
);

SitefTransactionClisitef venda(SitefModalityClisitef modality, double valor) {
  final now = DateTime.now();
  String two(int v) => v.toString().padLeft(2, '0');
  return SitefTransactionClisitef(
    sitefModality: modality,
    valuePayment: TefHelper.priceSanitized(valor),
    couponNumber: now.millisecondsSinceEpoch.toString(),
    dateFiscal: '${now.year}${two(now.month)}${two(now.day)}',
    hourFiscal: '${two(now.hour)}${two(now.minute)}${two(now.second)}',
    operatorPos: 'caixa1',
  );
}

final payment = ClisitefTech.I.payment;
```

### Crédito à vista ou parcelado

O CliSiTef pergunta nas telas se é à vista ou parcelado e o número de parcelas.

```dart
final r = await payment.clisitefTransaction(
  sitefSettings: settings,
  sitefTransaction: venda(SitefModalityClisitef.creditCard, 150.0),
);
```

### Débito

```dart
final r = await payment.clisitefTransaction(
  sitefSettings: settings,
  sitefTransaction: venda(SitefModalityClisitef.debitCard, 25.9),
);
```

### Restringir transações no crédito/débito

```dart
final r = await payment.clisitefTransaction(
  sitefSettings: settings.copyWith(enabledTransactions: '16;26'),
  sitefTransaction: venda(SitefModalityClisitef.creditCard, 50.0),
);
```

Os códigos de restrição são os da documentação da CliSiTef.

### PIX / carteira digital

O QR Code é exibido pela tela do plugin.

```dart
final r = await payment.clisitefTransaction(
  sitefSettings: settings,
  sitefTransaction: venda(SitefModalityClisitef.pix, 30.0),
);
print(r.tefDigitalWalletName);
```

### Cancelamento (estorno)

O CliSiTef pede nas telas os dados da transação original (data, NSU, valor).

```dart
final r = await payment.clisitefTransaction(
  sitefSettings: settings,
  sitefTransaction: venda(SitefModalityClisitef.cancellation, 150.0),
);
print(r.amountCancelled);
```

### Menu geral (reimpressão e outras funções)

```dart
final r = await payment.clisitefTransaction(
  sitefSettings: settings,
  sitefTransaction: venda(SitefModalityClisitef.undefined, 0),
);
```

### Comprovantes

O plugin não imprime: use `customerReceipt` e `establishmentReceipt` com a impressora do seu terminal.

```dart
if (r.resultCode == 0) {
  imprimir(r.customerReceipt);
  imprimir(r.establishmentReceipt);
}
```

## Tratamento de erros

### `PlatformException`

| Código | Quando |
| --- | --- |
| `NO_ACTIVITY` | O plugin ainda não está ligado a uma Activity (chamada antes de o app estar em primeiro plano). |
| `CONFIGURE_ERROR` | Em `clisitefTransaction`, o `configure` da CliSiTef falhou antes de abrir a transação. `details` traz o código (tabela de `configureClisitef` abaixo). |
| `UNEXPECTED_ERROR` | Exceção no nativo ao processar a chamada. `message` traz o detalhe. |

Um método que o nativo não conhece gera `MissingPluginException`.

### Retorno de `configureClisitef`

| Código | Significado |
| --- | --- |
| `0` | Configurado. |
| `1` | Endereço IP inválido ou não resolvido. |
| `2` | Código da loja inválido. |
| `3` | Código de terminal inválido. |
| `6` | Erro na inicialização do TCP/IP. |
| `7` | Falta de memória. |
| `8` | CliSiTef não encontrada ou com problemas. |
| `9` | Configuração de servidores SiTef excedida. |
| `10` | Erro de acesso à pasta da CliSiTef (permissão de escrita). |
| `11` | Dados inválidos passados pela automação. |
| `12` | Modo seguro não ativo (falta de configuração do `.cha` no servidor). |
| `13` | Caminho de bibliotecas inválido. |

### `resultCode` da transação

`0` é aprovada. `-1` faz o plugin reconfigurar e tentar de novo. Os demais códigos vêm do CliSiTef/autorizador, e `errorTransaction` traz a mensagem (tabela em `TefHelper.checkErrorTransactionTEF`). `99999` indica retorno `0` sem NSU do autorizador, e `999999`, que o fluxo terminou sem resposta.

## App de exemplo

O `example/` tem uma tela com os dados do servidor SiTef, o valor e botões para configurar o TEF e fazer crédito, débito, PIX e cancelamento.

```bash
cd example
flutter run
```

Rode num terminal Android com PIN pad e acesso ao servidor SiTef. O build do example assina com as chaves de `example/Chaves`, que não são publicadas no pub.dev. Num clone sem essas chaves, ajuste o `signingConfig` em `example/android/app/build.gradle`.

## Testes

Testes Dart (canal de plataforma, parâmetros, modalidades e resposta):

```bash
flutter test
```

Testes JVM do módulo nativo (roteamento do plugin), com JDK 17:

```bash
FLUTTER_SDK=$(dirname $(dirname $(which flutter))) \
  example/android/gradlew -p android/native testDebugUnitTest \
  --no-daemon -Dorg.gradle.jvmargs=-Xmx2g
```

O código Java fica em `android/native` e é distribuído pré-compilado em `android/libs/clisitef_payment_tech-native.jar`. Depois de alterar `android/native`, regenere os binários:

```bash
JAVA_TOOL_OPTIONS=-Xmx2g sh scripts/build_android_aar.sh
```

## Migração da 3.x para a 4.0.0

A licença paga foi removida. Mudanças:

- `initPayment` não recebe mais `licenceKey` nem `licenceInternalKey`.
- A constante `licenceCheck` e o import `package:clisitef_payment_tech/licence/constants/licence_constants.dart` não existem mais. `channel` e `CHANNEL_NAME` estão em `payments/payment.dart`, exportado pelo pacote.
- `Payment.ensureInitialized` foi removido.
- Os erros `LICENCE_ERROR` e `LICENCE_NOT_INITIALIZED` não existem mais: remova esse tratamento.
- Os modelos podem ser importados direto de `package:clisitef_payment_tech/clisitef_payment_tech.dart` (os imports antigos por caminho continuam funcionando).
- `clisitefTransaction` não lança mais `TypeError` ao ler a resposta do nativo, e uma falha de configuração agora vira `PlatformException(code: 'CONFIGURE_ERROR')` em vez de deixar o `Future` sem resposta.
- As `.so`, os assets, os layouts e as activities da CliSiTef voltaram a entrar no APK (estavam faltando desde a 3.0.0). Se você copiou esses arquivos para o seu app como contorno, pode removê-los.

Antes (3.x):

```dart
import 'package:clisitef_payment_tech/clisitef_payment_tech.dart';
import 'package:clisitef_payment_tech/payments/model/sitef_settings.dart';

try {
  await ClisitefTech.I.initPayment(licenceKey: 'SUA_LICENCA');
} on PlatformException catch (e) {
  if (e.code == 'LICENCE_ERROR') {
    // licença inválida
  }
}
final code = await ClisitefTech.I.payment.configureClisitef(sitefSettings: settings);
```

Depois (4.0.0):

```dart
import 'package:clisitef_payment_tech/clisitef_payment_tech.dart';

await ClisitefTech.I.initPayment();
final code = await ClisitefTech.I.payment.configureClisitef(sitefSettings: settings);
```

## Contribuindo

Issues e pull requests são bem-vindos em https://github.com/JY-Labtech/clisitef_payment_tech/issues. Antes de abrir um PR, rode `flutter analyze`, `flutter test` e os testes nativos. Se mexer em `android/native`, regenere os binários com `scripts/build_android_aar.sh`.

## Licença

[MIT](LICENSE). O SDK CliSiTef e as bibliotecas Gertec/PPComp em `android/libs` são dos seus fabricantes e seguem os termos deles.

## Créditos

Desenvolvido por [JY Labtech](https://jylabtech.com.br), com autoria de [Jhonathan Queiroz](https://github.com/jhonathanqz).
