class Tef {
  final String id;
  final String description;
  final String brand;
  final String brandId;
  final String cardBin;
  final String nsu;
  final String brandNsu;
  final String digitalWalletId;
  final String digitalWalletDescription;
  final String digitalWalletType;
  final String authorizationId;

  const Tef({
    this.id = '',
    this.description = '',
    this.brand = '',
    this.brandId = '',
    this.cardBin = '',
    this.nsu = '',
    this.brandNsu = '',
    this.digitalWalletId = '',
    this.digitalWalletDescription = '',
    this.digitalWalletType = '',
    this.authorizationId = '',
  });

  Tef copyWith({
    String? id,
    String? description,
    String? brand,
    String? brandId,
    String? cardBin,
    String? nsu,
    String? brandNsu,
    String? digitalWalletId,
    String? digitalWalletDescription,
    String? digitalWalletType,
    String? authorizationId,
  }) =>
      Tef(
        id: id ?? this.id,
        description: description ?? this.description,
        brand: brand ?? this.brand,
        brandId: brandId ?? this.brandId,
        cardBin: cardBin ?? this.cardBin,
        nsu: nsu ?? this.nsu,
        brandNsu: brandNsu ?? this.brandNsu,
        digitalWalletId: digitalWalletId ?? this.digitalWalletId,
        digitalWalletDescription: digitalWalletDescription ?? this.digitalWalletDescription,
        digitalWalletType: digitalWalletType ?? this.digitalWalletType,
        authorizationId: authorizationId ?? this.authorizationId,
      );
}
