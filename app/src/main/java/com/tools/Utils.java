package com.tools;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;

import com.fangstar.clipimage.R;

public class Utils{
	/**
	 * 创建底弹出式对话框
	 * @param context Activity 调用者
	 * @param content 显示内容
	 * @return 对话框
	 */
	public static Dialog createBottomSlideDialog(Context context, View content) {
		Dialog dialog = new Dialog(context, R.style.BottomSlideDialog);
		dialog.setContentView(content);
		dialog.getWindow().getAttributes().gravity = Gravity.BOTTOM;
		return dialog;
	}

}