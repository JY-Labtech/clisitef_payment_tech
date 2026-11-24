<div align="center" id="top"> 
  <img src="https://esqueciminhasenha.softwareexpress.com.br/migrador/login/img/SiTeffromFiserv_Logo_RGB.png" height=100 alt="clisitef_payment_tech" />

&#xa0;

</div>

<h1 align="center">Clisitef Payment Tech</h1>

<p align="center">
  <a href="#dart-about">Sobre</a> &#xa0; | &#xa0; 
  <a href="#sparkles-features">Funcionalidades</a> &#xa0; | &#xa0;
  <a href="#rocket-technologies">Tecnologias</a> &#xa0; | &#xa0;
  <a href="#white_check_mark-requirements">Requisitos</a> &#xa0; | &#xa0;
  <a href="#checkered_flag-starting">Start</a> &#xa0; | &#xa0;
  <a href="#memo-license">Licença</a> &#xa0; | &#xa0;
  <a href="https://github.com/JY-Labtech" target="_blank">Autor</a>
</p>

<br>

<a href="https://buymeacoffee.com/jhonathanqr" target="_blank">
  <img src="https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png" alt="Buy Me A Book" style="height: 41px !important;width: 174px !important;box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;-webkit-box-shadow: 0px 3px 2px 0px rgba(190, 190, 190, 0.5) !important;">
</a>

