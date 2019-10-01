package user.ai_camera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.support.v4.content.ContextCompat;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class Login_activity extends Activity {

	@InjectView(R.id.et_account)
	EditText et_account;
	@InjectView(R.id.et_password)
	EditText et_password;
	@InjectView(R.id.ib_text_password)
	ImageButton ib_textPassword;
	@InjectView(R.id.bn_sign)
	Button bn_login;
	@InjectView(R.id.bn_signup)
	Button bn_signup;
	@InjectView(R.id.iv_loading)
	ImageView iv_loading;
	@InjectView(R.id.tv_status)
	TextView tv_status;
	
	private String account, password;
	private Login_SignupTask httpconnect;
	private String[] response;
	private boolean IsTextPain = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		ButterKnife.inject(this);
		initView();
	
	}
	
	public void initView(){
		et_account.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean focus) {
				account = et_account.getText().toString();
			}
		});
		et_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean focus) {
				password = et_password.getText().toString();
			}
		});
		ib_textPassword.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(IsTextPain){
					ib_textPassword.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.eye_close_24));
					et_password.setTransformationMethod(PasswordTransformationMethod.getInstance());
					IsTextPain = false;
				}else{
					ib_textPassword.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(),R.drawable.eye_24));
					et_password.setInputType(InputType.TYPE_CLASS_TEXT);
					IsTextPain = true;
				}
			}
		});

		bn_login.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				account = et_account.getText().toString();
				password = et_password.getText().toString();
				InputMethodManager imm = (InputMethodManager) Login_activity.this.getSystemService(Activity.INPUT_METHOD_SERVICE);
				//Find the currently focused view, so we can grab the correct window token from it.
				View focusView = Login_activity.this.getCurrentFocus();
				//If no view currently has focus, create a new one, just so we can grab a window token from it
				if (focusView == null) {
					focusView = new View(getApplicationContext());
				}
				imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
				httpconnect = new Login_SignupTask(account,password);
				httpconnect.execute("http://140.115.51.177:8000/user/api=login");
				tv_status.setVisibility(View.GONE);
				iv_loading.setVisibility(View.VISIBLE);
				bn_login.setClickable(false);
				Handler handler = new Handler();
					handler.postDelayed(new Runnable() {
						@Override
						public void run() {
							if(httpconnect.resultBack()){
								Log.d("sign up test","OK");
								iv_loading.setVisibility(View.GONE);
								response = httpconnect.responseBack();
								if(response[0].equals(String.valueOf(0))){
									Intent intent = new Intent();
									intent.putExtra("ACCOUNT",account);
									intent.putExtra("AUTH",response[2]);
									setResult(RESULT_OK,intent);
									finish();
								}else{
									tv_status.setText(response[1]);
									tv_status.setVisibility(View.VISIBLE);
									bn_login.setClickable(true);
								}
							}else{
								handler.postDelayed(this, 100);
							}
						}
					},100);
					iv_loading.setVisibility(View.VISIBLE);
			}
		});
		bn_signup.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(Login_activity.this, Signup_activity.class);
				startActivity(intent);
				finish();
			}
		});
	}

	@Override
	public void onBackPressed() {
		if(response == null){
			setResult(RESULT_CANCELED);
		}
		super.onBackPressed();
	}
}
