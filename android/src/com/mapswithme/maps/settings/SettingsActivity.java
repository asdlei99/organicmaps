package com.mapswithme.maps.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.MailTo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.mapswithme.maps.BuildConfig;
import com.mapswithme.maps.MWMApplication;
import com.mapswithme.maps.R;
import com.mapswithme.util.Constants;
import com.mapswithme.util.UiUtils;
import com.mapswithme.util.Utils;
import com.mapswithme.util.Yota;
import com.mapswithme.util.statistics.Statistics;

public class SettingsActivity extends PreferenceActivity implements OnPreferenceClickListener, Preference.OnPreferenceChangeListener
{
  public final static String ZOOM_BUTTON_ENABLED = "ZoomButtonsEnabled";
  private static final String COPYRIGHT_HTML_URL = "file:///android_asset/copyright.html";
  private static final String FAQ_HTML_URL = "file:///android_asset/faq.html";

  private Preference mStoragePreference = null;
  private StoragePathManager mPathManager = new StoragePathManager();

  @SuppressWarnings("deprecation")
  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
    {
      // http://stackoverflow.com/questions/6867076/getactionbar-returns-null
      final ActionBar bar = getActionBar();
      if (bar != null)
        bar.setDisplayHomeAsUpEnabled(true);
    }

    addPreferencesFromResource(R.xml.preferences);
    initPreferences();
    yotaSetup();
  }

  @SuppressWarnings("deprecation")
  private void initPreferences()
  {
    mStoragePreference = findPreference(getString(R.string.pref_storage_activity));
    mStoragePreference.setOnPreferenceClickListener(this);

    final ListPreference lPref = (ListPreference) findPreference(getString(R.string.pref_munits));
    lPref.setValue(String.valueOf(UnitLocale.getUnits()));
    lPref.setOnPreferenceChangeListener(this);

    final CheckBoxPreference allowStatsPreference = (CheckBoxPreference) findPreference(getString(R.string.pref_allow_stat));
    allowStatsPreference.setChecked(Statistics.INSTANCE.isStatisticsEnabled(this));
    allowStatsPreference.setOnPreferenceChangeListener(this);

    final CheckBoxPreference enableZoomButtons = (CheckBoxPreference) findPreference(getString(R.string.pref_zoom_btns_enabled));
    enableZoomButtons.setChecked(MWMApplication.get().nativeGetBoolean(ZOOM_BUTTON_ENABLED, true));
    enableZoomButtons.setOnPreferenceChangeListener(this);

    findPreference(getString(R.string.pref_about)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_rate_app)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_contact)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_copyright)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_like_fb)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_follow_twitter)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_report_bug)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_subscribe)).setOnPreferenceClickListener(this);
    findPreference(getString(R.string.pref_help)).setOnPreferenceClickListener(this);
  }

  @SuppressWarnings("deprecation")
  private void storagePathSetup()
  {
    PreferenceScreen screen = (PreferenceScreen) findPreference(getString(R.string.pref_settings));
    if (Yota.isYota())
      screen.removePreference(mStoragePreference);
    else if (mPathManager.hasMoreThanOneStorage())
      screen.addPreference(mStoragePreference);
    else
      screen.removePreference(mStoragePreference);
  }

  @SuppressWarnings("deprecation")
  private void yotaSetup()
  {
    final PreferenceScreen screen = (PreferenceScreen) findPreference(getString(R.string.pref_settings));
    final Preference yopPreference = findPreference(getString(R.string.pref_yota));
    if (!Yota.isYota())
      screen.removePreference(yopPreference);
    else
    {
      yopPreference.setOnPreferenceClickListener(new OnPreferenceClickListener()
      {
        @Override
        public boolean onPreferenceClick(Preference preference)
        {
          SettingsActivity.this.startActivity(new Intent(Yota.ACTION_PREFERENCE));
          return true;
        }
      });
    }
  }

  @Override
  protected void onStart()
  {
    super.onStart();

    Statistics.INSTANCE.startActivity(this);
  }

  @Override
  protected void onStop()
  {
    super.onStop();

    Statistics.INSTANCE.stopActivity(this);
  }

  @Override
  protected void onResume()
  {
    super.onResume();
    BroadcastReceiver receiver = new BroadcastReceiver()
    {
      @Override
      public void onReceive(Context context, Intent intent)
      {
        storagePathSetup();
      }
    };
    mPathManager.startExternalStorageWatching(this, receiver, null);
    storagePathSetup();
  }

  @Override
  protected void onPause()
  {
    super.onPause();
    mPathManager.stopExternalStorageWatching();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
    if (item.getItemId() == android.R.id.home)
    {
      final InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
      imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
      onBackPressed();
      return true;
    }
    else
      return super.onOptionsItemSelected(item);
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
  {
    super.onPreferenceTreeClick(preferenceScreen, preference);
    if (preference != null && preference instanceof PreferenceScreen &&
        ((PreferenceScreen) preference).getDialog() != null)
      ((PreferenceScreen) preference).getDialog().getWindow().getDecorView().
          setBackgroundDrawable(getWindow().getDecorView().getBackground().getConstantState().newDrawable());
    return false;
  }

  private WebView buildWebViewDialog(String dialogTitle)
  {
    final LayoutInflater inflater = LayoutInflater.from(this);
    final View alertDialogView = inflater.inflate(R.layout.dialog_about, null);
    final WebView myWebView = (WebView) alertDialogView.findViewById(R.id.webview_about);

    myWebView.setWebViewClient(new WebViewClient()
    {
      @Override
      public void onPageFinished(WebView view, String url)
      {
        super.onPageFinished(view, url);
        UiUtils.show(myWebView);

        final AlphaAnimation aAnim = new AlphaAnimation(0, 1);
        aAnim.setDuration(750);
        myWebView.startAnimation(aAnim);
      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView v, String url)
      {
        if (MailTo.isMailTo(url))
        {
          MailTo parser = MailTo.parse(url);
          Context ctx = v.getContext();
          Intent mailIntent = CreateEmailIntent(parser.getTo(),
              parser.getSubject(),
              parser.getBody(),
              parser.getCc());
          ctx.startActivity(mailIntent);
          v.reload();
        }
        else
        {
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setData(Uri.parse(url));
          SettingsActivity.this.startActivity(intent);
        }
        return true;
      }

      private Intent CreateEmailIntent(String address,
                                       String subject,
                                       String body,
                                       String cc)
      {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{address});
        intent.putExtra(Intent.EXTRA_TEXT, body);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_CC, cc);
        intent.setType("message/rfc822");
        return intent;
      }
    });

    new AlertDialog.Builder(this)
        .setView(alertDialogView)
        .setTitle(dialogTitle)
        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            dialog.dismiss();
          }
        })
        .create()
        .show();

    return myWebView;
  }

  private void showWebViewDialogWithUrl(String url, String dialogTitle)
  {
    WebView webView = buildWebViewDialog(dialogTitle);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDefaultTextEncodingName("utf-8");
    webView.loadUrl(url);
  }

  private void showDialogWithData(String text, String title)
  {
    new AlertDialog.Builder(this)
        .setTitle(title)
        .setMessage(text)
        .setPositiveButton(R.string.close, new DialogInterface.OnClickListener()
        {
          @Override
          public void onClick(DialogInterface dialog, int which)
          {
            dialog.dismiss();
          }
        })
        .create()
        .show();
  }

  private native boolean isDownloadingActive();

  @Override
  public boolean onPreferenceClick(Preference preference)
  {
    final String key = preference.getKey();
    if (key.equals(getString(R.string.pref_rate_app)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.SETTINGS_RATE);
      UiUtils.runLiteOrProActivity(this);
    }
    else if (key.equals(getString(R.string.pref_contact)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.MAIL_INFO);
      final Intent intent = new Intent(Intent.ACTION_SENDTO);
      intent.setData(Utils.buildMailUri(Constants.Url.MAIL_MAPSME_INFO, "", ""));
      startActivity(intent);
    }
    else if (key.equals(getString(R.string.pref_subscribe)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.MAIL_SUBSCRIBE);
      final Intent intent = new Intent(Intent.ACTION_SENDTO);
      intent.setData(Utils.buildMailUri(Constants.Url.MAIL_MAPSME_SUBSCRIBE, getString(R.string.subscribe_me_subject), getString(R.string.subscribe_me_body)));
      startActivity(intent);
    }
    else if (key.equals(getString(R.string.pref_report_bug)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.REPORT_BUG);
      final Intent intent = new Intent(Intent.ACTION_SENDTO);
      intent.setData(Utils.buildMailUri(Constants.Url.MAIL_MAPSME_BUGS, "",
          "Android version : " + Build.VERSION.RELEASE + "\n" +
          "Device name : " + Build.MANUFACTURER + " " + Build.MODEL + "\n" +
          "App version : " + BuildConfig.PACKAGE_NAME + " " + BuildConfig.VERSION_NAME + "\n\n"
      ));
      startActivity(intent);
    }
    else if (key.equals(getString(R.string.pref_like_fb)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.SETTINGS_FB);
      UiUtils.showFacebookPage(this);
    }
    else if (key.equals(getString(R.string.pref_follow_twitter)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.SETTINGS_TWITTER);
      UiUtils.showTwitterPage(this);
    }
    else if (key.equals(getString(R.string.pref_help)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.SETTINGS_HELP);
      showWebViewDialogWithUrl(FAQ_HTML_URL, getString(R.string.help));
    }
    else if (key.equals(getString(R.string.pref_copyright)))
    {
      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.SETTINGS_COPYRIGHT);
      showWebViewDialogWithUrl(COPYRIGHT_HTML_URL, getString(R.string.copyright));
    }
    else if (key.equals(getString(R.string.pref_about)))
    {
      String versionStr = "";
      try
      {
        versionStr = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
      } catch (final NameNotFoundException e)
      {
        e.printStackTrace();
      }

      Statistics.INSTANCE.trackSimpleNamedEvent(Statistics.EventName.SETTINGS_ABOUT);
      showDialogWithData(getString(R.string.about_text),
          String.format(getString(R.string.version), versionStr));
    }
    else if (key.equals(getString(R.string.pref_storage_activity)))
    {
      if (isDownloadingActive())
      {
        new AlertDialog.Builder(SettingsActivity.this)
            .setTitle(getString(R.string.downloading_is_active))
            .setMessage(getString(R.string.cant_change_this_setting))
            .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener()
            {
              @Override
              public void onClick(DialogInterface dlg, int which)
              {
                dlg.dismiss();
              }
            })
            .create()
            .show();

        return false;
      }
      else
      {
        startActivity(new Intent(this, StoragePathActivity.class));
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean onPreferenceChange(Preference preference, Object newValue)
  {
    final String key = preference.getKey();
    if (key.equals(getString(R.string.pref_munits)))
      UnitLocale.setUnits(Integer.parseInt((String) newValue));
    else if (key.equals(getString(R.string.pref_allow_stat)))
      Statistics.INSTANCE.setStatEnabled(getApplicationContext(), (Boolean) newValue);
    else if (key.equals(getString(R.string.pref_zoom_btns_enabled)))
      MWMApplication.get().nativeSetBoolean(ZOOM_BUTTON_ENABLED, (Boolean) newValue);

    return true;
  }


  // needed for soft keyboard to appear in alertdialog.
  // check https://code.google.com/p/android/issues/detail?id=7189 for details
  public static class MyWebView extends WebView
  {

    public MyWebView(Context context)
    {
      super(context);
    }

    public MyWebView(Context context, AttributeSet attrs)
    {
      super(context, attrs);
    }

    public MyWebView(Context context, AttributeSet attrs, int defStyle)
    {
      super(context, attrs, defStyle);
    }

    @Override
    public boolean onCheckIsTextEditor()
    {
      return true;
    }
  }
}