[![Github Badge](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white&link=https://github.com/jhonathanqz)](https://github.com/jhonathanqz)

## :dart: Sobre

Plugin Flutter para facilitar integração com o TEF da `Clisitef`.

## 🔐 Licenciamento

O plugin só funciona com uma licença válida, validada pela API de licenças da JY Labtech (jylabtech_core).

### Onde adquirir

Cadastre-se e gerencie suas licenças no portal **[portal.jylabtech.com.br](https://portal.jylabtech.com.br)**. Quem já tinha conta no portal antigo entra com o mesmo e-mail e a mesma senha.

- Produto: **Licença plugin de pagamento** — a mesma licença vale para qualquer plugin de pagamento da JY Labtech.
- Plano `mensal`: cobrado por dispositivo. Cada aparelho que valida a licença ocupa uma vaga.
- Plano `debug`: cortesia concedida automaticamente no cadastro, válida por 365 dias e limitada a 2 aparelhos. Use para desenvolvimento e testes.
- Os aparelhos vinculados podem ser consultados e desativados pelo portal. Um aparelho desativado deixa de validar a licença.
- **As chaves de licença já existentes continuam valendo**, com os mesmos valores. Chaves novas (formato `JYL-...`) também funcionam.

### Chaves

As chaves são informadas em `ClisitefTech.I.initPayment` (não são mais lidas de `local.properties`):

| Parâmetro | Valor |
|---|---|
| `licenceKey` | Obrigatório. A chave da sua licença, copiada do portal. |
| `licenceInternalKey` | Opcional. Padrão: constante `licenceCheck` (`https://portal-api.jylabtech.com.br/pos-payments/licence/check` em base64). |

```dart
await ClisitefTech.I.initPayment(licenceKey: 'SUA_LICENCA');
```

O plugin sempre valida a licença no servidor oficial da JY Labtech. A URL fica travada no código nativo: se o `licenceInternalKey` apontar para qualquer outro endereço, inclusive o servidor antigo (`aHR0cHM6Ly9wb3MtcGF5bWVudHMtYXBp...`), o valor é ignorado e a validação é feita no servidor oficial.

### Como funciona a validação

- Na chamada de `initPayment`, o plugin envia ao servidor a chave e a identificação do aparelho (ANDROID_ID, serial e modelo). O aparelho é vinculado à licença na primeira validação.
- **Toda operação** (`configureClisitef`, `clisitefTransaction` e os métodos do m-SiTef) confere a licença no nativo antes de chamar o SDK. Não basta ter chamado `initPayment` uma vez: se a licença vencer, for suspensa ou cancelada, as operações seguintes falham.
- **Revalidação**: a primeira operação depois de abrir o app sempre consulta o servidor; depois, a licença é consultada de novo a cada **4 horas** de uso.
- **Bloqueio imediato**: se o servidor recusar a licença (cliente bloqueado no portal, licença vencida, cancelada ou suspensa, não encontrada, aparelho desativado ou limite de aparelhos), o cache é apagado e as operações passam a falhar com `LICENCE_ERROR` na hora.
- **Cache offline**: após uma validação bem-sucedida, o resultado fica salvo no aparelho de forma criptografada. Ele só é usado quando o servidor não responde (sem internet, timeout, 408/429 ou erro 5xx) e por no máximo **72 horas** desde a última validação online, desde que a licença continue dentro da validade. Depois de uma falha de rede o plugin espera 5 minutos antes de tentar o servidor de novo. Um cache com data de gravação no futuro (relógio adulterado), de outra chave ou de outro servidor é descartado. Sem internet e sem cache válido, as operações falham com `LICENCE_ERROR`.
- O vencimento (`expiredAt`) já considera o período de tolerância em caso de atraso no pagamento.
- A consulta ao servidor tem timeout de 15 segundos e roda fora da thread principal.

### Erros de licença

Se a licença for inválida, vencida, suspensa ou cancelada, `initPayment` e qualquer operação lançam `PlatformException` com `code: 'LICENCE_ERROR'` e `message` com a mensagem devolvida pelo servidor (ou `Erro ao validar licença (HTTP <código>)` quando o servidor não devolve mensagem). Chamar uma operação sem um `initPayment` bem-sucedido lança `PlatformException` com `code: 'LICENCE_NOT_INITIALIZED'`.

| HTTP | Situação | O que fazer |
|---|---|---|
| 404 | Licença não encontrada. | Confira o `licenceKey` (sem espaços). |
| 412 | Limite de aparelhos da licença atingido ou aparelho desativado. | Desative um aparelho antigo ou contrate mais vagas no portal; reative o aparelho, se for o caso. |
| 428 | Licença vencida, suspensa ou cancelada. | Regularize ou renove a licença no portal. |
| 403 | Chave não enviada. | Informe o `licenceKey` em `initPayment`. |
| 400 | O aparelho não enviou um identificador estável. | Verifique se o app consegue ler o ANDROID_ID do aparelho. |
| 429 | Muitas tentativas em pouco tempo. | Aguarde alguns instantes e tente de novo. |

Um `licenceKey` vazio gera `PlatformException` com `code: 'INVALID_ARGUMENT'`.

### Troubleshooting

- **Erro de conexão / timeout:** o aparelho precisa de acesso à internet (sem proxy) ao abrir o app e, depois, ao menos a cada 72 horas (sem internet, a operação só funciona se houver uma validação bem-sucedida nas últimas 72 horas).
- **404 com uma chave que funcionava:** confira se não houve erro ao copiar a chave.
- **412 após trocar de aparelho:** desative o aparelho antigo no portal para liberar a vaga.
- **Licença renovada, mas o app continua bloqueado:** reinicie o app para chamar `initPayment` novamente; com conexão, a licença é consultada de novo.

## :sparkles: Funcionalidades

:heavy_check_mark: Cartão de crédito
:heavy_check_mark: Cartão de débito
:heavy_check_mark: PIX
:heavy_check_mark: Cancelamento
:heavy_check_mark: Reimpressão

## :rocket: Tecnologias

#### Para que o plugin funcione corretamente é necessário ter cadastro na Fiserv e passar alguns dados de configuração como parâmetro das funções.

- [Flutter](https://docs.flutter.dev/)

## :white_check_mark: Requisitos

Para que o plugin funcione corretamente é necessário ter cadastro no Fiserv e suas credenciais de acesso.

### ⚠️⚠️⚠️ Esse plugin não funciona com PROXY!!! ⚠️⚠️⚠️

## :checkered_flag: Starting

Adicione a dependência no `pubspec.yaml`:

```yaml
dependencies:
  clisitef_payment_tech: ^3.1.0
```

Inicialmente para liberar o uso do plugin, é necessário chamar o método `initPayment`, responsável por validar as chaves de licença (veja [Licenciamento](#-licenciamento)) e inicializar o SDK. A chamada é assíncrona e deve ser aguardada antes de qualquer operação:

```dart
import 'package:clisitef_payment_tech/clisitef_payment_tech.dart';

await ClisitefTech.I.initPayment(licenceKey: 'SUA_LICENCA');
```

Os modelos ficam em `package:clisitef_payment_tech/payments/model/...`:

```dart
import 'package:clisitef_payment_tech/payments/enum/sitef_modality.dart';
import 'package:clisitef_payment_tech/payments/model/sitef_settings.dart';
import 'package:clisitef_payment_tech/payments/model/sitef_transaction.dart';
import 'package:clisitef_payment_tech/payments/model/tef_transaction_response.dart';
```

Através da função abaixo você irá realizar o setup do SDK conforme documentação da Fiserv. Passe como parâmetro as credenciais.

```dart
final int result = await ClisitefTech.I.payment.configureClisitef(
  sitefSettings: const SitefSettingsClisitef(
    serverIp: '...',
    storeCode: '...',
    posNumber: '...',
  ),
);
```

Transação:
Utilize a função abaixo para chamar um método de pagamento, passando os devidos parâmetros solicitados.

```dart
final TefTransactionResponseClisitef response =
    await ClisitefTech.I.payment.clisitefTransaction(
  sitefSettings: sitefSettings,
  sitefTransaction: sitefTransaction,
);
```

## Testes

Testes Dart (canal de plataforma e propagação de `LICENCE_ERROR`):

```bash
flutter test
```

Testes JVM do nativo (URL travada, regras da licença, resposta do servidor e cache offline), com JDK 17:

```bash
FLUTTER_SDK=$(dirname $(dirname $(which flutter))) \
  example/android/gradlew -p android/native testDebugUnitTest
```

Depois de alterar o código em `android/native`, regenere os binários com `make build-native` (ou `sh scripts/build_android_aar.sh`).

</br>

## :memo: License

This project is under license from MIT. For more details, see the [LICENSE](LICENSE.md) file.

</br>

<table>
  <tr>
    <td align="center"><a href="https://github.com/JY-Labtech"><img src="https://avatars.githubusercontent.com/u/185066406?s=200&v=4" width="100px;" alt=""/><br /><sub><b>JY Labtech</b></sub></a><br /> <a href="https://github.com/JY-Labtech" title="Autor"></a></td>
  </tr>
  
</table>

Feito por <a href="https://github.com/jhonathanqz" target="_blank">Jhonathan Queiroz</a>

&#xa0;

<a href="#top">Back to top</a>
