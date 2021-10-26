import 'dart:async';

import 'package:flutter/services.dart';

class Samsungpay {
  static const MethodChannel _channel = MethodChannel('samsungpay');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  static Future<SamsungpayStatus> get isAvailable async {
    try {
      final String? supported = await _channel.invokeMethod('avaialble');
      return SamsungpayStatus.SPAY_READY;
    } on PlatformException catch (e) {
      print(e);
      switch (e.code) {
        case 'SPAY_NOT_SUPPORTED':
          return SamsungpayStatus.SPAY_NOT_SUPPORTED;
        case 'ERROR_SPAY_APP_NEED_TO_UPDATE':
          return SamsungpayStatus.ERROR_SPAY_APP_NEED_TO_UPDATE;
        case 'NOT_READY':
          return SamsungpayStatus.NOT_READY;
        case 'SPAY_READY':
          return SamsungpayStatus.SPAY_READY;

        default:

         return  SamsungpayStatus.UNKNOWN;
      }
    }
    catch(e){print(e);
      return SamsungpayStatus.UNKNOWN;
    }


  }
}

enum SamsungpayStatus {
  SPAY_NOT_SUPPORTED,
  ERROR_SPAY_APP_NEED_TO_UPDATE,
  NOT_READY,
  SPAY_READY,
  UNKNOWN
}
