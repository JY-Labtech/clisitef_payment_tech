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

Para adquirir uma licença, cadastre-se no site [Licença JY Labtech](https://licenca.jylabtech.com.br).  
Cada licença é válida por terminal. Após a compra, será gerada uma chave `licenceKey` vinculada ao terminal.

##### Observação: A partir da versão 2.0.0 as chaves `licenceKey` e `licenceInternalKey` devem ser fornecidas diretamente pelo código Dart ao chamar `ClisitefTech.I.initPayment`.

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

Inicialmente para liberar o uso do plugin, é necessário chamar o método `initPayment`, responsável por validar as chaves de licença e inicializar o SDK. A chamada agora é assíncrona:

```dart
await ClisitefTech.I.initPayment(
  licenceKey: 'sua_licenca_publica',
  licenceInternalKey: 'sua_licenca_interna',
);
```

Para utilizar o plugin, basta chamar alguma de suas funções abaixo:

Através da função abaixo você irá realizar o setup do SDK conforme documentação da Fiserv. Passe como parâmetro as credencias.

```dart
ClisitefTech.I.payment.configureClisitef(SitefSettingsClisitef())
```

Transação:
Utilize a função abaixo para chamar um método de pagamento, passando os devidos parâmetros solicitados.

```dart
ClisitefTech.I.payment.clisitefTransaction({
    required SitefSettingsClisitef sitefSettings,
    required SitefTransactionClisitef sitefTransaction,
  })
```

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
