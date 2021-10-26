package com.example.samsungpay;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;

import com.samsung.android.sdk.samsungpay.v2.PartnerInfo;
import com.samsung.android.sdk.samsungpay.v2.SamsungPay;
import com.samsung.android.sdk.samsungpay.v2.SpaySdk;
import com.samsung.android.sdk.samsungpay.v2.StatusListener;
import com.samsung.android.sdk.samsungpay.v2.payment.CardInfo;
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentInfo;
import com.samsung.android.sdk.samsungpay.v2.payment.PaymentManager;

/** SamsungpayPlugin */
public class SamsungpayPlugin implements FlutterPlugin, MethodCallHandler {
  public static final String SPAY_SERVICE_ID = "b444667cd6034336af44d5";

  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private FlutterPluginBinding binding;

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), "samsungpay");
    binding = flutterPluginBinding;
    channel.setMethodCallHandler(this);
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("getPlatformVersion")) {
      result.success("Android " + android.os.Build.VERSION.RELEASE);

    } else if (call.method.equals("avaialble")) {
      samsungpay(result);
    }

    else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private void samsungpay(@NonNull Result result) {
    Bundle bundle = new Bundle();
    bundle.putString(SamsungPay.PARTNER_SERVICE_TYPE, SamsungPay.ServiceType.INAPP_PAYMENT.toString());
    PartnerInfo partnerInfo = new PartnerInfo(SPAY_SERVICE_ID, bundle);
    partnerInfo.getData().putBoolean(PaymentManager.EXTRA_KEY_TEST_MODE, true);
    final SamsungPay samsungPay = new SamsungPay(binding.getApplicationContext(), partnerInfo);

    samsungPay.getSamsungPayStatus(new StatusListener() {
      @Override
      public void onSuccess(int status, Bundle bundle) {
        switch (status) {
        case SamsungPay.SPAY_NOT_SUPPORTED:
          result.error("SPAY_NOT_SUPPORTED", "SPAY_NOT_SUPPORTED", null);
          break;
        case SamsungPay.SPAY_NOT_READY: // Activate Samsung Pay or update Samsung Pay, if needed
          int extra_reason = bundle.getInt(SamsungPay.EXTRA_ERROR_REASON);
          switch (extra_reason) {
          case SamsungPay.ERROR_SPAY_APP_NEED_TO_UPDATE:
            result.error("ERROR_SPAY_APP_NEED_TO_UPDATE", "ERROR_SPAY_APP_NEED_TO_UPDATE", null);
            break;
          case SamsungPay.ERROR_SPAY_SETUP_NOT_COMPLETED:
            samsungPay.activateSamsungPay();
            break;
          default:
            result.error("NOT_READY", "extra reason: " + extra_reason, null);
          }
          break;
        case SamsungPay.SPAY_READY:
          result.success("SPAY_READY");
          PartnerInfo partnerInfo = new PartnerInfo(SPAY_SERVICE_ID, bundle);

          PaymentManager mPaymentManager = new PaymentManager(binding.getApplicationContext(), partnerInfo);

          break;
        default:// Not expected result

          result.error("UNKNOWN", "UNKNOWN", null);
          break;
        }
      }

      @Override
      public void onFail(int errorCode, Bundle bundle) {

        Log.d("SAM", "Failed to initialize Samsung Pay service." + errorCode);
      }
    });
  }
}
