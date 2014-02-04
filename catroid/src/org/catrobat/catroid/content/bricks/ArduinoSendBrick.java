/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.content.bricks;

import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;

import org.catrobat.catroid.R;
import org.catrobat.catroid.common.SoundInfo;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.actions.ArduinoSendAction;
import org.catrobat.catroid.content.actions.ExtendedActions;

import java.util.List;

/**
 * @author Adrian Schnedlitz
 * 
 */

public class ArduinoSendBrick extends BrickBaseType implements OnItemSelectedListener {

	//TODO Change this to a proper value
	private static final long serialVersionUID = 1l;
	private transient View prototypeView;
	private transient AdapterView<?> adapterView;
	private char pinValue = 'H';
	private char pinNumberLowerByte = '0';
	private char pinNumberHigherByte = '0';

	public ArduinoSendBrick() {
	}

	public ArduinoSendBrick(Sprite sprite) {
		this.sprite = sprite;
	}

	@Override
	public int getRequiredResources() {
		return BLUETOOTH_ARDUINO; //instead of NO_RESOURCES
	}

	@Override
	public Brick copyBrickForSprite(Sprite sprite, Script script) {
		ArduinoSendBrick copyBrick = (ArduinoSendBrick) clone();
		copyBrick.sprite = sprite;
		return copyBrick;
	}

	@Override
	public View getPrototypeView(Context context) {
		prototypeView = View.inflate(context, R.layout.brick_arduino_send, null);

		Spinner arduinoPinSpinner = (Spinner) prototypeView.findViewById(R.id.brick_arduino_send_pin_spinner);
		arduinoPinSpinner.setFocusableInTouchMode(false);
		arduinoPinSpinner.setFocusable(false);

		Spinner arduinoValueSpinner = (Spinner) prototypeView.findViewById(R.id.brick_arduino_send_value_spinner);
		arduinoValueSpinner.setFocusableInTouchMode(false);
		arduinoValueSpinner.setFocusable(false);

		//correct way to fill a spinner with values

		ArrayAdapter<CharSequence> pinSpinnerAdapter = ArrayAdapter.createFromResource(context,
				R.array.arduino_pin_chooser, android.R.layout.simple_spinner_item);
		pinSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		ArrayAdapter<CharSequence> valueSpinnerAdapter = ArrayAdapter.createFromResource(context,
				R.array.arduino_value_chooser, android.R.layout.simple_spinner_item);
		valueSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		arduinoPinSpinner.setAdapter(pinSpinnerAdapter);
		arduinoValueSpinner.setAdapter(valueSpinnerAdapter);

		return prototypeView;

	}

	@Override
	public Brick clone() {
		return new ArduinoSendBrick(getSprite());
	}

