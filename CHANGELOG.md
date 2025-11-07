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
