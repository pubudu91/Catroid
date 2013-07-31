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
package org.catrobat.catroid.formulaeditor;

import java.io.Serializable;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.bricks.UserBrick;
import org.catrobat.catroid.formulaeditor.FormulaElement.ElementType;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

public class Formula implements Serializable {

	private static final long serialVersionUID = 1L;
	private FormulaElement formulaTree;
	private transient Integer formulaTextFieldId = null;
	private transient Drawable originalEditTextDrawable = null;
	private transient InternFormula internFormula = null;

	public Object readResolve() {

		if (formulaTree == null) {
			formulaTree = new FormulaElement(ElementType.NUMBER, "0 ", null);
		}

		internFormula = new InternFormula(formulaTree.getInternTokenList());

		return this;
	}

	public Formula(FormulaElement formulaElement) {
		formulaTree = formulaElement;
		internFormula = new InternFormula(formulaTree.getInternTokenList());
	}

	public Formula(Integer value) {
		if (value < 0) {
			formulaTree = new FormulaElement(ElementType.OPERATOR, Operators.MINUS.toString(), null);
			formulaTree.setRightChild(new FormulaElement(ElementType.NUMBER, Long.toString(Math.abs((long) value)),
					formulaTree));
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		} else {
			formulaTree = new FormulaElement(ElementType.NUMBER, value.toString(), null);
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		}
	}

	public Formula(Float value) {
		this(Double.valueOf(value));
	}

	public Formula(Double value) {
		if (value < 0) {
			formulaTree = new FormulaElement(ElementType.OPERATOR, Operators.MINUS.toString(), null);
			formulaTree.setRightChild(new FormulaElement(ElementType.NUMBER, Double.toString(Math.abs(value)),
					formulaTree));
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		} else {
			formulaTree = new FormulaElement(ElementType.NUMBER, value.toString(), null);
			internFormula = new InternFormula(formulaTree.getInternTokenList());
		}
	}

	public void updateVariableReferences(String oldName, String newName, Context context) {
		internFormula.updateVariableReferences(oldName, newName, context);
		formulaTree.updateVariableReferences(oldName, newName, context);
	}

	public boolean interpretBoolean(Sprite sprite) {
		UserBrick userBrick = ProjectManager.getInstance().getCurrentUserBrick();
		return interpretBoolean(userBrick, sprite);
	}

	public int interpretInteger(Sprite sprite) {
		UserBrick userBrick = ProjectManager.getInstance().getCurrentUserBrick();
		return interpretInteger(userBrick, sprite);
	}

	public double interpretDouble(Sprite sprite) {
		UserBrick userBrick = ProjectManager.getInstance().getCurrentUserBrick();
		return interpretDouble(userBrick, sprite);
	}

	public float interpretFloat(Sprite sprite) {
		UserBrick userBrick = ProjectManager.getInstance().getCurrentUserBrick();
		return interpretFloat(userBrick, sprite);
	}

	public boolean interpretBoolean(UserBrick userBrick, Sprite sprite) {
		int result = interpretInteger(userBrick, sprite);

		return result != 0 ? true : false;

	}

	public int interpretInteger(UserBrick userBrick, Sprite sprite) {
		Double interpretedValue = formulaTree.interpretRecursive(userBrick, sprite);
		return interpretedValue.intValue();
	}

	public double interpretDouble(UserBrick userBrick, Sprite sprite) {
		return this.formulaTree.interpretRecursive(userBrick, sprite);
	}

	public float interpretFloat(UserBrick userBrick, Sprite sprite) {
		return (float) interpretDouble(userBrick, sprite);
	}

	public void setRoot(FormulaElement formula) {
		formulaTree = formula;
		internFormula = new InternFormula(formula.getInternTokenList());

	}

	public void setTextFieldId(int id) {
		formulaTextFieldId = id;
	}