	@Override
	public View getView(Context context, int brickId, BaseAdapter baseAdapter) {
		if (animationState) {
			return view;
		}

		view = View.inflate(context, R.layout.brick_arduino_send, null);
		view = getViewWithAlpha(alphaValue);

		setCheckboxView(R.id.brick_arduino_send_checkbox);

		final Brick brickInstance = this;
		checkbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				checked = isChecked;
				adapter.handleCheck(brickInstance, isChecked);
			}
		});

		final Spinner arduinoPinSpinner = (Spinner) view.findViewById(R.id.brick_arduino_send_pin_spinner);
		final Spinner arduinoValueSpinner = (Spinner) view.findViewById(R.id.brick_arduino_send_value_spinner);

		if (!(checkbox.getVisibility() == View.VISIBLE)) {
			arduinoPinSpinner.setClickable(true);
			arduinoPinSpinner.setEnabled(true);
			arduinoValueSpinner.setClickable(true);
			arduinoValueSpinner.setEnabled(true);
		} else {
			arduinoPinSpinner.setClickable(false);
			arduinoPinSpinner.setEnabled(false);
			arduinoValueSpinner.setClickable(false);
			arduinoValueSpinner.setEnabled(false);
		}

		if (!(checkbox.getVisibility() == View.VISIBLE)) {
			arduinoPinSpinner.setOnItemSelectedListener(this);
			arduinoValueSpinner.setOnItemSelectedListener(this);
		}

		final ArrayAdapter<SoundInfo> spinnerAdapter = createArduinoPinAdapter(context);
		ArrayAdapter<CharSequence> arduinoPinAdapter = ArrayAdapter.createFromResource(context,
				R.array.arduino_pin_chooser, android.R.layout.simple_spinner_item);
		arduinoPinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		SpinnerAdapterWrapper spinnerAdapterWrapper = new SpinnerAdapterWrapper(context, spinnerAdapter);

		arduinoPinSpinner.setAdapter(spinnerAdapterWrapper);
		arduinoPinSpinner.setAdapter(arduinoPinAdapter);

		ArrayAdapter<CharSequence> arduinoValueAdapter = ArrayAdapter.createFromResource(context,
				R.array.arduino_value_chooser, android.R.layout.simple_spinner_item);
		arduinoValueAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		arduinoValueSpinner.setAdapter(spinnerAdapterWrapper);
		arduinoValueSpinner.setAdapter(arduinoValueAdapter);

		// Spinner OnSelectedItemListener:

		arduinoPinSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				String tempSavingString = "00";
				tempSavingString = parent.getItemAtPosition(position).toString();
				if (position < 10) {
					pinNumberLowerByte = '0';
					pinNumberHigherByte = tempSavingString.charAt(tempSavingString.length() - 1);
				} else {

					pinNumberLowerByte = tempSavingString.charAt(tempSavingString.length() - 2);
					pinNumberHigherByte = tempSavingString.charAt(tempSavingString.length() - 1);
				}

				adapterView = parent;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		arduinoValueSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (position == 0) {
					pinValue = 'H';
					ArduinoSendAction.initBluetoothConnection();
					BluetoothSocket tmpSocket = ArduinoSendAction.getBluetoothSocket();
					ArduinoSendAction.sendDataViaBluetoothSocket(tmpSocket, pinValue, pinNumberLowerByte,
							pinNumberHigherByte);
				} else {
					pinValue = 'L';
					ArduinoSendAction.initBluetoothConnection();
					BluetoothSocket tmpSocket = ArduinoSendAction.getBluetoothSocket();
					ArduinoSendAction.sendDataViaBluetoothSocket(tmpSocket, pinValue, pinNumberLowerByte,
							pinNumberHigherByte);
				}

				adapterView = parent;
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

		return view;
	}

	private void sendDataViaBluetooth() {
		//calling methods for data transfer
		ArduinoSendAction.turnOnBluetooth();
		ArduinoSendAction.initBluetoothConnection();

		BluetoothSocket tmpSocket = ArduinoSendAction.getBluetoothSocket();
		ArduinoSendAction.sendDataViaBluetoothSocket(tmpSocket, pinValue, pinNumberLowerByte, pinNumberHigherByte);
	}

	private ArrayAdapter<SoundInfo> createArduinoPinAdapter(Context context) {
		ArrayAdapter<SoundInfo> arrayAdapter = new ArrayAdapter<SoundInfo>(context,
				android.R.layout.simple_spinner_item);
		arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		SoundInfo dummySoundInfo = new SoundInfo();
		dummySoundInfo.setTitle(context.getString(R.string.new_broadcast_message));
		arrayAdapter.add(dummySoundInfo);
		//		for (SoundInfo soundInfo : sprite.) {  //requires new method in Sprite.java ?!?
		//			arrayAdapter.add(soundInfo);
		//		}
		return arrayAdapter;
	}

	@Override
	public View getViewWithAlpha(int alphaValue) {

		if (view != null) {
			//			Log.d("TAG", "VIEW != NULL");
			View layout = view.findViewById(R.id.brick_arduino_send_layout);
			Drawable background = layout.getBackground();
			background.setAlpha(alphaValue);

			//TODO Set alpha value on all bricks
			Spinner pinSpinner = (Spinner) view.findViewById(R.id.brick_arduino_send_pin_spinner);
			pinSpinner.getBackground().setAlpha(alphaValue);

			Spinner valueSpinner = (Spinner) view.findViewById(R.id.brick_arduino_send_value_spinner);
			valueSpinner.getBackground().setAlpha(alphaValue);

			this.alphaValue = (alphaValue);
		}

		return view;
	}

	@Override
	public List<SequenceAction> addActionToSequence(SequenceAction sequence) {
		sequence.addAction(ExtendedActions.sendArduinoVar(sprite, pinNumberLowerByte, pinNumberHigherByte, pinValue));
		return null;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

		adapterView = parent;
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
	}

	private class SpinnerAdapterWrapper implements SpinnerAdapter {

		protected Context context;
		protected ArrayAdapter<SoundInfo> spinnerAdapter;

		private boolean isTouchInDropDownView;

		public SpinnerAdapterWrapper(Context context, ArrayAdapter<SoundInfo> spinnerAdapter) {
			this.context = context;
			this.spinnerAdapter = spinnerAdapter;

			this.isTouchInDropDownView = false;
		}

		@Override
		public void registerDataSetObserver(DataSetObserver paramDataSetObserver) {
			spinnerAdapter.registerDataSetObserver(paramDataSetObserver);
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver paramDataSetObserver) {
			spinnerAdapter.unregisterDataSetObserver(paramDataSetObserver);
		}

		@Override
		public int getCount() {
			return spinnerAdapter.getCount();
		}

		@Override
		public Object getItem(int paramInt) {
			return spinnerAdapter.getItem(paramInt);
		}

		@Override
		public long getItemId(int paramInt) {
			//			SoundInfo currentSound = spinnerAdapter.getItem(paramInt);
			//			if (!currentSound.getTitle().equals(context.getString(R.string.new_broadcast_message))) {
			//				oldSelectedSound = currentSound;
			//			}
			return spinnerAdapter.getItemId(paramInt);
		}

		@Override
		public boolean hasStableIds() {
			return spinnerAdapter.hasStableIds();
		}

		@Override
		public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
			if (isTouchInDropDownView) {
				isTouchInDropDownView = false;
				//				if (paramInt == 0) {
				//					switchToSoundFragmentFromScriptFragment();
				//				}
			}
			return spinnerAdapter.getView(paramInt, paramView, paramViewGroup);
		}

		@Override
		public int getItemViewType(int paramInt) {
			return spinnerAdapter.getItemViewType(paramInt);
		}

		@Override
		public int getViewTypeCount() {
			return spinnerAdapter.getViewTypeCount();
		}

		@Override
		public boolean isEmpty() {
			return spinnerAdapter.isEmpty();
		}

		@Override
		public View getDropDownView(int paramInt, View paramView, ViewGroup paramViewGroup) {
			View dropDownView = spinnerAdapter.getDropDownView(paramInt, paramView, paramViewGroup);

			dropDownView.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
					isTouchInDropDownView = true;
					return false;
				}
			});

			return dropDownView;
		}

		//		private void switchToSoundFragmentFromScriptFragment() {
		//			ScriptActivity scriptActivity = ((ScriptActivity) context);
		//			scriptActivity.switchToFragmentFromScriptFragment(ScriptActivity.FRAGMENT_SOUNDS);
		//
		//			setOnSoundInfoListChangedAfterNewListener(context);
		//		}
	}
}
