import 'package:meta/meta.dart';

@immutable
class ResponseTefSettings {
  final int id;
  final String couponNumber;

  const ResponseTefSettings({
    this.id = 0,
    this.couponNumber = '',
  });

  ResponseTefSettings copyWith({
    int? id,
    String? couponNumber,
  }) =>
      ResponseTefSettings(
        id: id ?? this.id,
        couponNumber: couponNumber ?? this.couponNumber,
      );
}
