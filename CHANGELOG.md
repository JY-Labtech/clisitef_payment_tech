## 3.1.0

Validação de licença mais rígida. Revise o tratamento de `LICENCE_ERROR` no app.

- A licença é sempre validada no servidor oficial da JY Labtech (`https://portal-api.jylabtech.com.br/pos-payments/licence/check`). A URL fica travada no nativo: valores de `licenceInternalKey` que apontem para outro servidor (inclusive o antigo, do Cloud Run) são ignorados.
- `licenceInternalKey` passou a ser opcional em `initPayment`. O padrão é a constante `licenceCheck`, que agora é a URL completa de validação no core.
- Toda operação (`configureTef`, `startTransaction`, `transactionMsitef`, `menuMSitef`) checa a licença no nativo antes de chamar o SDK, usando o cache offline válido ou o servidor. Com licença inválida, vencida, suspensa ou cancelada, a operação falha com `PlatformException(code: 'LICENCE_ERROR')` (antes vinha como `UNEXPECTED_ERROR`).
- Corrigido: uma resposta HTTP 200 com licença vencida, inativa ou cancelada (`isCanceled`) era aceita. Agora é erro e nada é salvo no cache. JSON inválido ou sem `expiredAt` também é erro.
- Cache offline só vale com licença válida, gravado há no máximo 7 dias (e não no futuro, com tolerância de 5 minutos), da mesma chave e do mesmo servidor. Caso contrário é apagado e a licença é validada online de novo.
- Mensagem de erro sempre preenchida: sem corpo JSON, fica `Erro ao validar licença (HTTP <código>)`.
- Timeouts de 15 s (conexão e leitura) na consulta da licença. A validação roda em background e não bloqueia mais a main thread.
- Se a revalidação em `initPayment` falhar, o plugin deixa de ficar liberado no Dart até uma nova validação com sucesso.
- Testes: testes JVM do nativo (URL, regras da licença, resposta do servidor, cache e fluxo de validação) e testes Dart do canal de plataforma.
- Binários `android/libs/clisitef_payment_tech-native.jar` e `clisitef_payment_tech-release.aar` regenerados.
- Validação de licença migrada para a API do jylabtech_core: o novo valor do `licenceInternalKey` é `aHR0cHM6Ly9wb3J0YWwtYXBpLmp5bGFidGVjaC5jb20uYnIvcG9zLXBheW1lbnRzL2xpY2VuY2UvY2hlY2s=` (`https://portal-api.jylabtech.com.br/pos-payments/licence/check`). O valor antigo passa a ser ignorado (a validação vai sempre para o core; veja acima).
- Constante `licenceCheck` atualizada para a URL de validação do core.
- As chaves de licença existentes continuam valendo; licenças passam a ser adquiridas e gerenciadas em https://portal.jylabtech.com.br.
- README: seção de licenciamento reescrita (portal, planos, cache offline de 7 dias, tabela de erros e troubleshooting) e exemplos de uso corrigidos (`configureClisitef(sitefSettings: ...)`, imports dos modelos).
- Example: `licenceInternalKey` com o novo valor e remoção de `@override` em métodos que não existem mais em `IClisitefHandler`.

## 🔥 3.0.0 – Breaking Change

- Reestrutura a camada Android: todo o código nativo foi movido para um módulo interno compilado em `AAR/JAR`, evitando a exposição do código fonte ao publicar no pub.dev.
- Adicionado script `scripts/build_android_aar.sh` para gerar os binários proprietários (`clisitef_payment_tech-release.aar` e `clisitef_payment_tech-native.jar`).
- `initPayment` voltou a exigir as chaves `licenceKey` e `licenceInternalKey`, agora enviadas do Dart para o Android via canal de plataforma.
- `ClisitefTech.I.initPayment` passou a ser assíncrono (`Future<Payment>`) e deve ser aguardado antes de qualquer operação.
- As chaves de licença não são mais lidas de `local.properties`.

## 🔥 2.0.0+1 – Breaking Change

⚠️ Attention: In this version, there is an important change in the way payments are initialized.

🛠️ What changed:

Breaking Change: The initPayment method no longer requires the licenceKey to be passed as a parameter.

It is now mandatory to define the following fields in local.properties:

licenceKey → main licence key

licenceInternalKey → internal licence key

This means that any code that previously passed licenceKey directly to initPayment will need to be updated.

## 1.1.1

- Corrige bug ao validar licença
- Adiciona método para limpar cache de licença

## 1.1.0

- Corrige bug ao validar licença

## 1.0.6

- Primeiro release do Plugin.
- Funções: cŕedito, crédito parcelado, débito, pix, voucher, ativar terminal, abortar transação e cancelar transação.
- Adicionadl método para verificar licença
- update dart sdk 3.0.0

## 1.0.5

- Primeiro release do Plugin.
- Funções: cŕedito, crédito parcelado, débito, pix, voucher, ativar terminal, abortar transação e cancelar transação.
- Adicionadl método para verificar licença
- update dart sdk 3.0.0

## 1.0.4

- Primeiro release do Plugin.
- Funções: cŕedito, crédito parcelado, débito, pix, voucher, ativar terminal, abortar transação e cancelar transação.
- Adicionadl método para verificar licença

## 1.0.3

- Primeiro release do Plugin.
- Funções: cŕedito, crédito parcelado, débito, pix, voucher, ativar terminal, abortar transação e cancelar transação.
- Adicionadl método para verificar licença

## 1.0.2

- Primeiro release do Plugin.
- Funções: cŕedito, crédito parcelado, débito, pix, voucher, ativar terminal, abortar transação e cancelar transação.
- Adicionadl método para verificar licença

## 1.0.1

- Primeiro release do Plugin.
- Funções: cŕedito, crédito parcelado, débito, pix, voucher, ativar terminal, abortar transação e cancelar transação.
- Adicionadl método para verificar licença

## 1.0.0

- Primeiro release do Plugin.
- Funções: cŕedito, crédito parcelado, débito, pix, voucher, ativar terminal, abortar transação e cancelar transação.
