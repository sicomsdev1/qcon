package com.sicoms.smartplug.common;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.sicoms.smartplug.R;

public class SPEvent implements OnClickListener {

	Activity activity;

    public SPEvent(){}
	public SPEvent(Activity activity) {
		this.activity = activity;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
	}

	public void clearActivity(){
		for (int actCnt = 0; actCnt < SPActivity.actList.size(); actCnt++)
			SPActivity.actList.get(actCnt).finish();
		SPActivity.actList.clear();
	}
	
	// Back의 상태값을 저장하기 위한 변수
	private boolean m_close_flag = false;
	// 일정 시간 후 상태값을 초기화하기 위한 핸들러
	Handler m_close_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			m_close_flag = false;
		}
	};
	public void backButtonPressed(Activity activity, Activity superActivity){
		// m_close_flag 가 false 이면 첫번째로 키가 눌린 것이다.
		if (m_close_flag == false) { // Back 키가 첫번째로 눌린 경우

			// 안내 메세지를 토스트로 출력한다.
			Toast.makeText(activity, activity.getString(R.string.app_finish), Toast.LENGTH_LONG)
					.show();

			// 상태값 변경
			m_close_flag = true;

			// 핸들러를 이용하여 3초 후에 0번 메세지를 전송하도록 설정한다.
			m_close_handler.sendEmptyMessageDelayed(0, 3000);

		} else { // Back 키가 3초 내에 연달아서 두번 눌린 경우

			// 액티비티를 종료하는 상위 클래스의 onBackPressed 메소드를 호출한다.
			superActivity.finish();
			activity.finish();
            android.os.Process.killProcess(android.os.Process.myPid());

			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean m_back_flag = false;
	Handler m_back_handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			m_back_flag = false;
		}
	};
	public boolean isBack(){
		if( m_back_flag == false ){
			m_back_flag = true;
			m_back_handler.sendEmptyMessageDelayed(0, 3000);
			return true;
		} else {
			return false;
		}
	}
}