	public void refreshTextField(View view) {

		if (formulaTextFieldId != null && formulaTree != null && view != null) {
			EditText formulaTextField = (EditText) view.findViewById(formulaTextFieldId);
			if (formulaTextField == null) {
				return;
			}
			internFormula.generateExternFormulaStringAndInternExternMapping(view.getContext());

			formulaTextField.setText(internFormula.getExternFormulaString());
		}

	}

	public void refreshTextField(View view, String formulaString, int position) {
		if (formulaTextFieldId != null && formulaTree != null && view != null) {
			EditText formulaTextField = (EditText) view.findViewById(formulaTextFieldId);
			if (formulaTextField == null) {
				return;
			}
			formulaTextField.setText(formulaString);
			Layout formulaTextFieldLayout = formulaTextField.getLayout();
			if (position < 0 || formulaTextFieldLayout == null) {
				return;
			}
			int char_count = formulaTextFieldLayout.getLineVisibleEnd(0);
			if (formulaString.length() > char_count && char_count > 0) {
				int start = position - (char_count / 2);
				int end = position + (char_count / 2) + 1;
				if (end > formulaString.length() - 1) {
					end = formulaString.length() - 1;
					start = end - char_count;
				}
				if (start < 0) {
					start = 0;
					end = char_count;
				}
				formulaTextField.setText(formulaString.substring(start, end));
			}
		}
	}

	public void highlightTextField(View brickView, int orientation) {
		Drawable highlightBackground = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			highlightBackground = brickView.getResources().getDrawable(R.drawable.textfield_pressed_android4);
		} else {
			highlightBackground = brickView.getResources().getDrawable(R.drawable.textfield_pressed);
		}

		EditText formulaTextField = (EditText) brickView.findViewById(formulaTextFieldId);

		// trying to do this here is futile, it will always return null.
		//originalEditTextDrawable = formulaTextField.getBackground();

		int width = formulaTextField.getWidth();
		width = Math.max(width, 130);
		formulaTextField.setBackground(highlightBackground);
		if (brickView.getId() != R.id.brick_user_main_layout) {
			formulaTextField.setWidth(width);
		}
	}

	public void removeTextFieldHighlighting(View brickView, int orientation) {
		EditText formulaTextField = (EditText) brickView.findViewById(formulaTextFieldId);

		int width = formulaTextField.getWidth();
		originalEditTextDrawable = getDefaultBackgroundRecursively(brickView, formulaTextField);
		formulaTextField.setBackground(originalEditTextDrawable);
		if (brickView.getId() != R.id.brick_user_main_layout) {
			formulaTextField.setWidth(width);
		}
		originalEditTextDrawable = null;
	}

	private Drawable getDefaultBackgroundRecursively(View brickView, EditText formulaTextField) {
		if (brickView instanceof ViewGroup) {
			ViewGroup brickViewIterable = ((ViewGroup) brickView);
			for (int i = 0; i < brickViewIterable.getChildCount(); ++i) {
				View nextChild = brickViewIterable.getChildAt(i);
				Drawable recursiveCandidate = getDefaultBackgroundRecursively(nextChild, formulaTextField);
				if (recursiveCandidate != null) {
					return recursiveCandidate;
				}
			}
		} else if (brickView instanceof EditText && brickView != formulaTextField) {
			Drawable candidate = brickView.getBackground();
			if (candidate != null) {
				return candidate;
			}
		}

		return null;
	}

	public void prepareToRemove() {
		originalEditTextDrawable = null;
		formulaTextFieldId = null;
	}

	public InternFormulaState getInternFormulaState() {
		return internFormula.getInternFormulaState();
	}

	public boolean containsElement(FormulaElement.ElementType elementType) {
		if (formulaTree.containsElement(elementType)) {
			return true;
		}

		return false;
	}

	public boolean isLogicalFormula() {
		return formulaTree.isLogicalOperator();
	}

	public boolean isSingleNumberFormula() {
		return formulaTree.isSingleNumberFormula();
	}

	@Override
	public Formula clone() {
		if (formulaTree != null) {
			return new Formula(formulaTree.clone());
		}

		return new Formula(0);
	}

}
